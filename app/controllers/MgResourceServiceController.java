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
        MgHttpRequestParam param = request.getRequestParam();

        param.addParameter("OPERATION", "GETRESOURCECONTENT");
        param.addParameter("VERSION", "1.0.0");
        if (fmt.equals("xml")) {
            param.addParameter("FORMAT", MgMimeType.Xml);
        }
        else if (fmt.equals("json")) {
            param.addParameter("FORMAT", MgMimeType.Json);
        }

        param.addParameter("RESOURCEID", resId);

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
        MgHttpRequestParam param = request.getRequestParam();

        param.addParameter("OPERATION", "GETRESOURCEHEADER");
        param.addParameter("VERSION", "1.0.0");
        if (fmt.equals("xml")) {
            param.addParameter("FORMAT", MgMimeType.Xml);
        }
        else if (fmt.equals("json")) {
            param.addParameter("FORMAT", MgMimeType.Json);
        }
        else if (fmt.equals("html")) {
            param.addParameter("FORMAT", MgMimeType.Xml);
            param.addParameter("XSLSTYLESHEET", "ResourceHeader.xsl");
        }

        param.addParameter("RESOURCEID", resId);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }

    protected static Result setResourceContent(String resId, File content) throws MgException {
        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.getRequestParam();

        param.addParameter("OPERATION", "SETRESOURCE");
        param.addParameter("VERSION", "1.0.0");
        param.addParameter("RESOURCEID", resId);

        if (content != null) {
            param.addParameter("CONTENT", content.getAbsolutePath());
            param.setParameterType("CONTENT", "tempfile");
        }

        TryFillMgCredentials(param);
        //Don't use executeRequestInternal() as the result is not REST-ful. We want 201 (created) instead of
        //200 (OK)
        MgHttpResponse response = request.execute();
        MgHttpResult result = response.getResult();
        if (result.getStatusCode() == 200) {
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
        MgHttpRequestParam param = request.getRequestParam();

        param.addParameter("OPERATION", "SETRESOURCE");
        param.addParameter("VERSION", "1.0.0");
        param.addParameter("RESOURCEID", resId);

        if (content != null) {
            param.addParameter("HEADER", content.getAbsolutePath());
            param.setParameterType("HEADER", "tempfile");
        }

        TryFillMgCredentials(param);
        //Don't use executeRequestInternal() as the result is not REST-ful. We want 201 (created) instead of
        //200 (OK)
        MgHttpResponse response = request.execute();
        MgHttpResult result = response.getResult();
        if (result.getStatusCode() == 200) {
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
        MgHttpRequestParam param = request.getRequestParam();

        param.addParameter("OPERATION", "SETRESOURCEDATA");
        param.addParameter("VERSION", "1.0.0");
        param.addParameter("RESOURCEID", resId);
        param.addParameter("DATANAME", dataName);
        param.addParameter("DATATYPE", dataType);

        if (content != null) {
            param.addParameter("DATA", content.getAbsolutePath());
            param.setParameterType("DATA", "tempfile");
        }

        TryFillMgCredentials(param);
        //Don't use executeRequestInternal() as the result is not REST-ful. We want 201 (created) instead of
        //200 (OK)
        MgHttpResponse response = request.execute();
        MgHttpResult result = response.getResult();
        if (result.getStatusCode() == 200) {
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
        MgHttpRequestParam param = request.getRequestParam();

        param.addParameter("OPERATION", "ENUMERATERESOURCEDATA");
        param.addParameter("VERSION", "1.0.0");
        if (fmt.equals("xml")) {
            param.addParameter("FORMAT", MgMimeType.Xml);
        }
        else if (fmt.equals("json")) {
            param.addParameter("FORMAT", MgMimeType.Json);
        }
        else if (fmt.equals("html")) {
            param.addParameter("FORMAT", MgMimeType.Xml);
            param.addParameter("XSLSTYLESHEET", "ResourceDataList.xsl");
        }

        param.addParameter("RESOURCEID", resId);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }

    protected static Result getResourceData(String resId, String dataName) throws MgException {
        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.getRequestParam();

        param.addParameter("OPERATION", "GETRESOURCEDATA");
        param.addParameter("VERSION", "1.0.0");
        param.addParameter("RESOURCEID", resId);
        param.addParameter("DATANAME", dataName);

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
        MgHttpRequestParam param = request.getRequestParam();

        param.addParameter("OPERATION", "ENUMERATERESOURCEREFERENCES");
        param.addParameter("VERSION", "1.0.0");

        if (fmt.equals("xml")) {
            param.addParameter("FORMAT", MgMimeType.Xml);
        }
        else if (fmt.equals("json")) {
            param.addParameter("FORMAT", MgMimeType.Json);
        }
        else if (fmt.equals("html")) {
            param.addParameter("FORMAT", MgMimeType.Xml);
            param.addParameter("XSLSTYLESHEET", "ResourceReferenceList.xsl");
        }

        param.addParameter("RESOURCEID", resId);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }

    protected static Result deleteResourceData(String resId, String dataName) throws MgException {
        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.getRequestParam();

        param.addParameter("OPERATION", "DELETERESOURCEDATA");
        param.addParameter("VERSION", "1.0.0");
        param.addParameter("RESOURCEID", resId);
        param.addParameter("DATANAME", dataName);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }

    protected static Result deleteResource(String resId) throws MgException {
        String uri = "";
        MgHttpRequest request = new MgHttpRequest(uri);
        MgHttpRequestParam param = request.getRequestParam();

        param.addParameter("OPERATION", "DELETERESOURCE");
        param.addParameter("VERSION", "1.0.0");
        param.addParameter("RESOURCEID", resId);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
    }
}