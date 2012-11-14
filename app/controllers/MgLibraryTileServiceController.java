package controllers;

import util.*;

import java.io.*;
import play.*;
import play.mvc.*;

import org.osgeo.mapguide.*;

public class MgLibraryTileServiceController extends MgAbstractController {
    public static Result getTile(String resourcePath, String baseLayerGroupName, Long scaleIndex, Long tileCol, Long tileRow) {
        try {
            //TODO: I don't think the current MgTileService is conductive to supporting HTTP 304 responses at this point in time
            MgResourceIdentifier mdfId = constructLibraryResourceId(MgRepositoryType.Library, resourcePath);
            MgSiteConnection siteConn = createAnonymousMapGuideConnection();
            MgTileService tileSvc = (MgTileService)siteConn.CreateService(MgServiceType.TileService);
            MgByteReader image  = tileSvc.GetTile(mdfId, baseLayerGroupName, tileCol.intValue(), tileRow.intValue(), scaleIndex.intValue());
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
        } catch (MgException ex) {
            return notFound();
        }
    }
}