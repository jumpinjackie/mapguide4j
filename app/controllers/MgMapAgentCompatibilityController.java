package controllers;

import actions.*;
import play.*;
import play.mvc.*;
import java.lang.StringBuilder;

import java.util.Map;

import org.osgeo.mapguide.*;

@MgCheckSession
public abstract class MgMapAgentCompatibilityController extends MgAbstractAuthenticatedController {

    public static Result processGetRequest() {
        try {
            Map<String, String[]> queryParams = request().queryString();
            String op = "";
            String version = "1.0.0";
            String locale = "en";
            MgResourceIdentifier resourceId = null;
            String sessionId = "";

            if (queryParams.get("OPERATION") != null) {
                op = queryParams.get("OPERATION")[0];
            }
            if (queryParams.get("VERSION") != null) {
                version = queryParams.get("VERSION")[0];
            }
            if (queryParams.get("LOCALE") != null) {
                locale = queryParams.get("LOCALE")[0];
            }
            if (queryParams.get("SESSION") != null) {
                sessionId = queryParams.get("SESSION")[0];
            }
            if (queryParams.get("RESOURCEID") != null) {
                resourceId = new MgResourceIdentifier(queryParams.get("RESOURCEID")[0]);
            }

            if (!op.equals("")) {
                //TODO: There's surely a better way? C# has spoiled me with its elegance because I can't figure
                //out the proper and equivalent way in Java!
                if (op.equals("GETDYNAMICMAPOVERLAYIMAGE")) {
                    if (queryParams.get("MAPNAME") == null)
                        return badRequest("Missing MAPNAME parameter");
                    String mapName = queryParams.get("MAPNAME")[0];
                    return MgViewerController.getDynamicMapOverlayImage(sessionId, mapName);
                } else if (op.equals("ENUMERATERESOURCES")) {
                    if (resourceId != null && resourceId.GetRepositoryType() == MgRepositoryType.Library) {

                    } else {
                        return badRequest();
                    }
                } else if (op.equals("GETRESOURCECONTENT")) {

                } else if (op.equals("GETRESOURCEHEADER")) {

                } else if (op.equals("ENUMERATERESOURCEREFERENCES")) {

                } else {
                    return TODO;
                }
            } else {
                return badRequest();
            }
        } catch (MgException ex) {
            return mgServerError(ex);
        }
        return badRequest();
    }
    
    public static Result processPostRequest() {
        return TODO;
    }

}