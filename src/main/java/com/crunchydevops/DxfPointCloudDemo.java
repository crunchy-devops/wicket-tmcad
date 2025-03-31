package com.crunchydevops;

import org.kabeja.dxf.*;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import java.io.File;
import java.util.Random;

/**
 * Demo class that loads points from a DXF file layer named "z value TN"
 * and performs geometric calculations using PointCloud.
 */
public class DxfPointCloudDemo {
    private static final String TARGET_LAYER = "z value TN";
    private final PointCloud pointCloud;
    private final Random random;
    private long nextId;

    public DxfPointCloudDemo() {
        this.pointCloud = new PointCloud();
        this.random = new Random();
        this.nextId = 1;
    }

    /**
     * Loads points from a DXF file in the specified layer.
     *
     * @param dxfFile The DXF file to read
     * @throws ParseException if there's an error parsing the DXF file
     */
    public void loadFromDxf(File dxfFile) throws ParseException {
        Parser parser = ParserBuilder.createDefaultParser();
        parser.parse(dxfFile.getAbsolutePath());
        DXFDocument doc = parser.getDocument();
        
        DXFLayer layer = doc.getLayer(TARGET_LAYER);
        if (layer == null) {
            throw new IllegalArgumentException("Layer '" + TARGET_LAYER + "' not found in DXF file");
        }

        // Process points in the layer
        for (DXFEntity entity : layer.getDXFEntities(DXFConstants.ENTITY_TYPE_POINT)) {
            DXFPoint point = (DXFPoint) entity;
            Point3D p3d = new Point3D(
                (float) point.getX(),
                (float) point.getY(),
                (float) point.getZ()
            );
            pointCloud.addPoint(nextId++, p3d);
        }

        System.out.println("Loaded " + pointCloud.size() + " points from DXF file");
    }

    /**
     * Performs random geometric calculations on the loaded points.
     *
     * @param numCalculations Number of calculations to perform
     */
    public void performRandomCalculations(int numCalculations) {
        if (pointCloud.size() < 2) {
            System.out.println("Not enough points for calculations");
            return;
        }

        for (int i = 0; i < numCalculations; i++) {
            // Get two random point IDs
            long id1 = 1 + random.nextInt((int) (nextId - 1));
            long id2 = 1 + random.nextInt((int) (nextId - 1));
            
            if (id1 == id2) continue; // Skip if same point

            // Get points
            var point1 = pointCloud.getPoint(id1);
            var point2 = pointCloud.getPoint(id2);
            
            if (point1.isEmpty() || point2.isEmpty()) continue;

            System.out.println("\nCalculations between points " + id1 + " and " + id2 + ":");
            
            // Calculate distance
            pointCloud.distance(id1, id2).ifPresent(distance ->
                System.out.printf("Distance: %.2f meters%n", distance));
            
            // Calculate slope
            pointCloud.slope(id1, id2).ifPresent(slope ->
                System.out.printf("Slope: %.1f%%%n", slope));
            
            // Calculate bearing
            pointCloud.bearing(id1, id2).ifPresent(bearing ->
                System.out.printf("Bearing: %.1f degrees%n", bearing));
        }
    }

    public static void main(String[] args) {
        try {
            DxfPointCloudDemo demo = new DxfPointCloudDemo();
            
            // Load points from DXF file
            File dxfFile = new File("data/project.dxf");
            if (!dxfFile.exists()) {
                System.err.println("DXF file not found: " + dxfFile.getAbsolutePath());
                System.err.println("Please place a DXF file with layer '" + TARGET_LAYER + "' in the data directory");
                return;
            }

            demo.loadFromDxf(dxfFile);
            
            // Perform some random calculations
            demo.performRandomCalculations(5);
            
        } catch (ParseException e) {
            System.err.println("Error parsing DXF file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
