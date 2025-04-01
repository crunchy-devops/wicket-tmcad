package com.crunchydevops.dxf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A modern Java 17 DXF file reader that extracts layer information.
 */
public class DxfReader {
    // DXF keywords
    private static final String SECTION = "SECTION";
    private static final String ENDSEC = "ENDSEC";
    private static final String TABLES = "TABLES";
    private static final String ENTITIES = "ENTITIES";
    private static final String TABLE = "TABLE";
    private static final String ENDTAB = "ENDTAB";
    private static final String LAYER = "LAYER";
    private static final String TEXT = "TEXT";
    private static final String GROUP_CODE_0 = "0";
    private static final String GROUP_CODE_2 = "2";
    private static final String GROUP_CODE_8 = "8";
    private static final String GROUP_CODE_62 = "62";
    private static final String GROUP_CODE_6 = "6";

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
            
            if (GROUP_CODE_0.equals(groupCode) && SECTION.equals(value)) {
                processSection();
            }
        }
        return layers;
    }
    
    private void processSection() {
        // Read section type
        String sectionType = "";
        if (currentLine + 1 < lines.size() && GROUP_CODE_2.equals(lines.get(currentLine))) {
            sectionType = lines.get(currentLine + 1).trim();
            currentLine += 2;
        }
        
        switch (sectionType) {
            case TABLES -> processTables();
            case ENTITIES -> processEntities();
            default -> skipSection();
        }
    }
    
    private void processTables() {
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode)) {
                if (ENDSEC.equals(value)) {
                    return;
                } else if (TABLE.equals(value)) {
                    processTable();
                }
            }
        }
    }
    
    private void processTable() {
        // Read table type
        String tableType = "";
        if (currentLine + 1 < lines.size() && GROUP_CODE_2.equals(lines.get(currentLine))) {
            tableType = lines.get(currentLine + 1).trim();
            currentLine += 2;
        }
        
        if (LAYER.equals(tableType)) {
            processLayerTable();
        } else {
            skipTable();
        }
    }
    
    private void processLayerTable() {
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode)) {
                if (ENDTAB.equals(value)) {
                    return;
                } else if (LAYER.equals(value)) {
                    processLayerDefinition();
                }
            }
        }
    }
    
    private void processLayerDefinition() {
        String name = "";
        int color = 7;
        String lineType = "CONTINUOUS";
        boolean isVisible = true;
        
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode)) {
                currentLine -= 2; // Back up so next reader sees this
                break;
            }
            
            switch (groupCode) {
                case GROUP_CODE_2 -> name = value;
                case GROUP_CODE_62 -> {
                    color = Integer.parseInt(value);
                    isVisible = color >= 0;
                }
                case GROUP_CODE_6 -> lineType = value;
                default -> {} // Skip other group codes
            }
        }
        
        if (!name.isEmpty()) {
            layers.put(name, new DxfLayer(name, Math.abs(color), lineType, isVisible, new ArrayList<>()));
        }
    }
    
    private void skipTable() {
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode) && ENDTAB.equals(value)) {
                return;
            }
        }
    }
    
    private void processEntities() {
        String currentLayer = "0";  // Default layer
        Map<Integer, String> groupCodes = new HashMap<>();
        String entityType = "";
        
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode)) {
                if (ENDSEC.equals(value)) {
                    // Add last entity if exists
                    if (!entityType.isEmpty()) {
                        addEntityToLayer(currentLayer, new DxfEntity(entityType, groupCodes));
                    }
                    return;
                }
                
                // Add previous entity if exists
                if (!entityType.isEmpty()) {
                    addEntityToLayer(currentLayer, new DxfEntity(entityType, groupCodes));
                    groupCodes = new HashMap<>();
                }
                
                entityType = value;
                continue;
            }
            
            if (GROUP_CODE_8.equals(groupCode)) {
                currentLayer = value;
                continue;
            }
            
            try {
                int code = Integer.parseInt(groupCode);
                groupCodes.put(code, value);
            } catch (NumberFormatException e) {
                // Skip invalid group codes
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
            
            if (GROUP_CODE_0.equals(groupCode) && ENDSEC.equals(value)) {
                return;
            }
        }
    }
}
