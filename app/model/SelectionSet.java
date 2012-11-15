package model;

import java.util.*;

public class SelectionSet
{
    private HashMap<String, Vector<Feature>> _layers;

    public SelectionSet()
    {
        _layers = new HashMap<String, Vector<Feature>>();
    }

    public void addFeature(Feature feat)
    {
        if (!_layers.containsKey(feat.LayerName))
            _layers.put(feat.LayerName, new Vector<Feature>());

        _layers.get(feat.LayerName).add(feat);
    }

    public String[] getLayers()
    {
        String[] layers = new String[_layers.keySet().size()];
        _layers.keySet().toArray(layers);

        return layers;
    }

    public Feature[] getFeatures(String layerName)
    {
        if (_layers.containsKey(layerName))
        {
            Vector<Feature> layerFeatures = _layers.get(layerName);
            Feature[] feats = new Feature[layerFeatures.size()];
            layerFeatures.toArray(feats);
            return feats;
        }

        return null;
    }
}