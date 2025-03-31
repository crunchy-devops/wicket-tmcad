package com.crunchydevops;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static java.lang.Math.*;

/**
 * A collection of 3D points with unique identifiers and geometric operations.
 * This class provides functionality to store and manipulate points in 3D space,
 * including calculations for distance, slope, and bearing angles between points.
 */
public class PointCloud {
    private final Map<Long, Point3D> points;

    /**
     * Creates a new empty point cloud.
     */
    public PointCloud() {
        this.points = new HashMap<>();
    }

    /**
     * Adds a point to the cloud with a unique identifier.
     *
     * @param id The unique identifier for the point
     * @param point The 3D point to add
     * @return true if the point was added, false if the ID already exists
     * @throws IllegalArgumentException if point is null
     */
    public boolean addPoint(long id, Point3D point) {
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null");
        }
        if (points.containsKey(id)) {
            return false;
        }
        points.put(id, point);
        return true;
    }

    /**
     * Retrieves a point by its ID.
     *
     * @param id The ID of the point to retrieve
     * @return Optional containing the point if found, empty otherwise
     */
    public Optional<Point3D> getPoint(long id) {
        return Optional.ofNullable(points.get(id));
    }

    /**
     * Calculates the Euclidean distance between two points identified by their IDs.
     *
     * @param id1 ID of the first point
     * @param id2 ID of the second point
     * @return Optional containing the distance if both points exist, empty otherwise
     */
    public Optional<Double> distance(long id1, long id2) {
        Point3D p1 = points.get(id1);
        Point3D p2 = points.get(id2);
        
        if (p1 == null || p2 == null) {
            return Optional.empty();
        }

        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double dz = p2.getZ() - p1.getZ();

        return Optional.of(sqrt(dx * dx + dy * dy + dz * dz));
    }

    /**
     * Calculates the slope (angle from horizontal plane) between two points.
     * The slope is returned in degrees, where:
     * - Positive values indicate p2 is higher than p1
     * - Negative values indicate p2 is lower than p1
     * - 0 degrees indicates points are at the same height
     * - 90 degrees indicates p2 is directly above p1
     * - -90 degrees indicates p2 is directly below p1
     *
     * @param id1 ID of the first point
     * @param id2 ID of the second point
     * @return Optional containing the slope in degrees if both points exist, empty otherwise
     */
    public Optional<Double> slope(long id1, long id2) {
        Point3D p1 = points.get(id1);
        Point3D p2 = points.get(id2);
        
        if (p1 == null || p2 == null) {
            return Optional.empty();
        }

        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double dz = p2.getZ() - p1.getZ();
        
        // Calculate horizontal distance
        double horizontalDist = sqrt(dx * dx + dy * dy);
        
        // Handle case where points are directly above/below each other
        if (horizontalDist == 0) {
            return Optional.of(dz > 0 ? 90.0 : (dz < 0 ? -90.0 : 0.0));
        }

        // Convert to degrees
        return Optional.of(toDegrees(atan2(dz, horizontalDist)));
    }

    /**
     * Calculates the bearing angle between two points in degrees.
     * The bearing is measured clockwise from true north (positive Y-axis), where:
     * - 0 degrees points north (positive Y)
     * - 90 degrees points east (positive X)
     * - 180 degrees points south (negative Y)
     * - 270 degrees points west (negative X)
     *
     * @param id1 ID of the first point
     * @param id2 ID of the second point
     * @return Optional containing the bearing in degrees if both points exist and have different positions, empty otherwise
     */
    public Optional<Double> bearing(long id1, long id2) {
        Point3D p1 = points.get(id1);
        Point3D p2 = points.get(id2);
        
        if (p1 == null || p2 == null) {
            return Optional.empty();
        }

        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        
        // Handle coincident points
        if (dx == 0 && dy == 0) {
            return Optional.empty();
        }

        // Calculate bearing from north
        double bearing = toDegrees(atan2(dx, dy));
        
        // Convert to 0-360 range
        bearing = (bearing + 360) % 360;

        return Optional.of(bearing);
    }

    /**
     * Gets the number of points in the cloud.
     *
     * @return The number of points
     */
    public int size() {
        return points.size();
    }

    /**
     * Removes a point from the cloud.
     *
     * @param id The ID of the point to remove
     * @return true if the point was removed, false if it didn't exist
     */
    public boolean removePoint(long id) {
        return points.remove(id) != null;
    }
}
