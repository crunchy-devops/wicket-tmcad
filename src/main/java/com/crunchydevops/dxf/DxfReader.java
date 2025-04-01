package com.crunchydevops.dxf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A modern Java 17 DXF file reader that extracts layer information.
 */
public class DxfReader {
    private static final Logger logger = LoggerFactory.getLogger(DxfReader.class);

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
        logger.info("Reading DXF file: {}", filePath);
        this.lines = Files.readAllLines(filePath);
        logger.debug("Read {} lines from file", lines.size());
    }
    
    /**
     * Reads and processes the entire DXF file.
     */
    public Map<String, DxfLayer> readLayers() {
        logger.info("Starting to process DXF file");
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode) && SECTION.equals(value)) {
                processSection();
            }
        }
        logger.info("Finished processing DXF file. Found {} layers", layers.size());
        return layers;
    }
    
    private void processSection() {
        // Read section type
        String sectionType = "";
        if (currentLine + 1 < lines.size() && GROUP_CODE_2.equals(lines.get(currentLine))) {
            sectionType = lines.get(currentLine + 1).trim();
            currentLine += 2;
        }
        
        logger.debug("Processing section: {}", sectionType);
        switch (sectionType) {
            case TABLES -> processTables();
            case ENTITIES -> processEntities();
            default -> {
                logger.debug("Skipping unknown section: {}", sectionType);
                skipSection();
            }
        }
    }
    
    private void processTables() {
        logger.debug("Processing TABLES section");
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode)) {
                if (ENDSEC.equals(value)) {
                    logger.debug("Finished processing TABLES section");
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
        
        logger.debug("Processing table: {}", tableType);
        if (LAYER.equals(tableType)) {
            processLayerTable();
        } else {
            logger.debug("Skipping unknown table: {}", tableType);
            skipTable();
        }
    }
    
    private void processLayerTable() {
        logger.debug("Processing LAYER table");
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode)) {
                if (ENDTAB.equals(value)) {
                    logger.debug("Finished processing LAYER table");
                    return;
                } else if (LAYER.equals(value)) {
                    processLayerDefinition();
                }
            }
        }
    }
    
    private void processLayerDefinition() {
        logger.debug("Processing layer definition");
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
            logger.debug("Adding layer: {}", name);
            layers.put(name, new DxfLayer(name, Math.abs(color), lineType, isVisible, new ArrayList<>()));
        }
    }
    
    private void skipTable() {
        logger.debug("Skipping table");
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode) && ENDTAB.equals(value)) {
                return;
            }
        }
    }
    
    /**
     * Processes the ENTITIES section of the DXF file.
     */
    private void processEntities() {
        EntityProcessor processor = new EntityProcessor();
        logger.debug("Starting to process ENTITIES section");
        
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode)) {
                if (ENDSEC.equals(value)) {
                    processor.addCurrentEntity();
                    logger.debug("Finished processing ENTITIES section");
                    return;
                }
                processor.startNewEntity(value);
                continue;
            }
            
            if (GROUP_CODE_8.equals(groupCode)) {
                processor.setCurrentLayer(value);
                continue;
            }
            
            processor.addGroupCode(groupCode, value);
        }
    }
    
    /**
     * Helper class to manage entity processing state.
     */
    private class EntityProcessor {
        private String currentLayer = "0";  // Default layer
        private Map<Integer, String> groupCodes = new HashMap<>();
        private String entityType = "";
        
        /**
         * Starts processing a new entity.
         */
        void startNewEntity(String type) {
            addCurrentEntity();
            entityType = type;
            groupCodes = new HashMap<>();
            logger.trace("Starting new entity of type: {}", type);
        }
        
        /**
         * Sets the current layer for subsequent entities.
         */
        void setCurrentLayer(String layer) {
            logger.trace("Switching to layer: {}", layer);
            currentLayer = layer;
        }
        
        /**
         * Adds a group code to the current entity.
         */
        void addGroupCode(String code, String value) {
            try {
                int groupCode = Integer.parseInt(code);
                groupCodes.put(groupCode, value);
                logger.trace("Added group code {} = {} to entity", groupCode, value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid group code: {}", code);
            }
        }
        
        /**
         * Adds the current entity to its layer if valid.
         */
        void addCurrentEntity() {
            if (!entityType.isEmpty()) {
                DxfEntity entity = new DxfEntity(entityType, groupCodes);
                addEntityToLayer(currentLayer, entity);
                logger.debug("Added {} entity to layer '{}'", entityType, currentLayer);
            }
        }
    }
    
    private void addEntityToLayer(String layerName, DxfEntity entity) {
        layers.computeIfAbsent(layerName, name -> {
            logger.debug("Creating new layer: {}", name);
            return DxfLayer.create(name);
        }).addEntity(entity);
    }
    
    private void skipSection() {
        logger.debug("Skipping section");
        while (currentLine < lines.size()) {
            String groupCode = lines.get(currentLine++).trim();
            String value = currentLine < lines.size() ? lines.get(currentLine++).trim() : "";
            
            if (GROUP_CODE_0.equals(groupCode) && ENDSEC.equals(value)) {
                return;
            }
        }
    }
}
