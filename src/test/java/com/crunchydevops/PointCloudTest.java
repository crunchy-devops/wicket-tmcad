package com.crunchydevops;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PointCloud
 */
public class PointCloudTest {
    private PointCloud cloud;
    private Point3D p1, p2, p3;

    @BeforeEach
    void setUp() {
        cloud = new PointCloud();
        p1 = new Point3D(0.0f, 0.0f, 0.0f);
        p2 = new Point3D(3.0f, 4.0f, 0.0f);
        p3 = new Point3D(3.0f, 4.0f, 3.0f);
    }

    @Test
    void testAddAndGetPoint() {
        assertTrue(cloud.addPoint(1L, p1));
        assertEquals(p1, cloud.getPoint(1L).orElse(null));
        assertFalse(cloud.addPoint(1L, p2)); // Duplicate ID
        assertTrue(cloud.getPoint(2L).isEmpty()); // Non-existent ID
    }

    @Test
    void testAddNullPoint() {
        assertThrows(IllegalArgumentException.class, () -> cloud.addPoint(1L, null));
    }

    @Test
    void testSize() {
        assertEquals(0, cloud.size());
        cloud.addPoint(1L, p1);
        assertEquals(1, cloud.size());
        cloud.addPoint(2L, p2);
        assertEquals(2, cloud.size());
    }

    @Test
    void testRemovePoint() {
        cloud.addPoint(1L, p1);
        assertTrue(cloud.removePoint(1L));
        assertFalse(cloud.removePoint(1L)); // Already removed
        assertTrue(cloud.getPoint(1L).isEmpty());
    }

    @Test
    void testDistance() {
        cloud.addPoint(1L, p1);
        cloud.addPoint(2L, p2);
        cloud.addPoint(3L, p3);

        // Distance between origin and (3,4,0) should be 5
        assertEquals(5.0, cloud.distance(1L, 2L).orElse(-1.0), 0.0001);
        
        // Distance between (3,4,0) and (3,4,3) should be 3
        assertEquals(3.0, cloud.distance(2L, 3L).orElse(-1.0), 0.0001);
        
        // Distance between non-existent points should be empty
        assertTrue(cloud.distance(1L, 4L).isEmpty());
    }

    @Test
    void testSlope() {
        cloud.addPoint(1L, p1);
        cloud.addPoint(2L, p2);
        cloud.addPoint(3L, p3);

        // Slope between p1 and p2 (horizontal) should be 0
        assertEquals(0.0, cloud.slope(1L, 2L).orElse(-999.0), 0.0001);
        
        // Slope between p2 and p3 should be 90 degrees (vertical)
        assertEquals(90.0, cloud.slope(2L, 3L).orElse(-999.0), 0.0001);
        
        // Test with non-existent point
        assertTrue(cloud.slope(1L, 4L).isEmpty());
    }

    @Test
    void testBearing() {
        cloud.addPoint(1L, p1);
        cloud.addPoint(2L, p2);
        
        // Bearing from origin to (3,4,0) should be about 36.87 degrees
        assertEquals(36.87, cloud.bearing(1L, 2L).orElse(-1.0), 0.01);
        
        // Test with coincident points (should return empty)
        cloud.addPoint(3L, new Point3D(0.0f, 0.0f, 0.0f));
        assertTrue(cloud.bearing(1L, 3L).isEmpty());
        
        // Test with non-existent point
        assertTrue(cloud.bearing(1L, 4L).isEmpty());
    }

    @Test
    void testSpecialCases() {
        Point3D vertical = new Point3D(0.0f, 0.0f, 5.0f);
        cloud.addPoint(1L, p1);
        cloud.addPoint(2L, vertical);
        
        // Slope should be 90 degrees for vertical alignment
        assertEquals(90.0, cloud.slope(1L, 2L).orElse(-999.0), 0.0001);
        
        // Bearing should be empty for vertical alignment
        assertTrue(cloud.bearing(1L, 2L).isEmpty());
    }
}
