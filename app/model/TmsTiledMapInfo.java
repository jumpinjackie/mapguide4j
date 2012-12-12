package model;

public class TmsTiledMapInfo
{
    private String title;
    private String srs;
    private String profile;
    private String mapId;
    private String mapDefinitionId;
    private String mapAbstract;

    public TmsTiledMapInfo() { }

    public TmsTiledMapInfo(String title, String srs, String profile, String mapId, String mapDefinitionId) {
        this.title = title;
        this.srs = srs;
        this.profile = profile;
        this.mapId = mapId;
        this.mapDefinitionId = mapDefinitionId;
    }

    public String getAbstract() { return mapAbstract; }

    public void setAbstract(String value) { mapAbstract = value; }

    public String getMapId() { return mapId; }

    public void setMapId(String value) { mapId = value; }

    public String getTitle() { return title; }

    public void setTitle(String value) { title = value; }

    public String getSrs() { return srs; }

    public void setSrs(String value) { srs = value; }

    public String getProfile() { return profile; }

    public void setProfile(String value) { profile = value; }

    public String getMapDefinition() { return mapDefinitionId; }

    public void setMapDefinition(String value) { mapDefinitionId = value; }
}