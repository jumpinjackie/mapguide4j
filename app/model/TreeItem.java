package model;

import java.util.ArrayList;

public class TreeItem
{
    public TreeItem(String name, boolean isGroup, Object rtObject, String layerData)
    {
        this.name = name;
        this.isGroup = isGroup;
        this.rtObject = rtObject;
        this.layerData = layerData;
        if(isGroup)
            this.children = new ArrayList();
        else
            this.children = null;
        this.parent = null;
    }

    public void Attach(TreeItem child)
    {
        if(this.children == null)
            this.children = new ArrayList();

        this.children.add(child);
    }

    public String name;
    public boolean isGroup;
    public Object rtObject;
    public ArrayList children;
    public String parentName;
    public TreeItem parent;
    public String layerData;
}