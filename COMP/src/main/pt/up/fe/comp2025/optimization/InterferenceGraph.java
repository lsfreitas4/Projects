package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.Instruction;

import java.util.*;

public class InterferenceGraph {
    private final Map<String, Set<String>> graph = new HashMap<>();

    public void build(List<Instruction> instructions, Map<Instruction, Set<String>> out, Map<Instruction, Set<String>> def, Method method) {
        // Initialize graph with all variables
        for (var entry : method.getVarTable().entrySet()) {
            graph.put(normalize(entry.getKey()), new HashSet<>());
        }

        // Build interference between variables that are live at the same time
        for (int i = 0; i < instructions.size(); i++) {
            Instruction inst = instructions.get(i);
            Set<String> liveOut = out.get(inst);

            // Make all variables in liveOut interfere with each other
            List<String> liveVars = new ArrayList<>(liveOut);
            for (int j = 0; j < liveVars.size(); j++) {
                String var1 = normalize(liveVars.get(j));
                for (int k = j + 1; k < liveVars.size(); k++) {
                    String var2 = normalize(liveVars.get(k));
                    if (!var1.equals(var2)) {
                        addInterference(var1, var2);
                    }
                }
            }

            // Variables defined in this instruction interfere with variables live after it
            Set<String> defined = def.get(inst);
            if (defined != null) {
                for (String defVar : defined) {
                    String normalizedDef = normalize(defVar);
                    for (String liveVar : liveOut) {
                        String normalizedLive = normalize(liveVar);
                        if (!normalizedDef.equals(normalizedLive)) {
                            addInterference(normalizedDef, normalizedLive);
                        }
                    }
                }
            }
        }

        // Ensure parameters get different registers
        for (Element param : method.getParams()) {
            String paramName = normalize(param.toString());
            for (Element otherParam : method.getParams()) {
                String otherName = normalize(otherParam.toString());
                if (!paramName.equals(otherName)) {
                    addInterference(paramName, otherName);
                }
            }
        }

        System.out.println("\nFinal interference graph:");
        graph.forEach((var, neighbors) -> System.out.println(var + " interferes with " + neighbors));
    }

    private void addInterference(String var1, String var2) {
        graph.computeIfAbsent(var1, k -> new HashSet<>()).add(var2);
        graph.computeIfAbsent(var2, k -> new HashSet<>()).add(var1);
    }

    private String normalize(String varName) {
        return varName.replaceAll("(Operand: |LiteralElement: |\\.INT32|\\(SingleOp\\)|Inst: BINARYOPER |Inst: NOPER |ADD )", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public Map<String, Set<String>> getGraph() {
        return graph;
    }
}