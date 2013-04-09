package controllers;

import util.*;

import play.*;
import play.mvc.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.ArrayList;

import org.osgeo.mapguide.*;

/**
 * Abstract REST controller for MapGuide Feature Service operations
 */
public abstract class MgFeatureServiceController extends MgAbstractAuthenticatedController {

    protected static Result getSchemaNames(String repoType, String resourcePath, String format) {
        try {
            String fmt = format.toLowerCase();
            if (!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String fsId = constructResourceIdString(repoType, resourcePath, false);

            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "GETSCHEMAS");
            param.addParameter("VERSION", "1.0.0");
            param.addParameter("RESOURCEID", fsId);

            if (fmt.equals("xml")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.addParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
                param.addParameter("XSLSTYLESHEET", "FeatureSchemaNameList.xsl");
            }

            return executeRequestInternal(request);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    protected static Result getSpatialContexts(String repoType, String resourcePath, String format) {
        try {
            String fmt = format.toLowerCase();
            if (!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String fsId = constructResourceIdString(repoType, resourcePath, false);

            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "GETSPATIALCONTEXTS");
            param.addParameter("VERSION", "1.0.0");
            param.addParameter("RESOURCEID", fsId);
            param.addParameter("ACTIVEONLY", "0");

            if (fmt.equals("xml") || fmt.equals("html"))
                param.addParameter("FORMAT", MgMimeType.Xml);
            else if (fmt.equals("json"))
                param.addParameter("FORMAT", MgMimeType.Json);

            return executeRequestInternal(request);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    protected static Result getClassNames(String repoType, String resourcePath, String schemaName, String format) {
        try {
            String fmt = format.toLowerCase();
            if (!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String fsId = constructResourceIdString(repoType, resourcePath, false);

            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "GETCLASSES");
            param.addParameter("VERSION", "1.0.0");
            param.addParameter("RESOURCEID", fsId);
            param.addParameter("SCHEMA", schemaName);

            if (fmt.equals("xml") || fmt.equals("html"))
                param.addParameter("FORMAT", MgMimeType.Xml);
            else if (fmt.equals("json"))
                param.addParameter("FORMAT", MgMimeType.Json);

            return executeRequestInternal(request);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    protected static Result getFeatureSchema(String repoType, String resourcePath, String schemaName, String format) {
        try {
            String fmt = format.toLowerCase();
            if (!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String fsId = constructResourceIdString(repoType, resourcePath, false);

            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "DESCRIBEFEATURESCHEMA");
            param.addParameter("VERSION", "1.0.0");
            param.addParameter("RESOURCEID", fsId);
            param.addParameter("SCHEMA", schemaName);

            if (fmt.equals("xml")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.addParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
                param.addParameter("XSLSTYLESHEET", "FeatureSchema.xsl");
            }

            return executeRequestInternal(request);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    protected static Result getClassDefinition(String repoType, String resourcePath, String schemaName, String className, String format) {
        try {
            String fmt = format.toLowerCase();
            if (!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String fsId = constructResourceIdString(repoType, resourcePath, false);

            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "DESCRIBEFEATURESCHEMA");
            param.addParameter("VERSION", "1.0.0");
            param.addParameter("RESOURCEID", fsId);
            param.addParameter("SCHEMA", schemaName);
            param.addParameter("CLASSNAMES", className);

            if (fmt.equals("xml") || fmt.equals("html"))
                param.addParameter("FORMAT", MgMimeType.Xml);
            else if (fmt.equals("json"))
                param.addParameter("FORMAT", MgMimeType.Json);

            return executeRequestInternal(request);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    protected static Result deleteClasses(String repoType, String resourcePath, String schemaName, String classNames) {
        //NOTE: This is currently fubar due to:
        //http://trac.osgeo.org/mapguide/ticket/2198
        try {
            String[] classes = classNames.split(",");
            MgStringCollection deletedClasses = new MgStringCollection();
            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.createService(MgServiceType.FeatureService);

            MgFeatureSchemaCollection schemas = featSvc.describeSchema(fsId, schemaName);
            int schemaCount = schemas.getCount();
            for (int i = 0; i < schemaCount; i++) {
                MgFeatureSchema theSchema = schemas.getItem(i);
                if (theSchema.getName().equals(schemaName)) {
                    MgFeatureSchema applyMe = new MgFeatureSchema(theSchema.getName(), theSchema.getDescription());
                    MgClassDefinitionCollection deleteMe = applyMe.getClasses();

                    //Find matching classes and mark them for deletion
                    MgClassDefinitionCollection classDefs = theSchema.getClasses();
                    int clsCount = classDefs.getCount();
                    for (int j = 0; j < clsCount; j++) {
                        MgClassDefinition clsDef = classDefs.getItem(j);
                        String clsName = clsDef.getName();
                        for (String name : classes) {
                            if (name.equals(clsName)) {
                                deleteMe.add(clsDef);
                                Logger.debug("Class marked for deletion: " + clsName);
                                clsDef.markAsDeleted(); //Mark for deletion
                                deletedClasses.add(name);
                                break;
                            }
                        }
                    }

                    if (deletedClasses.getCount() > 0) {
                        Logger.debug("Apply Schema");
                        featSvc.applySchema(fsId, applyMe);
                    }
                    return mgStringCollectionXml(deletedClasses);
                }
            }
            return internalServerError("Schema not found: " + schemaName);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    protected static Result deleteSchema(String repoType, String resourcePath, String schemaName) {
        //NOTE: This is currently fubar due to:
        //http://trac.osgeo.org/mapguide/ticket/2198
        try {
            MgStringCollection deletedClasses = new MgStringCollection();
            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.createService(MgServiceType.FeatureService);

            MgFeatureSchemaCollection schemas = featSvc.describeSchema(fsId, schemaName);
            int schemaCount = schemas.getCount();
            for (int i = 0; i < schemaCount; i++) {
                MgFeatureSchema theSchema = schemas.getItem(i);
                if (theSchema.getName().equals(schemaName)) {
                    Logger.debug("Schema marked for deletion: " + schemaName);
                    theSchema.markAsDeleted(); //Mark as deleted
                    Logger.debug("Apply Schema");
                    featSvc.applySchema(fsId, theSchema);
                    return ok(schemaName);
                }
            }
            return internalServerError("Schema not found: " + schemaName);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    protected static Result selectFeatures(String repoType, String resourcePath, String schemaName, String className, String format) {
        try {
            String fmt = format.toLowerCase();
            if (!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.createService(MgServiceType.FeatureService);

            MgFeatureQueryOptions query = new MgFeatureQueryOptions();
            Map<String, String[]> queryParams = request().queryString();

            //TODO: Support:
            // - Computed Properties
            // - Ordering
            // - pagination

            if (queryParams.get("properties") != null) {
                String[] names = queryParams.get("properties")[0].split(",");
                for (String propName : names) {
                    query.addFeatureProperty(propName);
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
                query.setFilter(finalFilter);
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
                MgCoordinateSystem targetCs = csFactory.createFromCode(csCode);
                MgClassDefinition clsDef = featSvc.getClassDefinition(fsId, schemaName, className);
                //Has a designated Geometry property, we'll use its spatial context
                if (!clsDef.getDefaultGeometryPropertyName().equals("")) {
                    MgPropertyDefinitionCollection props = clsDef.getProperties();
                    int idx = props.indexOf(clsDef.getDefaultGeometryPropertyName());
                    if (idx >= 0) {
                        MgGeometricPropertyDefinition geomProp = (MgGeometricPropertyDefinition)props.getItem(idx);
                        String scName = geomProp.getSpatialContextAssociation();
                        MgSpatialContextReader scReader = featSvc.getSpatialContexts(fsId, false);
                        try {
                            while(scReader.readNext()) {
                                //Found the matching spatial context, create a MgCoordinateSystem from its wkt
                                if (scReader.getName().equals(scName)) {
                                    MgCoordinateSystem sourceCs = csFactory.create(scReader.getCoordinateSystemWkt());
                                    transform = csFactory.getTransform(sourceCs, targetCs);
                                    break;
                                }
                            }
                        }
                        finally {
                            scReader.close();
                        }
                    }
                }
            }

            MgFeatureReader reader = featSvc.selectFeatures(fsId, schemaName + ":" + className, query);
            MgFeatureSetChunkedResult result = new MgFeatureSetChunkedResult(featSvc, reader, limit);
            if (transform != null)
                result.setTransform(transform);
            response().setContentType("text/xml");
            return ok(result);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (UnsupportedEncodingException ex) {
            return javaException(ex);
        }
    }
}