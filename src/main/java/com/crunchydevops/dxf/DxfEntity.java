package com.crunchydevops.dxf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an entity in a DXF file.
 * Implements security best practices for data validation and immutability.
 */
public record DxfEntity(String type, Map<Integer, String> groupCodes) {
    private static final Logger logger = LoggerFactory.getLogger(DxfEntity.class);
    private static final int MAX_TYPE_LENGTH = 255;
    private static final int MAX_VALUE_LENGTH = 255;
    private static final int MIN_GROUP_CODE = 0;
    private static final int MAX_GROUP_CODE = 1071;

    /**
     * Creates a new DxfEntity with validation.
     */
    public DxfEntity {
        // Validate type
        Objects.requireNonNull(type, "Entity type cannot be null");
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Entity type cannot be empty");
        }
        if (type.length() > MAX_TYPE_LENGTH) {
            logger.warn("Entity type too long, truncating: {}", type);
            type = type.substring(0, MAX_TYPE_LENGTH);
        }
        type = type.replaceAll("[^a-zA-Z0-9_-]", "_");

        // Validate and copy group codes
        Objects.requireNonNull(groupCodes, "Group codes cannot be null");
        Map<Integer, String> validatedCodes = new HashMap<>();
        
        groupCodes.forEach((code, value) -> {
            if (code != null && value != null &&
                code >= MIN_GROUP_CODE && code <= MAX_GROUP_CODE) {
                
                if (value.length() > MAX_VALUE_LENGTH) {
                    logger.warn("Group code {} value too long, truncating: {}", code, value);
                    value = value.substring(0, MAX_VALUE_LENGTH);
                }
                validatedCodes.put(code, value);
            } else {
                logger.warn("Skipping invalid group code: {} = {}", code, value);
            }
        });

        groupCodes = Collections.unmodifiableMap(validatedCodes);
        logger.debug("Created entity: type='{}', groupCodes={}", type, groupCodes.size());
    }

    @Override
    public String toString() {
        return """
            Entity Type: %s
            Group Codes: %s
            """.formatted(type, groupCodes);
    }
}
