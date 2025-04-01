package com.crunchydevops.dxf;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DxfLayerTest {

    @Test
    void testCreateLayer() {
        String name = "z value TN";
        int colorNumber = 7;
        String lineType = "CONTINUOUS";
        boolean isVisible = true;
        List<DxfEntity> entities = new ArrayList<>();

        DxfLayer layer = new DxfLayer(name, colorNumber, lineType, isVisible, entities);
        
        assertEquals(name, layer.name());
        assertEquals(colorNumber, layer.colorNumber());
        assertEquals(lineType, layer.lineType());
        assertTrue(layer.isVisible());
        assertEquals(entities, layer.entities());
    }

    @Test
    void testLayerWithEntities() {
        // Create a layer
        String name = "TEST_LAYER";
        DxfLayer layer = new DxfLayer(name, 1, "CONTINUOUS", true, new ArrayList<>());

        // Create some test entities
        Map<Integer, String> textCodes = new HashMap<>();
        textCodes.put(10, "100.0");
        textCodes.put(20, "200.0");
        textCodes.put(1, "300.0");
        DxfEntity textEntity = new DxfEntity("TEXT", textCodes);

        Map<Integer, String> lineCodes = new HashMap<>();
        lineCodes.put(10, "0.0");
        lineCodes.put(20, "0.0");
        lineCodes.put(11, "1.0");
        lineCodes.put(21, "1.0");
        DxfEntity lineEntity = new DxfEntity("LINE", lineCodes);

        // Create layer with entities
        List<DxfEntity> entities = new ArrayList<>();
        entities.add(textEntity);
        entities.add(lineEntity);

        DxfLayer layerWithEntities = new DxfLayer(name, 1, "CONTINUOUS", true, entities);
        
        assertEquals(2, layerWithEntities.entities().size());
        assertEquals(textEntity, layerWithEntities.entities().get(0));
        assertEquals(lineEntity, layerWithEntities.entities().get(1));
    }

    @Test
    void testLayerEquality() {
        List<DxfEntity> entities1 = new ArrayList<>();
        List<DxfEntity> entities2 = new ArrayList<>();
        
        DxfLayer layer1 = new DxfLayer("Layer1", 1, "CONTINUOUS", true, entities1);
        DxfLayer layer2 = new DxfLayer("Layer1", 1, "CONTINUOUS", true, entities2);
        DxfLayer layer3 = new DxfLayer("Layer2", 1, "CONTINUOUS", true, entities1);

        // Test equals
        assertEquals(layer1, layer2);
        assertNotEquals(layer1, layer3);
        assertNotEquals(layer1, null);
        assertEquals(layer1, layer1);

        // Test hashCode
        assertEquals(layer1.hashCode(), layer2.hashCode());
        assertNotEquals(layer1.hashCode(), layer3.hashCode());
    }

    @Test
    void testInvisibleLayer() {
        DxfLayer layer = new DxfLayer("Hidden", -7, "CONTINUOUS", false, new ArrayList<>());
        
        assertFalse(layer.isVisible());
        assertEquals(-7, layer.colorNumber());
    }
}
