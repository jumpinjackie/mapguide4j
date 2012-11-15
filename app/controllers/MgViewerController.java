package controllers;

import util.*;

import play.*;
import play.mvc.*;

import java.util.Map;

import org.osgeo.mapguide.*;

public class MgViewerController extends MgAbstractAuthenticatedController {
    public static Result getDynamicMapOverlayImage(String sessionId, String mapName) {
        try {
            Map<String, String[]> queryParams = requestParameters();
            if (queryParams == null)
                return badRequest("Unknown or unsupported HTTP method");

            MgSiteConnection siteConn = createMapGuideConnection();
            //NOTE: Not a published API
            MgHtmlController controller = new MgHtmlController(siteConn);
            MgPropertyCollection mapViewCmds = new MgPropertyCollection();

            int behavior = 0;
            String selectionColor = "";
            String format = "";
            if (queryParams.get("BEHAVIOR") != null) {
                behavior = MgClassicAjaxViewerUtil.GetIntParameter(queryParams.get("BEHAVIOR")[0]);
            }
            if (queryParams.get("SETDISPLAYDPI") != null) {
                MgStringProperty cmd = new MgStringProperty("SETDISPLAYDPI", queryParams.get("SETDISPLAYDPI")[0]);
                mapViewCmds.Add(cmd);
            }
            if (queryParams.get("SETDISPLAYWIDTH") != null) {
                MgStringProperty cmd = new MgStringProperty("SETDISPLAYWIDTH", queryParams.get("SETDISPLAYWIDTH")[0]);
                mapViewCmds.Add(cmd);
            }
            if (queryParams.get("SETDISPLAYHEIGHT") != null) {
                MgStringProperty cmd = new MgStringProperty("SETDISPLAYHEIGHT", queryParams.get("SETDISPLAYHEIGHT")[0]);
                mapViewCmds.Add(cmd);
            }
            if (queryParams.get("SETVIEWSCALE") != null) {
                MgStringProperty cmd = new MgStringProperty("SETVIEWSCALE", queryParams.get("SETVIEWSCALE")[0]);
                mapViewCmds.Add(cmd);
            }
            if (queryParams.get("SETVIEWCENTERX") != null) {
                MgStringProperty cmd = new MgStringProperty("SETVIEWCENTERX", queryParams.get("SETVIEWCENTERX")[0]);
                mapViewCmds.Add(cmd);
            }
            if (queryParams.get("SETVIEWCENTERY") != null) {
                MgStringProperty cmd = new MgStringProperty("SETVIEWCENTERY", queryParams.get("SETVIEWCENTERY")[0]);
                mapViewCmds.Add(cmd);
            }
            if (queryParams.get("FORMAT") != null) {
                format = queryParams.get("FORMAT")[0];
            }

            MgColor selColor = null;
            if (!selectionColor.equals("")) {
                selColor = new MgColor(selectionColor);
            }
            MgRenderingOptions renderOpts = new MgRenderingOptions(format, behavior, selColor);
            MgByteReader image = controller.GetDynamicMapOverlayImage(mapName, renderOpts, mapViewCmds);

            response().setContentType(image.GetMimeType());
            return ok(MgClassicAjaxViewerUtil.ByteReaderToStream(image));
        } catch (MgException ex) { //TODO: Rasterize the error message as the standard response won't be visible most of the time
            return mgServerError(ex);
        }
    }

    public static Result queryMapFeatures(String sessionId, String mapName) {
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            
            Map<String, String[]> queryParams = requestParameters();
            if (queryParams == null)
                return badRequest("Unknown or unsupported HTTP method");
            
            MgStringCollection layerNames = null;
            MgGeometry selectionGeometry = null;
            int selectionVariant = 0;
            MgWktReaderWriter wktRw = new MgWktReaderWriter();
            boolean persist = false;
            int maxFeatures = -1;
            String featureFilter = "";
            // 1=Visible
            // 2=Selectable
            // 4=HasTooltips
            int layerAttributeFilter = 3; //Visible and selectable

            if (queryParams.get("FEATUREFILTER") != null) {
                featureFilter = queryParams.get("FEATUREFILTER")[0];
            }

            if (queryParams.get("SELECTIONVARIANT") != null) {
                String variant = queryParams.get("SELECTIONVARIANT")[0];
                if (variant.equals("TOUCHES")) {
                    selectionVariant = MgFeatureSpatialOperations.Touches;
                } else if (variant.equals("INTERSECTS")) {
                    selectionVariant = MgFeatureSpatialOperations.Intersects;
                } else if (variant.equals("WITHIN")) {
                    selectionVariant = MgFeatureSpatialOperations.Within;
                } else if (variant.equals("ENVELOPEINTERSECTS")) {
                    selectionVariant = MgFeatureSpatialOperations.EnvelopeIntersects;
                } else {
                    return badRequest("Invalid parameter: SELECTIONVARIANT");
                }
            }

            if (queryParams.get("GEOMETRY") == null)
                return badRequest("Missing required parameter: GEOMETRY");
            if (queryParams.get("LAYERNAMES") != null) {
                String[] names = queryParams.get("LAYERNAMES")[0].split(",");
                if (names.length > 0) {
                    layerNames = new MgStringCollection();
                    for (String s : names) {
                        layerNames.Add(s);
                    }
                }
            }

            selectionGeometry = wktRw.Read(queryParams.get("GEOMETRY")[0]);
            if (queryParams.get("PERSIST") != null) {
                persist = (queryParams.get("PERSIST")[0].equals("1"));
            }
            if (queryParams.get("LAYERATTRIBUTEFILTER") != null) {
                layerAttributeFilter = MgClassicAjaxViewerUtil.GetIntParameter(queryParams.get("LAYERATTRIBUTEFILTER")[0]);
            }

            //NOTE: Not a published API
            MgHtmlController controller = new MgHtmlController(siteConn);
            MgByteReader description = controller.QueryMapFeatures(mapName, layerNames, selectionGeometry, selectionVariant, featureFilter, maxFeatures, persist, layerAttributeFilter);
            response().setContentType(description.GetMimeType());
            return ok(MgClassicAjaxViewerUtil.ByteReaderToStream(description));
        } catch (MgException ex) { //TODO: Rasterize the error message as the standard response won't be visible most of the time
            return mgServerError(ex);
        }
    }
}