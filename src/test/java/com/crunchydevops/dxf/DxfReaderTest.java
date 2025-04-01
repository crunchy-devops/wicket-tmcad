package com.crunchydevops.dxf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DxfReaderTest {
    
    @Test
    void testReadEmptyDxf(@TempDir Path tempDir) throws IOException {
        // Create a minimal DXF file
        Path dxfFile = tempDir.resolve("empty.dxf");
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
        Files.writeString(dxfFile, minimalDxf);

        DxfReader reader = new DxfReader(dxfFile);
        Map<String, DxfLayer> layers = reader.readLayers();
        
        assertTrue(layers.isEmpty());
    }

    @Test
    void testReadLayerWithText(@TempDir Path tempDir) throws IOException {
        // Create a DXF file with one layer and one TEXT entity
        Path dxfFile = tempDir.resolve("text.dxf");
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
            z value TN
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
            z value TN
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
            """;
        Files.writeString(dxfFile, dxfContent);

        DxfReader reader = new DxfReader(dxfFile);
        Map<String, DxfLayer> layers = reader.readLayers();
        
        // Verify layer
        assertTrue(layers.containsKey("z value TN"));
        DxfLayer layer = layers.get("z value TN");
        assertEquals("z value TN", layer.name());
        assertEquals(7, layer.colorNumber());
        assertEquals("CONTINUOUS", layer.lineType());
        assertTrue(layer.isVisible());
        
        // Verify TEXT entity
        assertEquals(1, layer.entities().size());
        DxfEntity entity = layer.entities().get(0);
        assertEquals("TEXT", entity.type());
        assertEquals("100.0", entity.groupCodes().get(10));
        assertEquals("200.0", entity.groupCodes().get(20));
        assertEquals("300.0", entity.groupCodes().get(1));
    }

    @Test
    void testInvisibleLayer(@TempDir Path tempDir) throws IOException {
        // Create a DXF file with an invisible layer (negative color)
        Path dxfFile = tempDir.resolve("invisible.dxf");
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
            HIDDEN_LAYER
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
            """;
        Files.writeString(dxfFile, dxfContent);

        DxfReader reader = new DxfReader(dxfFile);
        Map<String, DxfLayer> layers = reader.readLayers();
        
        assertTrue(layers.containsKey("HIDDEN_LAYER"));
        DxfLayer layer = layers.get("HIDDEN_LAYER");
        assertEquals("HIDDEN_LAYER", layer.name());
        assertEquals(-7, layer.colorNumber()); // Negative color preserved
        assertEquals("CONTINUOUS", layer.lineType());
        assertFalse(layer.isVisible()); // Layer should be invisible
    }

    @Test
    void testInvalidDxfFile(@TempDir Path tempDir) throws IOException {
        // Create an invalid DXF file
        Path dxfFile = tempDir.resolve("invalid.dxf");
        String invalidDxf = "This is not a DXF file";
        Files.writeString(dxfFile, invalidDxf);

        DxfReader reader = new DxfReader(dxfFile);
        Map<String, DxfLayer> layers = reader.readLayers();
        
        assertTrue(layers.isEmpty());
    }

    @Test
    void testNonexistentFile() {
        Path nonexistentFile = Path.of("nonexistent.dxf");
        
        assertThrows(IOException.class, () -> new DxfReader(nonexistentFile));
    }
}
