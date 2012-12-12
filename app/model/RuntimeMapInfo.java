package model;

public class RuntimeMapInfo
{
    private String _mapName;

    private String _mapDefinitionId;

    private Bounds _extents;

    public RuntimeMapInfo(String mapDefinitionId, String mapName) {
        _mapDefinitionId = mapDefinitionId;
        _mapName = mapName;
    }

    public Bounds getMapExtents() { return _extents; }

    public void setMapExtents(Bounds extents) { _extents = extents; }

    public String getMapName() { return _mapName; }

    public String getMapDefinition() { return _mapDefinitionId; }
}