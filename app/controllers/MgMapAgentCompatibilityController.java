package controllers;

import actions.*;
import util.*;
import play.*;
import play.mvc.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
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

    private static boolean parseAuthenticationHeader(MgHttpRequestParam param) throws MgException, UnsupportedEncodingException {
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
                param.AddParameter("USERNAME", username);
                param.AddParameter("PASSWORD", password);
                //Logger.debug("Parsed: " + username + ":" + password);
                return true;
            }
        }
        return false;
    }

    private static void populateGetRequest(MgHttpRequestParam param) throws MgException {
        Map<String, String[]> query = request().queryString();
        if (query != null) {
            for (String name : query.keySet()) {
                param.AddParameter(name, query.get(name)[0]);
            }
        }
    }

    private static void populatePostRequest(MgHttpRequestParam param) throws MgException {
        Http.MultipartFormData formData = request().body().asMultipartFormData();
        Map<String, String[]> bodyData = request().body().asFormUrlEncoded();
        if (bodyData != null) {
            for (String name : bodyData.keySet()) {
                param.AddParameter(name, bodyData.get(name)[0]);
            }
        }
        if (formData != null) {
            Map<String, String[]> query = formData.asFormUrlEncoded();
            if (query != null) {
                for (String name : query.keySet()) {
                    param.AddParameter(name, query.get(name)[0]);
                }
            }
            //These are the file upload bits
            List<Http.MultipartFormData.FilePart> fileParts = formData.getFiles();
            if (fileParts != null && fileParts.size() > 0) {
                for (Http.MultipartFormData.FilePart part : fileParts) {
                    File f = part.getFile();
                    param.AddParameter(part.getKey(), f.getAbsolutePath());
                    param.SetParameterType(part.getKey(), "tempfile"); //This is the hint to MgHttpRequest handler to create an MgByteSource from this value
                }
            }
        }
    }

    private static Result executeRequestInternal(MgHttpRequest request) throws MgException {
        MgHttpResponse response = request.Execute();
        MgHttpResult result = response.GetResult();

        if (result.GetStatusCode() == 200) {
            MgDisposable resultObj = result.GetResultObject();
            if (resultObj != null) {
                response().setContentType(result.GetResultContentType());
                if (resultObj instanceof MgByteReader) {
                    return ok(MgAjaxViewerUtil.ByteReaderToStream((MgByteReader)resultObj));
                } else if (resultObj instanceof MgFeatureReader) {
                    MgByteReader br = ((MgFeatureReader)resultObj).ToXml();
                    return ok(MgAjaxViewerUtil.ByteReaderToStream((MgByteReader)br));
                } else if (resultObj instanceof MgStringCollection) {
                    return mgStringCollectionXml((MgStringCollection)resultObj);
                } else if (resultObj instanceof MgSqlDataReader) {
                    MgByteReader br = ((MgSqlDataReader)resultObj).ToXml();
                    return ok(MgAjaxViewerUtil.ByteReaderToStream((MgByteReader)br));
                } else if (resultObj instanceof MgDataReader) {
                    MgByteReader br = ((MgDataReader)resultObj).ToXml();
                    return ok(MgAjaxViewerUtil.ByteReaderToStream((MgByteReader)br));
                } else if (resultObj instanceof MgSpatialContextReader) {
                    MgByteReader br = ((MgSpatialContextReader)resultObj).ToXml();
                    return ok(MgAjaxViewerUtil.ByteReaderToStream((MgByteReader)br));
                } else if (resultObj instanceof MgLongTransactionReader) {
                    MgByteReader br = ((MgLongTransactionReader)resultObj).ToXml();
                    return ok(MgAjaxViewerUtil.ByteReaderToStream((MgByteReader)br));
                } else if (resultObj instanceof MgHttpPrimitiveValue) {
                    return ok(((MgHttpPrimitiveValue)resultObj).ToString());
                } else {
                    return badRequest("Not sure how to output: " + resultObj.toString());
                }
            } else {
                return ok();
            }
        } else {
            String statusMessage = result.GetHttpStatusMessage();
            if (statusMessage.equals("MgAuthenticationFailedException") || statusMessage.equals("MgUnauthorizedAccessException"))
            {
                //Logger.debug("401 - " + statusMessage);
                response().setHeader(MgCheckSessionAction.WWW_AUTHENTICATE, MgCheckSessionAction.REALM);
                return unauthorized("You must enter a valid login ID and password to access this site");
            }
            else
            {
                String errHtml = String.format("\r\n" +
                    "<html>\n<head>\n" +
                    "<title>%s</title>\n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                    "</head>\n" +
                    "<body>\n<h2>%s</h2>\n%s\n</body>\n</html>\n",
                    statusMessage,
                    result.GetErrorMessage(),
                    result.GetDetailedErrorMessage());
                response().setContentType("text/html");
                return internalServerError(errHtml);
            }
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
        File f = Play.application().getFile("internal/MapAgentForms/" + file);
        if (!f.exists())
            return notFound();
        else
            return ok(f);
    }
}