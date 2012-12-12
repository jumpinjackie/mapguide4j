package model;

public class TmsTileSetInfo
{
    private double unitsPerPixel;
    private int order;

    public TmsTileSetInfo() { }

    public TmsTileSetInfo(int order, double unitsPerPixel) {
        this.order = order;
        this.unitsPerPixel = unitsPerPixel;
    }

    //I can't believe we need this method!

    public String getUnitsPerPixelAsString() {
        return new java.math.BigDecimal(unitsPerPixel).toString();
    }

    public double getUnitsPerPixel() { return unitsPerPixel; }

    public void setUnitsPerPixel(double value) { unitsPerPixel = value; }

    public int getOrder() { return order; }

    public void setOrder(int value) { order = value; }
}