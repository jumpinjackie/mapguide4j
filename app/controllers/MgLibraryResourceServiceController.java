package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public class MgLibraryResourceServiceController extends MgResourceServiceController {

    public static Result enumerateResourcesDefault(String resourcePath) {
         try {
            MgResourceIdentifier resId = ConstructResourceId(MgRepositoryType.Library, resourcePath, true);
            MgSiteConnection siteConn = CreateMapGuideConnection();
            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resContent = resSvc.EnumerateResources(resId, -1, "", true);
            response().setContentType(resContent.GetMimeType());
            return ok(resContent.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result enumerateResources(String resourcePath, String type, Long depth, Boolean children) {
         try {
            MgResourceIdentifier resId = ConstructResourceId(MgRepositoryType.Library, resourcePath, true);
            MgSiteConnection siteConn = CreateMapGuideConnection();
            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resContent = resSvc.EnumerateResources(resId, depth.intValue(), type, children);
            response().setContentType(resContent.GetMimeType());
            return ok(resContent.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getResourceContent(String resourcePath) {
        return getResourceContent(MgRepositoryType.Library, resourcePath);
    }

    public static Result getResourceHeader(String resourcePath) {
        return getResourceHeader(MgRepositoryType.Library, resourcePath);
    }

    public static Result enumerateResourceData(String resourcePath) {
        return enumerateResourceData(MgRepositoryType.Library, resourcePath);
    }

    public static Result getResourceData(String resourcePath, String dataName) {
        return getResourceData(MgRepositoryType.Library, resourcePath, dataName);
    }

    public static Result enumerateResourceReferences(String resourcePath) {
        return enumerateResourceReferences(MgRepositoryType.Library, resourcePath);
    }

    public static Result deleteResourceData(String resourcePath, String dataName) {
        return deleteResourceData(MgRepositoryType.Library, resourcePath, dataName);
    }

    public static Result deleteResource(String resourcePath) {
        return deleteResource(MgRepositoryType.Library, resourcePath);
    }
}