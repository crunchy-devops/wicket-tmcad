package com.crunchydevops.dxf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A modern Java 17 DXF file reader that extracts layer information.
 */
public class DxfReader {
    private final List<String> lines;
    private int currentLine = 0;
    private final Map<String, DxfLayer> layers = new HashMap<>();
    
    public DxfReader(Path filePath) throws IOException {
        this.lines = Files.readAllLines(filePath);
    }
    
    /**
     * Reads and processes the entire DXF file.
     */
    public Map<String, DxfLayer> readLayers() {
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (groupCode.equals("0") && value.equals("SECTION")) {
                processSection();
            }
        }
        return layers;
    }
    
    private void processSection() {
        // Read section type
        String sectionType = "";
        if (currentLine + 1 < lines.size() && lines.get(currentLine).equals("2")) {
            sectionType = lines.get(currentLine + 1).trim();
            currentLine += 2;
        }
        
        switch (sectionType) {
            case "TABLES" -> processTables();
            case "ENTITIES" -> processEntities();
            default -> skipSection();
        }
    }
    
    private void processTables() {
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (groupCode.equals("0")) {
                if (value.equals("ENDSEC")) {
                    return;
                } else if (value.equals("LAYER")) {
                    processLayer();
                }
            }
        }
    }
    
    private void processLayer() {
        String name = "";
        int color = 7;
        String lineType = "CONTINUOUS";
        boolean isVisible = true;
        
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (groupCode.equals("0")) {
                currentLine -= 2; // Back up so next reader sees this
                break;
            }
            
            switch (groupCode) {
                case "2" -> name = value;
                case "62" -> {
                    color = Integer.parseInt(value);
                    isVisible = color >= 0;
                }
                case "6" -> lineType = value;
            }
        }
        
        if (!name.isEmpty()) {
            layers.put(name, new DxfLayer(name, Math.abs(color), lineType, isVisible, new ArrayList<>()));
        }
    }
    
    private void processEntities() {
        String currentType = "";
        Map<Integer, String> groupCodes = new HashMap<>();
        String currentLayer = "";
        
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (groupCode.equals("0")) {
                if (value.equals("ENDSEC")) {
                    // Add last entity if exists
                    if (!currentType.isEmpty() && !currentLayer.isEmpty()) {
                        addEntityToLayer(currentLayer, new DxfEntity(currentType, new HashMap<>(groupCodes)));
                    }
                    return;
                }
                
                // Add previous entity if exists
                if (!currentType.isEmpty() && !currentLayer.isEmpty()) {
                    addEntityToLayer(currentLayer, new DxfEntity(currentType, new HashMap<>(groupCodes)));
                }
                
                // Start new entity
                currentType = value;
                groupCodes.clear();
                currentLayer = "";
            } else if (groupCode.equals("8")) {
                currentLayer = value;
                if (!layers.containsKey(currentLayer)) {
                    layers.put(currentLayer, DxfLayer.create(currentLayer));
                }
            } else {
                try {
                    int code = Integer.parseInt(groupCode);
                    groupCodes.put(code, value);
                } catch (NumberFormatException e) {
                    // Skip invalid group codes
                }
            }
        }
    }
    
    private void addEntityToLayer(String layerName, DxfEntity entity) {
        layers.computeIfAbsent(layerName, DxfLayer::create)
              .addEntity(entity);
    }
    
    private void skipSection() {
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (groupCode.equals("0") && value.equals("ENDSEC")) {
                return;
            }
        }
    }
}
