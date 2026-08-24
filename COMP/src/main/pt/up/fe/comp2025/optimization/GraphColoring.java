package pt.up.fe.comp2025.optimization;

import java.util.*;

public class GraphColoring {
    private final Map<String, Set<String>> interferenceGraph;
    private final int maxRegisters;
    private final Map<String, Integer> colors = new HashMap<>();

    public GraphColoring(Map<String, Set<String>> interferenceGraph, int maxRegisters) {
        this.interferenceGraph = interferenceGraph;
        this.maxRegisters = maxRegisters;
        System.out.println("\n=== Starting Graph Coloring ===");
        System.out.println("Max registers: " + maxRegisters);
        System.out.println("Initial interference graph:");
        interferenceGraph.forEach((var, neighbors) -> 
            System.out.println(var + " -> interferes with: " + neighbors));
    }

    public void color() {
        List<String> nodes = new ArrayList<>(interferenceGraph.keySet());
        System.out.println("\nInitial nodes to color: " + nodes);
        
        // Handle special registers first
        if (nodes.contains("this")) {
            colors.put("this", 0);
            nodes.remove("this");
            System.out.println("Assigned register 0 to 'this'");
        }
        if (nodes.contains("arg")) {
            colors.put("arg", 1);
            nodes.remove("arg");
            System.out.println("Assigned register 1 to parameter 'arg'");
        }
        
        // Sort nodes by degree (number of interferences)
        nodes.sort((a, b) -> interferenceGraph.get(b).size() - interferenceGraph.get(a).size());
        System.out.println("\nNodes sorted by interference count: " + nodes);

        // Color remaining nodes
        for (String node : nodes) {
            System.out.println("\nProcessing node: " + node);
            Set<String> interferingNodes = interferenceGraph.get(node);
            System.out.println("Interferes with: " + interferingNodes);
            
            // If node has no interferences, try to reuse a register
            if (interferingNodes.isEmpty()) {
                int lowestUsed = colors.values().stream()
                                     .min(Integer::compareTo)
                                     .orElse(-1)+2;
                int color = Math.max(0, lowestUsed);
                colors.put(normalize(node), color);
                System.out.println("No interferences - reusing register " + color);
                continue;
            }

            // Get colors already used by neighbors
            Set<Integer> usedColors = new HashSet<>();
            for (String neighbor : interferingNodes) {
                Integer neighborColor = colors.get(normalize(neighbor));
                if (neighborColor != null) {
                    usedColors.add(neighborColor);
                    System.out.println("Neighbor " + neighbor + " uses color " + neighborColor);
                }
            }

            // Find lowest available color
            int color = 2;
            while (usedColors.contains(color)) {
                color++;
            }

            colors.put(normalize(node), color);
            System.out.println("Assigned color " + color + " to " + node);
        }

        System.out.println("\n=== Final Coloring Results ===");
        colors.forEach((var, color) -> 
            System.out.println(var + " -> color: " + color));
    }

    private String normalize(String var) {
        return var.replaceAll("(Operand: |LiteralElement: |\\.INT32|\\(SingleOp\\)|Inst: BINARYOPER |Inst: NOPER |ADD )", "")
                 .replaceAll("\\s+", " ")
                 .trim();
    }

    public Map<String, Integer> getColors() {
        return colors;
    }
}