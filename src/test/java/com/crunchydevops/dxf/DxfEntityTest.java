package com.crunchydevops.dxf;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DxfEntityTest {

    @Test
    void testCreateEntity() {
        String type = "TEXT";
        Map<Integer, String> groupCodes = new HashMap<>();
        groupCodes.put(10, "100.0");  // X coordinate
        groupCodes.put(20, "200.0");  // Y coordinate
        groupCodes.put(1, "300.0");   // Text content (Z value)

        DxfEntity entity = new DxfEntity(type, groupCodes);
        
        assertEquals(type, entity.type());
        assertEquals(groupCodes, entity.groupCodes());
        assertEquals("100.0", entity.groupCodes().get(10));
        assertEquals("200.0", entity.groupCodes().get(20));
        assertEquals("300.0", entity.groupCodes().get(1));
    }

    @Test
    void testToString() {
        String type = "LINE";
        Map<Integer, String> groupCodes = new HashMap<>();
        groupCodes.put(10, "0.0");   // Start X
        groupCodes.put(20, "0.0");   // Start Y
        groupCodes.put(11, "1.0");   // End X
        groupCodes.put(21, "1.0");   // End Y

        DxfEntity entity = new DxfEntity(type, groupCodes);
        String toString = entity.toString();
        
        assertTrue(toString.contains("Entity Type: LINE"));
        //assertTrue(toString.contains("Properties:"));
        assertTrue(toString.contains("10=0.0"));
        assertTrue(toString.contains("20=0.0"));
        assertTrue(toString.contains("11=1.0"));
        assertTrue(toString.contains("21=1.0"));
    }

    @Test
    void testEquality() {
        Map<Integer, String> codes1 = new HashMap<>();
        codes1.put(10, "1.0");
        codes1.put(20, "2.0");

        Map<Integer, String> codes2 = new HashMap<>();
        codes2.put(10, "1.0");
        codes2.put(20, "2.0");

        DxfEntity entity1 = new DxfEntity("TEXT", codes1);
        DxfEntity entity2 = new DxfEntity("TEXT", codes2);
        DxfEntity entity3 = new DxfEntity("LINE", codes1);

        // Test equals
        assertEquals(entity1, entity2);
        assertNotEquals(entity1, entity3);
        assertNotEquals(entity1, null);
        assertEquals(entity1, entity1);

        // Test hashCode
        assertEquals(entity1.hashCode(), entity2.hashCode());
        assertNotEquals(entity1.hashCode(), entity3.hashCode());
    }
}
