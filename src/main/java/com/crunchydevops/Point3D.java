package com.crunchydevops;

/**
 * An immutable 3D point representation using float coordinates.
 * This class provides a memory-efficient way to store 3D coordinates
 * while ensuring immutability.
 */
public final class Point3D {
    private final float x;
    private final float y;
    private final float z;

    /**
     * Constructs a new Point3D with the specified coordinates.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     */
    public Point3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @return The x coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * @return The y coordinate
     */
    public float getY() {
        return y;
    }

    /**
     * @return The z coordinate
     */
    public float getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point3D point3D = (Point3D) o;
        return Float.compare(point3D.x, x) == 0 &&
               Float.compare(point3D.y, y) == 0 &&
               Float.compare(point3D.z, z) == 0;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        result = 31 * result + Float.floatToIntBits(z);
        return result;
    }

    @Override
    public String toString() {
        return "Point3D{" +
               "x=" + x +
               ", y=" + y +
               ", z=" + z +
               '}';
    }
}
