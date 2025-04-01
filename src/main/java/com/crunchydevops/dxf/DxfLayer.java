package com.crunchydevops.dxf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a layer in a DXF file.
 */
public record DxfLayer(
    String name,
    int colorNumber,
    String lineType,
    boolean isVisible,
    List<DxfEntity> entities
) {
    public DxfLayer {
        Objects.requireNonNull(name, "Layer name cannot be null");
        Objects.requireNonNull(lineType, "Line type cannot be null");
        entities = new ArrayList<>(entities); // Defensive copy
    }

    /**
     * Creates a new layer with the given name.
     */
    public static DxfLayer create(String name) {
        return new DxfLayer(name, 7, "CONTINUOUS", true, new ArrayList<>());
    }

    /**
     * Adds an entity to this layer.
     */
    public void addEntity(DxfEntity entity) {
        entities.add(entity);
    }

    @Override
    public String toString() {
        return """
            Layer: %s
            Color: %d
            Line Type: %s
            Visible: %s
            Entity Count: %d
            """.formatted(name, colorNumber, lineType, isVisible, entities.size());
    }
}
