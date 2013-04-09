package controllers;

import actions.*;
import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

/**
 * REST controller for site-related MapGuide operations
 */
@MgCheckSession
public class MgSiteController extends MgAbstractController {

    public static Result getGroups() {
        try {
            String format = "xml";
            String fmt = format.toLowerCase();
            if (!fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "ENUMERATEGROUPS");
            param.addParameter("VERSION", "1.0.0");

            if (fmt.equals("xml")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.addParameter("FORMAT", MgMimeType.Json);
            }

            TryFillMgCredentials(param);
            return executeRequestInternal(request);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result getGroupsForUser(String userName) {
        try {
            //Hmmm. Should we allow Anonymous to discover its own groups?
            if (isAnonymous() && !userName.equals("Anonymous")) {
                //HACK: We don't want to trip the qunit test runner with interactive dialogs
                String fromTestHarness = request().getHeader("x-mapguide4j-test-harness");
                if (fromTestHarness == null || !fromTestHarness.toUpperCase().equals("TRUE"))
                    response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
                return unauthorized("You must enter a valid login ID and password to access this site");
            }

            MgSiteConnection siteConn = createMapGuideConnection();
            MgSite site = siteConn.getSite();

            try {
                String user = site.getUserForSession();
                //Hmmm. Should we allow Anonymous to discover its own roles?
                if (user.equals("Anonymous") && !userName.equals("Anonymous")) {
                    //HACK: We don't want to trip the qunit test runner with interactive dialogs
                    String fromTestHarness = request().getHeader("x-mapguide4j-test-harness");
                    if (fromTestHarness == null || !fromTestHarness.toUpperCase().equals("TRUE"))
                        response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
                    return unauthorized("You must enter a valid login ID and password to access this site");
                }
            } catch (MgException ex) { } //Could happen if we have non-anonymous credentials in the http authentication header

            MgByteReader content = site.enumerateGroups(userName);
            response().setContentType(content.getMimeType());
            return ok(content.toString());
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result getRolesForUser(String userName) {
        try {
            //Hmmm. Should we allow Anonymous to discover its own roles?
            if (isAnonymous() && !userName.equals("Anonymous")) {
                //HACK: We don't want to trip the qunit test runner with interactive dialogs
                String fromTestHarness = request().getHeader("x-mapguide4j-test-harness");
                if (fromTestHarness == null || !fromTestHarness.toUpperCase().equals("TRUE"))
                    response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
                return unauthorized("You must enter a valid login ID and password to access this site");
            }

            MgSiteConnection siteConn = createMapGuideConnection();
            MgSite site = siteConn.getSite();

            try {
                String user = site.getUserForSession();
                //Hmmm. Should we allow Anonymous to discover its own roles?
                if (user.equals("Anonymous") && !userName.equals("Anonymous")) {
                    //HACK: We don't want to trip the qunit test runner with interactive dialogs
                    String fromTestHarness = request().getHeader("x-mapguide4j-test-harness");
                    if (fromTestHarness == null || !fromTestHarness.toUpperCase().equals("TRUE"))
                        response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
                    return unauthorized("You must enter a valid login ID and password to access this site");
                }
            } catch (MgException ex) { } //Could happen if we have non-anonymous credentials in the http authentication header

            MgStringCollection content = site.enumerateRoles(userName);
            return mgStringCollectionXml(content);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result getUsersForGroup(String groupName) {
        try {
            if (isAnonymous()) {
                //HACK: We don't want to trip the qunit test runner with interactive dialogs
                String fromTestHarness = request().getHeader("x-mapguide4j-test-harness");
                if (fromTestHarness == null || !fromTestHarness.toUpperCase().equals("TRUE"))
                    response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
                return unauthorized("You must enter a valid login ID and password to access this site");
            }

            String format = "xml";
            String fmt = format.toLowerCase();
            if (!fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "ENUMERATEUSERS");
            param.addParameter("VERSION", "1.0.0");
            param.addParameter("GROUP", groupName);
            if (fmt.equals("xml")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.addParameter("FORMAT", MgMimeType.Json);
            }

            TryFillMgCredentials(param);
            return executeRequestInternal(request);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }
}