package com.crunchydevops.dxf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicTestDxf {
    private static final String PROJECT_DXF = "data/project.dxf";

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(PROJECT_DXF));
        Set<String> layerNames = new HashSet<>();
        
        for (int i = 0; i < lines.size() - 2; i++) {
            String line = lines.get(i);
            if (line.trim().equals("AcDbEntity")) {
                String nextLine = lines.get(i + 1).trim();
                if (nextLine.equals("8")) {
                    String layerName = lines.get(i + 2).trim();
                    layerNames.add(layerName);
                }
            }
        }
        
        System.out.println("Found " + layerNames.size() + " unique layer names:");
        List<String> sortedNames = new ArrayList<>(layerNames);
        sortedNames.sort(String.CASE_INSENSITIVE_ORDER);
        for (String name : sortedNames) {
            System.out.println("- " + name);
        }
    }
}
