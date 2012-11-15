package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

//We don't extend from MgResourceServiceController because we don't require session-checking before action invocation
//because he session id is already part of the resource URL
public class MgSessionResourceServiceController extends MgAbstractController {
    public static Result getResourceContent(String sessionId, String resourcePath) {
        try {
            Logger.debug("Session: " + sessionId + ", path: " + resourcePath);
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
            response().setContentType(resData.GetMimeType());
            return ok(resData.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}