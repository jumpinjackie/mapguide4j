package controllers;

import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public class MgCoordinateSystemController extends MgAbstractAuthenticatedController {

    public static Result enumerateCategories(String format) {
        try {
            String fmt = format.toLowerCase();
            if (!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "CS.ENUMERATECATEGORIES");
            param.addParameter("VERSION", "1.0.0");

            if (fmt.equals("xml")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.addParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
                param.addParameter("XSLSTYLESHEET", "CoordinateSystemCategoryList.xsl");
            }

            TryFillMgCredentials(param);
            return executeRequestInternal(request);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result enumerateCoordinateSystemsByCategory(String category, String format) {
        try {
            String fmt = format.toLowerCase();
            if (!fmt.equals("html") &&
                !fmt.equals("xml") &&
                !fmt.equals("json"))
            {
                return badRequest("Unsupported representation: " + format);
            }

            String uri =  "";
            MgHttpRequest request = new MgHttpRequest(uri);
            MgHttpRequestParam param = request.getRequestParam();

            param.addParameter("OPERATION", "CS.ENUMERATECOORDINATESYSTEMS");
            param.addParameter("VERSION", "1.0.0");
            param.addParameter("CSCATEGORY", category);

            if (fmt.equals("xml")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.addParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.addParameter("FORMAT", MgMimeType.Xml);
                param.addParameter("XSLSTYLESHEET", "CoordinateSystemList.xsl");
            }

            TryFillMgCredentials(param);
            return executeRequestInternal(request);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
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
            return ok(csFactory.convertCoordinateSystemCodeToWkt(mentorCode));
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getWktForEpsg(Long epsgCode) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            return ok(csFactory.convertEpsgCodeToWkt(epsgCode.intValue()));
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getEpsgForCoordinateSystemCode(String mentorCode) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            MgCoordinateSystem csDef = csFactory.createFromCode(mentorCode);
            return ok(csDef.getEpsgCode() + "");
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getCoordinateSystemCodeForEpsg(Long epsgCode) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            String csWkt = csFactory.convertEpsgCodeToWkt(epsgCode.intValue());
            MgCoordinateSystem csDef = csFactory.create(csWkt);
            return ok(csDef.getCsCode());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getCoordinateSystemCodeFromWkt(String wkt) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            MgCoordinateSystem csDef = csFactory.create(wkt);
            return ok(csDef.getCsCode());
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getEpsgFromWkt(String wkt) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            MgCoordinateSystem csDef = csFactory.create(wkt);
            return ok(csDef.getEpsgCode() + "");
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getMetersPerUnitForCoordinateSystemCode(String mentorCode, Double mpu) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            MgCoordinateSystem csDef = csFactory.createFromCode(mentorCode);
            return ok(csDef.convertCoordinateSystemUnitsToMeters(mpu.doubleValue()) + "");
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }

    public static Result getMetersPerUnitForEpsg(Long epsgCode, Double mpu) {
        try {
            MgCoordinateSystemFactory csFactory = new MgCoordinateSystemFactory();
            String csWkt = csFactory.convertEpsgCodeToWkt(epsgCode.intValue());
            MgCoordinateSystem csDef = csFactory.create(csWkt);
            return ok(csDef.convertCoordinateSystemUnitsToMeters(mpu.doubleValue()) + "");
        } catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}