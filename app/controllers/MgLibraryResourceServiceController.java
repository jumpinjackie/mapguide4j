package controllers;

import java.lang.Integer;
import java.lang.NumberFormatException;
import java.lang.String;
import java.util.Map;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public class MgLibraryResourceServiceController extends MgResourceServiceController {

    public static Result enumerateResources(String resourcePath) {
        try {
            Map<String, String[]> queryParams = request().queryString();
            int depth = -1;
            String resType = "";
            if (queryParams.get("type") != null && queryParams.get("type").length > 0) {
                resType = queryParams.get("type")[0];
            }
            if (queryParams.get("depth") != null && queryParams.get("depth").length > 0) {
                try {
                    depth = Integer.parseInt(queryParams.get("depth")[0]);
                }
                catch (NumberFormatException e) {
                    return badRequest(e.getMessage());
                }
            }
            MgResourceIdentifier resId = constructLibraryResourceId(MgRepositoryType.Library, resourcePath, true);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resContent = resSvc.EnumerateResources(resId, depth, resType, true);
            response().setContentType(resContent.GetMimeType());
            return ok(resContent.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getResourceContent(String resourcePath) {
        try {
            return getResourceContent(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getResourceHeader(String resourcePath) {
        try {
            return getResourceHeader(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result enumerateResourceData(String resourcePath) {
        try {
            return enumerateResourceData(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getResourceData(String resourcePath, String dataName) {
        try {
            return getResourceData(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection(), dataName);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result enumerateResourceReferences(String resourcePath) {
        try {
            return enumerateResourceReferences(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result deleteResourceData(String resourcePath, String dataName) {
        try {
            return deleteResourceData(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection(), dataName);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result deleteResource(String resourcePath) {
        try {
            return deleteResource(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}