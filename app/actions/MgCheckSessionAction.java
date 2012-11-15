package actions;

import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.*;

import org.osgeo.mapguide.*;

public class MgCheckSessionAction extends Action<MgCheckSessionAction> {

    public static final String MAPGUIDE_SESSION_ID_KEY = "mapguide.sessionid";

    private static final String AUTHORIZATION = "authorization";
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String REALM = "Basic realm=\"Your Realm Here\"";

    public Result call(Http.Context ctx) throws Throwable {
        Logger.debug(ctx.request().method() + ": " + ctx.request().uri());
        Logger.debug("Checking MapGuide Session");

        String sessionId = null;
        if (ctx.request().method().equals("GET")) {
            Map<String, String[]> params = ctx.request().queryString();
            if (params.get("SESSION") != null)
                sessionId = params.get("SESSION")[0];
        }
        else if (ctx.request().method().equals("POST")) {
            Map<String, String[]> params = ctx.request().body().asFormUrlEncoded();
            if (params != null && params.get("SESSION") != null)
                sessionId = params.get("SESSION")[0];  
        }
        if (sessionId == null)
            sessionId = ctx.session().get(MAPGUIDE_SESSION_ID_KEY);

        //TODO: Don't use the play session to store the MapGuide session id
        if (sessionId == null)
        {
            Logger.debug("No MapGuide Session ID found. Checking username/password");

            String authHeader = ctx.request().getHeader(AUTHORIZATION);
            if (authHeader == null) {
                Logger.debug("No HTTP authentication header found");
                ctx.response().setHeader(WWW_AUTHENTICATE, REALM);
                return unauthorized();
            }

            String auth = authHeader.substring(6);
            //Logger.debug("Authentication header: " + auth);
            byte[] decodedAuth = javax.xml.bind.DatatypeConverter.parseBase64Binary(auth);
            String decodedStr = new String(decodedAuth, "UTF-8");
            //Logger.debug("Decoded string: " + decodedStr);
            String[] credString = decodedStr.split(":");
            //Logger.debug("Decoded array: " + credString.length);
            if (credString == null || (credString.length != 1 && credString.length != 2)) {
                return unauthorized("malformed credentials supplied");
            }

            String username = credString[0];
            String password = "";
            if (credString.length == 2)
                password = credString[1];

            try {
                MgUserInformation userInfo = new MgUserInformation(username, password);
                MgSite site = new MgSite();
                site.Open(userInfo);
                sessionId = site.CreateSession();
                ctx.session().put(MAPGUIDE_SESSION_ID_KEY, sessionId);
                Logger.debug("MapGuide Session ID stashed");
            } catch (MgException ex) {
                return unauthorized(ex.GetExceptionMessage());
            }
        } else {
            Logger.debug("MapGuide Session ID already stashed. Checking it");
            try {
                MgUserInformation userInfo = new MgUserInformation(sessionId);
                MgSite site = new MgSite();
                site.Open(userInfo);
            } catch (MgException ex) {
                ctx.session().remove(MAPGUIDE_SESSION_ID_KEY);
                Logger.debug("Removed expired MapGuide Session ID");
                return unauthorized(ex.GetExceptionMessage());
            }
        }
        return delegate.call(ctx);
    }
}