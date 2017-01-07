package edu.utah.cs4962.asteroidtest;

/**
 * Created by CharlieBarber on 4/25/16.
 */
public class Vector
{
    public static float TO_RADIANS = (1 / 180.0f) * (float) Math.PI;
    public static float TO_DEGREES = (1 / (float) Math.PI) * 180;
    public float x, y;

    public Vector() {
    }

    public Vector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Vector other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector set(Vector other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    public float len() {
        return (float)Math.sqrt(x * x + y * y);
    }

    public float dist(Vector other) {
        float distX = this.x - other.x;
        float distY = this.y - other.y;
        return (float)Math.sqrt(distX * distX + distY * distY);
    }

    public float dist(float x, float y) {
        float distX = this.x - x;
        float distY = this.y - y;
        return (float)Math.sqrt(distX * distX + distY * distY);
    }

    public float distSquared(Vector other) {
        float distX = this.x - other.x;
        float distY = this.y - other.y;
        return distX*distX + distY*distY;
    }

    public float distSquared(float x, float y) {
        float distX = this.x - x;
        float distY = this.y - y;
        return distX*distX + distY*distY;
    }

}
