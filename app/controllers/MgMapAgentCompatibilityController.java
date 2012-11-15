package controllers;

import actions.*;
import play.*;
import play.mvc.*;

import java.io.File;
import java.lang.StringBuilder;
import java.util.Map;

import org.osgeo.mapguide.*;

public abstract class MgMapAgentCompatibilityController extends MgAbstractController {

    @MgCheckSession
    public static Result processGetRequest() {
        try {
            Map<String, String[]> queryParams = request().queryString();
            return processRequest("GET", queryParams);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
    
    private static Result processRequest(String method, Map<String, String[]> queryParams) throws MgException {
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
            if (op.equals("CREATESESSION")) {
                return TODO;
            } else if (op.equals("GETDYNAMICMAPOVERLAYIMAGE")) {
                if (queryParams.get("MAPNAME") == null)
                    return badRequest("Missing MAPNAME parameter");
                String mapName = queryParams.get("MAPNAME")[0];
                return MgViewerController.getDynamicMapOverlayImage(sessionId, mapName);
            } else if (op.equals("QUERYMAPFEATURES")) {
                if (queryParams.get("MAPNAME") == null)
                    return badRequest("Missing MAPNAME parameter");
                String mapName = queryParams.get("MAPNAME")[0];
                return MgViewerController.queryMapFeatures(sessionId, mapName);
            } else if (op.equals("ENUMERATERESOURCES")) {
                if (resourceId != null && resourceId.GetRepositoryType() == MgRepositoryType.Library) {
                    return TODO;
                } else {
                    return badRequest();
                }
            } else if (op.equals("GETRESOURCECONTENT")) {
                return TODO;
            } else if (op.equals("GETRESOURCEHEADER")) {
                return TODO;
            } else if (op.equals("ENUMERATERESOURCEREFERENCES")) {
                return TODO;
            } else {
                return TODO;
            }
        } else {
            return badRequest();
        }
    }

    @MgCheckSession
    public static Result processPostRequest() {
        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            return processRequest("POST", formData);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result agentasset(String file) {
        File f = Play.application().getFile("internal/MapAgentForms/" + file);
        if (!f.exists())
            return notFound();
        else
            return ok(f);
    }
}