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
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "CS.ENUMERATECATEGORIES");
            param.AddParameter("VERSION", "1.0.0");

            if (fmt.equals("xml")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.AddParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
                param.AddParameter("XSLSTYLESHEET", "CoordinateSystemCategoryList.xsl");
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
            MgHttpRequestParam param = request.GetRequestParam();

            param.AddParameter("OPERATION", "CS.ENUMERATECOORDINATESYSTEMS");
            param.AddParameter("VERSION", "1.0.0");
            param.AddParameter("CSCATEGORY", category);

            if (fmt.equals("xml")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
            }
            else if (fmt.equals("json")) {
                param.AddParameter("FORMAT", MgMimeType.Json);
            }
            else if (fmt.equals("html")) {
                param.AddParameter("FORMAT", MgMimeType.Xml);
                param.AddParameter("XSLSTYLESHEET", "CoordinateSystemList.xsl");
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