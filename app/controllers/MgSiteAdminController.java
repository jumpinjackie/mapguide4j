package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

/**
 * REST controller for MapGuide Site Admin operations
 */
public abstract class MgSiteAdminController extends MgAbstractAuthenticatedController {

    public static Result getSiteStatus() {
        MgServerAdmin admin = null;
        try {
            MgUserInformation userInfo = getMgCredentials();
            admin = new MgServerAdmin();
            admin.Open(userInfo);

            MgPropertyCollection status = admin.GetSiteStatus();
            return mgPropertyCollectionXml(status);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
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
            MgUserInformation userInfo = getMgCredentials();
            admin = new MgServerAdmin();
            admin.Open(userInfo);
            return ok(admin.GetSiteVersion());
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        } finally {
            if (admin != null) {
                try {
                    admin.Close();
                }
                catch (Exception e) { }
            }
        }
    }
}