package controllers;

import util.*;
import model.*;

import actions.*;
import play.*;
import play.mvc.*;
import java.lang.StringBuilder;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;
import java.text.*;

import org.osgeo.mapguide.*;

public abstract class MgClassicAjaxViewerController extends MgAbstractAuthenticatedController {

    private static String getViewerRoot() {
        return controllers.routes.MgClassicAjaxViewerController.index().url();
    }

    private static String getMapAgentUrl() {
        return controllers.routes.MgMapAgentCompatibilityController.processGetRequest().url();
    }

    private static int getClientOS() {
        return 0;
    }

    public static Result index() {
        try {
            //Path separator is not appended by java.io.File! Stupid!
            String localizedPath = Play.application().getFile("internal/localized/").getPath() + File.separator;
            Logger.debug("Setting localized files path to: " + localizedPath);
            MgLocalizationUtil.SetLocalizedFilesPath(localizedPath);
            //fetch the parameters for this request
            //
            Map<String, String[]> queryParams = request().queryString();
            String webLayoutDefinition = "";
            String locale = "en";
            if (queryParams.get("WEBLAYOUT") != null)
                webLayoutDefinition = queryParams.get("WEBLAYOUT")[0];

            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);

            Hashtable cmds = new Hashtable();
            BoxedInteger curFlyout = new BoxedInteger(0);

            //Open a connection with the server
            //
            MgSiteConnection site = createMapGuideConnection();
            MgSite siteObj = site.GetSite();
            String sessionId = siteObj.GetCurrentSession();
            //Get a MgWebLayout object initialized with the specified web layout definition
            //
            MgWebLayout webLayout = null;
            MgResourceService resourceSrvc = (MgResourceService)site.CreateService(MgServiceType.ResourceService);
            MgResourceIdentifier webLayoutId = new MgResourceIdentifier(webLayoutDefinition);
            webLayout = new MgWebLayout(resourceSrvc, webLayoutId);

            //calculate the size of the variable elements of the viewer
            //
            MgWebToolBar toolBar = webLayout.GetToolBar();
            MgWebUiPane statusBar = webLayout.GetStatusBar();
            MgWebTaskPane taskPane = webLayout.GetTaskPane();
            MgWebInformationPane infoPane = webLayout.GetInformationPane();
            MgWebTaskBar taskBar = taskPane.GetTaskBar();
            String mapDef = webLayout.GetMapDefinition();

            int forDwf = 0;
            boolean showTaskPane = taskPane.IsVisible();
            boolean showTaskBar = taskBar.IsVisible();
            boolean showStatusbar = statusBar.IsVisible();
            boolean showToolbar = toolBar.IsVisible();

            int taskPaneWidth = taskPane.GetWidth();
            int toolbarHeight = 30;
            int taskBarHeight = 30;
            int statusbarHeight = 26;

            taskPaneWidth = showTaskPane? taskPaneWidth: 0;
            toolbarHeight = showToolbar? toolbarHeight: 0;
            taskBarHeight = showTaskBar ? taskBarHeight : 0;
            statusbarHeight = showStatusbar? statusbarHeight: 0;

            //Encode the initial url so that it does not trip any sub-frames (especially if this url has parameters)
            String taskPaneUrl = URLEncoder.encode(taskPane.GetInitialTaskUrl(), "UTF-8");
            String vpath = getViewerRoot();
            boolean defHome = false;
            if(taskPaneUrl == null || taskPaneUrl.length() == 0) {
                taskPaneUrl = "gettingstarted.jsp";
                defHome = true;
            }

            String mapDefinitionUrl = URLEncoder.encode(mapDef, "UTF-8");
            // NOTE:
            //
            // We don't open a MgMap because it is being created by mapframe.jsp that is also probably running
            // as this script is running. However the naming convention is fixed enough that we can figure out
            // what to pass to the Task Pane
            MgResourceIdentifier resId = new MgResourceIdentifier(mapDef);
            String mapName = resId.GetName();
            
            String title = webLayout.GetTitle();

            boolean showLegend = infoPane.IsLegendBandVisible();
            boolean showProperties = infoPane.IsPropertiesBandVisible();

            int infoWidth = 0;
            if(showLegend || showProperties)
            {
                if(infoPane.IsVisible())
                {
                    infoWidth = infoPane.GetWidth();
                    if(infoWidth < 5)
                        infoWidth = 5;    //ensure visible
                }
                else
                    showProperties = showLegend = false;
            }

            //calculate the url of the inner pages
            //
            String srcToolbar = showToolbar ? ( "src=\"" + vpath + "toolbar.jsp?LOCALE=" + locale + "\"" ) : "";
            String srcStatusbar = showStatusbar ? ( "src=\"" + vpath + "statusbar.jsp?LOCALE=" + locale + "\"" ) : "";
            String srcTaskFrame = showTaskPane? ("src=\"" + vpath + "taskframe.jsp?MAPNAME=" + mapName + "&WEBLAYOUT=" + URLEncoder.encode(webLayoutDefinition, "UTF-8") + "&DWF=" + (forDwf!=0? "1": "0") + "&SESSION=" + (sessionId != ""? sessionId: "") + "&LOCALE=" + locale + "\"") : "";
            String srcTaskBar = "src=\"" + vpath + "taskbar.jsp?LOCALE=" + locale + "\"";

            //view center
            //
            MgPoint ptCenter = webLayout.GetCenter();
            String center = "null";
            if(ptCenter != null)
            {
                MgCoordinate coord = ptCenter.GetCoordinate();
                Object[] formatArgs = { Double.toString(coord.GetX()), Double.toString(coord.GetY()) };
                center = MessageFormat.format("new Point({0}, {1})", formatArgs);
            }

            //Process commands and declare command objects
            //
            MgWebCommandCollection commands = webLayout.GetCommands();
            String cmdObjects = "";
            String cmdObject = "";
            int navCmdIndex = 0;
            int searchCmdIndex = 0;
            int measureCmdIndex = 0;
            int printCmdIndex = 0;
            int scriptCmdIndex = 0;
            String userCode = "";
            String userCodeCalls = "\nswitch(funcIndex)\n{\n";
            int selAwareCmdCount = 0;
            String selAwareCmds = "";

            for(int i = 0; i < commands.GetCount(); i++)
            {
                MgWebCommand cmd = commands.GetItem(i);
                if(!cmd.IsUsed())
                    continue;
                int tgtViewer = cmd.GetTargetViewerType();
                if((tgtViewer == MgWebTargetViewerType.Dwf) != (forDwf == 1) && (tgtViewer != MgWebTargetViewerType.All))
                    continue;
                String name = cmd.GetName();
                int action = cmd.GetAction();
                if (action == MgWebActions.Search)
                {
                    MgWebSearchCommand searchCmd = (MgWebSearchCommand)cmd;

                    // create the column objects
                    String cols = "var resCols" + searchCmdIndex + " = new Array();\n";
                    if(searchCmd.GetResultColumnCount() > 0)
                    {
                        for(int j = 0; j < searchCmd.GetResultColumnCount(); j++)
                        {
                            Object[] formatArgs = { new Integer(searchCmdIndex), new Integer(j), MgClassicAjaxViewerUtil.StrEscape(searchCmd.GetColumnDisplayNameAt(j)), MgClassicAjaxViewerUtil.StrEscape(searchCmd.GetColumnPropertyNameAt(j)) };
                            String col = MessageFormat.format("resCols{0,number,integer}[{1,number,integer}] = new ResultColumn(\"{2}\", \"{3}\");\n", formatArgs);
                            cols += col;
                        }
                    }
                    cmdObjects += cols;

                    // declare a new search command object
                    Object[] formatArgs = { new Integer(i),
                                            MgClassicAjaxViewerUtil.StrEscape(name),
                                            MgClassicAjaxViewerUtil.StrEscape(searchCmd.GetLabel()),
                                            new Integer(action),
                                            searchCmd.GetIconUrl(),
                                            searchCmd.GetDisabledIconUrl(),
                                            MgClassicAjaxViewerUtil.StrEscape(searchCmd.GetTooltip()),
                                            MgClassicAjaxViewerUtil.StrEscape(searchCmd.GetDescription()),
                                            searchCmd.GetLayer(),
                                            MgClassicAjaxViewerUtil.StrEscape(searchCmd.GetPrompt()),
                                            new Integer(searchCmdIndex),
                                            MgClassicAjaxViewerUtil.StrEscape(searchCmd.GetFilter()),
                                            new Integer(searchCmd.GetMatchLimit()),
                                            new Integer(searchCmd.GetTarget()),
                                            searchCmd.GetTargetName() };
                    cmdObject = MessageFormat.format("commands[{0,number,integer}] = new SearchCommand(\"{1}\", \"{2}\", {3,number,integer}, \"{4}\", \"{5}\", \"{6}\", \"{7}\", \"{8}\", \"{9}\", resCols{10}, \"{11}\", {12,number,integer}, {13,number,integer}, \"{14}\");\n", formatArgs);

                    searchCmdIndex++;
                }
                else if(action == MgWebActions.InvokeUrl)
                {
                    MgWebInvokeUrlCommand invokeUrlCmd = (MgWebInvokeUrlCommand)cmd;

                    // create the parameter objects
                    String paramObjs = "var navParams" + navCmdIndex + " = new Array();\n";
                    String layers = "var layers" + navCmdIndex + " = new Array();\n";
                    if(invokeUrlCmd.GetParameterCount() > 0)
                    {
                        for(int j = 0; j < invokeUrlCmd.GetParameterCount(); j++)
                        {
                            Object[] formatArgs = { new Integer(navCmdIndex), new Integer(j), invokeUrlCmd.GetParameterNameAt(j), invokeUrlCmd.GetParameterValueAt(j) };
                            String param = MessageFormat.format("navParams{0,number,integer}[{1,number,integer}] = new NavParam(\"{2}\", \"{3}\");\n", formatArgs);
                            paramObjs = paramObjs + param;
                        }
                    }
                    for( int j = 0;  j < invokeUrlCmd.GetLayerCount(); j++ )
                    {
                        Object[] formatArgs = { new Integer(navCmdIndex), new Integer(j), invokeUrlCmd.GetLayerNameAt(j) };
                        String layer = MessageFormat.format("layers{0,number,integer}[{1,number,integer}] = \"{2}\";\n", formatArgs);
                        layers = layers + layer;
                    }
                    cmdObjects = cmdObjects + paramObjs + layers;

                    if(invokeUrlCmd.DisabledIfSelectionEmpty() || invokeUrlCmd.GetLayerCount() > 0)
                    {
                        Object[] formatArgs = { new Integer(selAwareCmdCount), new Integer(i) };
                        selAwareCmds = selAwareCmds + MessageFormat.format("selectionAwareCmds[{0,number,integer}] = {1,number,integer};\n", formatArgs);
                        selAwareCmdCount ++;
                    }

                    // declare a new invokeurl command object
                    Object[] formatArgs = { new Integer(i),
                                            MgClassicAjaxViewerUtil.StrEscape(name),
                                            new Integer(action),
                                            invokeUrlCmd.GetIconUrl(),
                                            invokeUrlCmd.GetDisabledIconUrl(),
                                            MgClassicAjaxViewerUtil.StrEscape( invokeUrlCmd.GetTooltip()),
                                            MgClassicAjaxViewerUtil.StrEscape( invokeUrlCmd.GetDescription()),
                                            invokeUrlCmd.GetUrl(),
                                            new Integer(navCmdIndex),
                                            invokeUrlCmd.DisabledIfSelectionEmpty() ? "true" : "false",
                                            new Integer(navCmdIndex),
                                            new Integer(invokeUrlCmd.GetTarget()),
                                            invokeUrlCmd.GetTargetName() };
                    cmdObject = MessageFormat.format("commands[{0,number,integer}] = new InvokeUrlCommand(\"{1}\", {2,number,integer}, \"{3}\", \"{4}\", \"{5}\", \"{6}\", \"{7}\", navParams{8,number,integer}, {9}, layers{10,number,integer}, {11,number,integer}, \"{12}\");\n", formatArgs);
                    navCmdIndex++;
                }
                else if(action == MgWebActions.Buffer || action == MgWebActions.SelectWithin ||
                    action == MgWebActions.Measure || action == MgWebActions.ViewOptions || action == MgWebActions.GetPrintablePage)
                {
                    MgWebUiTargetCommand targetCmd = (MgWebUiTargetCommand)cmd;

                    if(action == MgWebActions.Measure)
                    {
                        if(measureCmdIndex != 0)
                            throw new Exception(MgLocalizationUtil.GetString("ALREADYINMEASURE", locale));
                        measureCmdIndex = i;
                    }

                    // declare a new ui target command object
                    Object[] formatArgs = { new Integer(i),
                                            MgClassicAjaxViewerUtil.StrEscape( name ),
                                            new Integer( action ),
                                            targetCmd.GetIconUrl(),
                                            targetCmd.GetDisabledIconUrl(),
                                            MgClassicAjaxViewerUtil.StrEscape(targetCmd.GetTooltip()),
                                            MgClassicAjaxViewerUtil.StrEscape(targetCmd.GetDescription()),
                                            new Integer( targetCmd.GetTarget()),
                                            targetCmd.GetTargetName() };
                    cmdObject = MessageFormat.format("commands[{0,number,integer}] = new UiTargetCommand(\"{1}\", {2,number,integer}, \"{3}\", \"{4}\", \"{5}\", \"{6}\", {7,number,integer}, \"{8}\");\n", formatArgs);
                }
                else if(action == MgWebActions.Help)
                {
                    MgWebHelpCommand helpCmd = (MgWebHelpCommand)cmd;

                    // declare a new help  command object
                    Object[] formatArgs = { new Integer(i),
                                            MgClassicAjaxViewerUtil.StrEscape(name),
                                            new Integer(action),
                                            helpCmd.GetIconUrl(),
                                            helpCmd.GetDisabledIconUrl(),
                                            MgClassicAjaxViewerUtil.StrEscape(helpCmd.GetTooltip()),
                                            MgClassicAjaxViewerUtil.StrEscape(helpCmd.GetDescription()),
                                            helpCmd.GetUrl(),
                                            new Integer(helpCmd.GetTarget()),
                                            helpCmd.GetTargetName() };
                    cmdObject = MessageFormat.format("commands[{0,number,integer}] = new HelpCommand(\"{1}\", {2,number,integer}, \"{3}\", \"{4}\", \"{5}\", \"{6}\", \"{7}\", {8,number,integer}, \"{9}\");\n", formatArgs);
                }
                else if(action == MgWebActions.PrintMap)
                {
                    MgWebPrintCommand printCmd = (MgWebPrintCommand)cmd;

                    // declare the print layouts
                    String layouts = "var layouts" + printCmdIndex + " = new Array();\n";
                    for(int j = 0; j < printCmd.GetPrintLayoutCount(); j++)
                    {
                        String layout = "";
                        Object[] formatArgs = { new Integer(printCmdIndex), new Integer(j), printCmd.GetPrintLayoutAt(j) };
                        layout = MessageFormat.format("layouts{0,number,integer}[{1,number,integer}] = \"{2}\";\n", formatArgs);
                        layouts = layouts + layout;
                    }
                    cmdObjects = cmdObjects + layouts;

                    // declare a new print command object
                    Object[] formatArgs = { new Integer(i),
                                            MgClassicAjaxViewerUtil.StrEscape( name ),
                                            new Integer(action),
                                            printCmd.GetIconUrl(),
                                            printCmd.GetDisabledIconUrl(),
                                            MgClassicAjaxViewerUtil.StrEscape( printCmd.GetTooltip()),
                                            MgClassicAjaxViewerUtil.StrEscape( printCmd.GetDescription()),
                                            new Integer(printCmdIndex) };
                    cmdObject = MessageFormat.format("commands[{0,number,integer}] = new PrintCommand(\"{1}\", {2,number,integer}, \"{3}\", \"{4}\", \"{5}\", \"{6}\", layouts{7,number,integer});\n", formatArgs );
                    printCmdIndex++;
                }
                else if(action == MgWebActions.InvokeScript)
                {
                    MgWebInvokeScriptCommand invokeScriptCmd = (MgWebInvokeScriptCommand)cmd;

                    // declare a new basic command object
                    Object[] formatArgs = { new Integer(i),
                                            MgClassicAjaxViewerUtil.StrEscape(name),
                                            new Integer(action),
                                            invokeScriptCmd.GetIconUrl(),
                                            invokeScriptCmd.GetDisabledIconUrl(),
                                            MgClassicAjaxViewerUtil.StrEscape( invokeScriptCmd.GetTooltip()),
                                            MgClassicAjaxViewerUtil.StrEscape( invokeScriptCmd.GetDescription()),
                                            new Integer( scriptCmdIndex ) };
                    cmdObject = MessageFormat.format("commands[{0,number,integer}] = new InvokeScriptCommand(\"{1}\", {2,number,integer}, \"{3}\", \"{4}\", \"{5}\", \"{6}\", {7,number,integer});\n", formatArgs);

                    userCode = userCode + "\nfunction UserFunc" + scriptCmdIndex + "()\n{\n" + invokeScriptCmd.GetScriptCode() + "\n}\n";
                    Object[] formatArgs2 = { new Integer(scriptCmdIndex), new Integer(scriptCmdIndex) };
                    userCodeCalls = userCodeCalls + MessageFormat.format("case {0,number,integer}: UserFunc{0,number,integer}(); break;\n", formatArgs2);

                    scriptCmdIndex++;
                }
                else
                {
                    // declare a new basic command object
                    Object[] formatArgs = { new Integer(i),
                                            name,
                                            new Integer(action),
                                            cmd.GetIconUrl(),
                                            cmd.GetDisabledIconUrl(),
                                            MgClassicAjaxViewerUtil.StrEscape(cmd.GetTooltip()),
                                            MgClassicAjaxViewerUtil.StrEscape(cmd.GetDescription()) };
                    cmdObject = MessageFormat.format("commands[{0,number,integer}] = new BasicCommand(\"{1}\", {2,number,integer}, \"{3}\", \"{4}\", \"{5}\", \"{6}\");\n", formatArgs);
                }
                cmdObjects = cmdObjects + cmdObject;
                cmds.put(name, new Integer(i));
            }
            userCodeCalls = userCodeCalls + "\n}\n";

            //Declare toolbar items
            //
            String toolbarDef = MgClassicAjaxViewerUtil.DeclareUiItems(cmds, curFlyout, toolBar.GetWidgets(), "toolbarItems");

            //Declare task items
            String taskListDef = MgClassicAjaxViewerUtil.DeclareUiItems(cmds, curFlyout, taskBar.GetTaskList(), "taskItems");

            //Declare context menu items
            MgWebContextMenu ctxMenu = webLayout.GetContextMenu();
            String ctxMenuDef;
            if(ctxMenu.IsVisible())
                ctxMenuDef = MgClassicAjaxViewerUtil.DeclareUiItems(cmds, curFlyout, ctxMenu, "ctxMenuItems");
            else
                ctxMenuDef = "";

            //task items texts
            String taskItemTexts = "";
            MgWebWidgetCollection taskButtons = taskBar.GetTaskButtons();
            for(int i = 0; i < 4; i ++)
            {
                MgWebTaskBarWidget btn = (MgWebTaskBarWidget)taskButtons.GetWidget(i);
                if(i > 0)
                    taskItemTexts += ",";
                taskItemTexts += "\"" + MgClassicAjaxViewerUtil.StrEscape(btn.GetName()) + "\"," +
                                    "\"" + MgClassicAjaxViewerUtil.StrEscape(btn.GetTooltip()) + "\"," +
                                    "\"" + MgClassicAjaxViewerUtil.StrEscape(btn.GetDescription()) + "\"," +
                                    "\"" + MgClassicAjaxViewerUtil.StrEscape(btn.GetIconUrl()) + "\"," +
                                    "\"" + MgClassicAjaxViewerUtil.StrEscape(btn.GetDisabledIconUrl()) + "\"";
            }

            //transmit the session to the map pane if one was specified to this request
            String sessionParam = "&SESSION=" + sessionId;

            //load the frameset template and format it
            String frameset = "";
            String viewerType = forDwf != 0? "DWF": "HTML";

            if(showTaskBar)
            {
                String frameSetTempl = MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/framesettaskbar.templ"));
                String[] vals = {
                                String.valueOf(statusbarHeight),
                                String.valueOf(taskPaneWidth),
                                String.valueOf(toolbarHeight),
                                srcToolbar,
                                vpath + "mapframe.jsp",
                                mapDefinitionUrl,
                                viewerType,
                                showLegend? "1": "0",
                                showProperties? "1": "0",
                                String.valueOf(infoWidth),
                                locale,
                                String.valueOf(webLayout.GetHyperlinkTarget()),
                                webLayout.GetHyperlinkTargetFrame(),
                                webLayout.IsZoomControlVisible()? "1": "0",
                                sessionParam,
                                vpath + "formframe.jsp",
                                String.valueOf(taskBarHeight),
                                srcTaskBar,
                                srcTaskFrame,
                                srcStatusbar
                                };

                frameset = MgClassicAjaxViewerUtil.Substitute(frameSetTempl, vals);
            }
            else
            {
                String frameSetTempl = MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/framesetnotaskbar.templ"));
                String[] vals = {
                                String.valueOf(toolbarHeight),
                                String.valueOf(statusbarHeight),
                                srcToolbar,
                                String.valueOf(taskPaneWidth),
                                vpath + "mapframe.jsp",
                                mapDefinitionUrl,
                                viewerType,
                                showLegend? "1": "0",
                                showProperties? "1": "0",
                                String.valueOf(infoWidth),
                                locale,
                                String.valueOf(webLayout.GetHyperlinkTarget()),
                                webLayout.GetHyperlinkTargetFrame(),
                                webLayout.IsZoomControlVisible()? "1": "0",
                                sessionParam,
                                srcTaskFrame,
                                vpath + "formframe.jsp",
                                srcStatusbar
                                };

                frameset = MgClassicAjaxViewerUtil.Substitute(frameSetTempl, vals);
            }

            String homePageUrl = URLDecoder.decode(taskPaneUrl,"UTF-8");
            if(homePageUrl.length() < 8 || homePageUrl.substring(0, 7).compareToIgnoreCase("http://") != 0)
                homePageUrl = vpath + homePageUrl;

            //load the HTML template and format it
            // 0 - windows
            // 1 - mac
            // 2-  unknown
            String templ = MgLocalizationUtil.Localize(MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/mainframe.templ")), locale, 0);

            String int0 = "0";
            String int1 = "1";
            String[] vals = {
                              webLayout.GetTitle(),
                              getMapAgentUrl(),
                              webLayout.GetEnablePingServer()? int1 : int0,
                              String.valueOf(site.GetSite().GetSessionTimeout()),
                              locale,
                              showToolbar ? int1 : int0,
                              showStatusbar ? int1 : int0,
                              showTaskPane ? int1 : int0,
                              !showTaskPane ? int0 : (showTaskBar ? int1 : int0),
                              homePageUrl,
                              defHome? "1" : "0",
                              webLayoutDefinition,
                              mapDef,
                              String.valueOf(taskPaneWidth),
                              center,
                              String.valueOf(webLayout.GetScale()),
                              MgClassicAjaxViewerUtil.StrEscape(title),
                              (forDwf == 1)? "1" : "0",
                              cmdObjects,
                              toolbarDef,
                              taskListDef,
                              ctxMenuDef,
                              userCode,
                              taskItemTexts,
                              selAwareCmds,
                              vpath + "quickplotpanel.jsp",
                              vpath + "measureui.jsp",
                              vpath + "searchprompt.jsp",
                              vpath + "bufferui.jsp",
                              vpath + "selectwithinui.jsp",
                              userCodeCalls,
                              vpath + "viewoptions.jsp",
                              frameset
                             };

            templ = MgClassicAjaxViewerUtil.Substitute(templ, vals);
            response().setContentType("text/html");
            return ok(templ);
        }
        catch(MgUserNotFoundException e)
        {
            return unauthorized(e.getMessage());
        }
        catch(MgUnauthorizedAccessException e)
        {
            return unauthorized(e.getMessage());
        }
        catch(MgAuthenticationFailedException e)
        {
            return unauthorized(e.getMessage());
        }
        catch(MgException e)
        {
            return mgServerError(e);
        }
        catch(Exception e)
        {
            return javaException(e);
        }
    }

