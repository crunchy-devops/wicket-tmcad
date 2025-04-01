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
    @TempDir
    Path tempDir;
    
    private Path createDxfFile(String filename, String content) throws IOException {
        Path dxfFile = tempDir.resolve(filename);
        Files.writeString(dxfFile, content);
        return dxfFile;
    }
    
    @Nested
    @DisplayName("Basic DXF File Tests")
    class BasicDxfTests {
        @Test
        @DisplayName("Empty DXF file should have no layers")
        void testEmptyDxf() throws IOException {
            String minimalDxf = """
                0
                SECTION
                2
                HEADER
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
        @DisplayName("Invalid DXF file should return empty layer map")
        void testInvalidDxf() throws IOException {
            Path dxfFile = createDxfFile("invalid.dxf", "This is not a DXF file");

            DxfReader reader = new DxfReader(dxfFile);
            Map<String, DxfLayer> layers = reader.readLayers();
            
            assertTrue(layers.isEmpty(), "Invalid DXF should have no layers");
        }

        @Test
        @DisplayName("Non-existent file should throw IOException")
        void testNonexistentFile() {
            Path nonexistentFile = Path.of("nonexistent.dxf");
            
            assertThrows(IOException.class, () -> new DxfReader(nonexistentFile),
                        "Should throw IOException for non-existent file");
        }
    }
    
    @Nested
    @DisplayName("Layer Tests")
    class LayerTests {
        private static final String LAYER_NAME = "z value TN";
        private static final String HIDDEN_LAYER = "HIDDEN_LAYER";
        
        @Test
        @DisplayName("Layer with TEXT entity should be correctly read")
        void testLayerWithText() throws IOException {
            String dxfContent = """
                0
                SECTION
                2
                TABLES
                0
                TABLE
                2
                LAYER
                0
                LAYER
                2
                %s
                70
                0
                62
                7
                6
                CONTINUOUS
                0
                ENDTAB
                0
                ENDSEC
                0
                SECTION
                2
                ENTITIES
                0
                TEXT
                8
                %s
                10
                100.0
                20
                200.0
                1
                300.0
                0
                ENDSEC
                0
                EOF
                """.formatted(LAYER_NAME, LAYER_NAME);
            
            Path dxfFile = createDxfFile("text.dxf", dxfContent);
            DxfReader reader = new DxfReader(dxfFile);
            Map<String, DxfLayer> layers = reader.readLayers();
            
            assertTrue(layers.containsKey(LAYER_NAME), "Layer should exist");
            DxfLayer layer = layers.get(LAYER_NAME);
            
            // Verify layer properties
            assertAll("Layer properties",
                () -> assertEquals(LAYER_NAME, layer.name(), "Layer name should match"),
                () -> assertEquals(7, layer.colorNumber(), "Color number should be 7"),
                () -> assertEquals("CONTINUOUS", layer.lineType(), "Line type should be CONTINUOUS"),
                () -> assertTrue(layer.isVisible(), "Layer should be visible")
            );
            
            // Verify TEXT entity
            assertAll("TEXT entity",
                () -> assertEquals(1, layer.entities().size(), "Should have one entity"),
                () -> assertEquals("TEXT", layer.entities().get(0).type(), "Entity type should be TEXT"),
                () -> assertEquals("100.0", layer.entities().get(0).groupCodes().get(10), "X coordinate"),
                () -> assertEquals("200.0", layer.entities().get(0).groupCodes().get(20), "Y coordinate"),
                () -> assertEquals("300.0", layer.entities().get(0).groupCodes().get(1), "Text content")
            );
        }

        @Test
        @DisplayName("Invisible layer should have negative color and be marked invisible")
        void testInvisibleLayer() throws IOException {
            String dxfContent = """
                0
                SECTION
                2
                TABLES
                0
                TABLE
                2
                LAYER
                0
                LAYER
                2
                %s
                70
                0
                62
                -7
                6
                CONTINUOUS
                0
                ENDTAB
                0
                ENDSEC
                0
                EOF
                """.formatted(HIDDEN_LAYER);
            
            Path dxfFile = createDxfFile("invisible.dxf", dxfContent);
            DxfReader reader = new DxfReader(dxfFile);
            Map<String, DxfLayer> layers = reader.readLayers();
            
            assertTrue(layers.containsKey(HIDDEN_LAYER), "Hidden layer should exist");
            DxfLayer layer = layers.get(HIDDEN_LAYER);
            
            assertAll("Hidden layer properties",
                () -> assertEquals(HIDDEN_LAYER, layer.name(), "Layer name should match"),
                () -> assertEquals(-7, layer.colorNumber(), "Color should be -7"),
                () -> assertEquals("CONTINUOUS", layer.lineType(), "Line type should be CONTINUOUS"),
                () -> assertFalse(layer.isVisible(), "Layer should be invisible")
            );
        }
    }
}
