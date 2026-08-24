package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.InstructionType;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.inst.Instruction;
import org.specs.comp.ollir.*;

import java.util.*;

public class LivenessAnalyzer {
    private final Map<Instruction, Set<String>> in = new HashMap<>();
    private final Map<Instruction, Set<String>> out = new HashMap<>();
    private final Map<Instruction, Set<String>> def = new HashMap<>();
    private final Map<Instruction, Set<String>> use = new HashMap<>();

    private Set<String> computeUse(Instruction inst) {
        Set<String> useSet = new HashSet<>();

        if (inst instanceof AssignInstruction assignInst) {
            addOperandUses(assignInst.getRhs(), useSet);
        } else if (inst instanceof ReturnInstruction retInst) {
            // Handle return instruction operands using getOperand() which returns Optional<Element>
            retInst.getOperand().ifPresent(element -> addIfOperand(element, useSet));
        } else {
            addOperandUses(inst, useSet);
        }

        System.out.println("USE set for instruction " + inst + ": " + useSet);
        return useSet;
    }

    public void analyze(List<Instruction> instructions) {
        // Initialize sets
        System.out.println("\n=== Initializing Liveness Analysis ===");
        for (Instruction inst : instructions) {
            def.put(inst, computeDef(inst));
            use.put(inst, computeUse(inst));
            in.put(inst, new HashSet<>());
            out.put(inst, new HashSet<>());
        }

        // Build successor relationships
        Map<Instruction, Set<Instruction>> successors = new HashMap<>();
        for (int i = 0; i < instructions.size(); i++) {
            Instruction inst = instructions.get(i);
            Set<Instruction> succ = new HashSet<>();
            
            // Add fall-through successor if not last instruction and not a return
            if (i < instructions.size() - 1 && !(inst instanceof ReturnInstruction)) {
                succ.add(instructions.get(i + 1));
            }
            
            // Add branch targets
            for (Node node : inst.getSuccessors()) {
                if (node instanceof Instruction succInst) {
                    succ.add(succInst);
                }
            }
            
            successors.put(inst, succ);
        }

        // Iterative dataflow analysis
        boolean changed;
        int iteration = 0;
        do {
            changed = false;
            System.out.println("\n=== Iteration " + (++iteration) + " ===");
            
            // Iterate in reverse order for backward analysis
            for (int i = instructions.size() - 1; i >= 0; i--) {
                Instruction inst = instructions.get(i);
                
                Set<String> oldIn = new HashSet<>(in.get(inst));
                Set<String> oldOut = new HashSet<>(out.get(inst));

                // OUT[n] = ∪ IN[s] for all successors s
                Set<String> newOut = new HashSet<>();
                for (Instruction succ : successors.get(inst)) {
                    newOut.addAll(in.get(succ));
                }
                out.put(inst, newOut);

                // IN[n] = use[n] ∪ (out[n] - def[n])
                Set<String> newIn = new HashSet<>(use.get(inst));
                Set<String> outMinusDef = new HashSet<>(newOut);
                outMinusDef.removeAll(def.get(inst));
                newIn.addAll(outMinusDef);
                in.put(inst, newIn);

                System.out.println("\nInstruction: " + inst);
                System.out.println("Old IN: " + oldIn);
                System.out.println("New IN: " + newIn);
                System.out.println("Old OUT: " + oldOut);
                System.out.println("New OUT: " + newOut);

                if (!oldIn.equals(newIn) || !oldOut.equals(newOut)) {
                    changed = true;
                }
            }
        } while (changed);

        // Print final analysis
        System.out.println("\n=== Final Analysis ===");
        instructions.forEach(inst -> {
            System.out.println("\nInstruction: " + inst);
            System.out.println("IN:  " + in.get(inst));
            System.out.println("OUT: " + out.get(inst));
            System.out.println("DEF: " + def.get(inst));
            System.out.println("USE: " + use.get(inst));
        });
    }

    private Set<String> computeDef(Instruction inst) {
        Set<String> defSet = new HashSet<>();
        
        if (inst instanceof AssignInstruction assignInst) {
            Element dest = assignInst.getDest();
            if (dest instanceof Operand operand) {
                defSet.add(operand.getName());
            }
        }
        
        return defSet;
    }

    private void addOperandUses(Instruction inst, Set<String> useSet) {
        if (inst instanceof BinaryOpInstruction binOp) {
            addIfOperand(binOp.getLeftOperand(), useSet);
            addIfOperand(binOp.getRightOperand(), useSet);
        } else if (inst instanceof UnaryOpInstruction unaryOp) {
            addIfOperand(unaryOp.getOperand(), useSet);
        } else if (inst instanceof SingleOpInstruction singleOp) {
            addIfOperand(singleOp.getSingleOperand(), useSet);
        }
    }

    private void addIfOperand(Element element, Set<String> useSet) {
        if (element instanceof Operand operand && !operand.isLiteral()) {
            useSet.add(operand.getName());
        }
    }

    public Map<Instruction, Set<String>> getIn() { return in; }
    public Map<Instruction, Set<String>> getOut() { return out; }
    public Map<Instruction, Set<String>> getDef() { return def; }
}