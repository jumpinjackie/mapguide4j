package controllers;

import actions.*;
import play.*;
import play.mvc.*;

import java.lang.String;
import java.lang.StringBuilder;

import org.osgeo.mapguide.*;

public abstract class MgAbstractController extends Controller {

    protected static boolean isValidResourceType(String resType) {
        return resType == MgResourceType.Folder ||
               resType == MgResourceType.FeatureSource ||
               resType == MgResourceType.LayerDefinition ||
               resType == MgResourceType.MapDefinition ||
               resType == MgResourceType.WebLayout ||
               resType == MgResourceType.ApplicationDefinition ||
               resType == MgResourceType.SymbolDefinition ||
               resType == MgResourceType.SymbolLibrary ||
               resType == MgResourceType.PrintLayout ||
               resType == MgResourceType.LoadProcedure ||
               resType == MgResourceType.DrawingSource ||
               resType == MgResourceType.Map ||
               resType == MgResourceType.Selection;
    }

    public static String escapeXmlCharacters(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch(c) {
                case '&' :
                {
                    sb.append("&amp;");
                    break;
                }
                case '\'' :
                {
                    sb.append("&apos;");
                    break;
                }
                case '>' :
                {
                    sb.append("&gt;");
                    break;
                }
                case '<' :
                {
                    sb.append("&lt;");
                    break;
                }
                case '"' :
                {
                    sb.append("&quot;");
                    break;
                }
                default :
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    protected static MgSiteConnection createAnonymousMapGuideConnection() throws MgException {
        MgUserInformation userInfo = new MgUserInformation("Anonymous", "");
        MgSiteConnection siteConn = new MgSiteConnection();
        siteConn.Open(userInfo);
        return siteConn;
    }

    protected static MgSiteConnection createMapGuideConnection() throws MgException {
        String sessionId = session(MgCheckSessionAction.MAPGUIDE_SESSION_ID_KEY);
        MgUserInformation userInfo = new MgUserInformation(sessionId);
        MgSiteConnection siteConn = new MgSiteConnection();
        siteConn.Open(userInfo);
        return siteConn;
    }

    protected static MgResourceIdentifier constructResourceId(String repoType, String resourcePath, boolean appendSlashIfNeeded) throws MgException {
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

    protected static MgResourceIdentifier constructResourceId(String repoType, String resourcePath) throws MgException {
        return constructResourceId(repoType, resourcePath, false);
    }

    protected static Result mgServerError(MgException mex) {
        try {
            java.lang.Thread.currentThread().dumpStack();
            String data = String.format("%s%n%s", mex.GetExceptionMessage(), mex.GetStackTrace());
            //TODO: There are possibly more MapGuide Exceptions that can map cleanly to HTTP status codes
            if (mex instanceof MgResourceNotFoundException || mex instanceof MgResourceDataNotFoundException) { //404
                return notFound(data);
            } else if (mex instanceof MgAuthenticationFailedException || mex instanceof MgUnauthorizedAccessException) { //401
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

    protected static Result mgPropertyCollectionXml(MgPropertyCollection props) {
        try {
            MgByteReader reader = props.ToXml();
            response().setContentType("text/xml"); //(reader.GetMimeType());
            return ok(reader.ToString());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result mgStringCollectionXml(MgStringCollection strings) {
        try {
            if (strings != null) {
                MgByteReader reader = strings.ToXml();
                response().setContentType(reader.GetMimeType());
                return ok(reader.ToString());
            } else {
                response().setContentType("text/xml");
                return ok("<StringCollection />");
            }
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}