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

            Map<String, String[]> queryParams = request().queryString();
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
}