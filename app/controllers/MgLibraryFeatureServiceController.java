package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

import java.lang.String;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for MapGuide Feature Service operations on site repository resources
 */
public class MgLibraryFeatureServiceController extends MgFeatureServiceController {

    public static Result getFeatureProviders(String format) {
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
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "GETFEATUREPROVIDERS");
            param.addParameter("VERSION", "1.0.0");

            if (fmt.equals("xml")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.addParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
                param.addParameter("XSLSTYLESHEET", "FdoProviderList.xsl");
            }

            return executeRequestInternal(request);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result getProviderCapabilities(String fdoProviderName, String format) {
        try {
            String fmt = format.toLowerCase();
            if (//!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String partialConnString = "";
            Map<String, String[]> queryParams = request().queryString();
            Set<String> queryPropNames = queryParams.keySet();
            for (String name : queryPropNames) {
                //HACK: In the very infinitsimally small case that there is an FDO connection property named "session", this will obviously break down
                if (name.toLowerCase().equals("session"))
                    continue;

                String value = queryParams.get(name)[0];
                if (partialConnString.length() == 0) {
                    partialConnString = name + "=" + value;
                } else {
                    partialConnString += ";" + name + "=" + value;
                }
            }
            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "GETPROVIDERCAPABILITIES");
            param.addParameter("VERSION", "2.0.0");
            param.addParameter("PROVIDER", fdoProviderName);
            if (partialConnString.length() > 0)
                param.addParameter("CONNECTIONSTRING", partialConnString);

            if (fmt.equals("xml")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.addParameter("FORMAT", MgMimeType.Json);
            }
            //else if (fmt.equals("html")) {
            //    param.addParameter("FORMAT", MgMimeType.Xml);
            //    param.addParameter("XSLSTYLESHEET", "FdoProviderCapabilities.xsl");
            //}

            return executeRequestInternal(request);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result enumerateDataStores(String fdoProviderName, String format) {
        try {
            String fmt = format.toLowerCase();
            if (//!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String partialConnString = "";
            Map<String, String[]> queryParams = request().queryString();
            Set<String> queryPropNames = queryParams.keySet();
            for (String name : queryPropNames) {
                //HACK: In the very infinitsimally small case that there is an FDO connection property named "session", this will obviously break down
                if (name.toLowerCase().equals("session"))
                    continue;

                String value = queryParams.get(name)[0];
                if (partialConnString.length() == 0) {
                    partialConnString = name + "=" + value;
                } else {
                    partialConnString += ";" + name + "=" + value;
                }
            }

            String uri = "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "ENUMERATEDATASTORES");
            param.addParameter("VERSION", "1.0.0");
            param.addParameter("PROVIDER", fdoProviderName);
            if (partialConnString.length() > 0)
                param.addParameter("CONNECTIONSTRING", partialConnString);

            if (fmt.equals("xml")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.addParameter("FORMAT", MgMimeType.Json);
            }/*
            else if (fmt.equals("html")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
                param.addParameter("XSLSTYLESHEET", "FdoDataStoreList.xsl");
            }*/

            return executeRequestInternal(request);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result getConnectPropertyValues(String fdoProviderName, String propName, String format) {
        try {
            String fmt = format.toLowerCase();
            if (//!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String partialConnString = "";
            Map<String, String[]> queryParams = request().queryString();
            Set<String> queryPropNames = queryParams.keySet();
            for (String name : queryPropNames) {
                //HACK: In the very infinitsimally small case that there is an FDO connection property named "session", this will obviously break down
                if (name.toLowerCase().equals("session"))
                    continue;

                String value = queryParams.get(name)[0];
                if (partialConnString.length() == 0) {
                    partialConnString = name + "=" + value;
                } else {
                    partialConnString += ";" + name + "=" + value;
                }
            }
            String uri = "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "GETCONNECTIONPROPERTYVALUES");
            param.addParameter("VERSION", "1.0.0");
            param.addParameter("PROVIDER", fdoProviderName);
            param.addParameter("PROPERTY", propName);
            if (partialConnString.length() > 0)
                param.addParameter("CONNECTIONSTRING", partialConnString);

            if (fmt.equals("xml")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.addParameter("FORMAT", MgMimeType.Json);
            }/*
            else if (fmt.equals("html")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
                param.addParameter("XSLSTYLESHEET", "StringCollection.xsl");
            }*/

            return executeRequestInternal(request);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result selectFeatures(String resourcePath, String schemaName, String className, String format) {
        return MgFeatureServiceController.selectFeatures(MgRepositoryType.Library, resourcePath, schemaName, className, format);
    }

    public static Result getSchemaNames(String resourcePath, String format) {
        return MgFeatureServiceController.getSchemaNames(MgRepositoryType.Library, resourcePath, format);
    }

    public static Result getSpatialContexts(String resourcePath, String format) {
        return MgFeatureServiceController.getSpatialContexts(MgRepositoryType.Library, resourcePath, format);
    }

    public static Result getClassNames(String resourcePath, String schemaName, String format) {
        return MgFeatureServiceController.getClassNames(MgRepositoryType.Library, resourcePath, schemaName, format);
    }

    public static Result getClassDefinition(String resourcePath, String schemaName, String className, String format) {
        return MgFeatureServiceController.getClassDefinition(MgRepositoryType.Library, resourcePath, schemaName, className, format);
    }

    public static Result getFeatureSchema(String resourcePath, String schemaName, String format) {
        return MgFeatureServiceController.getFeatureSchema(MgRepositoryType.Library, resourcePath, schemaName, format);
    }

    public static Result deleteClasses(String resourcePath, String schemaName, String classNames) {
        return MgFeatureServiceController.deleteClasses(MgRepositoryType.Library, resourcePath, schemaName, classNames);
    }

    public static Result deleteSchema(String resourcePath, String schemaName) {
        return MgFeatureServiceController.deleteSchema(MgRepositoryType.Library, resourcePath, schemaName);
    }
}