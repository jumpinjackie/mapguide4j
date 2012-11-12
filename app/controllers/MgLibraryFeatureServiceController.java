package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public class MgLibraryFeatureServiceController extends MgFeatureServiceController {

    public static Result enumerateDataStores(String resourcePath) {
        return enumerateDataStores(MgRepositoryType.Library, resourcePath);
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