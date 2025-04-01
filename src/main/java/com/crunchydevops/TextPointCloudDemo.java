package com.crunchydevops;

import com.crunchydevops.dxf.DxfEntity;
import com.crunchydevops.dxf.DxfLayer;
import com.crunchydevops.dxf.DxfReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(TextPointCloudDemo.class);
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
            logger.trace("Added point: {}", point);
            
        } catch (NumberFormatException e) {
            // Skip invalid points
            logger.error("Failed to parse coordinates from TEXT entity: {}", entity);
        }
    }

    /**
     * Loads points from TEXT entities in the specified layer of the DXF file.
     */
    public void loadFromDxf(String filePath) {
        try {
            logger.info("Reading DXF file: {}", filePath);
            logger.info("Target layer: {}", TARGET_LAYER);
            
            DxfReader reader = new DxfReader(Path.of(filePath));
            Map<String, DxfLayer> layers = reader.readLayers();
            logger.debug("Found {} layers in file", layers.size());
            
            // Get target layer
            DxfLayer layer = layers.get(TARGET_LAYER);
            if (layer == null) {
                logger.error("Layer '{}' not found in DXF file", TARGET_LAYER);
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
            
            logger.info("Processed {} TEXT entities from layer '{}'", textCount, TARGET_LAYER);
            logger.info("Created {} valid points", pointCloud.size());
            
        } catch (Exception e) {
            logger.error("Error processing DXF file: {}", e.getMessage());
        }
    }

    /**
     * Performs some example calculations with the loaded points.
     */
    public void performCalculations() {
        if (pointCloud.size() < 2) {
            logger.info("Not enough points for calculations");
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

        logger.info("\nPoint Analysis:");
        logger.info("=".repeat(40));
        
        if (lowestPoint.isPresent() && highestPoint.isPresent()) {
            logger.info("Lowest point (ID: {}): {:.2f}, {:.2f}, {:.2f}",
                lowestId, lowestPoint.get().getX(), lowestPoint.get().getY(), lowestPoint.get().getZ());
            logger.info("Highest point (ID: {}): {:.2f}, {:.2f}, {:.2f}",
                highestId, highestPoint.get().getX(), highestPoint.get().getY(), highestPoint.get().getZ());
            
            // Calculate geometric properties between highest and lowest points
            logger.info("\nGeometric Properties:");
            logger.info("-".repeat(40));
            
            pointCloud.distance(lowestId, highestId)
                .ifPresent(distance -> logger.info("Distance: {:.2f} meters", distance));
            
            pointCloud.slope(lowestId, highestId)
                .ifPresent(slope -> logger.info("Slope: {:.1f}%%", slope));
            
            pointCloud.bearing(lowestId, highestId)
                .ifPresent(bearing -> logger.info("Bearing: {:.1f} degrees", bearing));
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
