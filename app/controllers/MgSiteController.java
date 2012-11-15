package controllers;

import actions.*;
import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public class MgSiteController extends MgAbstractController {
    public static Result createSession() {
        return TODO;
    }

    @MgCheckSession
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

    @MgCheckSession
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

    @MgCheckSession
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

    @MgCheckSession
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