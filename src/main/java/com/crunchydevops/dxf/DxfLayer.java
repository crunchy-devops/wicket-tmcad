package com.crunchydevops.dxf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a layer in a DXF file.
 * Implements security best practices for data validation and immutability.
 */
public record DxfLayer(String name, int colorNumber, String lineType, boolean isVisible,
                      List<DxfEntity> entities) {
    private static final Logger logger = LoggerFactory.getLogger(DxfLayer.class);
    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_LINE_TYPE_LENGTH = 255;

    /**
     * Creates a new DxfLayer with validation.
     */
    public DxfLayer {
        // Validate name
        Objects.requireNonNull(name, "Layer name cannot be null");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Layer name cannot be empty");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            logger.warn("Layer name too long, truncating: {}", name);
            name = name.substring(0, MAX_NAME_LENGTH);
        }
        name = name.replaceAll("[^a-zA-Z0-9_\\- ]", "_");

        // Validate color number (preserve negative for visibility)
        if (Math.abs(colorNumber) > 256) {
            logger.warn("Invalid color number {}, using default: 7", colorNumber);
            colorNumber = 7;
        }

        // Validate line type
        Objects.requireNonNull(lineType, "Line type cannot be null");
        if (lineType.isEmpty()) {
            lineType = "CONTINUOUS";
        }
        if (lineType.length() > MAX_LINE_TYPE_LENGTH) {
            logger.warn("Line type too long, truncating: {}", lineType);
            lineType = lineType.substring(0, MAX_LINE_TYPE_LENGTH);
        }
        lineType = lineType.replaceAll("[^a-zA-Z0-9_\\- ]", "_");

        // Ensure entities list is immutable
        Objects.requireNonNull(entities, "Entities list cannot be null");
        entities = Collections.unmodifiableList(new ArrayList<>(entities));

        logger.debug("Created layer: name='{}', color={}, lineType='{}', visible={}, entities={}",
                    name, colorNumber, lineType, isVisible, entities.size());
    }

    /**
     * Creates a new empty layer with the given name.
     */
    public static DxfLayer create(String name) {
        return new DxfLayer(name, 7, "CONTINUOUS", true, new ArrayList<>());
    }

    /**
     * Adds an entity to this layer, returning a new DxfLayer instance.
     */
    public DxfLayer addEntity(DxfEntity entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        var newEntities = new ArrayList<>(entities());
        newEntities.add(entity);
        return new DxfLayer(name(), colorNumber(), lineType(), isVisible(), newEntities);
    }

    @Override
    public String toString() {
        return """
            Layer: %s
            Color Number: %d
            Line Type: %s
            Visible: %b
            Entity Count: %d
            """.formatted(name, colorNumber, lineType, isVisible, entities.size());
    }
}
