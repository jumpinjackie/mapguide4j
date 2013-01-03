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

/**
 * A controller providing the existing functionality supported/required by the MapGuide AJAX viewer
 *
 * This is a straight transplant of all relevant .jsp code from the original AJAX viewer
 * with all servlet-isms removed and/or replaced.
 */
public abstract class MgAjaxViewerController extends MgAbstractController {

    private static String getViewerRoot() {
        return controllers.routes.MgAjaxViewerController.index().url();
    }

    private static String getMapAgentUrl() {
        return controllers.routes.MgMapAgentCompatibilityController.processGetRequest().url();
    }

    private static int getClientOS() {
        return 0;
    }

    private static void setupLocalizationPath() {
        //Path separator is not appended by java.io.File! Stupid!
        String localizedPath = Play.application().getFile("internal/localized/").getPath() + File.separator;
        Logger.debug("Setting localized files path to: " + localizedPath);
        MgLocalizationUtil.SetLocalizedFilesPath(localizedPath);
    }

    public static Result index() {
        try {
            setupLocalizationPath();
            //fetch the parameters for this request
            String webLayoutDefinition = getRequestParameter("WEBLAYOUT", "");
            String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
            String sessionId = MgAjaxViewerUtil.ValidateSessionId(getRequestParameter("SESSION", ""));
            String username = getRequestParameter("USERNAME", "");
            String password = getRequestParameter("PASSWORD", "");

            Hashtable cmds = new Hashtable();
            BoxedInteger curFlyout = new BoxedInteger(0);
            boolean createSession = true;

            //Open a connection with the server
            //
            MgUserInformation cred = new MgUserInformation();
            if(sessionId != null && !sessionId.equals(""))
            {
                Logger.debug("Set MG session id");
                cred.SetMgSessionId(sessionId);
                createSession = false;
            }
            else if(username != null && !username.equals(""))
            {
                Logger.debug("Set MG credentials");
                cred.SetMgUsernamePassword(username, password);
            }
            else
            {
                if (!TrySetMgCredentials(cred))
                {
                    Logger.debug("Send WWW-Authenticate");
                    response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
                    return unauthorized("You must enter a valid login ID and password to access this site");
                }
            }

            MgSiteConnection site = new MgSiteConnection();
            cred.SetLocale(locale);
            //cred.SetClientIp(GetClientIp(request));
            cred.SetClientAgent(MgAjaxViewerUtil.GetClientAgent());

            site.Open(cred);

            if (createSession)
            {
                MgSite site1 = site.GetSite();
                sessionId = site1.CreateSession();
            }
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
                            Object[] formatArgs = { new Integer(searchCmdIndex), new Integer(j), MgAjaxViewerUtil.StrEscape(searchCmd.GetColumnDisplayNameAt(j)), MgAjaxViewerUtil.StrEscape(searchCmd.GetColumnPropertyNameAt(j)) };
                            String col = MessageFormat.format("resCols{0,number,integer}[{1,number,integer}] = new ResultColumn(\"{2}\", \"{3}\");\n", formatArgs);
                            cols += col;
                        }
                    }
                    cmdObjects += cols;

