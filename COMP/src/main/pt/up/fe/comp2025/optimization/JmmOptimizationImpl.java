package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.CompilerConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JmmOptimizationImpl implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        var visitor = new OllirGeneratorVisitor(semanticsResult.getSymbolTable());
        var ollirCode = visitor.visit(semanticsResult.getRootNode());
        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        if (semanticsResult.getConfig().get("optimize") != null &&
                semanticsResult.getConfig().get("optimize").equals("true")) {
            ConstantPropagationVisitor visitor = new ConstantPropagationVisitor();
            Map<String, String> constantValues = new HashMap<>();

            // First perform constant propagation
            visitor.propagateConstants(semanticsResult.getRootNode(), constantValues);

            // Then perform constant folding (with updated constant values)
            visitor.foldConstants(semanticsResult.getRootNode(), constantValues);

            // One more propagation pass to catch any new constants from folding
            visitor.propagateConstants(semanticsResult.getRootNode(), constantValues);
        }
        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        int maxRegisters = CompilerConfig.getRegisterAllocation(ollirResult.getConfig());
        if (maxRegisters >= 0) {
            ClassUnit classUnit = ollirResult.getOllirClass();
            RegisterAllocator allocator = new RegisterAllocator(maxRegisters);
            allocator.allocate(classUnit);
        }
        return ollirResult;
    }
}