package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.Instruction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisterAllocator {
    private final int maxRegisters;

    public RegisterAllocator(int maxRegisters) {
        this.maxRegisters = maxRegisters;
    }

    public void allocate(ClassUnit classUnit) {
        for (Method method : classUnit.getMethods()) {
            processMethod(method);
        }
    }

    private void processMethod(Method method) {
        List<Instruction> instructions = method.getInstructions();

        // Step 1: Run liveness analysis
        LivenessAnalyzer livenessAnalyzer = new LivenessAnalyzer();
        livenessAnalyzer.analyze(instructions);

        // Step 2: Build interference graph
        InterferenceGraph interferenceGraph = new InterferenceGraph();
        interferenceGraph.build(instructions, livenessAnalyzer.getOut(), 
                              livenessAnalyzer.getDef(), method);

        // Step 3: Color the graph
        GraphColoring coloring = new GraphColoring(interferenceGraph.getGraph(), maxRegisters);
        coloring.color();

        // Step 4: Apply register allocation based on coloring
        applyRegisterAllocation(method, coloring.getColors());

        // Print variable table after allocation
        System.out.println("\nVariable table after allocation:");
        method.getVarTable().forEach((name, desc) -> 
            System.out.println(name + " -> reg: " + desc.getVirtualReg() + 
                             ", scope: " + desc.getScope()));
    }

    private void applyRegisterAllocation(Method method, Map<String, Integer> colorMap) {
        var varTable = method.getVarTable();
        int maxRegUsed = 1; // Start at 1 since we know we'll use 0 and 1

        // Reset all registers
        varTable.values().forEach(desc -> desc.setVirtualReg(-1));

        // Fixed assignments for 'this' and parameters
        if (varTable.containsKey("this")) {
            varTable.get("this").setVirtualReg(0);
        }

        // Parameters get register 1
        for (Element param : method.getParams()) {
            if (param instanceof Operand) {
                String paramName = ((Operand) param).getName();
                if (varTable.containsKey(paramName)) {
                    varTable.get(paramName).setVirtualReg(1);
                }
            }
        }

        // For all other variables, use the coloring result
        Map<Integer, Integer> colorToReg = new HashMap<>();

        for (var entry : varTable.entrySet()) {
            String varName = entry.getKey();
            var desc = entry.getValue();

            // Skip if already assigned
            if (desc.getVirtualReg() != -1) continue;

            Integer color = colorMap.get(varName);
            if (color != null) {
                // Map each new color to a register, reusing when possible
                if (!colorToReg.containsKey(color)) {
                    colorToReg.put(color, maxRegUsed+1);
                    maxRegUsed++;
                }
                desc.setVirtualReg(colorToReg.get(color));
            }
        }
    }
}