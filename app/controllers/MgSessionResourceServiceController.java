package controllers;

import util.*;

import play.*;
import play.mvc.*;
import play.mvc.BodyParser.*;

import org.osgeo.mapguide.*;

//We don't extend from MgResourceServiceController because we don't require session-checking before action invocation
//because the session id is already part of the resource URL

/**
 * REST controller for MapGuide Resource Service operations on session-based resources
 */
public class MgSessionResourceServiceController extends MgAbstractController {
    public static Result getResourceContent(String sessionId, String resourcePath, String format) {
        try {
            MgResourceIdentifier resId = new MgResourceIdentifier("Session:" + sessionId + "//" + resourcePath);
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.createService(MgServiceType.ResourceService);
            MgByteReader resContent = resSvc.getResourceContent(resId);
            response().setContentType(resContent.getMimeType());
            return ok(resContent.toString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getResourceHeader(String sessionId, String resourcePath, String format) {
        try {
            Logger.debug("Session: " + sessionId + ", path: " + resourcePath);
            MgResourceIdentifier resId = new MgResourceIdentifier("Session:" + sessionId + "//" + resourcePath);
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.createService(MgServiceType.ResourceService);
            MgByteReader resHeader = resSvc.getResourceHeader(resId);
            response().setContentType(resHeader.getMimeType());
            return ok(resHeader.toString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result enumerateResourceData(String sessionId, String resourcePath, String format) {
        try {
            Logger.debug("Session: " + sessionId + ", path: " + resourcePath);
            MgResourceIdentifier resId = new MgResourceIdentifier("Session:" + sessionId + "//" + resourcePath);
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.createService(MgServiceType.ResourceService);
            MgByteReader resDataList = resSvc.enumerateResourceData(resId);
            response().setContentType(resDataList.getMimeType());
            return ok(resDataList.toString());
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
            siteConn.open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.createService(MgServiceType.ResourceService);
            MgByteReader resData = resSvc.getResourceData(resId, dataName);

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
            siteConn.open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.createService(MgServiceType.ResourceService);

            MgByteSource src = new MgByteSource(bytes, bytes.length);
            MgByteReader content = src.getReader();
            resSvc.setResource(resId, content, null);
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
            siteConn.open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.createService(MgServiceType.ResourceService);

            MgByteSource src = new MgByteSource(bytes, bytes.length);
            MgByteReader header = src.getReader();
            resSvc.setResource(resId, null, header);
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
            siteConn.open(userInfo);

            MgResourceService resSvc = (MgResourceService)siteConn.createService(MgServiceType.ResourceService);
            MgByteSource src = new MgByteSource(raw.asFile().getAbsolutePath());
            MgByteReader resData = src.getReader();
            resSvc.setResourceData(resId, dataName, MgResourceDataType.File, resData);
            return created();
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}