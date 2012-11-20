package controllers;

import actions.*;
import play.*;
import play.mvc.*;

import java.io.*;
import java.util.Map;
import java.util.List;
import java.lang.String;
import java.lang.StringBuilder;

import org.osgeo.mapguide.*;
import org.w3c.dom.*;
import org.w3c.dom.ls.*;

public abstract class MgAbstractController extends Controller {

    protected static Result javaException(Throwable e) {
        //I need apache commons just for a class to get the full exception details to string? GTFO!
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        
        return internalServerError(sw.toString());
    }

    protected static String getMgSessionId() {
        //For any authenticated controller, the session id will be in one of 3 places:
        //The query string (GET)
        //The form body (POST)
        
        String sessionId = getRequestParameter("SESSION", null);
        
        return sessionId;
    }

    protected static String getRequestBodyAsXmlUtf8() {
        Document document = request().body().asXml();
        if (document != null) {
            DOMImplementation impl = document.getImplementation();
            DOMImplementationLS implLS = (DOMImplementationLS) impl.getFeature("LS", "3.0");
            LSSerializer lsSerializer = implLS.createLSSerializer();
            lsSerializer.getDomConfig().setParameter("format-pretty-print", true);
             
            LSOutput lsOutput = implLS.createLSOutput();
            lsOutput.setEncoding("UTF-8");
            Writer stringWriter = new StringWriter();
            lsOutput.setCharacterStream(stringWriter);
            lsSerializer.write(document, lsOutput);
             
            String result = stringWriter.toString();
            return result;
        }
        return null;
    }

    /**
     * Method: getRequestParameter
     * 
     * Convenience method to get a parameter by name. This method tries to get the named parameter:
     *  1. As-is
     *  2. As upper-case
     *  3. As lower-case
     * 
     * In that particular order, if none could be found after these attempts, the defaultValue is returned
     * instead, otherwise the matching parameter value is returned
     * 
     * Parameters:
     * 
     *   String name         - [String/The parameter name]
     *   String defaultValue - [String/The default value]
     * 
     * Returns:
     * 
     *   String - the matching parameter value or the default value if no matches can be found
     */
    protected static String getRequestParameter(String name, String defaultValue) {
        Map<String, String[]> params = requestParameters();
        if (params == null)
            return defaultValue;

        if (params.get(name) != null)
            return params.get(name)[0];
        else if (params.get(name.toUpperCase()) != null)
            return params.get(name.toUpperCase())[0];
        else if (params.get(name.toLowerCase()) != null) 
            return params.get(name.toLowerCase())[0];
        else
            return defaultValue;
    }

    /**
     * Method: hasRequestParameter
     * 
     * Convenience method to check whether a named parameter exists. This method tries to get the named parameter:
     *  1. As-is
     *  2. As upper-case
     *  3. As lower-case
     *  
     * Use in conjunction with getRequestParameter for case-insensitive parameter testing and retrieval
     * 
     * Parameters:
     * 
     *   String name - [String/The parameter name]
     * 
     * Returns:
     * 
     *   boolean - true if the parameter exists, false otherwise
     */
    protected static boolean hasRequestParameter(String name) {
        Map<String, String[]> params = requestParameters();
        if (params == null)
            return false;
        
        return (params.get(name) != null) || (params.get(name.toUpperCase()) != null) || (params.get(name.toLowerCase()) != null);
    }

    private static Map<String, String[]> requestParameters() {
        if (request().method() == "GET") {
            return request().queryString();
        } else if (request().method() == "POST") {
            return request().body().asFormUrlEncoded();
        } else {
            return null;
        }
    }

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

    protected static boolean TrySetMgCredentials(MgUserInformation cred) throws MgException, UnsupportedEncodingException {
        String authHeader = request().getHeader(MgCheckSessionAction.AUTHORIZATION);
        if (authHeader != null) {
            String auth = authHeader.substring(6);
            byte[] decodedAuth = javax.xml.bind.DatatypeConverter.parseBase64Binary(auth);
            String decodedStr = new String(decodedAuth, "UTF-8");
            String[] credString = decodedStr.split(":");
            if (credString.length == 1 || credString.length == 2) {
                String username = credString[0];
                String password = "";
                if (credString.length == 2)
                    password = credString[1];

                cred.SetMgUsernamePassword(username, password);
                Logger.debug("MG Credentials set from auth header");
                return true;
            }
        }
        return false;
    }

    protected static MgSiteConnection createAnonymousMapGuideConnection() throws MgException {
        MgUserInformation userInfo = new MgUserInformation("Anonymous", "");
        MgSiteConnection siteConn = new MgSiteConnection();
        siteConn.Open(userInfo);
        return siteConn;
    }

    protected static MgSiteConnection createMapGuideConnection() throws MgException, UnsupportedEncodingException {
        String sessionId = getMgSessionId();
        MgUserInformation userInfo = new MgUserInformation();
        if (sessionId != null && !sessionId.equals("")) {
            userInfo.SetMgSessionId(sessionId);
        }
        TrySetMgCredentials(userInfo);
        MgSiteConnection siteConn = new MgSiteConnection();
        siteConn.Open(userInfo);
        return siteConn;
    }

    protected static MgResourceIdentifier constructLibraryResourceId(String repoType, String resourcePath, boolean appendSlashIfNeeded) throws MgException {
        String resIdStr = repoType + ":";
        resIdStr += "//" + resourcePath;
        if (appendSlashIfNeeded && resIdStr.charAt(resIdStr.length() - 1) != '/') {
            resIdStr += "/";
        }
        //Logger.debug("Construct resid (" + repoType + ", " + resourcePath + " => " + resIdStr);
        return new MgResourceIdentifier(resIdStr);
    }

    protected static MgResourceIdentifier constructLibraryResourceId(String repoType, String resourcePath) throws MgException {
        return constructLibraryResourceId(repoType, resourcePath, false);
    }

    protected static Result mgServerError(MgException mex) {
        try {
            java.lang.Thread.currentThread().dumpStack();
            String data = String.format("%s%n%s", mex.GetExceptionMessage(), mex.GetStackTrace());
            //TODO: There are possibly more MapGuide Exceptions that can map cleanly to HTTP status codes
            if (mex instanceof MgResourceNotFoundException || mex instanceof MgResourceDataNotFoundException) { //404
                return notFound(data);
            } else if (mex instanceof MgAuthenticationFailedException || mex instanceof MgUnauthorizedAccessException || mex instanceof MgUserNotFoundException) { //401
                response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
                return unauthorized("You must enter a valid login ID and password to access this site");
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