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

            //TODO: Support:
            // - Computed Properties
            // - Ordering
            // - pagination

            if (queryParams.get("properties") != null) {
                String[] names = queryParams.get("properties")[0].split(",");
                for (String propName : names) {
                    query.AddFeatureProperty(propName);
                }
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

            MgCoordinateSystemTransform transform = null;
            if (queryParams.get("transformto") != null) {
                String csCode = queryParams.get("transformto")[0];
                MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
                MgCoordinateSystem targetCs = csFactory.CreateFromCode(csCode);
                MgClassDefinition clsDef = featSvc.GetClassDefinition(fsId, schemaName, className);
                //Has a designated Geometry property, we'll use its spatial context
                if (!clsDef.GetDefaultGeometryPropertyName().equals("")) {
                    MgPropertyDefinitionCollection props = clsDef.GetProperties();
                    int idx = props.IndexOf(clsDef.GetDefaultGeometryPropertyName());
                    if (idx >= 0) {
                        MgGeometricPropertyDefinition geomProp = (MgGeometricPropertyDefinition)props.GetItem(idx);
                        String scName = geomProp.GetSpatialContextAssociation();
                        MgSpatialContextReader scReader = featSvc.GetSpatialContexts(fsId, false);
                        try {
                            while(scReader.ReadNext()) {
                                //Found the matching spatial context, create a MgCoordinateSystem from its wkt
                                if (scReader.GetName().equals(scName)) {
                                    MgCoordinateSystem sourceCs = csFactory.Create(scReader.GetCoordinateSystemWkt());
                                    transform = csFactory.GetTransform(sourceCs, targetCs);
                                    break;
                                }
                            }
                        }
                        finally {
                            scReader.Close();
                        }
                    }
                }
            }

            MgFeatureReader reader = featSvc.SelectFeatures(fsId, schemaName + ":" + className, query);
            MgFeatureSetChunkedResult result = new MgFeatureSetChunkedResult(featSvc, reader, limit);
            if (transform != null)
                result.setTransform(transform);
            response().setContentType("text/xml");
            return ok(result);
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}