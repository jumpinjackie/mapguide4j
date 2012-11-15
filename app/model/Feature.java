package model;

import java.util.*;

public class Feature
{
    public String LayerName;
    public ZoomBox Zoom;

    private HashMap<String, FeatureProperty> _properties;

    public Feature(String layerName)
    {
        this.LayerName = layerName;
        _properties = new HashMap<String, FeatureProperty>();
    }

    public void addProperty(FeatureProperty prop)
    {
        _properties.put(prop.Name, prop);
    }

    public FeatureProperty[] getProperties()
    {
        Collection<FeatureProperty> values = _properties.values();
        FeatureProperty[] props = new FeatureProperty[values.size()];
        values.toArray(props);
        return props;
    }
}