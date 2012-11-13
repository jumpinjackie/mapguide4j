package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public abstract class MgSiteAdminController extends MgAbstractAuthenticatedController {

    public static Result getSiteStatus() {
        MgServerAdmin admin = null;
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            MgSite site = siteConn.GetSite();
            if (site.GetUserForSession().equals("Anonymous")) {
                return unauthorized("MapGuide Anonymous user account access denied");
            } else {
                MgUserInformation userInfo = new MgUserInformation(site.GetCurrentSession());
                admin = new MgServerAdmin();
                admin.Open(userInfo);

                MgPropertyCollection status = admin.GetSiteStatus();
                return mgPropertyCollectionXml(status);
            }
        } catch (MgException ex) {
            return mgServerError(ex);
        } finally {
            if (admin != null) {
                try {
                    admin.Close();
                }
                catch (Exception e) { }
            }
        }
    }

    public static Result getSiteVersion() {
        MgServerAdmin admin = null;
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            MgSite site = siteConn.GetSite();
            if (site.GetUserForSession().equals("Anonymous")) {
                return unauthorized("MapGuide Anonymous user account access denied");
            } else {
                MgUserInformation userInfo = new MgUserInformation(site.GetCurrentSession());
                admin = new MgServerAdmin();
                admin.Open(userInfo);
                return ok(admin.GetSiteVersion());
            }
        } catch (MgException ex) {
            return mgServerError(ex);
        } finally {
            if (admin != null) {
                try {
                    admin.Close();
                }
                catch (Exception e) { }
            }
        }
    }

    public static Result getGroups() {
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            MgSite site = siteConn.GetSite();
            if (site.GetUserForSession().equals("Anonymous")) {
                return unauthorized("MapGuide Anonymous user account access denied");
            } else {
                MgByteReader content = site.EnumerateGroups();
                response().setContentType(content.GetMimeType());
                return ok(content.ToString());
            }
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getGroupsForUser(String userName) {
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            MgSite site = siteConn.GetSite();
            if (site.GetUserForSession().equals("Anonymous")) {
                return unauthorized("MapGuide Anonymous user account access denied");
            } else {
                MgByteReader content = site.EnumerateGroups(userName);
                response().setContentType(content.GetMimeType());
                return ok(content.ToString());
            }
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getRolesForUser(String userName) {
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            MgSite site = siteConn.GetSite();
            if (site.GetUserForSession().equals("Anonymous")) {
                return unauthorized("MapGuide Anonymous user account access denied");
            } else {
                MgStringCollection content = site.EnumerateRoles(userName);
                return mgStringCollectionXml(content);
            }
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getUsersForGroup(String groupName) {
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            MgSite site = siteConn.GetSite();
            if (site.GetUserForSession().equals("Anonymous")) {
                return unauthorized("MapGuide Anonymous user account access denied");
            } else {
                MgByteReader content = site.EnumerateUsers(groupName);
                response().setContentType(content.GetMimeType());
                return ok(content.ToString());
            }
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

}