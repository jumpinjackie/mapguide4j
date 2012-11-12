package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public class MgCoordinateSystemController extends MgAbstractAuthenticatedController {
    
    public static Result enumerateCategories() {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            MgStringCollection categoryNames = csFactory.EnumerateCategories();
            return mgStringCollectionXml(categoryNames);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result enumerateCoordinateSystemsByCategory(String category) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            MgBatchPropertyCollection coordSystems = csFactory.EnumerateCoordinateSystems(category);
            return mgBatchPropertyCollectionXml(coordSystems);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    /*
    public static Result getCoordinateSystemDefinitionByCode(String mentorCode) {
        return ok("Called getCoordinateSystemDefinitionByCode(" + mentorCode + ")");
    }

    public static Result getCoordinateSystemDefinitionByEpsg(Long epsgCode) {
        return ok("Called getCoordinateSystemCodeForEpsg(" + epsgCode.toString() + ")");
    }
    */

    public static Result getWktForCoordinateSystemCode(String mentorCode) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            return ok(csFactory.ConvertCoordinateSystemCodeToWkt(mentorCode));
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getWktForEpsg(Long epsgCode) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            return ok(csFactory.ConvertEpsgCodeToWkt(epsgCode.intValue()));
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getEpsgForCoordinateSystemCode(String mentorCode) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            MgCoordinateSystem csDef = csFactory.CreateFromCode(mentorCode);
            return ok(csDef.GetEpsgCode() + "");
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getCoordinateSystemCodeForEpsg(Long epsgCode) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            String csWkt = csFactory.ConvertEpsgCodeToWkt(epsgCode.intValue());
            MgCoordinateSystem csDef = csFactory.Create(csWkt);
            return ok(csDef.GetCsCode());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getCoordinateSystemCodeFromWkt(String wkt) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            MgCoordinateSystem csDef = csFactory.Create(wkt);
            return ok(csDef.GetCsCode());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getEpsgFromWkt(String wkt) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            MgCoordinateSystem csDef = csFactory.Create(wkt);
            return ok(csDef.GetEpsgCode() + "");
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getMetersPerUnitForCoordinateSystemCode(String mentorCode, Double mpu) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            MgCoordinateSystem csDef = csFactory.CreateFromCode(mentorCode);
            return ok(csDef.ConvertCoordinateSystemUnitsToMeters(mpu.doubleValue()) + "");
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getMetersPerUnitForEpsg(Long epsgCode, Double mpu) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            String csWkt = csFactory.ConvertEpsgCodeToWkt(epsgCode.intValue());
            MgCoordinateSystem csDef = csFactory.Create(csWkt);
            return ok(csDef.ConvertCoordinateSystemUnitsToMeters(mpu.doubleValue()) + "");
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}