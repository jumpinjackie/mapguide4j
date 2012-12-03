package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

import java.lang.String;
import java.util.Map;
import java.util.Set;

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
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "GETFEATUREPROVIDERS");
            param.AddParameter("VERSION", "1.0.0");

            if (fmt.equals("xml")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.AddParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
                param.AddParameter("XSLSTYLESHEET", "FdoProviderList.xsl");
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
                String value = queryParams.get(name)[0];
                if (partialConnString.length() == 0) {
                    partialConnString = name + "=" + value;
                } else {
                    partialConnString += ";" + name + "=" + value;
                }
            }
            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "GETPROVIDERCAPABILITIES");
            param.AddParameter("VERSION", "2.0.0");
            param.AddParameter("PROVIDER", fdoProviderName);
            if (partialConnString.length() > 0)
                param.AddParameter("CONNECTIONSTRING", partialConnString);

            if (fmt.equals("xml")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.AddParameter("FORMAT", MgMimeType.Json);
            }
            //else if (fmt.equals("html")) {
            //    param.AddParameter("FORMAT", MgMimeType.Xml);
            //    param.AddParameter("XSLSTYLESHEET", "FdoProviderCapabilities.xsl");
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
                String value = queryParams.get(name)[0];
                if (partialConnString.length() == 0) {
                    partialConnString = name + "=" + value;
                } else {
                    partialConnString += ";" + name + "=" + value;
                }
            }

            String uri = "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "ENUMERATEDATASTORES");
            param.AddParameter("VERSION", "1.0.0");
            param.AddParameter("PROVIDER", fdoProviderName);
            if (partialConnString.length() > 0)
                param.AddParameter("CONNECTIONSTRING", partialConnString);

            if (fmt.equals("xml")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.AddParameter("FORMAT", MgMimeType.Json);
            }/*
            else if (fmt.equals("html")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
                param.AddParameter("XSLSTYLESHEET", "FdoDataStoreList.xsl");
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
                String value = queryParams.get(name)[0];
                if (partialConnString.length() == 0) {
                    partialConnString = name + "=" + value;
                } else {
                    partialConnString += ";" + name + "=" + value;
                }
            }
            String uri = "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "GETCONNECTIONPROPERTYVALUES");
            param.AddParameter("VERSION", "1.0.0");
            param.AddParameter("PROVIDER", fdoProviderName);
            param.AddParameter("PROPERTY", propName);
            if (partialConnString.length() > 0)
                param.AddParameter("CONNECTIONSTRING", partialConnString);

            if (fmt.equals("xml")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.AddParameter("FORMAT", MgMimeType.Json);
            }/*
            else if (fmt.equals("html")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
                param.AddParameter("XSLSTYLESHEET", "StringCollection.xsl");
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
}