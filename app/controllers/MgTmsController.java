package controllers;

import java.util.*;
import java.io.*;

import play.*;
import play.mvc.*;
import play.mvc.BodyParser.*;

import model.*;

import views.html.*;
import views.xml.*;

import org.osgeo.mapguide.*;

public class MgTmsController extends MgAbstractController
{
    public static Result testpage() {
        return ok(tmstest.render());
    }

    static final String serviceName = "mapguide4j TMS layer"; //TODO: Make configurable
    static final String serviceDesc = "mapguide4j TMS layer"; //TODO: Make configurable
    static final String tmsVersion = "1.0.0";

    //NOTE: MgTileService should be telling us this, but until then this has to sync up with
    //what's in serverconfig.ini
    static final String tileFormat = "png";
    static final String tileMimeType = "image/png";

    public static Result index() {
        response().setContentType(MgMimeType.Xml);
        return ok(serviceRoot.render(serviceName, tmsVersion));
    }

    //TODO: We're obviously currently flubbing this with one sample map. But once that's figured out, we need
    //to determine the best persistence mechanism to use (files? database?)

    private static TmsTiledMapInfo createTestMap() {
        return new TmsTiledMapInfo("Sheboygan Tiled", "EPSG:4326", "local-geodetic", "sheboygan", "Library://Samples/Sheboygan/MapsTiled/Sheboygan.MapDefinition");
    }

    public static Result serviceDescription(String version) {

        List<TmsTiledMapInfo> maps = new ArrayList<TmsTiledMapInfo>();
        maps.add(createTestMap());

        response().setContentType(MgMimeType.Xml);
        return ok(tmsServiceDescription.render(
            serviceName,
            tmsVersion,
            serviceDesc,
            maps
        ));
    }

    public static Result tileMap(String version, String tileMap) {
        try {
            TmsTiledMapInfo map = createTestMap();
            MgSiteConnection siteConn = createAnonymousMapGuideConnection();
            MgTileService tileSvc = (MgTileService)siteConn.CreateService(MgServiceType.TileService);
            TmsTiledMapDefinition mapDef = new TmsTiledMapDefinition(map, siteConn);
            response().setContentType(MgMimeType.Xml);
            return ok(tmsTileMap.render(
                tmsVersion,
                mapDef,
                tileSvc.GetDefaultTileSizeX(),
                tileSvc.GetDefaultTileSizeY(),
                tileFormat,
                tileMimeType
            ));
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
        catch (Exception ex) {
            return javaException(ex);
        }
    }

    public static Result tile(String version, String tileMap, Long tileSet, Long row, Long col, String format) {
        try {
            TmsTiledMapInfo map = createTestMap();
            MgResourceIdentifier mdfId = new MgResourceIdentifier(map.getMapDefinition());
            //TODO: I don't think the current MgTileService is conductive to supporting HTTP 304 responses at this point in time
            MgSiteConnection siteConn = createAnonymousMapGuideConnection();
            TmsTiledMapDefinition mapDef = new TmsTiledMapDefinition(map, siteConn);
            String baseLayerGroupName = "Base Layer Group";
            int scaleIndex = mapDef.getScaleIndex(tileSet.intValue());

            MgTileService tileSvc = (MgTileService)siteConn.CreateService(MgServiceType.TileService);
            MgByteReader image  = tileSvc.GetTile(mdfId, baseLayerGroupName, col.intValue(), row.intValue(), scaleIndex);
            response().setContentType(image.GetMimeType());
            //We *really* want to use MgReadOnlyStream, but that's not ready for prime-time
            //
            //MgReadOnlyStream inStream = new MgReadOnlyStream(image);
            //return ok(inStream);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] byteBuffer = new byte[1024];
            int numBytes = image.Read(byteBuffer, 1024);
            while(numBytes > 0)
            {
                bos.write(byteBuffer, 0, numBytes);
                numBytes = image.Read(byteBuffer, 1024);
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            return ok(bis);
        }
        catch (MgException ex) {
            return mgServerError(ex);
        }
    }
}