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
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "GETSCHEMAS");
            param.AddParameter("VERSION", "1.0.0");
            param.AddParameter("RESOURCEID", fsId);

            if (fmt.equals("xml")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.AddParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
                param.AddParameter("XSLSTYLESHEET", "FeatureSchemaNameList.xsl");
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
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "GETSPATIALCONTEXTS");
            param.AddParameter("VERSION", "1.0.0");
            param.AddParameter("RESOURCEID", fsId);
            param.AddParameter("ACTIVEONLY", "0");

            if (fmt.equals("xml") || fmt.equals("html"))
                param.AddParameter("FORMAT", MgMimeType.Xml);
            else if (fmt.equals("json"))
                param.AddParameter("FORMAT", MgMimeType.Json);

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
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "GETCLASSES");
            param.AddParameter("VERSION", "1.0.0");
            param.AddParameter("RESOURCEID", fsId);
            param.AddParameter("SCHEMA", schemaName);

            if (fmt.equals("xml") || fmt.equals("html"))
                param.AddParameter("FORMAT", MgMimeType.Xml);
            else if (fmt.equals("json"))
                param.AddParameter("FORMAT", MgMimeType.Json);

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
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "DESCRIBEFEATURESCHEMA");
            param.AddParameter("VERSION", "1.0.0");
            param.AddParameter("RESOURCEID", fsId);
            param.AddParameter("SCHEMA", schemaName);

            if (fmt.equals("xml")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.AddParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
                param.AddParameter("XSLSTYLESHEET", "FeatureSchema.xsl");
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
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "DESCRIBEFEATURESCHEMA");
            param.AddParameter("VERSION", "1.0.0");
            param.AddParameter("RESOURCEID", fsId);
            param.AddParameter("SCHEMA", schemaName);
            param.AddParameter("CLASSNAMES", className);

            if (fmt.equals("xml") || fmt.equals("html"))
                param.AddParameter("FORMAT", MgMimeType.Xml);
            else if (fmt.equals("json"))
                param.AddParameter("FORMAT", MgMimeType.Json);

            return executeRequestInternal(request);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }
    }

    protected static Result deleteClasses(String repoType, String resourcePath, String schemaName, String classNames) {
        /*
        try {
            String[] classes = classNames.split(",");
            MgStringCollection deletedClasses = new MgStringCollection();
            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);

            MgFeatureSchemaCollection schemas = featSvc.DescribeSchema(fsId, schemaName);
            int schemaCount = schemas.GetCount();
            for (int i = 0; i < schemaCount; i++) {
                MgFeatureSchema theSchema = schemas.GetItem(i);
                if (theSchema.GetName().equals(schemaName)) {
                    MgFeatureSchema applyMe = new MgFeatureSchema(theSchema.GetName(), theSchema.GetDescription());
                    MgClassDefinitionCollection deleteMe = applyMe.GetClasses();

                    //Find matching classes and mark them for deletion
                    MgClassDefinitionCollection classDefs = theSchema.GetClasses();
                    int clsCount = classDefs.GetCount();
                    for (int j = 0; j < clsCount; j++) {
                        MgClassDefinition clsDef = classDefs.GetItem(j);
                        String clsName = clsDef.GetName();
                        for (String name : classes) {
                            if (name.equals(clsName)) {
                                deleteMe.Add(clsDef);
                                Logger.debug("Class marked for deletion: " + clsName);
                                clsDef.Delete(); //Mark for deletion
                                deletedClasses.Add(name);
                                break;
                            }
                        }
                    }

                    if (deletedClasses.GetCount() > 0) {
                        Logger.debug("Apply Schema");
                        featSvc.ApplySchema(fsId, applyMe);
                    }
                    return mgStringCollectionXml(deletedClasses);
                }
            }
            return internalServerError("Schema not found: " + schemaName);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }*/
        return TODO;
    }

    protected static Result deleteSchema(String repoType, String resourcePath, String schemaName) {
        /*
        try {
            MgStringCollection deletedClasses = new MgStringCollection();
            MgResourceIdentifier fsId = constructResourceId(repoType, resourcePath);
            MgSiteConnection siteConn = createMapGuideConnection();
            MgFeatureService featSvc = (MgFeatureService)siteConn.CreateService(MgServiceType.FeatureService);

            MgFeatureSchemaCollection schemas = featSvc.DescribeSchema(fsId, schemaName);
            int schemaCount = schemas.GetCount();
            for (int i = 0; i < schemaCount; i++) {
                MgFeatureSchema theSchema = schemas.GetItem(i);
                if (theSchema.GetName().equals(schemaName)) {
                    Logger.debug("Schema marked for deletion: " + schemaName);
                    theSchema.Delete(); //Mark as deleted
                    Logger.debug("Apply Schema");
                    featSvc.ApplySchema(fsId, theSchema);
                    return ok(schemaName);
                }
            }
            return internalServerError("Schema not found: " + schemaName);
        } catch (MgException ex) {
            return mgServerError(ex);
        } catch (Exception ex) {
            return javaException(ex);
        }*/
        return TODO;
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
        } catch (UnsupportedEncodingException ex) {
            return javaException(ex);
        }
    }
}