package controllers;

import util.*;

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
            int depth = MgAjaxViewerUtil.GetIntParameter(getRequestParameter("depth", "-1"));
            String resType = getRequestParameter("type", "");
            Logger.debug("Type: " + resType + ", depth: " + depth);
            MgResourceIdentifier resId = constructLibraryResourceId(MgRepositoryType.Library, resourcePath, true);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resContent = resSvc.EnumerateResources(resId, depth, resType, true);
            response().setContentType(resContent.GetMimeType());
            return ok(resContent.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result setResourceContent(String resourcePath) {
        return TODO;
    }

    public static Result setResourceHeader(String resourcePath) {
        return TODO;
    }

    public static Result setResourceData(String resourcePath, String dataName) {
        return TODO;
    }

    public static Result getResourceContent(String resourcePath) {
        try {
            return getResourceContent(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection());
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result getResourceHeader(String resourcePath) {
        try {
            return getResourceHeader(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection());
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result enumerateResourceData(String resourcePath) {
        try {
            return enumerateResourceData(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection());
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result getResourceData(String resourcePath, String dataName) {
        try {
            return getResourceData(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection(), dataName);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result enumerateResourceReferences(String resourcePath) {
        try {
            return enumerateResourceReferences(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection());
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result deleteResourceData(String resourcePath, String dataName) {
        try {
            return deleteResourceData(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection(), dataName);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result deleteResource(String resourcePath) {
        try {
            return deleteResource(constructLibraryResourceId(MgRepositoryType.Library, resourcePath), createMapGuideConnection());
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }
}