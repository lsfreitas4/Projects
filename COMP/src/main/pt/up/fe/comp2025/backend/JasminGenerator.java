package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.*;

public class JasminGenerator {

    private static final String NL = "\n";
    private static final String TAB = "   ";
    private static final boolean DEBUG = false;

    private final OllirResult ollirResult;
    private final List<Report> reports;
    private final JasminUtils types;
    private final FunctionClassMap<TreeNode, String> generators;

    private int labelCounter = 0;
    private String code = null;
    private Method currentMethod = null;

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
        this.reports = new ArrayList<>();
        this.types = new JasminUtils(ollirResult);

        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(CallInstruction.class, this::generateCall);
        generators.put(PutFieldInstruction.class, this::generatePutField);
        generators.put(GetFieldInstruction.class, this::generateGetField);
        generators.put(SingleOpCondInstruction.class, this::generateSingleOpCond);
        generators.put(GotoInstruction.class, this::generateGoto);
        generators.put(OpCondInstruction.class, this::generateOpCond);
    }

    public String build() {
        if (code == null) {
            code = apply(ollirResult.getOllirClass());
        }
        return code;
    }

    public List<Report> getReports() {
        return reports;
    }

    private String apply(TreeNode node) {
        if (DEBUG) System.out.println("Applying node: " + node);
        return generators.apply(node);
    }


    private void emitLabelIfNeeded(Instruction inst, StringBuilder code) {
        String label = null;
        if (inst instanceof GotoInstruction) label = ((GotoInstruction) inst).getLabel();
        else if (inst instanceof OpCondInstruction) label = ((OpCondInstruction) inst).getLabel();
        else if (inst instanceof SingleOpCondInstruction) label = ((SingleOpCondInstruction) inst).getLabel();

        if (label != null && !label.isEmpty()) {
            code.append(label).append(":").append(NL);
        }
    }

    private String generateClassUnit(ClassUnit classUnit) {
        StringBuilder code = new StringBuilder();

        String className = classUnit.getClassName();
        code.append(".class ").append(className).append(NL)
                .append(".super java/lang/Object").append(NL).append(NL);

        code.append("""
            ;default constructor
            .method public <init>()V
                aload_0
                invokespecial java/lang/Object/<init>()V
                return
            .end method
            """);

        for (Method method : classUnit.getMethods()) {
            if (!method.isConstructMethod()) {
                code.append(apply(method));
            }
        }

        return code.toString();
    }

    private String generateMethod(Method method) {
        this.currentMethod = method;
        this.types.setCurrentMethod(method);

        boolean isMain = method.getMethodName().equals("main");
        StringBuilder code = new StringBuilder();

        String header = isMain
                ? ".method public static main([Ljava/lang/String;)V"
                : ".method " + types.getModifier(method.getMethodAccessModifier()) + method.getMethodName() + "(" +
                method.getParams().stream()
                        .map(p -> types.getJasminType(p.getType().toString()))
                        .reduce("", String::concat) +
                ")" + types.getJasminType(method.getReturnType().toString());

        code.append(header).append(NL);
        code.append(TAB).append(".limit stack 32").append(NL);
        code.append(TAB).append(".limit locals ")
                .append(method.getVarTable().size() + (method.isStaticMethod() || isMain ? 0 : 1))
                .append(NL);

        for (Instruction inst : method.getInstructions()) {
            emitLabelIfNeeded(inst, code);
            String instCode = apply(inst);
            for (String line : instCode.split("\n")) {
                if (!line.isBlank()) code.append(TAB).append(line.trim()).append(NL);
            }
        }

        code.append(".end method").append(NL);
        this.currentMethod = null;
        this.types.setCurrentMethod(null);
        return code.toString();
    }

    private String generateAssign(AssignInstruction assign) {
        StringBuilder code = new StringBuilder();
        code.append(apply(assign.getRhs()));

        if (!(assign.getDest() instanceof Operand operand))
            throw new NotImplementedException(assign.getDest().getClass());

        Descriptor desc = currentMethod.getVarTable().get(operand.getName());
        String storeInstr = types.getStoreInstruction(operand.getType()).trim();
        code.append(desc.getVirtualReg() <= 3
                ? storeInstr + "_" + desc.getVirtualReg()
                : storeInstr + " " + desc.getVirtualReg()).append(NL);
        return code.toString();
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateOperand(Operand operand) {
        return types.getLoadInstruction(operand) + NL;
    }

    private String generateLiteral(LiteralElement literal) {
        return "ldc " + literal.getLiteral() + NL;
    }

    private String generateBinaryOp(BinaryOpInstruction binOp) {
        StringBuilder code = new StringBuilder();
        if (binOp.getOperation().getOpType() == OperationType.AND) {
            // Special handling for logical AND with short-circuit
            String falseLabel = generateUniqueLabel("false");
            String endLabel = generateUniqueLabel("end");

            // Evaluate left operand
            code.append(apply(binOp.getLeftOperand()))
                    .append("ifeq ").append(falseLabel).append(NL)  // Jump if false
                    // If we get here, left was true - evaluate right
                    .append(apply(binOp.getRightOperand()))
                    .append("ifeq ").append(falseLabel).append(NL)  // Jump if false
                    // Both were true
                    .append("iconst_1")
                    .append(NL)
                    .append("goto ").append(endLabel).append(NL)
                    .append(falseLabel).append(":")
                    .append(NL)
                    .append("iconst_0")
                    .append(NL)
                    .append(endLabel).append(":")
                    .append(NL);
            return code.toString();
        }
        code.append(apply(binOp.getLeftOperand()));
        code.append(apply(binOp.getRightOperand()));

        return switch (binOp.getOperation().getOpType()) {
            case ADD -> code.append("iadd").append(NL).toString();
            case SUB -> code.append("isub").append(NL).toString();
            case MUL -> code.append("imul").append(NL).toString();
            case DIV -> code.append("idiv").append(NL).toString();
            case AND -> code.append("iand").append(NL).toString();
            case LTH, GTH -> generateComparison(binOp, code);
            default -> throw new RuntimeException("Unsupported binary op: " + binOp.getOperation().getOpType());
        };
    }

    private String generateComparison(BinaryOpInstruction binOp, StringBuilder code) {
        String trueLabel = generateUniqueLabel("true");
        String endLabel = generateUniqueLabel("end");
        String op = binOp.getOperation().getOpType() == OperationType.LTH ? "if_icmplt" : "if_icmpgt";

        code.append(op).append(" ").append(trueLabel).append(NL)
                .append("iconst_0").append(NL)
                .append("goto ").append(endLabel).append(NL)
                .append(trueLabel).append(":").append(NL)
                .append("iconst_1").append(NL)
                .append(endLabel).append(":").append(NL);

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        StringBuilder code = new StringBuilder();
        returnInst.getOperand().ifPresent(e -> code.append(apply(e)));
        code.append(types.getReturnInstruction(returnInst.getReturnType())).append(NL);
        return code.toString();
    }

    private String generateCall(CallInstruction callInst) {
        StringBuilder code = new StringBuilder();

        if (callInst instanceof NewInstruction newInst) {
            String type = types.getJasminType(newInst.getCaller().getType().toString());
            return "new " + type + NL;
        }

        if (callInst instanceof ArrayLengthInstruction) {
            return apply(callInst.getCaller()) + "arraylength" + NL;
        }

        if (callInst instanceof InvokeSpecialInstruction) {
            String className = types.getJasminType(callInst.getCaller().getType().toString());
            code.append("new ").append(className).append(NL).append("dup").append(NL);
            for (Element arg : callInst.getArguments())
                code.append(types.getLoadInstruction(arg)).append(NL);
            code.append("invokespecial ").append(className).append("/<init>(");
            for (Element arg : callInst.getArguments())
                code.append(types.getJasminType(arg.getType().toString()));
            return code.append(")V").append(NL).toString();
        }

        if (callInst instanceof InvokeStaticInstruction) {
            // Get the actual method name string
            String methodName = callInst.getMethodName().toString();

            // Special handling for print calls
            if (methodName.endsWith("print.STRING") || methodName.endsWith("print")) {
                // Load all arguments
                for (Element arg : callInst.getArguments()) {
                    code.append(types.getLoadInstruction(arg)).append(NL);
                }
                return code.append("invokestatic io/print(I)V").append(NL).toString();
            }

            // Normal static method handling
            String className = types.getJasminType(callInst.getCaller().getType().toString());
            String[] parsed = types.extractClassAndMethod(className, methodName);
            // Load all arguments
            for (Element arg : callInst.getArguments()) {
                code.append(types.getLoadInstruction(arg)).append(NL);
            }

            // Build method descriptor
            code.append("invokestatic ").append(parsed[0]).append("/").append(parsed[1]).append("(");
            for (Element arg : callInst.getArguments()) {
                code.append(types.getJasminType(arg.getType().toString()));
            }
            code.append(")").append(types.getJasminType(callInst.getReturnType().toString())).append(NL);

            return code.toString();
        }

        if (callInst instanceof InvokeVirtualInstruction) {
            String className = types.getJasminType(callInst.getCaller().getType().toString());
            String methodName = callInst.getMethodName().toString();

            String[] parsed = types.extractClassAndMethod(className, methodName);
            // Load the object reference
            code.append(types.getLoadInstruction(callInst.getCaller())).append(NL);

            // Load all arguments
            for (Element arg : callInst.getArguments()) {
                code.append(types.getLoadInstruction(arg)).append(NL);
            }

            // Build method descriptor
            code.append("invokevirtual ").append(parsed[0]).append("/").append(parsed[1]).append("(");
            for (Element arg : callInst.getArguments()) {
                code.append(types.getJasminType(arg.getType().toString()));
            }
            code.append(")").append(types.getJasminType(callInst.getReturnType().toString())).append(NL);

            return code.toString();
        }

        throw new RuntimeException("Unknown call type: " + callInst.getClass());
    }

    private String generateGetField(GetFieldInstruction getField) {
        String className = ollirResult.getOllirClass().getClassName();
        return apply(getField.getObject()) +
                "getfield " + className + "/" +
                getField.getField().getName() + " " +
                types.getJasminType(getField.getFieldType().toString()) + NL;
    }

    private String generatePutField(PutFieldInstruction putField) {
        String className = ollirResult.getOllirClass().getClassName();
        return apply(putField.getObject()) +
                apply(putField.getValue()) +
                "putfield " + className + "/" +
                putField.getField().getName() + " " +
                types.getJasminType(putField.getFieldType().toString()) + NL;
    }

    private String generateSingleOpCond(SingleOpCondInstruction condInst) {
        System.out.println("\n=== DEBUG: Generating SingleOpCond ===");
        System.out.println("Instruction: " + condInst);
        System.out.println("Condition: " + condInst.getCondition());
        System.out.println("Single Operand: " + condInst.getCondition().getSingleOperand());
        System.out.println("Label: " + condInst.getLabel());

        String loadInstr = types.getLoadInstruction(condInst.getCondition().getSingleOperand());
        String branchInstr = "ifeq " + condInst.getLabel();

        System.out.println("Generated:");
        System.out.println(loadInstr);
        System.out.println(branchInstr);

        return loadInstr + NL + branchInstr + NL;
    }

    private String generateGoto(GotoInstruction gotoInst) {
        System.out.println("\n=== DEBUG: Generating Goto ===");
        System.out.println("Instruction: " + gotoInst);
        System.out.println("Label: " + gotoInst.getLabel());

        // For ending if blocks
        if (gotoInst.getLabel().startsWith("endif")) {
            System.out.println("Processing endif block");
            System.out.println("Using end label: " + gotoInst.getLabel());

            String gotoCode = "goto " + gotoInst.getLabel() + NL;
            System.out.println("Generated goto:");
            System.out.println(gotoCode.trim());

            return gotoCode;
        }
        else if (gotoInst.getLabel().startsWith("else")) {
            System.out.println("Processing else block");
            String gotoCode = gotoInst.getLabel() + ":" + NL;
            System.out.println("Generated else label:");
            System.out.println(gotoCode.trim());
            return gotoCode;
        }

        System.out.println("Regular goto");
        String gotoCode = "goto " + gotoInst.getLabel() + NL;
        System.out.println("Generated goto:");
        System.out.println(gotoCode.trim());
        return gotoCode;
    }

    private String generateOpCond(OpCondInstruction condInst) {
        System.out.println("\n=== DEBUG: Generating OpCond ===");
        System.out.println("Instruction: " + condInst);
        System.out.println("Condition: " + condInst.getCondition());
        System.out.println("Operands: " + condInst.getOperands());

        StringBuilder code = new StringBuilder();

        // Evaluate condition
        String conditionCode = apply(condInst.getCondition());
        System.out.println("Condition code generated:");
        System.out.println(conditionCode.trim());
        code.append(conditionCode);

        // Generate labels
        String elseLabel = "else_" + labelCounter++;
        String endLabel = "endif_" + labelCounter++;
        System.out.println("Generated labels:");
        System.out.println("Else label: " + elseLabel);
        System.out.println("End label: " + endLabel);

        // Store labels for later use
        condInst.setLabel(elseLabel);

        String branchCode = "ifeq " + elseLabel;
        System.out.println("Branch code: " + branchCode);
        code.append(branchCode).append(NL);

        System.out.println("Full OpCond generated:");
        System.out.println(code.toString().trim());

        return code.toString();
    }

    // Helper method
    private String generateUniqueLabel(String prefix) {
        String label = prefix + "_" + labelCounter++;
        System.out.println("Generated new label: " + label);
        return label;
    }
}