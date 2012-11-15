package controllers;

import actions.*;
import util.*;
import play.*;
import play.mvc.*;

import java.io.File;
import java.lang.StringBuilder;
import java.util.Map;

import org.osgeo.mapguide.*;

public abstract class MgMapAgentCompatibilityController extends MgAbstractController {

    @MgCheckSession
    public static Result processGetRequest() {
        String uri =  controllers.routes.MgMapAgentCompatibilityController.processGetRequest().absoluteURL(request());
        try {
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.GetRequestParam();
            populateGetRequest(param);
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
                    return unauthorized();
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
        catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    private static void populateGetRequest(MgHttpRequestParam param) throws MgException {
        Map<String, String[]> query = request().queryString();
        if (query != null) {
            for (String name : query.keySet()) {
                param.AddParameter(name, query.get(name)[0]);
            }
        }
        if (session().get(MgCheckSessionAction.MAPGUIDE_SESSION_ID_KEY) != null && !param.ContainsParameter("SESSION")) {
            param.AddParameter("SESSION", session().get(MgCheckSessionAction.MAPGUIDE_SESSION_ID_KEY));
        }
    }

     private static void populatePostRequest(MgHttpRequestParam param) throws MgException {
        Map<String, String[]> query = request().body().asFormUrlEncoded();
        if (query != null) {
            for (String name : query.keySet()) {
                param.AddParameter(name, query.get(name)[0]);
            }
        }
        if (session().get(MgCheckSessionAction.MAPGUIDE_SESSION_ID_KEY) != null && !param.ContainsParameter("SESSION")) {
            param.AddParameter("SESSION", session().get(MgCheckSessionAction.MAPGUIDE_SESSION_ID_KEY));
        }
    }

    @MgCheckSession
    public static Result processPostRequest() {
        String uri =  controllers.routes.MgMapAgentCompatibilityController.processGetRequest().absoluteURL(request());

        try {
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.GetRequestParam();
            populatePostRequest(param);
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
                    return unauthorized();
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
        catch (MgException ex) {
            return mgServerError(ex);
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