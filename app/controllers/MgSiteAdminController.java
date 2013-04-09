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
            admin.open(userInfo);

            MgPropertyCollection status = admin.getSiteStatus();
            return mgPropertyCollectionXml(status);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        } finally {
            if (admin != null) {
                try {
                    admin.close();
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
            admin.open(userInfo);
            return ok(admin.getSiteVersion());
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        } finally {
            if (admin != null) {
                try {
                    admin.close();
                }
                catch (Exception e) { }
            }
        }
    }
}