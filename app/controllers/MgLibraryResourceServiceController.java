package controllers;

import util.*;

import java.io.*;
import java.lang.Integer;
import java.lang.NumberFormatException;
import java.lang.String;
import java.util.Map;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

/**
 * REST controller for MapGuide Resource Service operations on site repository resources
 */
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
            param.AddParameter("TYPE", getRequestParameter("type", ""));
            param.AddParameter("COMPUTECHILDREN", "1");

            String strDepth = getRequestParameter("depth", "");
            if (fmt.equals("xml")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.AddParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
                param.AddParameter("XSLSTYLESHEET", "ResourceList.xsl");

                //No depth specified, then set to 1. As for html we want a single level view
                if (strDepth.equals(""))
                    strDepth = "1";
            }

            if (strDepth.equals(""))
                strDepth = "-1";
            param.AddParameter("DEPTH", strDepth);
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
        try {
            String resId = constructResourceIdString(MgRepositoryType.Library, resourcePath, false);
            //HACK: We'll need to revise the routing here in the future, as we are *obviously* assuming that user
            //will never have or create a folder resource id with a leaf folder name of the form <name>.<resource type>.
            //For now we consider this a corner case that we hope will never happen. Such folders will be interpreted as documents
            if (!isResourceDocument(resId)) {
                resId += "/";
                //We're emulating the same behavior as the mapagent. That is, folder creation is just a SETRESOURCE
                //call on the folder resource id without any content or header
                return MgResourceServiceController.setResourceContent(resId, null);
            } else {
                File file = request().body().asRaw().asFile();
                return MgResourceServiceController.setResourceContent(resId, file);
            }
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result setResourceHeader(String resourcePath) {
        try {
            String resId = constructResourceIdString(MgRepositoryType.Library, resourcePath, false);
            //HACK: We'll need to revise the routing here in the future, as we are *obviously* assuming that user
            //will never have or create a folder resource id with a leaf folder name of the form <name>.<resource type>.
            //For now we consider this a corner case that we hope will never happen. Such folders will be interpreted as documents
            if (!isResourceDocument(resId)) {
                resId += "/";
            }
            File file = request().body().asRaw().asFile();
            return MgResourceServiceController.setResourceHeader(resId, file);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result setResourceData(String resourcePath, String dataName) {
        try {
            String resId = constructResourceIdString(MgRepositoryType.Library, resourcePath, false);
            //HACK: We'll need to revise the routing here in the future, as we are *obviously* assuming that user
            //will never have or create a folder resource id with a leaf folder name of the form <name>.<resource type>.
            //For now we consider this a corner case that we hope will never happen. Such folders will be interpreted as documents
            if (!isResourceDocument(resId)) {
                resId += "/";
            }
            File file = request().body().asRaw().asFile();
            return MgResourceServiceController.setResourceData(resId, dataName, "File", file);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
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