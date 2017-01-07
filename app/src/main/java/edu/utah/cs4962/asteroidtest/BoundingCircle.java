package edu.utah.cs4962.asteroidtest;

/**
 * Created by CharlieBarber on 4/25/16.
 */
public class BoundingCircle
{
    public final Vector center = new Vector();
    public float radius;

    public BoundingCircle(float x, float y, float radius) {
        this.center.set(x,y);
        this.radius = radius;
    }

    public static boolean overlapCircles(BoundingCircle c1, BoundingCircle c2) {
        float distance = c1.center.distSquared(c2.center);
        float radiusSum = c1.radius + c2.radius;
        return distance <= radiusSum * radiusSum;
    }

    public Vector getCenter() {
        return center;
    }

    public void setCenter(float x, float y)
    {
        this.center.set(x, y);
    }
}
