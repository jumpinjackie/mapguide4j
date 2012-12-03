package controllers;

import util.*;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public abstract class MgResourceServiceController extends MgAbstractAuthenticatedController {
    protected static Result getResourceContent(String resId, String format) throws MgException {
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

        param.AddParameter("OPERATION", "GETRESOURCECONTENT");
        param.AddParameter("VERSION", "1.0.0");
        if (fmt.equals("xml") || fmt.equals("html"))
            param.AddParameter("FORMAT", MgMimeType.Xml);
        else if (fmt.equals("json"))
            param.AddParameter("FORMAT", MgMimeType.Json);

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
        if (fmt.equals("xml") || fmt.equals("html"))
            param.AddParameter("FORMAT", MgMimeType.Xml);
        else if (fmt.equals("json"))
            param.AddParameter("FORMAT", MgMimeType.Json);

        param.AddParameter("RESOURCEID", resId);

        TryFillMgCredentials(param);
        return executeRequestInternal(request);
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
        if (fmt.equals("xml") || fmt.equals("html"))
            param.AddParameter("FORMAT", MgMimeType.Xml);
        else if (fmt.equals("json"))
            param.AddParameter("FORMAT", MgMimeType.Json);

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
        if (fmt.equals("xml") || fmt.equals("html"))
            param.AddParameter("FORMAT", MgMimeType.Xml);
        else if (fmt.equals("json"))
            param.AddParameter("FORMAT", MgMimeType.Json);

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