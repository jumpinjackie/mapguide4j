package controllers;

import util.*;

import java.io.*;
import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

/**
 * Abstract REST controller for MapGuide Resource Service operations
 */
public abstract class MgResourceServiceController extends MgAbstractAuthenticatedController {
    protected static Result getResourceContent(String resId, String format) throws MgException {
        String fmt = format.toLowerCase();
        if (!fmt.equals("xml") &&
            !fmt.equals("json"))
        {
            return badRequest("Unsupported representation: " + format);
        }

        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.GetRequestParam();

        param.AddParameter("OPERATION", "GETRESOURCECONTENT");
        param.AddParameter("VERSION", "1.0.0");
        if (fmt.equals("xml")) {
            param.AddParameter("FORMAT", MgMimeType.Xml);
        }
        else if (fmt.equals("json")) {
            param.AddParameter("FORMAT", MgMimeType.Json);
        }

        param.AddParameter("RESOURCEID", resId);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }

    protected static Result getResourceHeader(String resId, String format) throws MgException {
        String fmt = format.toLowerCase();
        if (!fmt.equals("html") &&
            !fmt.equals("xml") &&
            !fmt.equals("json"))
        {
            return badRequest("Unsupported representation: " + format);
        }

        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.GetRequestParam();

        param.AddParameter("OPERATION", "GETRESOURCEHEADER");
        param.AddParameter("VERSION", "1.0.0");
        if (fmt.equals("xml")) {
            param.AddParameter("FORMAT", MgMimeType.Xml);
        }
        else if (fmt.equals("json")) {
            param.AddParameter("FORMAT", MgMimeType.Json);
        }
        else if (fmt.equals("html")) {
            param.AddParameter("FORMAT", MgMimeType.Xml);
            param.AddParameter("XSLSTYLESHEET", "ResourceHeader.xsl");
        }

        param.AddParameter("RESOURCEID", resId);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }

    protected static Result setResourceContent(String resId, File content) throws MgException {
        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.GetRequestParam();

        param.AddParameter("OPERATION", "SETRESOURCE");
        param.AddParameter("VERSION", "1.0.0");
        param.AddParameter("RESOURCEID", resId);

        if (content != null) {
            param.AddParameter("CONTENT", content.getAbsolutePath());
            param.SetParameterType("CONTENT", "tempfile");
        }

        TryFillMgCredentials(param);
        //Don't use executeRequestInternal() as the result is not REST-ful. We want 201 (created) instead of
        //200 (OK)
        MgHttpResponse response = request.Execute();
        MgHttpResult result = response.GetResult();
        if (result.GetStatusCode() == 200) {
            //TODO: If we want to be RESTfully pedantic we'd check the existence of this resource
            //first before executing and return a different status code
            return created(resId);
        } else {
            return mgHttpError(result);
        }
    }

    protected static Result setResourceHeader(String resId, File content) throws MgException {
        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.GetRequestParam();

        param.AddParameter("OPERATION", "SETRESOURCE");
        param.AddParameter("VERSION", "1.0.0");
        param.AddParameter("RESOURCEID", resId);

        if (content != null) {
            param.AddParameter("HEADER", content.getAbsolutePath());
            param.SetParameterType("HEADER", "tempfile");
        }

        TryFillMgCredentials(param);
        //Don't use executeRequestInternal() as the result is not REST-ful. We want 201 (created) instead of
        //200 (OK)
        MgHttpResponse response = request.Execute();
        MgHttpResult result = response.GetResult();
        if (result.GetStatusCode() == 200) {
            //TODO: If we want to be RESTfully pedantic we'd check the existence of this resource
            //first before executing and return a different status code
            return created(resId);
        } else {
            return mgHttpError(result);
        }
    }

    protected static Result setResourceData(String resId, String dataName, String dataType, File content) throws MgException {
        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.GetRequestParam();

        param.AddParameter("OPERATION", "SETRESOURCEDATA");
        param.AddParameter("VERSION", "1.0.0");
        param.AddParameter("RESOURCEID", resId);
        param.AddParameter("DATANAME", dataName);
        param.AddParameter("DATATYPE", dataType);

        if (content != null) {
            param.AddParameter("DATA", content.getAbsolutePath());
            param.SetParameterType("DATA", "tempfile");
        }

        TryFillMgCredentials(param);
        //Don't use executeRequestInternal() as the result is not REST-ful. We want 201 (created) instead of
        //200 (OK)
        MgHttpResponse response = request.Execute();
        MgHttpResult result = response.GetResult();
        if (result.GetStatusCode() == 200) {
            //TODO: If we want to be RESTfully pedantic we'd check the existence of this resource
            //first before executing and return a different status code
            return created(resId);
        } else {
            return mgHttpError(result);
        }
    }

    protected static Result enumerateResourceData(String resId, String format) throws MgException {
        String fmt = format.toLowerCase();
        if (!fmt.equals("html") &&
            !fmt.equals("xml") &&
            !fmt.equals("json"))
        {
            return badRequest("Unsupported representation: " + format);
        }

        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.GetRequestParam();

        param.AddParameter("OPERATION", "ENUMERATERESOURCEDATA");
        param.AddParameter("VERSION", "1.0.0");
        if (fmt.equals("xml")) {
            param.AddParameter("FORMAT", MgMimeType.Xml);
        }
        else if (fmt.equals("json")) {
            param.AddParameter("FORMAT", MgMimeType.Json);
        }
        else if (fmt.equals("html")) {
            param.AddParameter("FORMAT", MgMimeType.Xml);
            param.AddParameter("XSLSTYLESHEET", "ResourceDataList.xsl");
        }

        param.AddParameter("RESOURCEID", resId);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }

    protected static Result getResourceData(String resId, String dataName) throws MgException {
        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.GetRequestParam();

        param.AddParameter("OPERATION", "GETRESOURCEDATA");
        param.AddParameter("VERSION", "1.0.0");
        param.AddParameter("RESOURCEID", resId);
        param.AddParameter("DATANAME", dataName);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }

    protected static Result enumerateResourceReferences(String resId, String format) throws MgException {
        String fmt = format.toLowerCase();
        if (!fmt.equals("html") &&
            !fmt.equals("xml") &&
            !fmt.equals("json"))
        {
            return badRequest("Unsupported representation: " + format);
        }

        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.GetRequestParam();

        param.AddParameter("OPERATION", "ENUMERATERESOURCEREFERENCES");
        param.AddParameter("VERSION", "1.0.0");

        if (fmt.equals("xml")) {
            param.AddParameter("FORMAT", MgMimeType.Xml);
        }
        else if (fmt.equals("json")) {
            param.AddParameter("FORMAT", MgMimeType.Json);
        }
        else if (fmt.equals("html")) {
            param.AddParameter("FORMAT", MgMimeType.Xml);
            param.AddParameter("XSLSTYLESHEET", "ResourceReferenceList.xsl");
        }

        param.AddParameter("RESOURCEID", resId);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }

    protected static Result deleteResourceData(String resId, String dataName) throws MgException {
        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.GetRequestParam();

        param.AddParameter("OPERATION", "DELETERESOURCEDATA");
        param.AddParameter("VERSION", "1.0.0");
        param.AddParameter("RESOURCEID", resId);
        param.AddParameter("DATANAME", dataName);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }

    protected static Result deleteResource(String resId) throws MgException {
        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.GetRequestParam();

        param.AddParameter("OPERATION", "DELETERESOURCE");
        param.AddParameter("VERSION", "1.0.0");
        param.AddParameter("RESOURCEID", resId);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }
}