package controllers;

import actions.*;
import play.*;
import play.mvc.*;
import java.lang.StringBuilder;

import org.osgeo.mapguide.*;

public abstract class MgAbstractController extends Controller {

    protected static MgSiteConnection CreateAnonymousMapGuideConnection() throws MgException {
        MgUserInformation userInfo = new MgUserInformation("Anonymous", "");
        MgSiteConnection siteConn = new MgSiteConnection();
        siteConn.Open(userInfo);
        return siteConn;
    }

    protected static MgSiteConnection CreateMapGuideConnection() throws MgException {
        String sessionId = session(MgCheckSessionAction.MAPGUIDE_SESSION_ID_KEY);
        MgUserInformation userInfo = new MgUserInformation(sessionId);
        MgSiteConnection siteConn = new MgSiteConnection();
        siteConn.Open(userInfo);
        return siteConn;
    }

    protected static MgResourceIdentifier ConstructResourceId(String repoType, String resourcePath, boolean appendSlashIfNeeded) throws MgException {
        String resIdStr = repoType + ":";
        if (repoType == MgRepositoryType.Session) {
            resIdStr += session(MgCheckSessionAction.MAPGUIDE_SESSION_ID_KEY);
        }
        resIdStr += "//" + resourcePath;
        if (appendSlashIfNeeded && resIdStr.charAt(resIdStr.length() - 1) != '/') {
            resIdStr += "/";
        }
        Logger.debug("Construct resid (" + repoType + ", " + resourcePath + " => " + resIdStr);
        return new MgResourceIdentifier(resIdStr);
    }

    protected static MgResourceIdentifier ConstructResourceId(String repoType, String resourcePath) throws MgException {
        return ConstructResourceId(repoType, resourcePath, false);
    }

    protected static Result mgServerError(MgException mex) {
        try {
            java.lang.Thread.currentThread().dumpStack();
            String data = String.format("%s%n%s", mex.GetExceptionMessage(), mex.GetStackTrace());
            //TODO: There are possibly more MapGuide Exceptions that can map cleanly to HTTP status codes
            if (mex instanceof MgResourceNotFoundException || mex instanceof MgResourceDataNotFoundException) { //404
                return notFound(data);
            } else if (mex instanceof MgAuthenticationFailedException) { //401
                return unauthorized(data);
            }
            return internalServerError(data);
        } catch (MgException ex) { //This is a SWIG-ism: http://trac.osgeo.org/mapguide/ticket/9
            return internalServerError();
        }
    }

    protected static Result mgSpatialContextReaderXml(MgSpatialContextReader scReader) {
        try {
            MgByteReader reader = scReader.ToXml();
            response().setContentType(reader.GetMimeType());
            return ok(reader.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result mgBatchPropertyCollectionXml(MgBatchPropertyCollection batchProps) {
        try {
            MgByteReader reader = batchProps.ToXml();
            response().setContentType(reader.GetMimeType());
            return ok(reader.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result mgStringCollectionXml(MgStringCollection strings) {
        try {
            MgByteReader reader = strings.ToXml();
            response().setContentType(reader.GetMimeType());
            return ok(reader.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}