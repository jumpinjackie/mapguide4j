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

    public static Result createRuntimeMap(String sessionId, String mapName) {
        try {
            MgSiteConnection siteConn = new MgSiteConnection();
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            siteConn.Open(userInfo);

            return TODO;
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result queryMapFeatures(String sessionId, String mapName) {
        try {
            //Like getDynamicMapOverlayImage, we don't need MgHtmlController as previously thought. Just set up the required
            //parameter dictionary for MgHttpRequest
            String uri =  controllers.routes.MgViewerController.getDynamicMapOverlayImage(sessionId, mapName).absoluteURL(request());
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "QUERYMAPFEATURES");
            param.AddParameter("VERSION", "1.0.0");
            param.AddParameter("SESSION", sessionId);
            param.AddParameter("MAPNAME", mapName);
            param.AddParameter("PERSIST", getRequestParameter("PERSIST", "0"));
            if (hasRequestParameter("FEATUREFILTER"))
                param.AddParameter("FEATUREFILTER", getRequestParameter("FEATUREFILTER", ""));
            param.AddParameter("LAYERATTRIBUTEFILTER", getRequestParameter("LAYERATTRIBUTEFILTER", "3"));
            String selectionVariant = "";
            if (hasRequestParameter("SELECTIONVARIANT")) {
                String variant = getRequestParameter("SELECTIONVARIANT", "");
                if (variant.equals("TOUCHES") || variant.equals("INTERSECTS") || variant.equals("WITHIN") || variant.equals("ENVELOPEINTERSECTS")) {
                    param.AddParameter("SELECTIONVARIANT", variant);
                } else {
                    return badRequest("Invalid parameter: SELECTIONVARIANT");
                }
            }
            if (!hasRequestParameter("GEOMETRY"))
                return badRequest("Missing required parameter: GEOMETRY");
            param.AddParameter("GEOMETRY", getRequestParameter("GEOMETRY", ""));
            param.AddParameter("LAYERNAMES", getRequestParameter("LAYERNAMES", ""));

            return executeRequestInternal(request);
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