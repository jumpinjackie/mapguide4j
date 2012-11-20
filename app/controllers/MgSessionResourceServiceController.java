package controllers;

import util.*;

import play.*;
import play.mvc.*;
import play.mvc.BodyParser.*;

import org.osgeo.mapguide.*;

//We don't extend from MgResourceServiceController because we don't require session-checking before action invocation
//because he session id is already part of the resource URL
public class MgSessionResourceServiceController extends MgAbstractController {
    public static Result getResourceContent(String sessionId, String resourcePath) {
        try {
            MgResourceIdentifier resId = new MgResourceIdentifier("Session:" + sessionId + "//" + resourcePath);
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.Open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resContent = resSvc.GetResourceContent(resId);
            response().setContentType(resContent.GetMimeType());
            return ok(resContent.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getResourceHeader(String sessionId, String resourcePath) {
        try {
            Logger.debug("Session: " + sessionId + ", path: " + resourcePath);
            MgResourceIdentifier resId = new MgResourceIdentifier("Session:" + sessionId + "//" + resourcePath);
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.Open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resHeader = resSvc.GetResourceHeader(resId);
            response().setContentType(resHeader.GetMimeType());
            return ok(resHeader.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result enumerateResourceData(String sessionId, String resourcePath) {
        try {
            Logger.debug("Session: " + sessionId + ", path: " + resourcePath);
            MgResourceIdentifier resId = new MgResourceIdentifier("Session:" + sessionId + "//" + resourcePath);
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.Open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resDataList = resSvc.EnumerateResourceData(resId);
            response().setContentType(resDataList.GetMimeType());
            return ok(resDataList.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getResourceData(String sessionId, String resourcePath, String dataName) {
        try {
            Logger.debug("Session: " + sessionId + ", path: " + resourcePath);
            MgResourceIdentifier resId = new MgResourceIdentifier("Session:" + sessionId + "//" + resourcePath);
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.Open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteReader resData = resSvc.GetResourceData(resId, dataName);
            
            response().setHeader("Content-Disposition", "attachment; filename=" + dataName);
            return ok(MgAjaxViewerUtil.ByteReaderToStream(resData));
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result setResourceContent(String sessionId, String resourcePath) {
        try {
            String str = getRequestBodyAsXmlUtf8();
            if (str == null || str.equals(""))
                return badRequest("Empty XML request body");
            byte [] bytes = str.getBytes();

            MgResourceIdentifier resId = new MgResourceIdentifier("Session:" + sessionId + "//" + resourcePath);
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.Open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);

            MgByteSource src = new MgByteSource(bytes, bytes.length);
            MgByteReader content = src.GetReader();
            resSvc.SetResource(resId, content, null);
            return created();
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    //FIXME: MapGuide Server either doesn't allow this for session-based resources or it is broken for session-based
    //resources. My money's on the latter
    public static Result setResourceHeader(String sessionId, String resourcePath) {
        try {
            String str = getRequestBodyAsXmlUtf8();
            if (str == null || str.equals(""))
                return badRequest("Empty XML request body");
            byte [] bytes = str.getBytes();

            MgResourceIdentifier resId = new MgResourceIdentifier("Session:" + sessionId + "//" + resourcePath);
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.Open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);

            MgByteSource src = new MgByteSource(bytes, bytes.length);
            MgByteReader header = src.GetReader();
            resSvc.SetResource(resId, null, header);
            return created();
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result setResourceData(String sessionId, String resourcePath, String dataName) {
        try {
            Http.RawBuffer raw = request().body().asRaw();
            if (raw == null)
                return badRequest("Empty raw request body");

            MgResourceIdentifier resId = new MgResourceIdentifier("Session:" + sessionId + "//" + resourcePath);
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.Open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.CreateService(MgServiceType.ResourceService);
            MgByteSource src = new MgByteSource(raw.asFile().getAbsolutePath());
            MgByteReader resData = src.GetReader();
            resSvc.SetResourceData(resId, dataName, MgResourceDataType.File, resData);
            return created();
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}