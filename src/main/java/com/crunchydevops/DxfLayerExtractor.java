package com.crunchydevops;

import com.crunchydevops.dxf.DxfLayer;
import com.crunchydevops.dxf.DxfReader;

import java.nio.file.Path;
import java.util.Map;

/**
 * Application that reads a DXF file and extracts all layer information.
 */
public class DxfLayerExtractor {
    public static void main(String[] args) {
        try {
            // Get DXF file path
            String dxfPath = args.length > 0 ? args[0] : "data/project.dxf";
            Path filePath = Path.of(dxfPath);
            
            // Create DXF reader and process file
            System.out.println("Reading DXF file: " + filePath.toAbsolutePath());
            DxfReader reader = new DxfReader(filePath);
            Map<String, DxfLayer> layers = reader.readLayers();
            
            // Print layer information
            System.out.println("\nFound " + layers.size() + " layers:");
            System.out.println("=".repeat(40));
            
            layers.values().stream()
                  .sorted((l1, l2) -> l1.name().compareToIgnoreCase(l2.name()))
                  .forEach(layer -> {
                      System.out.println(layer);
                      System.out.println("-".repeat(40));
                  });
            
        } catch (Exception e) {
            System.err.println("Error processing DXF file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