                    // declare a new search command object
                    Object[] formatArgs = { new Integer(i),
                                            MgAjaxViewerUtil.StrEscape(name),
                                            MgAjaxViewerUtil.StrEscape(searchCmd.GetLabel()),
                                            new Integer(action),
                                            searchCmd.GetIconUrl(),
                                            searchCmd.GetDisabledIconUrl(),
                                            MgAjaxViewerUtil.StrEscape(searchCmd.GetTooltip()),
                                            MgAjaxViewerUtil.StrEscape(searchCmd.GetDescription()),
                                            searchCmd.GetLayer(),
                                            MgAjaxViewerUtil.StrEscape(searchCmd.GetPrompt()),
                                            new Integer(searchCmdIndex),
                                            MgAjaxViewerUtil.StrEscape(searchCmd.GetFilter()),
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
                                            MgAjaxViewerUtil.StrEscape(name),
                                            new Integer(action),
                                            invokeUrlCmd.GetIconUrl(),
                                            invokeUrlCmd.GetDisabledIconUrl(),
                                            MgAjaxViewerUtil.StrEscape( invokeUrlCmd.GetTooltip()),
                                            MgAjaxViewerUtil.StrEscape( invokeUrlCmd.GetDescription()),
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
                                            MgAjaxViewerUtil.StrEscape( name ),
                                            new Integer( action ),
                                            targetCmd.GetIconUrl(),
                                            targetCmd.GetDisabledIconUrl(),
                                            MgAjaxViewerUtil.StrEscape(targetCmd.GetTooltip()),
                                            MgAjaxViewerUtil.StrEscape(targetCmd.GetDescription()),
                                            new Integer( targetCmd.GetTarget()),
                                            targetCmd.GetTargetName() };
                    cmdObject = MessageFormat.format("commands[{0,number,integer}] = new UiTargetCommand(\"{1}\", {2,number,integer}, \"{3}\", \"{4}\", \"{5}\", \"{6}\", {7,number,integer}, \"{8}\");\n", formatArgs);
                }
                else if(action == MgWebActions.Help)
                {
                    MgWebHelpCommand helpCmd = (MgWebHelpCommand)cmd;

                    // declare a new help  command object
                    Object[] formatArgs = { new Integer(i),
                                            MgAjaxViewerUtil.StrEscape(name),
                                            new Integer(action),
                                            helpCmd.GetIconUrl(),
                                            helpCmd.GetDisabledIconUrl(),
                                            MgAjaxViewerUtil.StrEscape(helpCmd.GetTooltip()),
                                            MgAjaxViewerUtil.StrEscape(helpCmd.GetDescription()),
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
                                            MgAjaxViewerUtil.StrEscape( name ),
                                            new Integer(action),
                                            printCmd.GetIconUrl(),
                                            printCmd.GetDisabledIconUrl(),
                                            MgAjaxViewerUtil.StrEscape( printCmd.GetTooltip()),
                                            MgAjaxViewerUtil.StrEscape( printCmd.GetDescription()),
                                            new Integer(printCmdIndex) };
                    cmdObject = MessageFormat.format("commands[{0,number,integer}] = new PrintCommand(\"{1}\", {2,number,integer}, \"{3}\", \"{4}\", \"{5}\", \"{6}\", layouts{7,number,integer});\n", formatArgs );
                    printCmdIndex++;
                }
                else if(action == MgWebActions.InvokeScript)
                {
                    MgWebInvokeScriptCommand invokeScriptCmd = (MgWebInvokeScriptCommand)cmd;

                    // declare a new basic command object
                    Object[] formatArgs = { new Integer(i),
                                            MgAjaxViewerUtil.StrEscape(name),
                                            new Integer(action),
                                            invokeScriptCmd.GetIconUrl(),
                                            invokeScriptCmd.GetDisabledIconUrl(),
                                            MgAjaxViewerUtil.StrEscape( invokeScriptCmd.GetTooltip()),
                                            MgAjaxViewerUtil.StrEscape( invokeScriptCmd.GetDescription()),
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
                                            MgAjaxViewerUtil.StrEscape(cmd.GetTooltip()),
                                            MgAjaxViewerUtil.StrEscape(cmd.GetDescription()) };
                    cmdObject = MessageFormat.format("commands[{0,number,integer}] = new BasicCommand(\"{1}\", {2,number,integer}, \"{3}\", \"{4}\", \"{5}\", \"{6}\");\n", formatArgs);
                }
                cmdObjects = cmdObjects + cmdObject;
                cmds.put(name, new Integer(i));
            }
            userCodeCalls = userCodeCalls + "\n}\n";

            //Declare toolbar items
            //
            String toolbarDef = MgAjaxViewerUtil.DeclareUiItems(cmds, curFlyout, toolBar.GetWidgets(), "toolbarItems");

            //Declare task items
            String taskListDef = MgAjaxViewerUtil.DeclareUiItems(cmds, curFlyout, taskBar.GetTaskList(), "taskItems");

            //Declare context menu items
            MgWebContextMenu ctxMenu = webLayout.GetContextMenu();
            String ctxMenuDef;
            if(ctxMenu.IsVisible())
                ctxMenuDef = MgAjaxViewerUtil.DeclareUiItems(cmds, curFlyout, ctxMenu, "ctxMenuItems");
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
                taskItemTexts += "\"" + MgAjaxViewerUtil.StrEscape(btn.GetName()) + "\"," +
                                    "\"" + MgAjaxViewerUtil.StrEscape(btn.GetTooltip()) + "\"," +
                                    "\"" + MgAjaxViewerUtil.StrEscape(btn.GetDescription()) + "\"," +
                                    "\"" + MgAjaxViewerUtil.StrEscape(btn.GetIconUrl()) + "\"," +
                                    "\"" + MgAjaxViewerUtil.StrEscape(btn.GetDisabledIconUrl()) + "\"";
            }

            //transmit the session to the map pane if one was specified to this request
            String sessionParam = "&SESSION=" + sessionId;

            //load the frameset template and format it
            String frameset = "";
            String viewerType = forDwf != 0? "DWF": "HTML";

            if(showTaskBar)
            {
                String frameSetTempl = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/framesettaskbar.templ"));
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

                frameset = MgAjaxViewerUtil.Substitute(frameSetTempl, vals);
            }
            else
            {
                String frameSetTempl = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/framesetnotaskbar.templ"));
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

                frameset = MgAjaxViewerUtil.Substitute(frameSetTempl, vals);
            }

            String homePageUrl = URLDecoder.decode(taskPaneUrl,"UTF-8");
            if(homePageUrl.length() < 8 || homePageUrl.substring(0, 7).compareToIgnoreCase("http://") != 0)
                homePageUrl = vpath + homePageUrl;

            //load the HTML template and format it
            // 0 - windows
            // 1 - mac
            // 2-  unknown
            String templ = MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/mainframe.templ")), locale, 0);

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
                              MgAjaxViewerUtil.StrEscape(title),
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

            templ = MgAjaxViewerUtil.Substitute(templ, vals);
            response().setContentType("text/html");
            return ok(templ);
        }
        catch(MgUserNotFoundException e)
        {
            Logger.debug("401 - MgUserNotFoundException");
            response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
            return unauthorized("You must enter a valid login ID and password to access this site");
        }
        catch(MgUnauthorizedAccessException e)
        {
            Logger.debug("401 - MgUnauthorizedAccessException");
            response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
            return unauthorized("You must enter a valid login ID and password to access this site");
        }
        catch(MgAuthenticationFailedException e)
        {
            Logger.debug("401 - MgAuthenticationFailedException");
            response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
            return unauthorized("You must enter a valid login ID and password to access this site");
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
        //TODO: 304 responses. This stuff rarely changes
        File f = Play.application().getFile("internal/viewerfiles/" + file);
        if (!f.exists())
            return notFound();
        else
            return ok(f);
    }

    public static Result localizedasset(String file) {
        //TODO: 304 responses. This stuff rarely changes
        File f = Play.application().getFile("internal/localized/" + file);
        if (!f.exists())
            return notFound();
        else
            return ok(f);
    }

    public static Result viewericon(String file) {
        //TODO: 304 responses. This stuff rarely changes
        File f = Play.application().getFile("internal/stdicons/" + file);
        if (!f.exists())
            return notFound();
        return ok(f);
    }

    public static Result legendctrl() {
        try {
            String mapName = MgAjaxViewerUtil.ValidateMapName(getRequestParameter("MAPNAME", ""));
            String sessionId = getMgSessionId();
            String mapFrame = MgAjaxViewerUtil.ValidateFrameName(getRequestParameter("MAPFRAME", "parent"));
            String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));

            MgSiteConnection site = createMapGuideConnection();
            if (sessionId == null) {
                MgSite siteObj = site.GetSite();
                sessionId = siteObj.GetCurrentSession();
            }

            String vpath = getViewerRoot();
            String vals[] = {
                mapFrame,
                vpath + "legend.jsp",
                vpath + "legend.jsp",
                URLEncoder.encode(mapName, "UTF-8"),
                sessionId,
                vpath + "legendui.jsp",
                locale };

            String templ = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/legendctrl.templ"));
            response().setContentType("text/html");
            return ok(MgAjaxViewerUtil.Substitute(templ, vals));
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
            String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
            String mapFrame = MgAjaxViewerUtil.ValidateFrameName(getRequestParameter("MAPFRAME", "parent"));

            String templ = MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/propertyctrl.templ")), locale, getClientOS());
            String vals[] = { mapFrame };
            response().setContentType("text/html");
            return ok(MgAjaxViewerUtil.Substitute(templ, vals));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result mapframe() {
        try {
            String mapDefinition = MgAjaxViewerUtil.ValidateResourceId(getRequestParameter("MAPDEFINITION", ""));
            String locale =  MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
            int infoWidth = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("INFOWIDTH", "250"));
            int showLegend = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("SHOWLEGEND", "1"));
            int showProperties = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("SHOWPROP", "1"));
            int showSlider = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("SHOWSLIDER", "1"));
            String sessionId = getMgSessionId();;
            String type = getRequestParameter("TYPE", "");
            String hlTgt = MgAjaxViewerUtil.ValidateHyperlinkTargetValue(getRequestParameter("HLTGT", ""));
            String hlTgtName = MgAjaxViewerUtil.ValidateFrameName(getRequestParameter("HLTGTNAME", ""));

            MgSiteConnection site = createMapGuideConnection();
            if (sessionId == null) {
                MgSite siteObj = site.GetSite();
                sessionId = siteObj.GetCurrentSession();
            }

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
                String templ = MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/dwfmappane.templ")), locale, getClientOS());
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
                String templ = MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/ajaxmappane.templ")), locale, getClientOS());
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
                templ = MgAjaxViewerUtil.Substitute(templ, vals);
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
            String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
            String templ = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/toolbar.templ"));
            response().setContentType("text/html");
            return ok(templ);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result formframe() {
        try {
            String templ = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/formframe.templ"));
            response().setContentType("text/html");
            return ok(templ);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result taskframe() {
        String sessionId = getMgSessionId();
        String webLayoutId = MgAjaxViewerUtil.ValidateResourceId(getRequestParameter("WEBLAYOUT", ""));
        int dwf = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("DWF", "0"));
        String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
        String mapName = MgAjaxViewerUtil.ValidateMapName(getRequestParameter("MAPNAME", ""));
        try {
            //connect to the site and get a feature service and a resource service instances
            MgSiteConnection site = createMapGuideConnection();
            if (sessionId == null) {
                MgSite siteObj = site.GetSite();
                sessionId = siteObj.GetCurrentSession();
            }

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
            String templ = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/taskframe.templ"));
            String[] vals = { vpath + "tasklist.jsp",
                        locale,
                        url };

            response().setContentType("text/html");
            return ok(MgAjaxViewerUtil.Substitute(templ, vals));
        }
        catch (MgException ex) {
            try {
                String templ = MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/errorpage.templ")), locale, getClientOS());
                String[] vals = { "0", MgLocalizationUtil.GetString("TASKS", locale), ex.GetExceptionMessage() };
                response().setContentType("text/html");
                return internalServerError(MgAjaxViewerUtil.Substitute(templ, vals));
            } catch (Throwable t) {}
        }
        catch (Exception ex) {
            try {
                String templ = MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/errorpage.templ")), locale, getClientOS());
                String[] vals = { "0", MgLocalizationUtil.GetString("TASKS", locale), ex.getMessage() };
                response().setContentType("text/html");
                return internalServerError(MgAjaxViewerUtil.Substitute(templ, vals));
            } catch (Throwable t) {
                return javaException(t);
            }
        }
        return badRequest();
    }

    public static Result taskbar() {
        try {
            String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
            response().setContentType("text/html");
            return ok(MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/taskbar.templ")), locale, getClientOS()));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result statusbar() {
        try {
            String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
            response().setContentType("text/html");
            return ok(MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/statusbar.templ")), locale, getClientOS()));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result legendui() {
        try {
            String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
            response().setContentType("text/html");
            return ok(MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/legendui.templ")), locale, getClientOS()));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result legend() {
        String mapName = MgAjaxViewerUtil.ValidateMapName(getRequestParameter("MAPNAME", ""));
        String sessionId = getMgSessionId();
        String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
        boolean summary = false;
        int layerCount = 0;
        StringBuilder output = new StringBuilder("\nvar layerData = new Array();\n");
        try {
            BoxedInteger intermediateVar = new BoxedInteger(0);

            MgSiteConnection site = createMapGuideConnection();
            if (sessionId == null) {
                MgSite siteObj = site.GetSite();
                sessionId = siteObj.GetCurrentSession();
            }

            MgResourceService resourceSrvc = (MgResourceService)site.CreateService(MgServiceType.ResourceService);

            //Load the map runtime state.
            //
            MgMap map = new MgMap();
            map.Open(resourceSrvc, mapName);

            int updateType = -1;

            ArrayList tree = MgAjaxViewerUtil.BuildLayerTree(map, resourceSrvc);
            if(summary)
            {
                updateType = 0;
                // return only the layer structure, that is mainly groups/layers/layer-ids. Do not parse layer definitions.
                //
                MgAjaxViewerUtil.BuildClientSideTree(tree, null, "null", false, "layerData", resourceSrvc, null, intermediateVar, output);
            }
            else
            {
                HashMap layerMap = null;
                if(layerCount == 0)
                    updateType = 1;
                else
                {
                    updateType = 2;
                    layerMap = MgAjaxViewerUtil.BuildLayerMap(map);
                }
                MgAjaxViewerUtil.BuildClientSideTree(tree, null, "null", true, "layerData", resourceSrvc, layerMap, intermediateVar, output);
            }

            //load html template code and format it
            //
            String templ = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/legendupdate.templ"));
            String vals[] = { String.valueOf(updateType), output.toString(), getViewerRoot() + "legend.jsp"};
            String outputString = MgAjaxViewerUtil.Substitute(templ, vals);

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
            String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
            response().setContentType("text/html");
            return ok(MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/tasklist.templ")), locale, getClientOS()));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result measure() {
        String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", ""));;
        String mapName = MgAjaxViewerUtil.ValidateMapName(getRequestParameter("MAPNAME", ""));
        String sessionId = getMgSessionId();
        int target = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("TGT", "0"));
        int popup = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("POPUP", "0"));
        boolean clear = false;
        double x1 = 0;
        double y1 = 0;
        double x2 = 0;
        double y2 = 0;
        double total = 0;
        String srs = "";
        String units = getRequestParameter("UNITS", "");
        int segId = 1;
        String error = "";
        double distance = 0;
        String legendName = "Measure";
        String featureName = "Measure";
        String dataSource = "Session:" + sessionId + "//Measure.FeatureSource";
        String layerDef = "Session:" + sessionId + "//Measure.LayerDefinition";
        try {
            if (hasRequestParameter("CLEAR"))
            {
                clear = true;
            }
            else
            {
                clear = false;
                x1 = MgAjaxViewerUtil.GetDoubleParameter(getRequestParameter("X1", "0"));
                y1 = MgAjaxViewerUtil.GetDoubleParameter(getRequestParameter("Y1", "0"));
                x2 = MgAjaxViewerUtil.GetDoubleParameter(getRequestParameter("X2", "0"));
                y2 = MgAjaxViewerUtil.GetDoubleParameter(getRequestParameter("Y2", "0"));
                total = MgAjaxViewerUtil.GetDoubleParameter(getRequestParameter("TOTAL", "0"));
                segId = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("SEGID", "1"));
            }

            MgSiteConnection site = createMapGuideConnection();
            MgFeatureService featureSrvc = (MgFeatureService)site.CreateService(MgServiceType.FeatureService);
            MgResourceService resourceSrvc = (MgResourceService)site.CreateService(MgServiceType.ResourceService);

            MgResourceIdentifier dataSourceId = new MgResourceIdentifier(dataSource);
            MgResourceIdentifier layerDefId = new MgResourceIdentifier(layerDef);

            //load the map runtime state and locate the measure layer
            //
            MgMap map = new MgMap();
            map.Open(resourceSrvc, mapName);
            MgLayerCollection layers = map.GetLayers();
            srs = MgAjaxViewerUtil.GetMapSrs(map);

            MgLayer layer = MgAjaxViewerUtil.FindLayer(layers, layerDef);

            if(clear)
            {
                total = 0;
                if(layer != null)
                    layers.Remove(layer);
                if(MgAjaxViewerUtil.DataSourceExists(resourceSrvc, dataSourceId))
                    MgAjaxViewerUtil.ClearDataSource(featureSrvc, dataSourceId, featureName);
            }
            else
            {
                MgCoordinateSystemFactory srsFactory = new MgCoordinateSystemFactory();
                MgCoordinateSystem srsMap = srsFactory.Create(srs);

                int srsType = srsMap.GetType();
                if(srsType == MgCoordinateSystemType.Geographic)
                    distance = srsMap.MeasureGreatCircleDistance(x1, y1, x2, y2);
                else
                    distance = srsMap.MeasureEuclideanDistance(x1, y1, x2, y2);

                distance = srsMap.ConvertCoordinateSystemUnitsToMeters(distance);

                if (units.equals("mi")) distance *= 0.000621371192;  //get miles
                if (units.equals("km")) distance *= 0.001;           //get kilometers
                if (units.equals("ft")) distance *= 3.2808399;       //get feet
                if (units.equals("usft")) distance *= 3.2808333;       //get US survey feet

                total += distance;

                //create the line string geometry representing this segment
                //
                MgGeometryFactory geomFactory = new MgGeometryFactory();
                MgCoordinateCollection coordinates = new MgCoordinateCollection();
                coordinates.Add(geomFactory.CreateCoordinateXY(x1, y1));
                coordinates.Add(geomFactory.CreateCoordinateXY(x2, y2));
                MgLineString geom = geomFactory.CreateLineString(coordinates);

                if(segId == 1)
                {
                    //first segment
                    //
                    if(!MgAjaxViewerUtil.DataSourceExists(resourceSrvc, dataSourceId))
                    {
                        //create feature source
                        //
                        MgClassDefinition classDef = new MgClassDefinition();

                        classDef.SetName(featureName);
                        classDef.SetDescription(MgLocalizationUtil.GetString("MEASUREFEATURECLASS", locale));
                        classDef.SetDefaultGeometryPropertyName("GEOM");

                        //Set KEY property
                        MgDataPropertyDefinition prop = new MgDataPropertyDefinition("KEY");
                        prop.SetDataType(MgPropertyType.Int32);
                        prop.SetAutoGeneration(true);
                        prop.SetReadOnly(true);
                        classDef.GetIdentityProperties().Add(prop);
                        classDef.GetProperties().Add(prop);

                        //Set PARTIAL property. Hold the distance for this segment
                        prop = new MgDataPropertyDefinition("PARTIAL");
                        prop.SetDataType(MgPropertyType.Double);
                        classDef.GetProperties().Add(prop);

                        //Set TOTAL property. Hold the total distance up to this segment, including it
                        prop = new MgDataPropertyDefinition("TOTAL");
                        prop.SetDataType(MgPropertyType.Double);
                        classDef.GetProperties().Add(prop);

                        //Set geometry property
                        MgGeometricPropertyDefinition geomProp = new MgGeometricPropertyDefinition("GEOM");
                        //geomProp.SetGeometryTypes(MgFeatureGeometricType.mfgtSurface); //TODO use the constant when exposed
                        geomProp.SetGeometryTypes(4);
                        classDef.GetProperties().Add(geomProp);

                        //Create the schema
                        MgFeatureSchema schema = new MgFeatureSchema("MeasureSchema", MgLocalizationUtil.GetString("MEASURESCHEMADESCR", locale));
                        schema.GetClasses().Add(classDef);

                        //finally, creation of the feature source
                        MgCreateSdfParams parameters = new MgCreateSdfParams("MapSrs", srs, schema);
                        featureSrvc.CreateFeatureSource(dataSourceId, parameters);

                        //build map tip
                        String unitText = "";
                        if (units.equals("mi")) unitText = "DISTANCEMILES";
                        if (units.equals("km")) unitText = "DISTANCEKILOMETERS";
                        if (units.equals("ft")) unitText = "DISTANCEFEET";
                        if (units.equals("usft")) unitText = "DISTANCEUSFEET";
                        if (units.equals("m")) unitText = "DISTANCEMETERS";
                        unitText = MgLocalizationUtil.GetString(unitText, locale);

                        String tip = "Concat(Concat(Concat('" + MgLocalizationUtil.GetString("MEASUREPARTIAL", locale) + ": ', PARTIAL), Concat(', " + MgLocalizationUtil.GetString("MEASURETOTAL", locale) + ": ', TOTAL)), ' (" + unitText + ")')";

                        //Create the layer definition
                        String layerTempl = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/linelayerdef.templ"));
                        MgByteReader layerDefContent = MgAjaxViewerUtil.BuildLayerDefinitionContent(layerTempl, dataSource, featureName, tip);
                        resourceSrvc.SetResource(layerDefId, layerDefContent, null);
                    }
                    else
                    {
                        //data source already exist. clear its content
                        MgAjaxViewerUtil.ClearDataSource(featureSrvc, dataSourceId, featureName);
                    }

                    //Add the layer to the map, if it's not already in it
                    if(layer == null)
                    {
                        legendName = MgLocalizationUtil.GetString("MEASURELAYER", locale);
                        layer = new MgLayer(layerDefId, resourceSrvc);
                        layer.SetDisplayInLegend(true);
                        layer.SetLegendLabel(legendName);
                        layers.Insert(0, layer);
                    }
                }
                // create a feature representing this segment and insert it into the data source
                //
                MgPropertyCollection measureProps = new MgPropertyCollection();

                MgDoubleProperty partialProp = new MgDoubleProperty("PARTIAL", distance);
                measureProps.Add(partialProp);

                MgDoubleProperty totalProp = new MgDoubleProperty("TOTAL", total);
                measureProps.Add(totalProp);

                MgAgfReaderWriter agf = new MgAgfReaderWriter();
                MgByteReader geomReader = agf.Write(geom);
                MgGeometryProperty geometryProp = new MgGeometryProperty("GEOM", geomReader);
                measureProps.Add(geometryProp);

                MgInsertFeatures cmd = new MgInsertFeatures(featureName, measureProps);
                MgFeatureCommandCollection commands = new MgFeatureCommandCollection();
                commands.Add(cmd);

                //Insert the distance feature in the temporary data source
                //
                MgAjaxViewerUtil.ReleaseReader(featureSrvc.UpdateFeatures(dataSourceId, commands, false));
            }

            if(layer != null)
                layer.ForceRefresh();
            map.Save(resourceSrvc);

            String templ = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/measureui.templ"));

            templ = MgLocalizationUtil.Localize(templ, locale, getClientOS());

            String vpath = getViewerRoot();
            String[] vals = {
                    locale,
                    String.valueOf(target),
                    String.valueOf(popup),
                    mapName,
                    sessionId,
                    String.valueOf(total),
                    String.valueOf(distance),
                    "1",
                    units,
                    vpath + "measure.jsp",
                    vpath + "measure.jsp"
            };
            response().setContentType("text/html");
            return ok(MgAjaxViewerUtil.Substitute(templ, vals));

        }
        catch(MgException exc)
        {
            return mgServerError(exc);
        }
        catch(Exception ne)
        {
            return javaException(ne);
        }
    }

    public static Result measureui() {
        try {
            int target = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("TARGET", "0"));
            String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
            int popup = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("POPUP", "0"));
            int cmdIndex = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("CMDINDEX", "-1"));
            String mapName = MgAjaxViewerUtil.ValidateMapName(getRequestParameter("MAPNAME", ""));
            String sessionId = getMgSessionId();
            String units = getRequestParameter("UNITS", "");
            double total = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("TOTAL", "0"));
            String templ = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/measureui.templ"));
            setupLocalizationPath();
            templ = MgLocalizationUtil.Localize(templ, locale, getClientOS());

            String vpath = getViewerRoot();
            String[] vals = {
                    locale,
                    String.valueOf(target),
                    String.valueOf(popup),
                    mapName,
                    sessionId,
                    String.valueOf(total),
                    "0",
                    "0",
                    units,
                    vpath + "measure.jsp",
                    vpath + "measure.jsp"
            };
            response().setContentType("text/html");
            return ok(MgAjaxViewerUtil.Substitute(templ, vals));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result bufferui() {
        try {
            String mapName = MgAjaxViewerUtil.ValidateMapName(getRequestParameter("MAPNAME", ""));
            String sessionId = getMgSessionId();
            String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
            int popup = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("POPUP", "0"));
            int us = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("US", "0"));

            setupLocalizationPath();

            String vpath = getViewerRoot();
            String templ = MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/bufferui.templ")), locale, getClientOS());
            String[] vals = { String.valueOf(popup),
                              vpath + "colorpicker.jsp",
                              locale,
                              vpath + "buffer.jsp",
                              us == 1? "selected" : "",
                              us == 1? "" : "selected",
                              mapName,
                              sessionId,
                              String.valueOf(popup),
                              locale
                              };

            response().setContentType("text/html");
            return ok(MgAjaxViewerUtil.Substitute(templ, vals));
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result buffer() {

        String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
        String mapName = MgAjaxViewerUtil.ValidateMapName(getRequestParameter("MAPNAME", ""));
        String sessionId = getMgSessionId();
        String bufferName = getRequestParameter("BUFFER", "");
        String layersParam = getRequestParameter("LAYERS", "");
        int popup = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("POPUP", "0"));
        String lcolor = MgAjaxViewerUtil.ValidateColorString(getRequestParameter("LCOLOR", ""));
        String ffcolor = MgAjaxViewerUtil.ValidateColorString(getRequestParameter("FFCOLOR", ""));
        String fbcolor = MgAjaxViewerUtil.ValidateColorString(getRequestParameter("FBCOLOR", ""));
        int transparent = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("TRANSPARENT", ""));
        double distance = MgAjaxViewerUtil.GetLocalizedDoubleParameter(getRequestParameter("DISTANCE", "0"), locale);
        String units = getRequestParameter("UNITS", "");
        String linestyle = getRequestParameter("LINESTYLE", "");
        String fillstyle = getRequestParameter("FILLSTYLE", "");
        double thickness = MgAjaxViewerUtil.GetDoubleParameter(getRequestParameter("THICKNESS", "0"));
        int merge = 0;
        double foretrans = MgAjaxViewerUtil.GetDoubleParameter(getRequestParameter("FORETRANS", "50"));
        String selText = getRequestParameter("SELECTION", "");
        String srs = "";
        String featureName = "Buffer";
        String dataSource = "Session:" + sessionId + "//" + bufferName + "_Buffer.FeatureSource";

        if(foretrans < 0 || foretrans > 100)
        {
            foretrans = 50;
        }
        if (hasRequestParameter("MERGE"))
             merge = 1;

        setupLocalizationPath();
        String layerDef = "Session:" + sessionId + "//" + bufferName + "_Buffer.LayerDefinition";
        try
        {
            boolean newBuffer = false;

            //connect to the site and get a feature service and a resource service instances
            MgSiteConnection site = createMapGuideConnection();
            MgFeatureService featureSrvc = (MgFeatureService)site.CreateService(MgServiceType.FeatureService);
            MgResourceService resourceSrvc = (MgResourceService)site.CreateService(MgServiceType.ResourceService);

            MgResourceIdentifier dataSourceId = new MgResourceIdentifier(dataSource);
            MgResourceIdentifier layerDefId = new MgResourceIdentifier(layerDef);

            //load the map runtime state
            //
            MgMap map = new MgMap();
            map.Open(resourceSrvc, mapName);

            //locate the buffer layer in the map. It might or might not already exist
            //
            MgLayerCollection layers = map.GetLayers();
            MgLayer layer = MgAjaxViewerUtil.FindLayer(layers, bufferName);

            String[] layerNames = layersParam.split(",");

            // convert distance to meters
            if(units.equals("mi"))      //miles
                distance *= 1609.35;
            else if(units.equals("ki")) //kilometers
                distance *= 1000;
            else if(units.equals("fe")) //feet
                distance *= 0.30480;

            // Get the map SRS
            //
            MgCoordinateSystemFactory srsFactory = new MgCoordinateSystemFactory();
            String srsDefMap = MgAjaxViewerUtil.GetMapSrs(map);
            String mapSrsUnits = "";
            MgCoordinateSystem srsMap = srsFactory.Create(srsDefMap);
            boolean arbitraryMapSrs = (srsMap.GetType() == MgCoordinateSystemType.Arbitrary);
            if(arbitraryMapSrs)
                mapSrsUnits = srsMap.GetUnits();

            //Create/Modify layer definition
            String layerTempl = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/arealayerdef.templ"));
            MgByteReader layerDefContent = MgAjaxViewerUtil.BuildAreaLayerDefinitionContent(layerTempl, dataSource, featureName, fillstyle, ffcolor, transparent, fbcolor, linestyle, thickness, foretrans, lcolor);
            resourceSrvc.SetResource(layerDefId, layerDefContent, null);

            if(layer == null)
            {
                newBuffer = true;
                //Targetting a new layer. create a data source for it
                //
                MgClassDefinition classDef = new MgClassDefinition();

                classDef.SetName(featureName);
                classDef.SetDescription(MgLocalizationUtil.GetString("BUFFERCLASSDESCR", locale));
                classDef.SetDefaultGeometryPropertyName("GEOM");

                //Set KEY property
                MgDataPropertyDefinition propKey = new MgDataPropertyDefinition("KEY");
                propKey.SetDataType(MgPropertyType.Int32);
                propKey.SetAutoGeneration(true);
                propKey.SetReadOnly(true);
                classDef.GetIdentityProperties().Add(propKey);
                classDef.GetProperties().Add(propKey);

                //Set ID property. Hold this segment ID
                MgDataPropertyDefinition propID = new MgDataPropertyDefinition("ID");
                propID.SetDataType(MgPropertyType.Int32);
                classDef.GetProperties().Add(propID);

                //Set geometry property
                MgGeometricPropertyDefinition geomProp = new MgGeometricPropertyDefinition("GEOM");
                //geomProp.SetGeometryTypes(MgFeatureGeometricType.mfgtSurface); //TODO use the constant when exposed
                geomProp.SetGeometryTypes(4);
                classDef.GetProperties().Add(geomProp);

                //Create the schema
                MgFeatureSchema schema = new MgFeatureSchema("BufferSchema", MgLocalizationUtil.GetString("BUFFERSCHEMADESCR", locale));
                schema.GetClasses().Add(classDef);

                //finally, creation of the feature source
                MgCreateSdfParams sdfParams = new MgCreateSdfParams("LatLong", srsDefMap, schema);
                featureSrvc.CreateFeatureSource(dataSourceId, sdfParams);

                //Add layer to map
                layer = new MgLayer(layerDefId, resourceSrvc);
                layer.SetName(bufferName);
                layer.SetLegendLabel(bufferName);
                layer.SetDisplayInLegend(true);
                layer.SetSelectable(true);
                layers.Insert(0, layer);
            }
            else
            {
                //data source already exist. clear its content
                //
                MgAjaxViewerUtil.ClearDataSource(featureSrvc, dataSourceId, featureName);
            }

            MgSelection sel = new MgSelection(map, selText);
            MgReadOnlyLayerCollection selLayers = sel.GetLayers();

            MgAgfReaderWriter agfRW = new MgAgfReaderWriter();
            MgGeometryCollection bufferGeometries = new MgGeometryCollection();
            MgGeometry geomBuffer;

            MgFeatureCommandCollection commands = new MgFeatureCommandCollection();
            int featId = 0;

            MgBatchPropertyCollection propCollection = new MgBatchPropertyCollection();

            int excludedLayers = 0;
            MgCoordinateSystem srsDs = null;
            MgGeometryCollection inputGeometries = new MgGeometryCollection();

            int bufferFeatures = 0;
            for(int li =0; li < selLayers.GetCount(); li++)
            {
                MgLayer selLayer = (MgLayer) selLayers.GetItem(li);
                boolean inputLayer = false;
                String selLayerName = selLayer.GetName();
                for(int il = 0; il < layerNames.length; il++)
                {
                    if(layerNames[il].equals(selLayerName))
                    {
                        inputLayer = true;
                        break;
                    }
                }
                if(inputLayer == false)
                    continue;

                // get the data source SRS
                //
                MgResourceIdentifier featSourceId = new MgResourceIdentifier(selLayer.GetFeatureSourceId());
                MgSpatialContextReader ctxs = featureSrvc.GetSpatialContexts(featSourceId, false);
                String srsDefDs = "";
                if(ctxs != null && ctxs.ReadNext())
                    srsDefDs = ctxs.GetCoordinateSystemWkt();

                if(srsDefDs == null || srsDefDs.length() == 0)
                {
                    excludedLayers++;
                    continue;
                }

                srsDs = srsFactory.Create(srsDefDs);
                boolean arbitraryDsSrs = (srsDs.GetType() == MgCoordinateSystemType.Arbitrary);
                String dsSrsUnits = "";

                if(arbitraryDsSrs)
                    dsSrsUnits = srsDs.GetUnits();

                // exclude layer if:
                //  the map is non-arbitrary and the layer is arbitrary or vice-versa
                //     or
                //  layer and map are both arbitrary but have different units
                //
                if((arbitraryDsSrs != arbitraryMapSrs) || (arbitraryDsSrs && !dsSrsUnits.equals(mapSrsUnits)))
                {
                    excludedLayers++;
                    continue;
                }

                // calculate distance in the data source SRS units
                //
                double dist = srsDs.ConvertMetersToCoordinateSystemUnits(distance);

                // calculate great circle unless data source srs is arbitrary
                MgCoordinateSystemMeasure measure;
                if(!arbitraryDsSrs)
                     measure = srsDs.GetMeasure();
                else
                     measure = null;

                // create a SRS transformer if necessary
                MgCoordinateSystemTransform srsXform;
                if(!srsDefDs.equals(srsDefMap))
                    srsXform = srsFactory.GetTransform(srsDs, srsMap);
                else
                    srsXform = null;

                String featureClassName = selLayer.GetFeatureClassName();
                String filter = sel.GenerateFilter(selLayer, featureClassName);
                if(filter == null || filter.length() == 0)
                    continue;

                MgFeatureQueryOptions query = new MgFeatureQueryOptions();
                query.SetFilter(filter);

                MgResourceIdentifier featureSource = new MgResourceIdentifier(selLayer.GetFeatureSourceId());

                MgFeatureReader features = featureSrvc.SelectFeatures(featureSource, featureClassName, query);

                if(features.ReadNext())
                {
                    MgClassDefinition classDef = features.GetClassDefinition();
                    String geomPropName = classDef.GetDefaultGeometryPropertyName();

                    do
                    {
                        MgByteReader geomReader = features.GetGeometry(geomPropName);
                        MgGeometry geom = agfRW.Read(geomReader);

                        if(merge == 0)
                        {
                            geomBuffer = geom.Buffer(dist, measure);
                            if (geomBuffer != null)
                            {
                                if(srsXform != null)
                                    geomBuffer = (MgGeometry)geomBuffer.Transform(srsXform);
                                MgAjaxViewerUtil.AddFeatureToCollection(propCollection, agfRW, featId++, geomBuffer);
                                bufferFeatures++;
                            }
                        }
                        else
                        {
                            if(srsXform != null)
                                geom = (MgGeometry)geom.Transform(srsXform);
                            inputGeometries.Add(geom);
                        }
                    }
                    while(features.ReadNext());

                    features.Close();
                }
            }

            if(merge == 1)
            {
                if(inputGeometries.GetCount() > 0)
                {
                    double dist = srsMap.ConvertMetersToCoordinateSystemUnits(distance);
                    MgCoordinateSystemMeasure measure;
                    if(!arbitraryMapSrs)
                        measure = srsMap.GetMeasure();
                    else
                        measure = null;

                    MgGeometryFactory geomFactory = new MgGeometryFactory();
                    geomBuffer = geomFactory.CreateMultiGeometry(inputGeometries).Buffer(dist, measure);
                    if (geomBuffer != null)
                    {
                        MgAjaxViewerUtil.AddFeatureToCollection(propCollection, agfRW, featId, geomBuffer);
                        bufferFeatures = 1;
                    }
                }
            }

            if(propCollection.GetCount() > 0)
            {
                commands.Add(new MgInsertFeatures(featureName, propCollection));

                //Insert the features in the temporary data source
                //
                MgAjaxViewerUtil.ReleaseReader(featureSrvc.UpdateFeatures(dataSourceId, commands, false), commands);
            }

            // Save the new map state
            //
            layer.ForceRefresh();
            map.Save(resourceSrvc);

            //build report message
            String title = MgLocalizationUtil.GetString("BUFFERREPORTTITLE", locale);
            String createdUpdatedFmt = newBuffer ? MgLocalizationUtil.GetString("BUFFERREPORTCREATED", locale) : MgLocalizationUtil.GetString("BUFFERREPORTUPDATED", locale);
            String createdUpdatedStr = MgAjaxViewerUtil.Substitute(createdUpdatedFmt, new String[] { bufferName } );
            String featuresFmt = bufferFeatures > 1 ? MgLocalizationUtil.GetString("BUFFERREPORTFEATURESPLURAL", locale) : MgLocalizationUtil.GetString("BUFFERREPORTFEATURESSINGULAR", locale);
            String featuresStr = MgAjaxViewerUtil.Substitute(featuresFmt, new String[] { String.valueOf(bufferFeatures) } );
            String msg = createdUpdatedStr + "<p><p>"  + featuresStr;

            if(excludedLayers > 0) {
                String warningFmt = excludedLayers > 1 ? MgLocalizationUtil.GetString("BUFFERREPORTWARNINGPLURAL", locale) : MgLocalizationUtil.GetString("BUFFERREPORTWARNINGSINGULAR", locale);
                String warningStr = MgAjaxViewerUtil.Substitute(warningFmt, new String[] { String.valueOf(excludedLayers) } );
                msg = msg + "<p><p>" + warningStr;
            }

            // return the report page
            String templ = MgLocalizationUtil.Localize(MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("/internal/viewerfiles/bufferreport.templ")), locale, getClientOS());
            String[] vals = {
                        String.valueOf(popup),
                        title,
                        msg};
            response().setContentType("text/html");
            return ok(MgAjaxViewerUtil.Substitute(templ, vals));
        }
        catch(MgException exc)
        {
            //OnError(MgLocalizationUtil.GetString("BUFFERREPORTERRORTITLE", locale), exc.GetDetails(), outStream, request);
            return mgServerError(exc);
        }
        catch(Exception ne)
        {
            //OnError(MgLocalizationUtil.GetString("BUFFERREPORTERRORTITLE", locale), ne.getMessage(), outStream, request);
            return javaException(ne);
        }
    }

    public static Result gettingstarted() {
        String cmdListPage = "WS1a9193826455f5ff9110c71085341391d-2e28.htm";
        String sessionId = getMgSessionId();
        String webLayout = MgAjaxViewerUtil.ValidateResourceId(getRequestParameter("WEBLAYOUT", ""));
        String pageName = getRequestParameter("PAGE", "");
        String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
        boolean dwf = getRequestParameter("DWF", "0").equals("1");
        try {
            if(pageName.equals("")) {
                //no page name specified, assume main getting started page (the command list)
                pageName = cmdListPage;
            }
            //load the original page
            String orgHtml = "";
            try {
                orgHtml = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/localized/help/" + locale + "/" + pageName));
            } catch(Exception e) {
                orgHtml = MgAjaxViewerUtil.LoadTemplate(Play.application().getFile("internal/localized/help/en/" + pageName));
                locale = MgAjaxViewerUtil.GetDefaultLocale();
            }
            String mgWebRoot = getViewerRoot() + "../";
            String fixedupHtml = MgAjaxViewerUtil.FixupPageReferences(orgHtml, webLayout, dwf, mgWebRoot, locale);
            response().setContentType("text/html");
            if(pageName.equals(cmdListPage)) {
                //filter out unused commands
                //
                MgSiteConnection site = createMapGuideConnection();
                MgResourceService resourceSrvc = (MgResourceService)site.CreateService(MgServiceType.ResourceService);
                MgResourceIdentifier wli = new MgResourceIdentifier(webLayout);
                byte[] hb = fixedupHtml.getBytes("UTF-8");
                MgByteSource src = new MgByteSource(hb, hb.length);
                MgWebLayout wl = new MgWebLayout(resourceSrvc, wli);
                MgByteReader pagestream = wl.ProcessGettingStartedPage(src.GetReader(), dwf);
                if (pagestream == null)
                    return ok(fixedupHtml);
                else
                    return ok(pagestream.ToString());
            }
            else
                return ok(fixedupHtml);
        }
        catch(MgException mge) {
            return mgServerError(mge);
        }
        catch(Exception e) {
            String errorMsg = MgAjaxViewerUtil.EscapeForHtml(e.getMessage());
            return internalServerError(errorMsg);
        }
    }

    public static Result getselectedfeatures() {
        String mapName = MgAjaxViewerUtil.ValidateMapName(getRequestParameter("MAPNAME", ""));
        String sessionId = getMgSessionId();
        String locale = MgAjaxViewerUtil.ValidateLocaleString(getRequestParameter("LOCALE", "en"));
        try
        {
            MgSiteConnection site = createMapGuideConnection();
            if (sessionId == null) {
                MgSite siteObj = site.GetSite();
                sessionId = siteObj.GetCurrentSession();
            }

            MgResourceService resSvc = (MgResourceService)site.CreateService(MgServiceType.ResourceService);

            MgMap map = new MgMap(site);
            map.Open(mapName);

            MgSelection selection = new MgSelection(map);
            selection.Open(resSvc, mapName);

            MgReadOnlyLayerCollection layers = selection.GetLayers();
            if (layers != null && layers.GetCount() > 0)
            {
                int layerCount = layers.GetCount();
                MgAgfReaderWriter agfRW = new MgAgfReaderWriter();
                SelectionSet selectionSet = new SelectionSet();

                for (int i = 0; i < layerCount; i++)
                {
                    MgLayerBase layer = layers.GetItem(i);
                    String layerName = layer.GetName();

                    MgResourceIdentifier fsId = new MgResourceIdentifier(layer.GetFeatureSourceId());
                    String className = layer.GetFeatureClassName();
                    String geomName = layer.GetFeatureGeometryName();

                    MgFeatureQueryOptions query = new MgFeatureQueryOptions();
                    HashMap<String, String> mappings = MgAjaxViewerUtil.GetLayerPropertyMappings(resSvc, layer);
                    Set<String> propNames = mappings.keySet();

                    for (String name : propNames)
                    {
                        query.AddFeatureProperty(name);
                    }

                    query.AddFeatureProperty(geomName);
                    String filter = selection.GenerateFilter(layer, className);
                    query.SetFilter(filter);

                    MgFeatureReader reader = layer.SelectFeatures(query);

                    MgClassDefinition clsDef = reader.GetClassDefinition();
                    MgPropertyDefinitionCollection props = clsDef.GetProperties();

                    while (reader.ReadNext())
                    {
                        Feature feat = new Feature(layerName);
                        ZoomBox zoom = null;

                        for (int k = 0; k < props.GetCount(); k++)
                        {
                            MgPropertyDefinition propDef = props.GetItem(k);
                            String propName = propDef.GetName();
                            int propType = reader.GetPropertyType(propName);

                            if (mappings.get(propName) != null || propType == MgPropertyType.Geometry)
                            {
                                String value = "";
                                if (!reader.IsNull(propName))
                                {
                                    if (propName.equals(geomName))
                                    {
                                        MgByteReader agf = reader.GetGeometry(propName);
                                        MgGeometry geom = agfRW.Read(agf);

                                        MgEnvelope env = geom.Envelope();
                                        MgCoordinate ll = env.GetLowerLeftCoordinate();
                                        MgCoordinate ur = env.GetUpperRightCoordinate();

                                        zoom = new ZoomBox();
                                        zoom.MinX = ll.GetX();
                                        zoom.MinY = ll.GetY();
                                        zoom.MaxX = ur.GetX();
                                        zoom.MaxY = ur.GetY();

                                        feat.Zoom = zoom;
                                    }
                                    else
                                    {
                                        value = MgAjaxViewerUtil.GetPropertyValueFromFeatureReader(reader, agfRW, propType, propName, locale);
                                    }

                                    if (mappings.get(propName) != null)
                                    {
                                        FeatureProperty fp = new FeatureProperty();
                                        fp.Name = mappings.get(propName);
                                        fp.Value = value;

                                        feat.addProperty(fp);
                                    }
                                }
                            }
                        }
                        selectionSet.addFeature(feat);
                    }
                    reader.Close();
                }

                //Now output the selection set
                response().setContentType("application/json");
                return ok(MgAjaxViewerUtil.GetJson(selectionSet));
            } else {
                response().setContentType("application/json");
                return ok("{}");
            }
        }
        catch (MgException ex)
        {
            response().setContentType("application/json");
            return internalServerError(MgAjaxViewerUtil.JsonifyError(ex, locale));
        }
        catch (Exception ex)
        {
            response().setContentType("application/json");
            return internalServerError(MgAjaxViewerUtil.JsonifyError(ex, locale));
        }
    }
}