package com.crunchydevops;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Test class for Point3D
 * Aims for 80% code coverage by testing all main functionalities
 */
public class Point3DTest {

    @Test
    public void testConstructorAndGetters() {
        Point3D point = new Point3D(1.0f, 2.0f, 3.0f);
        
        assertEquals(1.0f, point.getX(), "X coordinate should be 1.0");
        assertEquals(2.0f, point.getY(), "Y coordinate should be 2.0");
        assertEquals(3.0f, point.getZ(), "Z coordinate should be 3.0");
    }

    @Test
    public void testEqualsWithSameObject() {
        Point3D point = new Point3D(1.0f, 2.0f, 3.0f);
        assertTrue(point.equals(point), "Point should be equal to itself");
    }

    @Test
    public void testEqualsWithEqualObject() {
        Point3D point1 = new Point3D(1.0f, 2.0f, 3.0f);
        Point3D point2 = new Point3D(1.0f, 2.0f, 3.0f);
        assertTrue(point1.equals(point2), "Points with same coordinates should be equal");
        assertTrue(point2.equals(point1), "Equals should be symmetric");
    }

    @Test
    public void testEqualsWithDifferentObject() {
        Point3D point1 = new Point3D(1.0f, 2.0f, 3.0f);
        Point3D point2 = new Point3D(1.0f, 2.0f, 3.1f);
        assertFalse(point1.equals(point2), "Points with different coordinates should not be equal");
    }

    @Test
    public void testEqualsWithNull() {
        Point3D point = new Point3D(1.0f, 2.0f, 3.0f);
        assertFalse(point.equals(null), "Point should not be equal to null");
    }

    @Test
    public void testEqualsWithDifferentClass() {
        Point3D point = new Point3D(1.0f, 2.0f, 3.0f);
        assertFalse(point.equals("not a point"), "Point should not be equal to object of different class");
    }

    @Test
    public void testHashCode() {
        Point3D point1 = new Point3D(1.0f, 2.0f, 3.0f);
        Point3D point2 = new Point3D(1.0f, 2.0f, 3.0f);
        Point3D point3 = new Point3D(1.0f, 2.0f, 3.1f);
        
        assertEquals(point1.hashCode(), point2.hashCode(), 
            "Equal points should have same hash code");
        assertNotEquals(point1.hashCode(), point3.hashCode(), 
            "Different points should likely have different hash codes");
    }

    @Test
    public void testToString() {
        Point3D point = new Point3D(1.0f, 2.0f, 3.0f);
        String expected = "Point3D{x=1.0, y=2.0, z=3.0}";
        assertEquals(expected, point.toString(), "toString should match expected format");
    }

    @Test
    public void testWithZeroValues() {
        Point3D point = new Point3D(0.0f, 0.0f, 0.0f);
        assertEquals(0.0f, point.getX(), "X coordinate should be 0.0");
        assertEquals(0.0f, point.getY(), "Y coordinate should be 0.0");
        assertEquals(0.0f, point.getZ(), "Z coordinate should be 0.0");
    }

    @Test
    public void testWithNegativeValues() {
        Point3D point = new Point3D(-1.0f, -2.0f, -3.0f);
        assertEquals(-1.0f, point.getX(), "X coordinate should be -1.0");
        assertEquals(-2.0f, point.getY(), "Y coordinate should be -2.0");
        assertEquals(-3.0f, point.getZ(), "Z coordinate should be -3.0");
    }
}
