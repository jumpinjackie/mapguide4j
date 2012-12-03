package controllers;

import util.*;

import java.lang.Integer;
import java.lang.NumberFormatException;
import java.lang.String;
import java.util.Map;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public class MgLibraryResourceServiceController extends MgResourceServiceController {

    public static Result enumerateResources(String resourcePath, String format) {
        try {
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

            param.AddParameter("OPERATION", "ENUMERATERESOURCES");
            param.AddParameter("VERSION", "1.0.0");
            param.AddParameter("DEPTH", getRequestParameter("depth", "-1"));
            param.AddParameter("TYPE", getRequestParameter("type", ""));
            param.AddParameter("COMPUTECHILDREN", "1");

            if (fmt.equals("xml") || fmt.equals("html"))
                param.AddParameter("FORMAT", MgMimeType.Xml);
            else if (fmt.equals("json"))
                param.AddParameter("FORMAT", MgMimeType.Json);

            param.AddParameter("RESOURCEID", constructResourceIdString(MgRepositoryType.Library, resourcePath, true));

            TryFillMgCredentials(param);
            return executeRequestInternal(request);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result setResourceContent(String resourcePath) {
        return TODO;
    }

    public static Result setResourceHeader(String resourcePath) {
        return TODO;
    }

    public static Result setResourceData(String resourcePath, String dataName) {
        return TODO;
    }

    public static Result getResourceContent(String resourcePath, String format) {
        try {
            return MgResourceServiceController.getResourceContent(constructResourceIdString(MgRepositoryType.Library, resourcePath, false), format);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result getResourceHeader(String resourcePath, String format) {
        try {
            return MgResourceServiceController.getResourceHeader(constructResourceIdString(MgRepositoryType.Library, resourcePath, false), format);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result enumerateResourceData(String resourcePath, String format) {
        try {
            return MgResourceServiceController.enumerateResourceData(constructResourceIdString(MgRepositoryType.Library, resourcePath, false), format);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result getResourceData(String resourcePath, String dataName) {
        try {
            return MgResourceServiceController.getResourceData(constructResourceIdString(MgRepositoryType.Library, resourcePath, false), dataName);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result enumerateResourceReferences(String resourcePath, String format) {
        try {
            return MgResourceServiceController.enumerateResourceReferences(constructResourceIdString(MgRepositoryType.Library, resourcePath, false), format);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result deleteResourceData(String resourcePath, String dataName) {
        try {
            return MgResourceServiceController.deleteResourceData(constructResourceIdString(MgRepositoryType.Library, resourcePath, false), dataName);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result deleteResource(String resourcePath) {
        try {
            return MgResourceServiceController.deleteResource(constructResourceIdString(MgRepositoryType.Library, resourcePath, false));
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }
}