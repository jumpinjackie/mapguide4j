package model;

public class Bounds
{
    private double minx;
    private double miny;
    private double maxx;
    private double maxy;

    public Bounds(double minx, double miny, double maxx, double maxy) {
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
    }

    public double getMinX() { return minx; }

    public double getMinY() { return miny; }

    public double getMaxX() { return maxx; }

    public double getMaxY() { return maxy; }

    public void setMinX(double value) { minx = value; }

    public void setMinY(double value) { miny = value; }

    public void setMaxX(double value) { maxx = value; }

    public void setMaxY(double value) { maxy = value; }
}