package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

import java.lang.String;
import java.util.Map;
import java.util.Set;

public class MgLibraryFeatureServiceController extends MgFeatureServiceController {

    public static Result getFeatureProviders() {
        try {
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            MgByteReader providersContent = featSvc.GetFeatureProviders();
            response().setContentType(providersContent.GetMimeType());
            return ok(providersContent.ToString());
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getProviderCapabilities(String fdoProviderName) {
        try {
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
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            MgByteReader capabilitiesContent = featSvc.GetCapabilities(fdoProviderName, partialConnString);
            response().setContentType(capabilitiesContent.GetMimeType());
            return ok(capabilitiesContent.ToString());
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result enumerateDataStores(String fdoProviderName) {
        try {
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
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            MgByteReader dataStoresContent = featSvc.EnumerateDataStores(fdoProviderName, partialConnString);
            response().setContentType(dataStoresContent.GetMimeType());
            return ok(dataStoresContent.ToString());
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getConnectPropertyValues(String fdoProviderName, String propName) {
        try {
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
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            MgStringCollection propVals = featSvc.GetConnectionPropertyValues(fdoProviderName, propName, partialConnString);
            return mgStringCollectionXml(propVals);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result selectFeatures(String resourcePath, String schemaName, String className) {
        return selectFeatures(MgRepositoryType.Library, resourcePath, schemaName, className);
    }

    public static Result getSchemaNames(String resourcePath) {
        return getSchemaNames(MgRepositoryType.Library, resourcePath);
    }

    public static Result getSpatialContexts(String resourcePath) {
        return getSpatialContexts(MgRepositoryType.Library, resourcePath);
    }

    public static Result getClassNames(String resourcePath, String schemaName) {
        return getClassNames(MgRepositoryType.Library, resourcePath, schemaName);
    }

    public static Result getClassDefinition(String resourcePath, String schemaName, String className) {
        return getClassDefinition(MgRepositoryType.Library, resourcePath, schemaName, className);
    }

    public static Result getFeatureSchema(String resourcePath, String schemaName) {
        return getFeatureSchema(MgRepositoryType.Library, resourcePath, schemaName);
    }
}