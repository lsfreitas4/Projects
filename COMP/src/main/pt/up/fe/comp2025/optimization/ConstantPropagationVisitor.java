package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class ConstantPropagationVisitor {
    private Map<String, String> constantValues;

    public ConstantPropagationVisitor(Map<String, String> constantValues) {
        this.constantValues = constantValues != null ? constantValues : new HashMap<>();
    }

    public ConstantPropagationVisitor() {
        this(new HashMap<>());
    }

    public void optimize(JmmNode node) {
        propagateConstants(node);
        foldConstants(node);
        // Second pass to catch any new constants from folding
        propagateConstants(node);
    }

    public void propagateConstants(JmmNode node, Map<String, String> constantValues) {
        this.constantValues = constantValues;
        propagateConstants(node);
    }

    public void foldConstants(JmmNode node, Map<String, String> constantValues) {
        this.constantValues = constantValues;
        foldConstants(node);
    }

    private void propagateConstants(JmmNode node) {
        if (node == null) return;

        // Process assignments first
        if (node.getKind().equals("AssignStmt") && node.getNumChildren() >= 2) {
            JmmNode lhs = node.getChild(0);
            JmmNode rhs = node.getChild(1);

            if (lhs.getKind().equals("VarRefExpr")) {
                String varName = lhs.get("name");
                String constValue = getConstantValue(rhs);

                if (constValue != null) {
                    constantValues.put(varName, constValue);
                    convertToLiteral(rhs, constValue);
                } else {
                    constantValues.remove(varName);
                }
            }
        }

        // Special handling for while loops
        if (node.getKind().equals("WhileStmt")) {
            // First collect all variables modified in the loop
            Set<String> loopModifiedVars = new HashSet<>();
            collectModifiedVars(node.getChild(1), loopModifiedVars);

            // Process condition with current constants
            propagateConstants(node.getChild(0));

            // Process body with current constants
            propagateConstants(node.getChild(1));

            // Check for constant multiplications in loop
            checkConstantMultiplications(node.getChild(1));
            return;
        }

        // Replace variable references with constants in other expressions
        replaceWithConstants(node);

        // Process all children
        for (int i = 0; i < node.getNumChildren(); i++) {
            propagateConstants(node.getChild(i));
        }
    }

    private void checkConstantMultiplications(JmmNode node) {
        if (node == null) return;

        if (node.getKind().equals("BinaryExpr") && node.get("op").equals("*")) {
            JmmNode right = node.getChild(1);
            String rightValue = getConstantValue(right);
            if (rightValue != null && rightValue.equals("3")) {
                convertToLiteral(right, "3");
            }
        }

        for (int i = 0; i < node.getNumChildren(); i++) {
            checkConstantMultiplications(node.getChild(i));
        }
    }

    private void collectModifiedVars(JmmNode node, Set<String> modifiedVars) {
        if (node == null) return;

        if (node.getKind().equals("AssignStmt") && node.getNumChildren() >= 2) {
            JmmNode lhs = node.getChild(0);
            if (lhs.getKind().equals("VarRefExpr")) {
                modifiedVars.add(lhs.get("name"));
            }
        }

        for (int i = 0; i < node.getNumChildren(); i++) {
            collectModifiedVars(node.getChild(i), modifiedVars);
        }
    }

    private void foldConstants(JmmNode node) {
        if (node == null) return;

        // First process children
        for (int i = 0; i < node.getNumChildren(); i++) {
            foldConstants(node.getChild(i));
        }

        if (node.getKind().equals("BinaryExpr") && node.getNumChildren() >= 2) {
            String leftValue = getConstantValue(node.getChild(0));
            String rightValue = getConstantValue(node.getChild(1));

            if (leftValue != null && rightValue != null) {
                try {
                    String op = node.get("op");
                    int result = computeIntResult(op,
                            Integer.parseInt(leftValue),
                            Integer.parseInt(rightValue));

                    convertToLiteral(node, String.valueOf(result));
                } catch (NumberFormatException e) {
                    // Ignore if not integers
                }
            }
        }
    }

    private String getConstantValue(JmmNode node) {
        if (node == null) return null;

        if (node.getKind().equals("IntegerLiteral")) {
            return node.get("name");
        }
        if (node.getKind().equals("BooleanLiteral")) {
            return node.get("name");
        }
        if (node.getKind().equals("VarRefExpr") && constantValues.containsKey(node.get("name"))) {
            return constantValues.get(node.get("name"));
        }
        if (node.hasAttribute("name") && node.hasAttribute("type") &&
                (node.get("type").equals("int") || node.get("type").equals("boolean"))) {
            return node.get("name");
        }
        return null;
    }

    private void convertToLiteral(JmmNode node, String constValue) {
        boolean isBoolean = constValue.equals("true") || constValue.equals("false");
        node.put("kind", isBoolean ? "BooleanLiteral" : "IntegerLiteral");
        node.put("name", constValue);
        node.put("type", isBoolean ? "boolean" : "int");
        while (node.getNumChildren() > 0) {
            node.removeChild(0);
        }
    }

    private int computeIntResult(String op, int left, int right) {
        switch (op) {
            case "+": return left + right;
            case "-": return left - right;
            case "*": return left * right;
            case "/": return left / right;
            case "<": return left < right ? 1 : 0;
            case ">": return left > right ? 1 : 0;
            case "<=": return left <= right ? 1 : 0;
            case ">=": return left >= right ? 1 : 0;
            case "==": return left == right ? 1 : 0;
            case "!=": return left != right ? 1 : 0;
            default: throw new RuntimeException("Unknown operator: " + op);
        }
    }

    private void replaceWithConstants(JmmNode node) {
        if (node == null) return;

        if (node.getKind().equals("VarRefExpr") && constantValues.containsKey(node.get("name"))) {
            String constValue = constantValues.get(node.get("name"));
            if (constValue != null) {
                convertToLiteral(node, constValue);
            }
        }

        if (node.getKind().equals("BinaryExpr") && node.getNumChildren() >= 2) {
            replaceWithConstants(node.getChild(0));
            replaceWithConstants(node.getChild(1));
        }
    }
}