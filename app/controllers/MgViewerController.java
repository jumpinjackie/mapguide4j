package controllers;

import util.*;

import play.*;
import play.mvc.*;

import java.util.Map;

import org.osgeo.mapguide.*;

public class MgViewerController extends MgAbstractAuthenticatedController {
    public static Result getDynamicMapOverlayImage(String sessionId, String mapName) {
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            //NOTE: Not a published API
            MgHtmlController controller = new MgHtmlController(siteConn);
            MgPropertyCollection mapViewCmds = new MgPropertyCollection();

            int behavior = 0;
            String selectionColor = getRequestParameter("SELECTIONCOLOR", "");
            String format = getRequestParameter("FORMAT", "");
            if (hasRequestParameter("BEHAVIOR")) {
                behavior = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("BEHAVIOR", "0"));
            }
            if (hasRequestParameter("SETDISPLAYDPI")) {
                MgStringProperty cmd = new MgStringProperty("SETDISPLAYDPI", getRequestParameter("SETDISPLAYDPI", ""));
                mapViewCmds.Add(cmd);
            }
            if (hasRequestParameter("SETDISPLAYWIDTH")) {
                MgStringProperty cmd = new MgStringProperty("SETDISPLAYWIDTH", getRequestParameter("SETDISPLAYWIDTH", ""));
                mapViewCmds.Add(cmd);
            }
            if (hasRequestParameter("SETDISPLAYHEIGHT")) {
                MgStringProperty cmd = new MgStringProperty("SETDISPLAYHEIGHT", getRequestParameter("SETDISPLAYHEIGHT", ""));
                mapViewCmds.Add(cmd);
            }
            if (hasRequestParameter("SETVIEWSCALE")) {
                MgStringProperty cmd = new MgStringProperty("SETVIEWSCALE", getRequestParameter("SETVIEWSCALE", ""));
                mapViewCmds.Add(cmd);
            }
            if (hasRequestParameter("SETVIEWCENTERX")) {
                MgStringProperty cmd = new MgStringProperty("SETVIEWCENTERX", getRequestParameter("SETVIEWCENTERX", ""));
                mapViewCmds.Add(cmd);
            }
            if (hasRequestParameter("SETVIEWCENTERY")) {
                MgStringProperty cmd = new MgStringProperty("SETVIEWCENTERY", getRequestParameter("SETVIEWCENTERY", ""));
                mapViewCmds.Add(cmd);
            }

            MgColor selColor = null;
            if (!selectionColor.equals("")) {
                selColor = new MgColor(selectionColor);
            }
            MgRenderingOptions renderOpts = new MgRenderingOptions(format, behavior, selColor);
            MgByteReader image = controller.GetDynamicMapOverlayImage(mapName, renderOpts, mapViewCmds);

            response().setContentType(image.GetMimeType());
            return ok(MgAjaxViewerUtil.ByteReaderToStream(image));
        } catch (MgException ex) { //TODO: Rasterize the error message as the standard response won't be visible most of the time
            return mgServerError(ex);
        }
    }

    public static Result queryMapFeatures(String sessionId, String mapName) {
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            
            MgStringCollection layerNames = null;
            MgGeometry selectionGeometry = null;
            int selectionVariant = 0;
            MgWktReaderWriter wktRw = new MgWktReaderWriter();
            boolean persist = (getRequestParameter("PERSIST", "0").equals("1"));
            int maxFeatures = -1;
            String featureFilter = getRequestParameter("FEATUREFILTER", "");
            // 1=Visible
            // 2=Selectable
            // 4=HasTooltips
            int layerAttributeFilter = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("LAYERATTRIBUTEFILTER", "3")); //Visible and selectable

            if (hasRequestParameter("SELECTIONVARIANT")) {
                String variant = getRequestParameter("SELECTIONVARIANT", "");
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

            if (!hasRequestParameter("GEOMETRY"))
                return badRequest("Missing required parameter: GEOMETRY");
            if (hasRequestParameter("LAYERNAMES")) {
                String[] names = getRequestParameter("LAYERNAMES", "").split(",");
                if (names.length > 0) {
                    layerNames = new MgStringCollection();
                    for (String s : names) {
                        layerNames.Add(s);
                    }
                }
            }

            selectionGeometry = wktRw.Read(getRequestParameter("GEOMETRY", ""));

            //NOTE: Not a published API
            MgHtmlController controller = new MgHtmlController(siteConn);
            MgByteReader description = controller.QueryMapFeatures(mapName, layerNames, selectionGeometry, selectionVariant, featureFilter, maxFeatures, persist, layerAttributeFilter);
            response().setContentType(description.GetMimeType());
            return ok(MgAjaxViewerUtil.ByteReaderToStream(description));
        } catch (MgException ex) { //TODO: Rasterize the error message as the standard response won't be visible most of the time
            return mgServerError(ex);
        }
    }

    public static Result getMapLayers(String sessionId, String mapName) {
        return TODO;
    }

    public static Result getMapLayerGroups(String sessionId, String mapName) {
        return TODO;
    }

    public static Result getLegendImage(String resourcePath, String scale, Long geomType, Long themeCat) {
        return TODO;
    }
}