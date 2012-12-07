package model;

public class RuntimeMapInfo
{
    private String _mapName;

    private String _mapDefinitionId;

    private ZoomBox _extents;

    public RuntimeMapInfo(String mapDefinitionId, String mapName) {
        _mapDefinitionId = mapDefinitionId;
        _mapName = mapName;
    }

    public ZoomBox getMapExtents() { return _extents; }

    public void setMapExtents(ZoomBox extents) { _extents = extents; }

    public String getMapName() { return _mapName; }

    public String getMapDefinition() { return _mapDefinitionId; }
}