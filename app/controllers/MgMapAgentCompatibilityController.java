package controllers;

import actions.*;
import util.*;
import play.*;
import play.mvc.*;

import java.io.*;
import java.lang.StringBuilder;
import java.util.Map;
import java.util.List;

import org.osgeo.mapguide.*;

public abstract class MgMapAgentCompatibilityController extends MgAbstractController {

    public static Result processGetRequest() {
        //Logger.debug("mapagent - " + request().method() + ": " + request().uri());
        String uri =  controllers.routes.MgMapAgentCompatibilityController.processGetRequest().absoluteURL(request());
        try {
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.GetRequestParam();

            boolean bGotAuth = parseAuthenticationHeader(param);
            //Logger.debug("Got authentication header: " + bGotAuth);
            populateGetRequest(param);

            //A request is valid if it contains any of the following:
            //
            // 1. A SESSION parameter
            // 2. A USERNAME parameter (PASSWORD optional). If not specified the http authentication header is checked and extracted if found
            //
            //Whether these values are valid will be determined by MgSiteConnection in the MgHttpRequest handler when we come to execute it
            boolean bValid = param.ContainsParameter("SESSION");
            if (!bValid)
                bValid = param.ContainsParameter("USERNAME");

            if (!bValid) {
                //Logger.debug("Un-authenticated request. Sending WWW-Authenticate");
                response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
                return unauthorized("You must enter a valid login ID and password to access this site");
            }
            //Logger.debug("Valid request");

            return executeRequestInternal(request);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result processPostRequest() {
        //Logger.debug("mapagent - " + request().method() + ": " + request().uri());
        String uri =  controllers.routes.MgMapAgentCompatibilityController.processGetRequest().absoluteURL(request());
        try {
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.GetRequestParam();

            boolean bGotAuth = parseAuthenticationHeader(param);
            //Logger.debug("Got authentication header: " + bGotAuth);
            populatePostRequest(param);

            //A request is valid if it contains any of the following:
            //
            // 1. A SESSION parameter
            // 2. A USERNAME parameter (PASSWORD optional). If not specified the http authentication header is checked and extracted if found
            //
            //Whether these values are valid will be determined by MgSiteConnection in the MgHttpRequest handler when we come to execute it
            boolean bValid = param.ContainsParameter("SESSION");
            if (!bValid)
                bValid = param.ContainsParameter("USERNAME");

            if (!bValid) {
                //Logger.debug("Un-authenticated request. Sending WWW-Authenticate");
                response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
                return unauthorized("You must enter a valid login ID and password to access this site");
            }
            //Logger.debug("Valid request");
            //Logger.debug("OPERATION - " + param.GetParameterValue("OPERATION"));

            return executeRequestInternal(request);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result agentasset(String file) {
        InputStream assetStream = Play.application().classloader().getResourceAsStream("resources/MapAgentForms/" + file);
        response().setContentType("text/html");
        return ok(assetStream);
    }
}