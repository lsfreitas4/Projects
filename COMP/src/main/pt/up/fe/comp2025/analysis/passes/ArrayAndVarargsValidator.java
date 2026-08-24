package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;

public class ArrayAndVarargsValidator extends AnalysisVisitor {
    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.ARRAY_INIT_EXPR, this::validateArrayInitializer);
        addVisit(Kind.METHOD_CALL_EXPR, this::validateVarargsCall);
        addVisit(Kind.PARAM, this::validateVarargsDeclaration);
    }

    private Void validateArrayInitializer(JmmNode node, SymbolTable table) {
        System.out.println("Validating Array Initialization: " + node);

        // Validate array initialization (e.g., a = new int[]{10, 20})
        if (node.getKind().equals("ArrayInitExpr")) {
            Type arrayType = TypeUtils.getType(node, table);
            if (arrayType == null) {
                System.out.println("Error: Unable to infer array type for array initializer at node: " + node);
                addReport(newError(node, "Unable to infer array type for array initializer"));
                return null;
            }

            // Ensure the array type is correctly inferred
            Type elementType = TypeUtils.getType(node.getChild(0), table);
            if (elementType == null) {
                System.out.println("Error: Array element has an unknown type at node: " + node);
                addReport(newError(node, "Array element has an unknown type"));
                return null;
            }

            // Check if all array elements have the correct type
            if (!arrayType.getName().equals(elementType.getName())) {
                System.out.println("Error: Array initializer elements must be of type " + arrayType.getName());
                addReport(newError(node, "Array initializer elements must be of type " + arrayType.getName()));
            }

            // Check all elements of the array initializer
            for (JmmNode element : node.getChildren()) {
                Type childElementType = TypeUtils.getType(element, table);
                if (childElementType == null) {
                    System.out.println("Error: Array element has an unknown type at element: " + element);
                    addReport(newError(element, "Array element has an unknown type"));
                } else if (!childElementType.getName().equals(elementType.getName()) || childElementType.isArray()) {
                    System.out.println("Error: Array initializer elements must match the type of the array at element: " + element);
                    addReport(newError(element, "Array initializer elements must match the type of the array"));
                }
            }
        }
        return null;
    }

    private Type getExpectedArrayType(JmmNode node, SymbolTable table) {
        JmmNode parent = node.getParent();
        if (parent.isInstance(Kind.ASSIGN_STMT)) {
            JmmNode lhs = parent.getChild(0); // left-hand side of assignment
            Type lhsType = TypeUtils.getType(lhs, table);
            if (lhsType != null && lhsType.isArray()) {
                System.out.println("Expected array type inferred from left-hand side: " + lhsType);
                return lhsType; // Return the expected array type directly from the LHS
            }
        } else if (parent.isInstance(Kind.RETURN_STMT)) {
            // For return statements, return the expected return type (usually an array)
            System.out.println("Expected array type inferred from return statement for method: " + currentMethod);
            return table.getReturnType(currentMethod);
        }
        return null;
    }

    private Void validateVarargsCall(JmmNode node, SymbolTable table) {
        String methodName = node.get("name");
        System.out.println("Validating varargs call for method: " + methodName);

        List<Symbol> params = table.getParameters(methodName);
        if (params.isEmpty()) {
            System.out.println("No parameters found for method: " + methodName);
            return null;
        }

        Symbol lastParam = params.get(params.size() - 1);
        if (!lastParam.getType().isArray()) {
            System.out.println("Method " + methodName + " does not have varargs, skipping varargs validation.");
            return null; // Not varargs
        }

        List<JmmNode> args = node.getChildren().subList(1, node.getNumChildren());

        // Check fixed parameters
        for (int i = 0; i < params.size() - 1; i++) {
            if (i >= args.size()) {
                System.out.println("Error: Missing argument for parameter " + params.get(i).getName());
                addReport(newError(node, "Missing argument for parameter " + params.get(i).getName()));
                continue;
            }
            Type argType = TypeUtils.getType(args.get(i), table);
            System.out.println("Checking argument " + args.get(i) + " of type " + argType + " against expected type " + params.get(i).getType());
            if (!argType.equals(params.get(i).getType())) {
                System.out.println("Error: Argument type mismatch for parameter " + params.get(i).getName());
                addReport(newError(node, "Argument type mismatch for parameter " + params.get(i).getName()));
            }
        }

        // Check varargs
        if (args.size() >= params.size()) {
            Type expected = TypeUtils.newIntType();
            for (int i = params.size() - 1; i < args.size(); i++) {
                Type argType = TypeUtils.getType(args.get(i), table);
                System.out.println("Checking varargs argument " + args.get(i) + " of type " + argType + " against expected type " + expected);
                if (!argType.equals(expected)) {
                    System.out.println("Error: Varargs argument must be int, found " + argType);
                    addReport(newError(node, "Varargs argument must be int"));
                }
            }
        }
        return null;
    }

    private Void validateVarargsDeclaration(JmmNode node, SymbolTable table) {
        if (!node.getChild(0).hasAttribute("isVarArgs")) return null;

        // Check position is last
        JmmNode method = node.getParent();
        if (node != method.getChildren().get(method.getNumChildren() - 1)) {
            addReport(newError(node, "Varargs parameter must be last"));
        }

        // Check type is int[]
        Type type = TypeUtils.convertType(node.getChild(0));
        if (!type.equals(TypeUtils.newIntArrayType())) {
            addReport(newError(node, "Varargs must be of type int[]"));
        }

        // Check only one varargs parameter
        long varargsCount = method.getChildren().stream()
                .filter(n -> n.getChild(0).hasAttribute("isVarArgs"))
                .count();
        if (varargsCount > 1) {
            addReport(newError(node, "Only one varargs parameter allowed"));
        }

        return null;
    }
}