    public static Result viewerasset(String file) {
        File f = Play.application().getFile("internal/viewerfiles/" + file);
        if (!f.exists())
            return notFound();
        else
            return ok(f);
    }

    public static Result localizedasset(String file) {
        File f = Play.application().getFile("internal/localized/" + file);
        if (!f.exists())
            return notFound();
        else
            return ok(f);
    }

    public static Result viewericon(String file) {
        File f = Play.application().getFile("internal/stdicons/" + file);
        if (!f.exists())
            return notFound();
        else
            return ok(f);
    }

    public static Result legendctrl() {
        try {
            String mapName = "";
            String sessionId = "";
            String mapFrame = "parent";
            String locale = "";
            
            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);
            if (queryParams.get("MAPNAME") != null)
                mapName = MgClassicAjaxViewerUtil.ValidateMapName(queryParams.get("MAPNAME")[0]);
            if (queryParams.get("MAPFRAME") != null)
                mapFrame = MgClassicAjaxViewerUtil.ValidateFrameName(queryParams.get("MAPFRAME")[0]);

            MgSiteConnection site = createMapGuideConnection();
            MgSite siteObj = site.GetSite();
            sessionId = siteObj.GetCurrentSession();

            String vpath = getViewerRoot();
            String vals[] = {
                mapFrame,
                vpath + "legend.jsp",
                vpath + "legend.jsp",
                URLEncoder.encode(mapName, "UTF-8"),
                sessionId,
                vpath + "legendui.jsp",
                locale };

            String templ = MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/legendctrl.templ"));
            response().setContentType("text/html");
            return ok(MgClassicAjaxViewerUtil.Substitute(templ, vals));
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result propertyctrl() {
        try {
            String locale = "";
            String mapFrame = "parent";

            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);
            if (queryParams.get("MAPFRAME") != null)
                mapFrame = MgClassicAjaxViewerUtil.ValidateFrameName(queryParams.get("MAPFRAME")[0]);

            String templ = MgLocalizationUtil.Localize(MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/propertyctrl.templ")), locale, getClientOS());
            String vals[] = { mapFrame };
            response().setContentType("text/html");
            return ok(MgClassicAjaxViewerUtil.Substitute(templ, vals));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result mapframe() {
        try {
            String mapDefinition = "";
            String locale = "en";
            int infoWidth = 250;
            int showLegend = 1;
            int showProperties = 1;
            int showSlider = 1;
            String sessionId = "";
            String type = "";
            String hlTgt = "";
            String hlTgtName = "";

            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);
            if (queryParams.get("MAPDEFINITION") != null)
                mapDefinition = MgClassicAjaxViewerUtil.ValidateResourceId(queryParams.get("MAPDEFINITION")[0]);
            if (queryParams.get("HLTGT") != null)
                hlTgt = MgClassicAjaxViewerUtil.ValidateHyperlinkTargetValue(queryParams.get("HLTGT")[0]);
            if (queryParams.get("HLTGTNAME") != null)
                hlTgtName = MgClassicAjaxViewerUtil.ValidateFrameName(queryParams.get("HLTGTNAME")[0]);
            if (queryParams.get("INFOWIDTH") != null)
                infoWidth = MgClassicAjaxViewerUtil.GetIntParameter(queryParams.get("INFOWIDTH")[0]);
            if (queryParams.get("SHOWLEGEND") != null)
                showLegend = MgClassicAjaxViewerUtil.GetIntParameter(queryParams.get("SHOWLEGEND")[0]);
            if (queryParams.get("SHOWSLIDER") != null)
                showSlider = MgClassicAjaxViewerUtil.GetIntParameter(queryParams.get("SHOWSLIDER")[0]);
            if (queryParams.get("SHOWPROP") != null)
                showProperties = MgClassicAjaxViewerUtil.GetIntParameter(queryParams.get("SHOWPROP")[0]);
            if (queryParams.get("TYPE") != null)
                type = queryParams.get("TYPE")[0];

            MgSiteConnection site = createMapGuideConnection();
            MgSite siteObj = site.GetSite();
            sessionId = siteObj.GetCurrentSession();

            if (type.equals("DWF"))
            {
                String frameName = "";
                if(hlTgt.equals("1"))
                {
                    frameName = "taskPaneFrame";
                }
                else if(hlTgt.equals("3"))
                {
                    frameName = hlTgtName;
                }
                else
                {
                    frameName = "_BLANK";
                }
                String mapRequest = getMapAgentUrl() + "?OPERATION=GETMAP&VERSION=1.0&MAPDEFINITION=" + URLEncoder.encode(mapDefinition, "UTF-8") + "&DWFVERSION=6.01&EMAPVERSION=1.0&LOCALE=" + locale + (sessionId != ""? "&SESSION=" + sessionId: "") + "&reload=true";
                String vals[] = { mapRequest,
                              String.valueOf(infoWidth),
                              showLegend != 0 || showProperties != 0? "true": "false",
                              showLegend != 0 ? "true": "false",
                              showProperties != 0 ? "true": "false",
                              frameName
                              };
                //load html template code and format it
                //
                String templ = MgLocalizationUtil.Localize(MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/dwfmappane.templ")), locale, getClientOS());
                response().setContentType("text/html");
                return ok(templ);
            }
            else
            {
                MgTileService tileSrvc = (MgTileService)site.CreateService(MgServiceType.TileService);
                int tileSizeX = tileSrvc.GetDefaultTileSizeX();
                int tileSizeY = tileSrvc.GetDefaultTileSizeY();

                MgResourceService resourceSrvc = (MgResourceService)site.CreateService(MgServiceType.ResourceService);

                MgMap map = new MgMap();
                MgResourceIdentifier resId = new MgResourceIdentifier(mapDefinition);
                String mapName = resId.GetName();
                map.Create(resourceSrvc, resId, mapName);

                //create an empty selection object and store it in the session repository
                MgSelection sel = new MgSelection(map);
                sel.Save(resourceSrvc, mapName);

                //get the map extent and calculate the scale factor
                //
                MgEnvelope mapExtent = map.GetMapExtent();
                String srs = map.GetMapSRS();
                double metersPerUnit;
                String unitsType;
                if(srs != null && srs.length() > 0)
                {
                    MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
                    MgCoordinateSystem cs = csFactory.Create(srs);
                    metersPerUnit = cs.ConvertCoordinateSystemUnitsToMeters(1.0);
                    unitsType = cs.GetUnits();
                }
                else
                {
                    metersPerUnit = 1.0;
                    unitsType = MgLocalizationUtil.GetString("DISTANCEMETERS", locale);
                }

                MgCoordinate llExtent = mapExtent.GetLowerLeftCoordinate();
                MgCoordinate urExtent = mapExtent.GetUpperRightCoordinate();
                String bgColor = map.GetBackgroundColor();
                if(bgColor.length() == 8)
                {
                    bgColor = "#" + bgColor.substring(2);
                }
                else
                {
                    bgColor = "white";
                }

                String scaleCreationCode = "";

                // Create a sorted set of display scales
                TreeSet scales = new TreeSet();
                for(int i = 0; i < map.GetFiniteDisplayScaleCount(); i++)
                {
                    scales.add(new Double(map.GetFiniteDisplayScaleAt(i)));
                }
                Iterator iter = scales.iterator();
                int i = 0;
                while(iter.hasNext())
                {
                    scaleCreationCode = scaleCreationCode + "scales[" + i + "]=" + iter.next().toString().replace(',','.') + "; ";
                    i++;
                }
                MgResourceIdentifier mapStateId = new MgResourceIdentifier("Session:" + sessionId + "//" + mapName + "." + MgResourceType.Map);
                map.Save(resourceSrvc, mapStateId);

                //load html template code and format it
                //
                String templ = MgLocalizationUtil.Localize(MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/ajaxmappane.templ")), locale, getClientOS());
                String vpath = getViewerRoot();
                String vals[] = {
                            String.valueOf(tileSizeX),
                            String.valueOf(tileSizeY),
                            getMapAgentUrl(),
                            mapName,
                            mapDefinition,
                            String.valueOf(infoWidth),
                            showLegend != 0 ? "true": "false",
                            showProperties != 0 ? "true": "false",
                            sessionId,
                            String.valueOf(llExtent.GetX()), String.valueOf(llExtent.GetY()),
                            String.valueOf(urExtent.GetX()), String.valueOf(urExtent.GetY()),
                            String.valueOf(metersPerUnit),
                            unitsType,
                            bgColor,
                            hlTgt,
                            hlTgtName,
                            vpath + "setselection.jsp",
                            showSlider != 0? "true": "false",
                            locale,
                            vpath + "getselectedfeatures.jsp",
                            scaleCreationCode,
                            vpath + "ajaxviewerabout.jsp",
                            vpath + "legendctrl.jsp",
                            URLEncoder.encode(mapName, "UTF-8"),
                            sessionId,
                            locale,
                            vpath + "propertyctrl.jsp",
                            locale };
                templ = MgClassicAjaxViewerUtil.Substitute(templ, vals);
                response().setContentType("text/html");
                return ok(templ);
            }
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result toolbar() {
        try {
            String locale = "";
            
            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);

            String templ = MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/toolbar.templ"));
            response().setContentType("text/html");
            return ok(templ);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result formframe() {
        try {
            String templ = MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/formframe.templ"));
            response().setContentType("text/html");
            return ok(templ);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result taskframe() {
        String sessionId = "";
        String webLayoutId = "";
        int dwf = 0;
        String locale = "";
        String mapName = "";
        try {
            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);
            if (queryParams.get("MAPNAME") != null)
                mapName = MgClassicAjaxViewerUtil.ValidateMapName(queryParams.get("MAPNAME")[0]);
            if (queryParams.get("WEBLAYOUT") != null)
                webLayoutId = MgClassicAjaxViewerUtil.ValidateResourceId(queryParams.get("WEBLAYOUT")[0]);
            if (queryParams.get("DWF") != null)
                dwf = MgClassicAjaxViewerUtil.GetIntParameter(queryParams.get("DWF")[0]);

            //connect to the site and get a feature service and a resource service instances
            MgSiteConnection site = createMapGuideConnection();

            //Get the MgWebLayout object
            MgResourceService resourceSrvc = (MgResourceService)site.CreateService(MgServiceType.ResourceService);
            MgResourceIdentifier webLayoutResId = new MgResourceIdentifier(webLayoutId);
            MgWebLayout webLayout = new MgWebLayout(resourceSrvc, webLayoutResId);
            MgWebTaskPane taskPane = webLayout.GetTaskPane();
            String taskPaneUrl = taskPane.GetInitialTaskUrl();
            String vpath = getViewerRoot();
            if (taskPaneUrl == null || taskPaneUrl.length() == 0)
            {
                taskPaneUrl = "gettingstarted.jsp";
            }

            String url = URLDecoder.decode(taskPaneUrl, "UTF-8");
            int index = url.indexOf("?");

            if(index > 0)
            {
                String path = url.substring(0, index);
                String query = url.substring(index+1);

                if(query.length() > 0)
                    url = String.format("%s?SESSION=%s&MAPNAME=%s&WEBLAYOUT=%s&DWF=%s&LOCALE=%s&%s", path, sessionId, mapName, URLEncoder.encode(webLayoutId, "UTF-8"), dwf, locale, query);
                else
                    url = String.format("%s?SESSION=%s&MAPNAME=%s&WEBLAYOUT=%s&DWF=%s&LOCALE=%s", path, sessionId, mapName, URLEncoder.encode(webLayoutId, "UTF-8"), dwf, locale);
            }
            else
            {
                url = String.format("%s?SESSION=%s&MAPNAME=%s&WEBLAYOUT=%s&DWF=%s&LOCALE=%s", taskPaneUrl, sessionId, mapName, URLEncoder.encode(webLayoutId), dwf, locale);
            }
            String templ = MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/taskframe.templ"));
            String[] vals = { vpath + "tasklist.jsp",
                        locale,
                        url };
            
            response().setContentType("text/html");
            return ok(MgClassicAjaxViewerUtil.Substitute(templ, vals));
        }
        catch (MgException ex) {
            try {
                String templ = MgLocalizationUtil.Localize(MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/errorpage.templ")), locale, getClientOS());
                String[] vals = { "0", MgLocalizationUtil.GetString("TASKS", locale), ex.GetExceptionMessage() };
                response().setContentType("text/html");
                return internalServerError(MgClassicAjaxViewerUtil.Substitute(templ, vals));
            } catch (Throwable t) {}
        }
        catch (Exception ex) {
            try {
                String templ = MgLocalizationUtil.Localize(MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/errorpage.templ")), locale, getClientOS());
                String[] vals = { "0", MgLocalizationUtil.GetString("TASKS", locale), ex.getMessage() };
                response().setContentType("text/html");
                return internalServerError(MgClassicAjaxViewerUtil.Substitute(templ, vals));
            } catch (Throwable t) {
                return javaException(t);
            }
        }
        return badRequest();
    }

    public static Result taskbar() {
        try {
            String locale = "";
            
            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);

            response().setContentType("text/html");
            return ok(MgLocalizationUtil.Localize(MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/taskbar.templ")), locale, getClientOS()));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result statusbar() {
        try {
            String locale = "";
            
            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);

            response().setContentType("text/html");
            return ok(MgLocalizationUtil.Localize(MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/statusbar.templ")), locale, getClientOS()));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result legendui() {
        try {
            String locale = "";
            
            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);

            response().setContentType("text/html");
            return ok(MgLocalizationUtil.Localize(MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/legendui.templ")), locale, getClientOS()));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result legend() {
        String mapName = "";
        String sessionId = "";
        String locale = "";
        boolean summary = false;
        int layerCount = 0;
        StringBuilder output = new StringBuilder("\nvar layerData = new Array();\n");
        try {
            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);
            if (queryParams.get("MAPNAME") != null)
                mapName = MgClassicAjaxViewerUtil.ValidateMapName(queryParams.get("MAPNAME")[0]);

            BoxedInteger intermediateVar = new BoxedInteger(0);
            
            MgSiteConnection site = createMapGuideConnection();
            MgSite siteObj = site.GetSite();
            sessionId = siteObj.GetCurrentSession();

            MgResourceService resourceSrvc = (MgResourceService)site.CreateService(MgServiceType.ResourceService);

            //Load the map runtime state.
            //
            MgMap map = new MgMap();
            map.Open(resourceSrvc, mapName);

            int updateType = -1;

            ArrayList tree = MgClassicAjaxViewerUtil.BuildLayerTree(map, resourceSrvc);
            if(summary)
            {
                updateType = 0;
                // return only the layer structure, that is mainly groups/layers/layer-ids. Do not parse layer definitions.
                //
                MgClassicAjaxViewerUtil.BuildClientSideTree(tree, null, "null", false, "layerData", resourceSrvc, null, intermediateVar, output);
            }
            else
            {
                HashMap layerMap = null;
                if(layerCount == 0)
                    updateType = 1;
                else
                {
                    updateType = 2;
                    layerMap = MgClassicAjaxViewerUtil.BuildLayerMap(map);
                }
                MgClassicAjaxViewerUtil.BuildClientSideTree(tree, null, "null", true, "layerData", resourceSrvc, layerMap, intermediateVar, output);
            }

            //load html template code and format it
            //
            String templ = MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/legendupdate.templ"));
            String vals[] = { String.valueOf(updateType), output.toString(), getViewerRoot() + "legend.jsp"};
            String outputString = MgClassicAjaxViewerUtil.Substitute(templ, vals);

            response().setContentType("text/html");
            return ok(outputString);
        }
        catch(MgException e)
        {
            return mgServerError(e);
        }
        catch(Exception ne)
        {
            return javaException(ne);
        }
    }

    public static Result tasklist() {
        try {
            String locale = "";
            
            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("LOCALE") != null)
                locale = MgClassicAjaxViewerUtil.ValidateLocaleString(queryParams.get("LOCALE")[0]);

            response().setContentType("text/html");
            return ok(MgLocalizationUtil.Localize(MgClassicAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/viewerfiles/tasklist.templ")), locale, getClientOS()));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }
}