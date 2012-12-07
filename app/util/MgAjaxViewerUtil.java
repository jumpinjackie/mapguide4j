package util;

import model.*;

import play.*;
import org.osgeo.mapguide.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;
import java.text.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

//port of common.jsp with servlet-isms removed and/or replaced

/**
 * Utility functions for the AJAX viewer
 */
public class MgAjaxViewerUtil {

    public static InputStream LoadViewerIconResourceStream(String name) {
        return Play.application().classloader().getResourceAsStream("resources/stdicons/" + name);
    }

    public static InputStream LoadViewerFileResourceStream(String name) {
        return Play.application().classloader().getResourceAsStream("resources/viewerfiles/" + name);
    }

    public static String LoadViewerFileResource(String templateName) throws IOException {
        InputStream assetStream = LoadViewerFileResourceStream(templateName);
        BufferedReader br = new BufferedReader(new InputStreamReader(assetStream));StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        return sb.toString();
    }

    public static String LoadTemplate(String filename) throws FileNotFoundException, IOException
    {
        File theFile = new File(filename);
        return LoadTemplate(theFile);
    }

    public static String LoadTemplate(File file) throws FileNotFoundException, IOException
    {
        Logger.debug("Loading template: " + file.getPath());
        int size = (int)file.length();
        FileInputStream is = new FileInputStream(file);
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, "UTF-8");
    }

    public static String Substitute(String templ, String[] vals)
    {
        StringBuffer res = new StringBuffer();
        int index = 0, val = 0;
        boolean found;
        do
        {
            found = false;
            int i = templ.indexOf('%', index);
            if(i != -1)
            {
                found = true;
                res.append(templ.substring(index, i));
                if(i < templ.length() - 1)
                {
                    if(templ.charAt(i+1) == '%')
                        res.append('%');
                    else if(templ.charAt(i+1) == 's')
                    {
                        res.append(vals[val ++]);
                    }
                    else
                        res.append('@');    //add a character illegal in jscript so we know the template was incorrect
                    index = i + 2;
                }
            }
        } while(found);
        res.append(templ.substring(index));
        return res.toString();
    }

    public static String EscapeForHtml(String str)
    {
        str = str.replaceAll("'", "&#39;");
        str = str.replaceAll("\"", "&quot;");
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll(">", "&gt;");
        str = str.replaceAll("\\\\n", "<br>");
        return str;
    }

    public static String GetDefaultLocale()
    {
        return "en";
    }

    public static String GetClientAgent()
    {
        return "Ajax Viewer";
    }

    public static String ValidateSessionId(String proposedSessionId)
    {
        // 00000000-0000-0000-0000-000000000000_aa_[aaaaaaaaaaaaa]000000000000
        // the [aaaaaaaaaaaaa] is a based64 string and in variant length
        String validSessionId = "";
        if(proposedSessionId != null &&
            Pattern.matches("^[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}_[A-Za-z]{2}_\\w+[A-Fa-f0-9]{12}$", proposedSessionId))
        {
            validSessionId = proposedSessionId;
        }
        return validSessionId;
    }

    public static String ValidateLocaleString(String proposedLocaleString)
    {
        // aa or aa-aa
        String validLocaleString = GetDefaultLocale(); // Default
        if(proposedLocaleString != null &&
            (Pattern.matches("^[A-Za-z]{2}$", proposedLocaleString) || Pattern.matches("^[A-Za-z]{2}-[A-Za-z]{2}$", proposedLocaleString)))
        {
            validLocaleString = proposedLocaleString;
        }
        return validLocaleString;
    }

    public static String ValidateHyperlinkTargetValue(String proposedHyperlinkTarget)
    {
        // 1, 2 or 3
        String validHyperlinkTarget = "1"; // Default
        if(proposedHyperlinkTarget != null && Pattern.matches("^[1-3]$", proposedHyperlinkTarget))
        {
            validHyperlinkTarget = proposedHyperlinkTarget;
        }
        return validHyperlinkTarget;
    }

    public static String ValidateFrameName(String proposedFrameName)
    {
        // Allowing alphanumeric characters and underscores in the frame name
        String validFrameName = "";
        if(proposedFrameName != null && Pattern.matches("^[a-zA-Z0-9_]*$", proposedFrameName))
        {
            validFrameName = proposedFrameName;
        }
        return validFrameName;
    }

    public static String ValidateIntegerString(String proposedNumberString)
    {
        // Allow numeric characters only
        String validNumberString = "";
        if(proposedNumberString != null && Pattern.matches("^[0-9]*$", proposedNumberString))
        {
            validNumberString = proposedNumberString;
        }
        return validNumberString;
    }

    public static String ValidateResourceId(String proposedResourceId)
    {
        String validResourceId = "";
        try
        {
            MgResourceIdentifier resId = new MgResourceIdentifier(proposedResourceId);
            validResourceId = resId.ToString();
        }
        catch(MgException e)
        {
            validResourceId = "";
        }
        return validResourceId;
    }

    public static String ValidateMapName(String proposedMapName)
    {
        String validMapName = "";
        if (proposedMapName != null && Pattern.matches("^[^\\*:|\\?<'&\">=]*$", proposedMapName))
        {
            validMapName = proposedMapName;
        }
        return validMapName;
    }

    public static String ValidateColorString(String proposedColorString)
    {
        String validColorString = "000000";
        if (proposedColorString != null &&
            Pattern.matches("^[A-Fa-f0-9]{6}$", proposedColorString))
        {
            validColorString = proposedColorString;
        }
        return validColorString;
    }

    public static int GetIntParameter(String strval)
    {
        if(strval.equals(""))
            return 0;
        if(Pattern.matches("^-{0,1}\\d+$", strval))
            return Integer.parseInt(strval);
        else
            return 0;
    }

    public static double GetDoubleParameter(String strval)
    {
        if(strval.equals(""))
            return 0;
        if(Pattern.matches("^([-]{0,1})(\\d+)([.]{0,1})(\\d*)$", strval))
            return Double.parseDouble(strval);
        else
            return 0;
    }

    public static double GetLocalizedDoubleParameter(String strval, String locale)
    {
        if(strval.equals(""))
            return 0;

        if(locale != null && locale.length() > 0)
        {
            //Remove thousand separators
            String thousandSeparator = MgLocalizationUtil.GetString("THOUSANDSEPARATOR", locale);
            if(thousandSeparator != null && thousandSeparator.length() > 0)
            {
                strval = strval.replace(thousandSeparator, "");
            }

            //Replace localized decimal separator with "."
            String decimalSeparator = MgLocalizationUtil.GetString("DECIMALSEPARATOR", locale);
            if(decimalSeparator != null && decimalSeparator.length() > 0 && !decimalSeparator.equals("."))
            {
                strval = strval.replace(decimalSeparator, ".");
            }
        }

        return Double.parseDouble(strval);
    }

    public static String StrEscape(String str)
    {
        return StrEscape(str, false);
    }

    public static String StrEscape(String str, boolean single)
    {
        String org = single ? "'" : "\"";
        String rep = single ? "\\\\'": "\\\\\"";

        return str.replaceAll(org, rep);
    }

    public static String str_replace(String[] args, Object[] vals, String format)
    {
        String formattedString = new String(format);

        int numReplacements = args.length;
        for (int i = 0; i < numReplacements; i++)
            formattedString = formattedString.replaceFirst(args[i], vals[i].toString());

        return formattedString;
    }

    public static String DeclareUiItems(Hashtable cmds, BoxedInteger curFlyout, MgWebWidgetCollection coll, String varname) throws MgException
    {
        String def = "";

        if(coll != null)
        {
            for(int i = 0, j = 0; i < coll.GetCount(); i++)
            {
                MgWebWidget item = coll.GetWidget(i);
                int it = item.GetType();
                if (it == MgWebWidgetType.Separator)
                {
                    Object[] formatArgs = { varname, new Integer(j++) };
                    def = def + MessageFormat.format("{0}[{1,number,integer}] = new UiItem(\"\");\n", formatArgs );
                }
                else if ( it == MgWebWidgetType.Command && item instanceof MgWebCommandWidget )
                {
                    MgWebCommand cmd = ((MgWebCommandWidget)item).GetCommand();
                    Integer cmdIndex = (Integer)cmds.get(cmd.GetName());
                    if(cmdIndex == null)
                        continue;
                    Object[] formatArgs = { varname, new Integer(j++), StrEscape(cmd.GetLabel()), cmdIndex };
                    def = def + MessageFormat.format("{0}[{1,number,integer}] = new CommandItem(\"{2}\", {3,number,integer});\n", formatArgs);
                }
                else
                {
                    curFlyout.increment();
                    String subVarname = "flyoutDef" + curFlyout.value();
                    String htmlName = "FlyoutDiv" + curFlyout.value();
                    Object[] formatArgs1 = { subVarname };
                    def = def + MessageFormat.format("var {0} = new Array()\n", formatArgs1);
                    def = def + DeclareUiItems(cmds, curFlyout, ((MgWebFlyoutWidget) item).GetSubItems(), subVarname);
                    Object[] formatArgs2 = { varname, new Integer(j++), StrEscape( ((MgWebFlyoutWidget) item).GetLabel() ), subVarname, StrEscape( htmlName ), ((MgWebFlyoutWidget) item).GetIconUrl() };
                    def = def + MessageFormat.format("{0}[{1,number,integer}] = new FlyoutItem(\"{2}\", {3}, \"{4}\", \"{5}\");\n", formatArgs2);
                }
            }
        }
        return def;
    }

    public static ArrayList BuildLayerTree(MgMap map, MgResourceService resSrvc) throws MgException
    {
        ArrayList tree = new ArrayList();
        HashMap knownGroups = new HashMap();
        ArrayList unresolved = new ArrayList();
        MgLayerGroupCollection groups = map.GetLayerGroups();

        for(int i = 0; i < groups.GetCount(); i++)
        {
            MgLayerGroup rtGroup = (MgLayerGroup)groups.GetItem(i);
            TreeItem node = new TreeItem(rtGroup.GetName(), true, rtGroup, "null");
            knownGroups.put(node.name, node);
            MgLayerGroup parentGroup = rtGroup.GetGroup();
            if(parentGroup == null)
            {
                tree.add(node);
            }
            else
            {
                String parentName = parentGroup.GetName();
                TreeItem parentNode = (TreeItem)knownGroups.get(parentName);
                if(parentNode != null)
                    parentNode.Attach(node);
                else
                {
                    node.parentName = parentName;
                    unresolved.add(node);
                }
            }
        }
        if(unresolved.size() > 0)
        {
            for(int i = 0; i < unresolved.size(); i++)
            {
                TreeItem node = (TreeItem)unresolved.get(i);
                TreeItem parentNode = (TreeItem)knownGroups.get(node.parentName);
                if(parentNode != null)
                    parentNode.Attach(node);
                else
                    tree.add(node); //should not happen. place group in the root if parent is not known
            }
        }
        // Get the layers
        MgLayerCollection layers = map.GetLayers();

        // Get the resource Ids of the layers
        MgStringCollection resIds = new MgStringCollection();
        for(int i = 0; i < layers.GetCount(); i++)
        {
            MgLayer rtLayer = (MgLayer) layers.GetItem(i);
            MgResourceIdentifier resId = rtLayer.GetLayerDefinition();
            resIds.Add(resId.ToString());
        }
        MgStringCollection layersData = resSrvc.GetResourceContents(resIds, null);

        for(int i = 0; i < layers.GetCount(); i++)
        {
            MgLayer rtLayer = (MgLayer) layers.GetItem(i);
            TreeItem node = new TreeItem(rtLayer.GetName(), false, rtLayer, (String)layersData.GetItem(i));
            MgLayerGroup parentGroup = rtLayer.GetGroup();
            if(parentGroup == null)
                tree.add(node);
            else
            {
                TreeItem parentNode = (TreeItem)knownGroups.get(parentGroup.GetName());
                if(parentNode != null)
                    parentNode.Attach(node);
                else
                    tree.add(node); //should not happen. place layer in the root if parent is not known
            }
        }

        return tree;
    }

    public static HashMap BuildLayerMap(MgMap map) throws MgException
    {
        HashMap layerMap = new HashMap();
        MgLayerCollection layers = map.GetLayers();
        for(int i = 0; i < layers.GetCount(); i++)
        {
            MgLayer rtLayer = (MgLayer) layers.GetItem(i);
            layerMap.put(rtLayer.GetObjectId(), rtLayer);
        }
        return layerMap;
    }

    public static void BuildClientSideTree(ArrayList tree, TreeItem parent, String parentName, boolean fulldata, String container, MgResourceService resSrvc, HashMap layerMap, BoxedInteger intermediateVar, StringBuilder output) throws MgException
    {
        // 2 passes: pass 1 adds layers to the tree, pass 2 adds groups
        //
        int treeIndex = 0;
        for(int pass = 0; pass < 2; pass++)
        {
            for(int i = 0; i < tree.size(); i++)
            {
                TreeItem node = (TreeItem)tree.get(i);
                if(node.isGroup)
                {
                    if(pass == 1)
                    {
                        String groupName = "grp" + (intermediateVar.increment());
                        String arrChildName;
                        if(node.children != null)
                        {
                            arrChildName = "c" + (intermediateVar.increment());
                            output.append("var " + arrChildName + " = new Array();\n");
                        }
                        else
                            arrChildName = "null";

                        MgLayerGroup rtLayerGroup = (MgLayerGroup)node.rtObject;
                        if(fulldata)
                        {
                            output.append(String.format("var %s = new GroupItem(\"%s\", %s, %s, %s, %s, \"%s\", \"%s\", %s);\n",
                                                               new Object[] {groupName,
                                                               StrEscape(rtLayerGroup.GetLegendLabel()),
                                                               rtLayerGroup.GetExpandInLegend()? "true": "false",
                                                               parentName,
                                                               rtLayerGroup.GetVisible()? "true": "false",
                                                               rtLayerGroup.GetDisplayInLegend()? "true": "false",
                                                               rtLayerGroup.GetObjectId(),
                                                               StrEscape(rtLayerGroup.GetName()),
                                                               rtLayerGroup.GetLayerGroupType() == MgLayerGroupType.BaseMap? "true": "false"}));
                        }
                        else
                        {
                            output.append(String.format("var %s = new GroupSummary(\"%s\", \"%s\", %s, %s);\n",
                                                               new Object[] {groupName,
                                                               StrEscape(rtLayerGroup.GetName()),
                                                               rtLayerGroup.GetObjectId(),
                                                               arrChildName,
                                                               parentName}));
                        }
                        output.append(String.format("%s[%d] = %s;\n", new Object[] {container, Integer.valueOf(treeIndex), groupName}));
                        ++treeIndex;

                        if(node.children != null)
                        {
                            BuildClientSideTree(node.children, node, groupName, fulldata, arrChildName, resSrvc, null, intermediateVar, output);
                            output.append(String.format("%s.children = %s;\n", new Object[] {groupName, arrChildName}));
                        }
                    }
                }
                else
                {
                    if(pass == 0)
                    {
                        MgLayer rtLayer = (MgLayer)node.rtObject;
                        if(fulldata)
                        {
                            MgResourceIdentifier resId = rtLayer.GetLayerDefinition();
                            String layerData = node.layerData;
                            String layerName = "lyr" + (intermediateVar.increment());
                            String objectId = rtLayer.GetObjectId();
                            output.append(String.format("var %s = new LayerItem(\"%s\", \"%s\", %s, %s, %s, %s, %s, \"%s\", \"%s\", %s);\n",
                                                               new Object[] {layerName,
                                                               StrEscape(rtLayer.GetLegendLabel()),
                                                               rtLayer.GetName(),
                                                               rtLayer.GetExpandInLegend()? "true": "false",
                                                               parentName,
                                                               rtLayer.GetVisible()? "true": "false",
                                                               rtLayer.GetDisplayInLegend()? "true": "false",
                                                               rtLayer.GetSelectable()? "true": "false",
                                                               resId.ToString(),
                                                               objectId,
                                                               rtLayer.GetLayerType() == MgLayerType.BaseMap? "true": "false"}));

                            output.append(String.format("%s[%d] = %s;\n",
                                                               new Object[] {container,
                                                               Integer.valueOf(treeIndex),
                                                               layerName}));
                            ++treeIndex;

                            if(layerMap == null || !layerMap.containsKey(objectId))
                            {
                                BuildLayerDefinitionData(layerData, layerName, intermediateVar, output);
                            }
                        }
                        else
                        {
                            output.append(String.format("%s[%d] = new LayerSummary(\"%s\", \"%s\", \"%s\");\n",
                                                                new Object[] {container,
                                                                Integer.valueOf(i),
                                                                StrEscape(rtLayer.GetName()),
                                                                rtLayer.GetObjectId(),
                                                                rtLayer.GetLayerDefinition().ToString()}));
                        }
                    }
                }
            }
        }
    }

    public static void BuildLayerDefinitionData(String layerData, String layerVarName, BoxedInteger intermediateVar, StringBuilder output)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(layerData.getBytes("UTF-8"));
            Document doc = builder.parse(is);
            int type = 0;
            NodeList scaleRanges = doc.getElementsByTagName("VectorScaleRange");
            if(scaleRanges.getLength() == 0) {
                scaleRanges = doc.getElementsByTagName("GridScaleRange");
                if(scaleRanges.getLength() == 0) {
                    scaleRanges = doc.getElementsByTagName("DrawingLayerDefinition");
                    if(scaleRanges.getLength() == 0)
                        return;
                    type = 2;
                }
                else
                    type = 1;
            }

            String typeStyles[] = new String[]{"PointTypeStyle", "LineTypeStyle", "AreaTypeStyle", "CompositeTypeStyle"};
            String ruleNames[] = new String[]{"PointRule", "LineRule", "AreaRule", "CompositeRule"};

            for(int sc = 0; sc < scaleRanges.getLength(); sc++)
            {
                Element scaleRange = (Element)scaleRanges.item(sc);
                String scaleRangeVarName = "sc" + (intermediateVar.increment());
                String minScale, maxScale;
                NodeList minElt = scaleRange.getElementsByTagName("MinScale");
                NodeList maxElt = scaleRange.getElementsByTagName("MaxScale");
                minScale = "0";
                maxScale = "1000000000000.0";  // as MDF's VectorScaleRange::MAX_MAP_SCALE
                if(minElt.getLength() > 0)
                    minScale = minElt.item(0).getChildNodes().item(0).getNodeValue().toString();
                if(maxElt.getLength() > 0)
                    maxScale = maxElt.item(0).getChildNodes().item(0).getNodeValue().toString();
                output.append(String.format("var %s = new ScaleRangeItem(%s, %s, %s);\n",
                                            new Object[]{scaleRangeVarName,
                                            minScale,
                                            maxScale,
                                            layerVarName}));
                output.append(String.format("%s.children[%d] = %s;\n", new Object[] {layerVarName, Integer.valueOf(sc), scaleRangeVarName}));

                if(type != 0)
                    break;

                int styleIndex = 0;
                for(int ts=0; ts < typeStyles.length; ts++)
                {
                    NodeList typeStyle = scaleRange.getElementsByTagName(typeStyles[ts]);
                    int catIndex = 0;
                    for(int st = 0; st < typeStyle.getLength(); st++)
                    {
                        // We will check if this typestyle is going to be shown in the legend
                        NodeList showInLegend = ((Element)typeStyle.item(st)).getElementsByTagName("ShowInLegend");
                        if(showInLegend != null && showInLegend.getLength() > 0)
                            if(showInLegend.item(0).getChildNodes().item(0).getNodeValue().equals("false"))
                                continue;   // This typestyle does not need to be shown in the legend

                        NodeList rules = ((Element)typeStyle.item(st)).getElementsByTagName(ruleNames[ts]);
                        for(int r = 0; r < rules.getLength(); r++)
                        {
                            Element rule = (Element)rules.item(r);
                            NodeList label = rule.getElementsByTagName("LegendLabel");
                            NodeList filter = rule.getElementsByTagName("Filter");

                            String labelText = "";
                            if(label != null && label.getLength() > 0)
                            {
                                NodeList subItems = label.item(0).getChildNodes();
                                if(subItems != null && subItems.getLength() > 0)
                                    labelText = subItems.item(0).getNodeValue();
                            }
                            String filterText = "";
                            if(filter != null && filter.getLength() > 0)
                            {
                                NodeList subItems2 = filter.item(0).getChildNodes();
                                if(subItems2 != null && subItems2.getLength() > 0)
                                    filterText = subItems2.item(0).getNodeValue();
                            }

                            output.append(String.format("%s.children[%d] = new StyleItem(\"%s\", \"%s\", %d, %d);\n",
                                                        new Object[]{scaleRangeVarName,
                                                        Integer.valueOf(styleIndex++),
                                                        StrEscape(labelText.trim()),
                                                        StrEscape(filterText.trim()),
                                                        ts+1,
                                                        catIndex++}));
                        }
                    }
                }
            }
            output.append(String.format("%s.lyrtype = %d;\n", new Object[]{layerVarName, new Integer(type) }));
        }
        catch(Exception e)
        {
            //broken layer definition. just don't create any info for that layer
            return;
        }
    }

    public static InputStream ByteReaderToStream(MgByteReader byteReader) throws MgException
    {
        InputStream stream = null;
        if(byteReader != null)
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] byteBuffer = new byte[1024];
            int numBytes = byteReader.Read(byteBuffer, 1024);
            while(numBytes > 0)
            {
                bos.write(byteBuffer, 0, numBytes);
                numBytes = byteReader.Read(byteBuffer, 1024);
            }
            stream = new ByteArrayInputStream(bos.toByteArray());
        }
        return stream;
    }

    public static String getTextValue(Element el, String tagName)
    {
        String textVal = null;
        NodeList nl = el.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0)
        {
            Element e = (Element)nl.item(0);
            textVal = e.getFirstChild().getNodeValue();
        }
        return textVal;
    }

    public static HashMap<String, String> GetLayerPropertyMappings(MgResourceService resSvc, MgLayerBase layer) throws Exception
    {
        HashMap<String, String> mappings = new HashMap<String, String>();

        MgByteReader content = resSvc.GetResourceContent(layer.GetLayerDefinition());
        ByteArrayInputStream contentReader = new ByteArrayInputStream(content.ToString().getBytes("UTF-8"));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(contentReader);

        doc.getDocumentElement().normalize();
        NodeList propNodes = doc.getElementsByTagName("PropertyMapping");

        for (int i = 0; i < propNodes.getLength(); i++)
        {
            Element propEl = (Element)propNodes.item(i);
            String name = getTextValue(propEl, "Name");
            String value = getTextValue(propEl, "Value");

            if (name != null && value != null)
                mappings.put(name, value);
        }

        return mappings;
    }

    public static String GetPropertyValueFromFeatureReader(MgFeatureReader reader, MgAgfReaderWriter agfRw, int propType, String propName, String locale) throws Exception
    {
        String value = "";
        switch(propType)
        {
            case MgPropertyType.Boolean:
                value = String.format(locale, "%s", reader.GetBoolean(propName));
                break;
            case MgPropertyType.Byte:
                value = String.format(locale, "%d", reader.GetByte(propName));
                break;
            case MgPropertyType.DateTime:
                value = GetDateTimeString(reader.GetDateTime(propName)); // yyyy-mm-dd is enforced regardless of locale
                break;
            case MgPropertyType.Single:
                value = String.format(locale, "%f", reader.GetSingle(propName));
                break;
            case MgPropertyType.Double:
                value = String.format(locale, "%f", reader.GetDouble(propName));
                break;
            case MgPropertyType.Int16:
                value = String.format(locale, "%d", reader.GetInt16(propName));
                break;
            case MgPropertyType.Int32:
                value = String.format(locale, "%d", reader.GetInt32(propName));
                break;
            case MgPropertyType.Int64:
                value = String.format(locale, "%d", reader.GetInt64(propName));
                break;
            case MgPropertyType.String:
                value = JsonEscape(reader.GetString(propName)); // string content is arbitrary
                value = value.replaceAll("\\s+", " ").trim();
                break;
            default: //NOT PRESENTABLE IN PROPERTY GRID
                value = "";
                break;
        }
        return value;
    }

    public static String GetDateTimeString(MgDateTime value) throws MgException
    {
        return value.GetYear() + "-" + value.GetMonth() + "-" + value.GetDay();
    }

    public static String JsonEscape(String str)
    {
        return EscapeForHtml(str).replace("\\", "\\\\");
    }

    public static String JsonifyError(Exception ex, String localeCode)
    {
        if (ex == null)
            return "";
        /*
        {
            "Error" : {
                "Message" : <exception-message>,
                "StackTrace" : <exception-stack-trace>
            }
        }
        */

        StringBuffer sb = new StringBuffer();
        //Use exception message or type name if no message found
        String msg = ex.getMessage();
        if (msg == null || msg.length() == 0)
        {
            msg = MgLocalizer.GetString("SERVERERROR", localeCode);
        }
        //Begin response
        sb.append("{\"Error\":{");
        //Exception message
        sb.append("\"Message\":\"" + JsonEscape(msg) + "\",");
        StringBuffer strace = new StringBuffer();
        StackTraceElement[] st = ex.getStackTrace();
        for (int i = 0; i < st.length; i++)
        {
            strace.append(st[i].getClassName() + "." + st[i].getMethodName() + "(" + st[i].getLineNumber() + ")\\n");
        }
        sb.append("\"StackTrace\":\"" + JsonEscape(strace.toString()) + "\"");
        //End response
        sb.append("}}");
        return sb.toString();
    }

    public static String GetJson(SelectionSet set)
    {
        /*
        A sample of the JSON output this method will produce:


        {
            "Layer1" : [
                {
                    'values' { "name" : "name1" , "value" : "value1" },
                    'zoom' : { x: num1, y: num2 }
                } ,
                ..,
                ..,
                ..,
            ],
            "Layer2" : [
                {
                    'values' { "name" : "name2" , "value" : "value2" },
                    'zoom' : { x: num1, y: num2 }
                } ,
                ..,
                ..,
                ..,
            ]
        }
        */

        if (set == null)
            return "";

        StringBuffer sb = new StringBuffer();
        //Begin selection set
        sb.append("{");
        String[] layers = set.getLayers();
        for (int i = 0; i < layers.length; i++)
        {
            //Begin layer
            sb.append("\"" + layers[i] + "\" : [");
            Feature[] features = set.getFeatures(layers[i]);
            for (int j = 0; j < features.length; j++)
            {
                Feature feat = features[j];
                //begin feature
                //begin feature properties
                sb.append("{\"values\" : [");
                FeatureProperty[] properties = feat.getProperties();
                for(int k = 0; k < properties.length; k++)
                {
                    FeatureProperty fp = properties[k];
                    sb.append("{\"name\" : \"" + fp.Name + "\", \"value\" : \"" + fp.Value + "\" }");
                    if (k != properties.length - 1)
                        sb.append(",");
                }
                //end feature properties
                //begin zoom
                sb.append("], \"zoom\" : ");
                if (feat.Zoom == null)
                    sb.append("null");
                else
                    sb.append(String.format(Locale.ROOT, "{\"minx\" : %f, \"miny\" : %f, \"maxx\" : %f, \"maxy\" : %f }", feat.Zoom.MinX, feat.Zoom.MinY, feat.Zoom.MaxX, feat.Zoom.MaxY));
                //end zoom
                //end feature
                sb.append("}");
                if (j != features.length - 1)
                    sb.append(",");
            }
            //End Layer
            sb.append("]");
            if (i != layers.length - 1)
                sb.append(",");
        }
        //End selection set
        sb.append("}");
        return sb.toString();
    }

    public static String FixupPageReferences(String html, String webLayout, boolean dwf, String vpath, String locale) throws UnsupportedEncodingException {
        String htmlPrefix = new StringBuffer().append("gettingstarted.jsp?WEBLAYOUT=").append(URLEncoder.encode(webLayout, "UTF-8")).append("&DWF=").append(dwf?"1":"0").append("&LOCALE=").append(locale).append("&PAGE=").toString();
        String imgSrcPrefix = new StringBuffer().append(vpath).append("localized/help/").append(locale).append("/").toString();
        StringBuffer res = new StringBuffer();
        int index = 0;
        boolean found;
        do
        {
            found = false;
            int i = html.indexOf("href=\"", index);
            int j = html.indexOf("src=\"", index);
            if(i != -1 || j != -1) {
                found = true;
                boolean htmlRef = false;
                if(i != -1) {
                    if(j != -1) {
                        if(i < j) {
                            htmlRef = html.substring(i - 3, i - 1).equals("<a");
                            i += 6;
                        }
                        else
                            i = j + 5;
                    }
                    else {
                        htmlRef = html.substring(i - 3, i - 1).equals("<a");
                        i += 6;
                    }
                }
                else
                    i = j + 5;
                res.append(html.substring(index, i));
                if(htmlRef) {
                    if(FixupRequired(html, i))
                        res.append(htmlPrefix);
                }
                else {
                    if(FixupRequired(html, i))
                        res.append(imgSrcPrefix);
                }
                index = i;
            }
        } while(found);
        res.append(html.substring(index));
        return res.toString();
    }

    public static boolean FixupRequired(String html, int refIndex) {
        return !html.substring(refIndex, refIndex + 7).equals("http://") &&
               !html.substring(refIndex, refIndex + 11).equals("javascript:");
    }

    public static String GetMapSrs(MgMap map) throws MgException
    {
        String srs = map.GetMapSRS();
        if(!srs.equals(""))
            return srs;

        //SRS is currently optional. Waiting for this to change, set the default SRS to ArbitrayXY meters
        //
        return "LOCALCS[\"Non-Earth (Meter)\",LOCAL_DATUM[\"Local Datum\",0],UNIT[\"Meter\", 1],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH]]";
    }

    public static MgLayer FindLayer(MgLayerCollection layers, String layerDef) throws MgException
    {
        MgLayer layer = null;
        int i;
        for(i = 0; i < layers.GetCount(); i++)
        {
            MgLayer layer1 = (MgLayer) layers.GetItem(i);
            if(layer1.GetLayerDefinition().ToString().equals(layerDef))
            {
                layer = layer1;
                break;
            }
        }
        return layer;
    }

    public static boolean DataSourceExists(MgResourceService resourceSrvc, MgResourceIdentifier resId) throws MgException
    {
        return resourceSrvc.ResourceExists(resId);
    }

    public static MgByteReader BuildLayerDefinitionContent(String layerTempl, String dataSource, String featureName, String tip) throws MgException, Exception
    {
        String[] vals = {
                    dataSource,
                    featureName,
                    "GEOM",
                    tip,
                    "1",
                    "ff0000" };
        layerTempl = Substitute(layerTempl, vals);
        byte[] bytes = layerTempl.getBytes("UTF-8");

        MgByteSource src = new MgByteSource(bytes, bytes.length);
        return src.GetReader();
    }

    public static MgByteReader BuildAreaLayerDefinitionContent(
        String layerTempl, String dataSource, String featureName, String fillstyle, String ffcolor,
        int transparent, String fbcolor, String linestyle, double thickness, double foretrans, String lcolor) throws MgException, Exception
    {
        String xtrans = String.format("%02x", new Object[]{new Integer((int)(255 * foretrans / 100))});
        String[] vals = {
                    dataSource,
                    featureName,
                    "GEOM",
                    fillstyle,
                    xtrans + ffcolor,
                    (0 != transparent? ("ff" + fbcolor): ("00" + fbcolor)),
                    linestyle,
                    String.valueOf(thickness),
                    lcolor };
        layerTempl = Substitute(layerTempl, vals);
        byte[] bytes = layerTempl.getBytes("UTF-8");

        MgByteSource src = new MgByteSource(bytes, bytes.length);
        return src.GetReader();
    }

    public static void ClearDataSource(MgFeatureService featureSrvc, MgResourceIdentifier dataSourceId, String featureName)  throws MgException
    {
        MgDeleteFeatures deleteCmd = new MgDeleteFeatures(featureName, "KEY >= 0");
        MgFeatureCommandCollection commands = new MgFeatureCommandCollection();
        commands.Add(deleteCmd);
        featureSrvc.UpdateFeatures(dataSourceId, commands, false);
    }

    public static void ReleaseReader(MgPropertyCollection res) throws MgException
    {
        if(res == null)
            return;
        MgProperty prop = res.GetItem(0);
        if(prop == null)
            return;
        if (prop instanceof MgStringProperty)
            throw new RuntimeException(((MgStringProperty)prop).GetValue());
        MgFeatureProperty resProp = (MgFeatureProperty)prop;
        MgFeatureReader reader = (MgFeatureReader)resProp.GetValue();
        if(reader == null)
            return;
        reader.Close();
    }

    public static void ReleaseReader(MgPropertyCollection res, MgFeatureCommandCollection commands) throws MgException
    {
        if(res == null)
            return;

        for(int i = 0; i < res.GetCount(); i++)
        {
            MgFeatureCommand cmd = commands.GetItem(i);
            if(cmd instanceof MgInsertFeatures)
            {
                MgFeatureProperty resProp = (MgFeatureProperty)res.GetItem(i);
                if(resProp != null)
                {
                    MgFeatureReader reader = (MgFeatureReader)resProp.GetValue();
                    if(reader == null)
                        return;
                    reader.Close();
                }
            }
        }
    }


    public static void AddFeatureToCollection(MgBatchPropertyCollection propCollection, MgAgfReaderWriter agfRW, int featureId, MgGeometry featureGeom) throws MgException
    {
        MgPropertyCollection bufferProps = new MgPropertyCollection();
        MgInt32Property idProp = new MgInt32Property("ID", featureId);
        bufferProps.Add(idProp);
        MgByteReader geomReader = agfRW.Write(featureGeom);
        MgGeometryProperty geomProp = new MgGeometryProperty("GEOM", geomReader);
        bufferProps.Add(geomProp);
        propCollection.Add(bufferProps);
    }
}