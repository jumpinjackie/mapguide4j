package controllers;

import util.*;

import java.io.InputStream;
import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public class MgLibraryTileServiceController extends MgAbstractController {
    public static Result getTile(String resourcePath, String baseLayerGroupName, Long scaleIndex, Long tileCol, Long tileRow) {
        try {
            MgResourceIdentifier mdfId = ConstructResourceId(MgRepositoryType.Library, resourcePath);
            MgSiteConnection siteConn = CreateAnonymousMapGuideConnection();
            MgTileService tileSvc = (MgTileService)siteConn.CreateService(MgServiceType.TileService);
            MgByteReader image  = tileSvc.GetTile(mdfId, baseLayerGroupName, tileCol.intValue(), tileRow.intValue(), scaleIndex.intValue());
            response().setContentType(image.GetMimeType());
            MgReadOnlyStream inStream = new MgReadOnlyStream(image);
            return ok(inStream);
        } catch (MgException ex) {
            return notFound();
        }
    }
}