package controllers;

import util.*;

import play.*;
import play.mvc.*;

import java.util.Map;

import org.osgeo.mapguide.*;

public class MgViewerController extends MgAbstractAuthenticatedController {
    public static Result getDynamicMapOverlayImage(String sessionId, String mapName) {
        try {
            //We don't need MgHtmlController as previously thought. Just set up the required
            //parameter dictionary for MgHttpRequest
            String uri =  controllers.routes.MgViewerController.getDynamicMapOverlayImage(sessionId, mapName).absoluteURL(request());
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "GETDYNAMICMAPOVERLAYIMAGE");
            param.AddParameter("VERSION", "2.1.0");
            param.AddParameter("SESSION", sessionId);
            param.AddParameter("MAPNAME", mapName);
            param.AddParameter("FORMAT", getRequestParameter("FORMAT", ""));

            if (hasRequestParameter("SELECTIONCOLOR")) {
                param.AddParameter("SELECTIONCOLOR", getRequestParameter("SELECTIONCOLOR", ""));
            }
            if (hasRequestParameter("BEHAVIOR")) {
                param.AddParameter("BEHAVIOR", getRequestParameter("BEHAVIOR", "0"));
            }
            if (hasRequestParameter("SETDISPLAYDPI")) {
                param.AddParameter("SETDISPLAYDPI", getRequestParameter("SETDISPLAYDPI", ""));
            }
            if (hasRequestParameter("SETDISPLAYWIDTH")) {
                param.AddParameter("SETDISPLAYWIDTH", getRequestParameter("SETDISPLAYWIDTH", ""));
            }
            if (hasRequestParameter("SETDISPLAYHEIGHT")) {
                param.AddParameter("SETDISPLAYHEIGHT", getRequestParameter("SETDISPLAYHEIGHT", ""));
            }
            if (hasRequestParameter("SETVIEWSCALE")) {
                param.AddParameter("SETVIEWSCALE", getRequestParameter("SETVIEWSCALE", ""));
            }
            if (hasRequestParameter("SETVIEWCENTERX")) {
                param.AddParameter("SETVIEWCENTERX", getRequestParameter("SETVIEWCENTERX", ""));
            }
            if (hasRequestParameter("SETVIEWCENTERY")) {
                param.AddParameter("SETVIEWCENTERY", getRequestParameter("SETVIEWCENTERY", ""));
            }
            return executeRequestInternal(request);
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
        } catch (Exception ex) {
            return javaException(ex);
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