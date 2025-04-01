package com.crunchydevops.dxf;

import java.util.Map;

/**
 * Represents an entity in a DXF file.
 */
public record DxfEntity(
    String type,
    Map<Integer, String> groupCodes
) {
    @Override
    public String toString() {
        return "Entity Type: " + type + "\n" +
               "Properties: " + groupCodes;
    }
}
