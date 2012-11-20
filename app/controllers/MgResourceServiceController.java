package controllers;

import util.*;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public abstract class MgResourceServiceController extends MgAbstractAuthenticatedController {
    protected static Result getResourceContent(MgResourceIdentifier resId, MgSiteConnection siteConn) throws MgException {
        MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
        MgByteReader resContent = resSvc.GetResourceContent(resId);
        response().setContentType(resContent.GetMimeType());
        return ok(resContent.ToString());
    }

    protected static Result getResourceHeader(MgResourceIdentifier resId, MgSiteConnection siteConn) throws MgException {
        MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
        MgByteReader resHeader = resSvc.GetResourceHeader(resId);
        response().setContentType(resHeader.GetMimeType());
        return ok(resHeader.ToString());
    }

    protected static Result enumerateResourceData(MgResourceIdentifier resId, MgSiteConnection siteConn) throws MgException {
        MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
        MgByteReader resDataList = resSvc.EnumerateResourceData(resId);
        response().setContentType(resDataList.GetMimeType());
        return ok(resDataList.ToString());
    }

    protected static Result getResourceData(MgResourceIdentifier resId, MgSiteConnection siteConn, String dataName) throws MgException {
        MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
        MgByteReader resData = resSvc.GetResourceData(resId, dataName);
        response().setHeader("Content-Disposition", "attachment; filename=" + dataName);
        return ok(MgAjaxViewerUtil.ByteReaderToStream(resData));
    }

    protected static Result enumerateResourceReferences(MgResourceIdentifier resId, MgSiteConnection siteConn) throws MgException {
        MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
        MgByteReader resRefList = resSvc.EnumerateReferences(resId);
        response().setContentType(resRefList.GetMimeType());
        return ok(resRefList.ToString());
    }

    protected static Result deleteResourceData(MgResourceIdentifier resId, MgSiteConnection siteConn, String dataName) throws MgException {
        return ok("called deleteResourceData(" + resId.ToString() + ", "  + dataName + ")");
    }

    protected static Result deleteResource(MgResourceIdentifier resId, MgSiteConnection siteConn) throws MgException {
        return ok("called deleteResource(" + resId.ToString() + ")");
    }
}