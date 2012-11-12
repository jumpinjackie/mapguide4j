package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public abstract class MgFeatureServiceController extends MgAbstractAuthenticatedController {

    protected static Result enumerateDataStores(String repoType, String resourcePath) {
        try {
            MgResourceIdentifier fsId = ConstructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = CreateMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            MgStringCollection schemaNames = featSvc.GetSchemas(fsId);
            return mgStringCollectionXml(schemaNames);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result getSchemaNames(String repoType, String resourcePath) {
        try {
            MgResourceIdentifier fsId = ConstructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = CreateMapGuideConnection();
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
            MgResourceIdentifier fsId = ConstructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = CreateMapGuideConnection();
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
            MgResourceIdentifier fsId = ConstructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = CreateMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);
            MgStringCollection classNames = featSvc.GetClasses(fsId, schemaName);
            return mgStringCollectionXml(classNames);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    protected static Result getFeatureSchema(String repoType, String resourcePath, String schemaName) {
        try {
            MgResourceIdentifier fsId = ConstructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = CreateMapGuideConnection();
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
            MgResourceIdentifier fsId = ConstructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = CreateMapGuideConnection();
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
}