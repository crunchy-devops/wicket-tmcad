package com.crunchydevops.dxf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DxfReader Tests")
class DxfReaderTest {
    private static final String PROJECT_DXF = "data/project.dxf";
    private static final String LAYER_ZERO = "0";
    private static final String LAYER_CARROYAGE = "carroyage";
    private static final String LAYER_CROIX = "croix";
    private static final String LAYER_TERRAIN = "terrain naturel";
    private static final String LAYER_Z_VALUE = "z value TN";
    private static final String LAYER_CLOTURE = "cloture";
    private static final String LAYER_CONSTRUCTION = "$CONSTRUCTION";
    
    @TempDir
    Path tempDir;
    
    private Path createDxfFile(String filename, String description) throws IOException {
        Path dxfFile = tempDir.resolve(filename);
        Files.writeString(dxfFile, description);
        return dxfFile;
    }
    
    @Test
    @DisplayName("Project DXF file should be correctly read")
    void testProjectDxf() throws IOException {
        DxfReader reader = new DxfReader(Path.of(PROJECT_DXF));
        Map<String, DxfLayer> layers = reader.readLayers();
        
        // Verify all layers exist
        assertAll("Project layers existence",
            () -> assertTrue(layers.containsKey(LAYER_ZERO), "Layer 0 should exist"),
            () -> assertTrue(layers.containsKey(LAYER_CARROYAGE), "Layer carroyage should exist"),
            () -> assertTrue(layers.containsKey(LAYER_CROIX), "Layer croix should exist"),
            () -> assertTrue(layers.containsKey(LAYER_TERRAIN), "Layer terrain naturel should exist"),
            () -> assertTrue(layers.containsKey(LAYER_Z_VALUE), "Layer z value TN should exist"),
            () -> assertTrue(layers.containsKey(LAYER_CLOTURE), "Layer cloture should exist"),
            () -> assertTrue(layers.containsKey(LAYER_CONSTRUCTION), "Layer $CONSTRUCTION should exist")
        );
        
        // Verify layer 0 (default layer)
        DxfLayer layer0 = layers.get(LAYER_ZERO);
        assertAll("Layer 0 properties",
            () -> assertEquals(LAYER_ZERO, layer0.name(), "Layer name should be 0"),
            () -> assertEquals(-7, layer0.colorNumber(), "Color should be -7"),
            () -> assertEquals("Continuous", layer0.lineType(), "Line type should be Continuous"),
            () -> assertFalse(layer0.isVisible(), "Layer should be invisible")
        );
        
        // Verify z value TN layer
        DxfLayer layerZValue = layers.get(LAYER_Z_VALUE);
        assertAll("z value TN layer properties",
            () -> assertEquals(LAYER_Z_VALUE, layerZValue.name(), "Layer name should be z value TN"),
            () -> assertEquals(-18, layerZValue.colorNumber(), "Color should be -18"),
            () -> assertEquals("Continuous", layerZValue.lineType(), "Line type should be Continuous"),
            () -> assertFalse(layerZValue.isVisible(), "Layer should be invisible")
        );
        
        // Verify croix layer (colored layer)
        DxfLayer layerCroix = layers.get(LAYER_CROIX);
        assertAll("croix layer properties",
            () -> assertEquals(LAYER_CROIX, layerCroix.name(), "Layer name should be croix"),
            () -> assertEquals(-147, layerCroix.colorNumber(), "Color should be -147"),
            () -> assertEquals("Continuous", layerCroix.lineType(), "Line type should be Continuous"),
            () -> assertFalse(layerCroix.isVisible(), "Layer should be invisible")
        );
    }
    
    @Nested
    @DisplayName("Basic DXF File Tests")
    class BasicDxfTests {
        @Test
        @DisplayName("Empty but valid DXF file should have no layers")
        void testEmptyDxf() throws IOException {
            String minimalDxf = """
                0
                SECTION
                2
                HEADER
                0
                ENDSEC
                0
                SECTION
                2
                TABLES
                0
                TABLE
                2
                LAYER
                0
                ENDTAB
                0
                ENDSEC
                0
                SECTION
                2
                ENTITIES
                0
                ENDSEC
                0
                EOF
                """;
            Path dxfFile = createDxfFile("empty.dxf", minimalDxf);

            DxfReader reader = new DxfReader(dxfFile);
            Map<String, DxfLayer> layers = reader.readLayers();
            
            assertTrue(layers.isEmpty(), "Empty DXF should have no layers");
        }

        @Test
        @DisplayName("Invalid DXF file should throw IllegalArgumentException")
        void testInvalidDxf() throws IOException {
            // Test cases for invalid DXF files
            String[] invalidDxfContents = {
                "This is not a DXF file",
                """
                0
                INVALID
                2
                HEADER
                0
                ENDSEC
                """,
                """
                0
                SECTION
                2
                HEADER
                0
                ENDSEC
                0
                INVALID
                """,
                """
                0
                SECTION
                2
                TABLES
                0
                TABLE
                2
                INVALID_TABLE
                0
                ENDTAB
                0
                ENDSEC
                """
            };

            for (String content : invalidDxfContents) {
                Path dxfFile = createDxfFile("invalid.dxf", content);
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                    DxfReader reader = new DxfReader(dxfFile);
                    reader.readLayers();
                }, "Should throw IllegalArgumentException for invalid DXF content: " + content.substring(0, Math.min(20, content.length())));
                
                assertNotNull(exception.getMessage(), "Exception message should not be null");
                assertTrue(exception.getMessage().length() > 0, "Exception should have a descriptive message");
            }
        }

        @Test
        @DisplayName("Non-existent file should throw IOException")
        void testNonexistentFile() {
            Path nonexistentFile = Path.of("nonexistent.dxf");
            
            assertThrows(IOException.class, () -> new DxfReader(nonexistentFile),
                        "Should throw IOException for non-existent file");
        }
    }
}
