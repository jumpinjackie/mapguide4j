package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public abstract class MgResourceServiceController extends MgAbstractAuthenticatedController {
    protected static Result getResourceContent(String repoType, String resourcePath) {
        try {
            MgResourceIdentifier resId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resContent = resSvc.GetResourceContent(resId);
            response().setContentType(resContent.GetMimeType());
            return ok(resContent.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result getResourceHeader(String repoType, String resourcePath) {
        try {
            MgResourceIdentifier resId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resHeader = resSvc.GetResourceHeader(resId);
            response().setContentType(resHeader.GetMimeType());
            return ok(resHeader.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result enumerateResourceData(String repoType, String resourcePath) {
        try {
            MgResourceIdentifier resId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resDataList = resSvc.EnumerateResourceData(resId);
            response().setContentType(resDataList.GetMimeType());
            return ok(resDataList.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result getResourceData(String repoType, String resourcePath, String dataName) {
        try {
            MgResourceIdentifier resId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resDataList = resSvc.EnumerateResourceData(resId);
            response().setContentType(resDataList.GetMimeType());
            return ok(resDataList.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result enumerateResourceReferences(String repoType, String resourcePath) {
        try {
            MgResourceIdentifier resId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resRefList = resSvc.EnumerateReferences(resId);
            response().setContentType(resRefList.GetMimeType());
            return ok(resRefList.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result deleteResourceData(String repoType, String resourcePath, String dataName) {
        return ok("called deleteResourceData(" + repoType + ", " + resourcePath + ", "  + dataName + ")");
    }

    protected static Result deleteResource(String repoType, String resourcePath) {
        return ok("called deleteResource(" + repoType + ", " + resourcePath + ")");
    }
}