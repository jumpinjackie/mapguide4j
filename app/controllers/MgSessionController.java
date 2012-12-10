package controllers;

import actions.*;
import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public class MgSessionController extends MgAbstractController {

    @MgCheckSession
    public static Result createSession() {
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            MgSite site = siteConn.GetSite();
            return created(site.CreateSession());
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result destroySession(String sessionId) {
        try {
            MgUserInformation userInfo = new MgUserInformation(sessionId);
            MgSiteConnection siteConn = new MgSiteConnection();
            siteConn.Open(userInfo);
            MgSite site = siteConn.GetSite();
            site.DestroySession(sessionId);
            return ok();
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }
}