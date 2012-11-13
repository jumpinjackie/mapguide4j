package controllers;

import util.*;

import play.*;
import play.mvc.*;

import java.util.Map;

import org.osgeo.mapguide.*;

public abstract class MgFeatureServiceController extends MgAbstractAuthenticatedController {

    protected static Result getSchemaNames(String repoType, String resourcePath) {
        try {
            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            MgStringCollection schemaNames = featSvc.GetSchemas(fsId);
            return mgStringCollectionXml(schemaNames);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result getSpatialContexts(String repoType, String resourcePath) {
        MgSpatialContextReader spatialContexts = null;
        try {
            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            spatialContexts = featSvc.GetSpatialContexts(fsId, false);
            return mgSpatialContextReaderXml(spatialContexts);
        } catch (MgException ex) {
            return mgServerError(ex);
        } finally {
            try {
                if (spatialContexts != null) {
                    spatialContexts.Close();
                }
            } catch (MgException ex) {
                return mgServerError(ex);
            }
        }
    }

    protected static Result getClassNames(String repoType, String resourcePath, String schemaName) {
        try {
            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            MgStringCollection classNames = featSvc.GetClasses(fsId, schemaName);
            return mgStringCollectionXml(classNames);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result getFeatureSchema(String repoType, String resourcePath, String schemaName) {
        try {
            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            String schemaXml = featSvc.DescribeSchemaAsXml(fsId, schemaName, null);
            response().setContentType(MgMimeType.Xml);
            return ok(schemaXml);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result getClassDefinition(String repoType, String resourcePath, String schemaName, String className) {
        try {
            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            MgStringCollection classNames = new MgStringCollection();
            classNames.Add(className);
            String schemaXml = featSvc.DescribeSchemaAsXml(fsId, schemaName, classNames);
            response().setContentType(MgMimeType.Xml);
            return ok(schemaXml);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result selectFeatures(String repoType, String resourcePath, String schemaName, String className) {
        try {
            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            
            MgFeatureQueryOptions query = new MgFeatureQueryOptions();
            Map<String, String[]> queryParams = request().queryString();
            if (queryParams.get("properties") != null) {
                String[] names = queryParams.get("properties")[0].split(",");
                for (String propName : names) {
                    query.AddFeatureProperty(propName);
                }
            }
            if (queryParams.get("aliases") != null && queryParams.get("expressions") != null) {
                String[] names = queryParams.get("aliases")[0].split(",");
                String[] values = queryParams.get("expressions")[0].split(",");
            }
            String finalFilter = "";
            if (queryParams.get("filter") != null) {
                finalFilter = queryParams.get("filter")[0];
            }
            if (queryParams.get("spatialfilter") != null) {
                if (finalFilter.length() > 0) {
                    finalFilter += " AND " + queryParams.get("spatialfilter")[0];
                } else {
                    finalFilter = queryParams.get("spatialfilter")[0];
                }
            }
            if (finalFilter.length() > 0) {
                query.SetFilter(finalFilter);
            }
            int limit = -1;
            if (queryParams.get("maxfeatures") != null) {
                try {
                    limit = Integer.parseInt(queryParams.get("maxfeatures")[0]);
                } catch (NumberFormatException e) {
                    return badRequest(e.getMessage());
                }
            }
            MgFeatureReader reader = featSvc.SelectFeatures(fsId, schemaName + ":" + className, query);
            MgFeatureSetChunkedResult result = new MgFeatureSetChunkedResult(featSvc, reader, limit);
            response().setContentType("text/xml");
            return ok(result);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}