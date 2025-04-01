package com.crunchydevops;

import com.crunchydevops.dxf.DxfEntity;
import com.crunchydevops.dxf.DxfLayer;
import com.crunchydevops.dxf.DxfReader;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Demo application that reads TEXT entities from the "z value TN" layer in a DXF file
 * and creates a PointCloud.
 * - X,Y coordinates are taken from the text position
 * - Z coordinate is parsed from the text content
 */
public class TextPointCloudDemo {
    private static final String TARGET_LAYER = "z value TN";
    private final PointCloud pointCloud;
    private long nextId = 1;

    public TextPointCloudDemo() {
        this.pointCloud = new PointCloud();
    }

    /**
     * Processes a TEXT entity and adds it to the point cloud if valid.
     * Group codes for TEXT entities:
     * 10: X coordinate
     * 20: Y coordinate
     * 1: Text string (contains Z value)
     */
    private void processTextEntity(DxfEntity entity) {
        Map<Integer, String> codes = entity.groupCodes();
        
        try {
            // Get X and Y from text position
            float x = Float.parseFloat(codes.getOrDefault(10, "0"));
            float y = Float.parseFloat(codes.getOrDefault(20, "0"));
            
            // Get Z from text content
            String text = codes.getOrDefault(1, "").trim();
            float z = Float.parseFloat(text);
            
            // Create point and add to cloud
            Point3D point = new Point3D(x, y, z);
            pointCloud.addPoint(nextId++, point);
            
        } catch (NumberFormatException e) {
            // Skip invalid points
            System.err.println("Skipping invalid point: " + e.getMessage());
        }
    }

    /**
     * Loads points from TEXT entities in the specified layer of the DXF file.
     */
    public void loadFromDxf(String filePath) {
        try {
            System.out.println("Reading DXF file: " + filePath);
            System.out.println("Target layer: " + TARGET_LAYER);
            
            DxfReader reader = new DxfReader(Path.of(filePath));
            Map<String, DxfLayer> layers = reader.readLayers();
            
            // Get target layer
            DxfLayer layer = layers.get(TARGET_LAYER);
            if (layer == null) {
                System.err.println("Layer '" + TARGET_LAYER + "' not found in DXF file");
                return;
            }
            
            // Process TEXT entities from target layer
            int textCount = 0;
            for (DxfEntity entity : layer.entities()) {
                if (entity.type().equals("TEXT")) {
                    processTextEntity(entity);
                    textCount++;
                }
            }
            
            System.out.printf("Processed %d TEXT entities from layer '%s'%n", textCount, TARGET_LAYER);
            System.out.printf("Created %d valid points%n", pointCloud.size());
            
        } catch (Exception e) {
            System.err.println("Error processing DXF file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Performs some example calculations with the loaded points.
     */
    public void performCalculations() {
        if (pointCloud.size() < 2) {
            System.out.println("Not enough points for calculations");
            return;
        }

        // Find points with minimum and maximum Z values
        Optional<Point3D> lowestPoint = Optional.empty();
        Optional<Point3D> highestPoint = Optional.empty();
        long lowestId = 0, highestId = 0;
        float minZ = Float.MAX_VALUE;
        float maxZ = Float.MIN_VALUE;

        for (long id = 1; id < nextId; id++) {
            Optional<Point3D> point = pointCloud.getPoint(id);
            if (point.isPresent()) {
                float z = point.get().getZ();
                if (z < minZ) {
                    minZ = z;
                    lowestPoint = point;
                    lowestId = id;
                }
                if (z > maxZ) {
                    maxZ = z;
                    highestPoint = point;
                    highestId = id;
                }
            }
        }

        System.out.println("\nPoint Analysis:");
        System.out.println("=".repeat(40));
        
        if (lowestPoint.isPresent() && highestPoint.isPresent()) {
            System.out.printf("Lowest point (ID: %d): %.2f, %.2f, %.2f%n",
                lowestId, lowestPoint.get().getX(), lowestPoint.get().getY(), lowestPoint.get().getZ());
            System.out.printf("Highest point (ID: %d): %.2f, %.2f, %.2f%n",
                highestId, highestPoint.get().getX(), highestPoint.get().getY(), highestPoint.get().getZ());
            
            // Calculate geometric properties between highest and lowest points
            System.out.println("\nGeometric Properties:");
            System.out.println("-".repeat(40));
            
            pointCloud.distance(lowestId, highestId)
                .ifPresent(distance -> System.out.printf("Distance: %.2f meters%n", distance));
            
            pointCloud.slope(lowestId, highestId)
                .ifPresent(slope -> System.out.printf("Slope: %.1f%%%n", slope));
            
            pointCloud.bearing(lowestId, highestId)
                .ifPresent(bearing -> System.out.printf("Bearing: %.1f degrees%n", bearing));
        }
    }

    public static void main(String[] args) {
        TextPointCloudDemo demo = new TextPointCloudDemo();
        
        // Load points from DXF file
        String dxfPath = args.length > 0 ? args[0] : "data/project.dxf";
        demo.loadFromDxf(dxfPath);
        
        // Perform example calculations
        demo.performCalculations();
    }
}
