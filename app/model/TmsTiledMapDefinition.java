package model;

import util.*;

import java.util.*;

import org.osgeo.mapguide.*;

public class TmsTiledMapDefinition
{
    private TmsTiledMapInfo info;

    private Bounds bounds;

    private ArrayList<TmsTileSetInfo> tileSets;

    public TmsTiledMapDefinition(TmsTiledMapInfo info, MgSiteConnection siteConn) {
        this.info = info;
        this.tileSets = new ArrayList<TmsTileSetInfo>();
        this.init(siteConn);
    }

    public TmsTiledMapInfo getInfo() { return info; }

    public Bounds getBounds() { return bounds; }

    public List<TmsTileSetInfo> getTileSets() { return tileSets; }

    public String getUrl(TmsTileSetInfo ts, String urlBase) {
        if (info.getProfile().equals("local"))
            return urlBase + "/" + Math.abs(ts.getOrder() - this.tileSets.size());
        else if (info.getProfile().equals("global-geodetic"))
            return urlBase + "/" + ts.getOrder();
        else
            return "";
    }

    public int getScaleIndex(int tileSetNo) {
        if (info.getProfile().equals("local"))
            return Math.abs(tileSetNo - this.tileSets.size());
        else if (info.getProfile().equals("global-geodetic"))
            return tileSetNo;
        else
            return tileSetNo;
    }

    public void init(MgSiteConnection siteConn) {
        try {
            this.tileSets.clear();

            MgMap map = new MgMap(siteConn);
            MgResourceIdentifier mdfId = new MgResourceIdentifier(info.getMapDefinition());
            map.Create(mdfId, mdfId.GetName());

            MgCoordinateSystemFactory fact = new MgCoordinateSystemFactory();
            int epsg = fact.ConvertWktToEpsgCode(map.GetMapSRS());
            if (epsg == 0) {
                throw new RuntimeException("Invalid map definition. Map's coordinate system does not resolved to a valid EPSG code");
            }
            MgCoordinateSystem mapCs = fact.Create(map.GetMapSRS());
            double metersPerUnit = mapCs.ConvertCoordinateSystemUnitsToMeters(1.0);
            double metersPerPixel = 0.0254 / map.GetDisplayDpi();

            int scaleCount = map.GetFiniteDisplayScaleCount();
            if (scaleCount == 0) {
                throw new RuntimeException("Invalid map definition. Map has no finite scale lists");
            }

            MgEnvelope mapBounds = map.GetMapExtent();
            MgCoordinate boundsLL = mapBounds.GetLowerLeftCoordinate();
            MgCoordinate boundsUR = mapBounds.GetUpperRightCoordinate();

            this.bounds = new Bounds(boundsLL.GetX(), boundsLL.GetY(), boundsUR.GetX(), boundsUR.GetY());

            for (int i = 0; i < scaleCount; i++) {
                double scale = map.GetFiniteDisplayScaleAt(i);
                double unitsPerPixel = 0.0;
                if (info.getProfile().equals("local"))
                    unitsPerPixel = Math.pow(2, i);//metersPerPixel / scale * metersPerUnit;
                this.tileSets.add(new TmsTileSetInfo(i, unitsPerPixel));
            }
        } catch (MgException ex) {
            UncheckedThrow.throwUnchecked(ex);
        }
    }
}