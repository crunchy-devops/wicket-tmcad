package com.crunchydevops;

import com.jsevy.jdxf.DXFEntity;
import com.jsevy.jdxf.DXFDocument;
import com.jsevy.jdxf.DXFGraphics;
import com.jsevy.jdxf.DXFPoint;

import java.io.*;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
     * @throws IOException if there's an error reading the DXF file
     */
    public void loadFromDxf(File dxfFile) throws IOException {
        // Read DXF file content
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(dxfFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        }

        boolean inTargetLayer = false;
        boolean inPoint = false;
        float x = 0, y = 0, z = 0;

        // Parse DXF file manually
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String nextLine = (i + 1 < lines.size()) ? lines.get(i + 1) : "";

            // Check for layer
            if (line.equals("8") && nextLine.equals(TARGET_LAYER)) {
                inTargetLayer = true;
                continue;
            }

            // Check for point entity
            if (line.equals("0") && nextLine.equals("POINT")) {
                inPoint = true;
                continue;
            }

            // Get coordinates if we're in a point in the target layer
            if (inTargetLayer && inPoint) {
                if (line.equals("10")) { // X coordinate
                    x = Float.parseFloat(nextLine);
                } else if (line.equals("20")) { // Y coordinate
                    y = Float.parseFloat(nextLine);
                } else if (line.equals("30")) { // Z coordinate
                    z = Float.parseFloat(nextLine);
                    // Create point and add to cloud
                    Point3D point = new Point3D(x, y, z);
                    pointCloud.addPoint(nextId++, point);
                    // Reset flags
                    inTargetLayer = false;
                    inPoint = false;
                }
            }
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
            
        } catch (IOException e) {
            System.err.println("Error reading DXF file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
