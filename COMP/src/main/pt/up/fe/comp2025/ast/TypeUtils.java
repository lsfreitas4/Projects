package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

/**
 * Utility methods for type handling and conversion.
 */
public class TypeUtils {
    private SymbolTable symbolTable;

    // Constructor for instance usage (used by Ollir)
    public TypeUtils(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    // Static factory method for convenience
    public static TypeUtils withSymbolTable(SymbolTable symbolTable) {
        return new TypeUtils(symbolTable);
    }

    public Type getExprType(JmmNode node, String currentMethod)
    {
        return getType(node, this.symbolTable, currentMethod);

    }
    // Instance method (used by Ollir)
    public Type getExprType(JmmNode node) {
        return getType(node, this.symbolTable, null);
    }

    // Primitive type factories
    public static Type newIntType() {
        return new Type("int", false);
    }

    public static Type newBooleanType() {
        return new Type("boolean", false);
    }

    public static Type newStringType() {
        return new Type("String", false);
    }

    public static Type newVoidType() {
        return new Type("void", false);
    }

    // Array type factories
    public static Type newIntArrayType() {
        return new Type("int", true);
    }

    public static Type newBooleanArrayType() {
        return new Type("boolean", true);
    }

    public static Type newStringArrayType() {
        return new Type("String", true);
    }

    // Object type factories
    public static Type newObjectType(String className) {
        return new Type(className, false);
    }

    public static Type newObjectArrayType(String className) {
        return new Type(className, true);
    }


    /**
     * Checks if the given variable name corresponds to a field in the current class.
     * @param varName the variable name to check
     * @return true if the variable is a field in the current class, false otherwise
     */
    public boolean isField(String varName) {
        // Check if the variable name corresponds to a field in the class
        for (Symbol field : symbolTable.getFields()) {
            if (field.getName().equals(varName)) {
                return true; // It's a field
            }
        }
        return false; // It's not a field
    }

    /**
     * Converts a type node from AST to Type object.
     */
    public static Type convertType(JmmNode typeNode) {
        String name = typeNode.get("name");
        boolean isArray = typeNode.getKind().equals("ArrayType");
        boolean isVarArgs = typeNode.hasAttribute("isVarArgs") &&
                Boolean.parseBoolean(typeNode.get("isVarArgs"));

        if (isArray || isVarArgs) {
            return switch (name) {
                case "int" -> newIntArrayType();
                case "boolean" -> newBooleanArrayType();
                case "String" -> newStringArrayType();
                default -> newObjectArrayType(name);
            };
        }

        return switch (name) {
            case "int" -> newIntType();
            case "boolean" -> newBooleanType();
            case "String" -> newStringType();
            case "void" -> newVoidType();
            default -> newObjectType(name);
        };
    }

    /**
     * Gets the type of a variable by name.
     */
    public static Type getTypeFromName(String varName, SymbolTable table, String methodName) {
        // Check local variables
        if (methodName != null) {
            for (Symbol local : table.getLocalVariables(methodName)) {
                if (local.getName().equals(varName)) {
                    return local.getType();
                }
            }

            // Check parameters
            for (Symbol param : table.getParameters(methodName)) {
                if (param.getName().equals(varName)) {
                    return param.getType();
                }
            }
        }

        // Check fields
        for (Symbol field : table.getFields()) {
            if (field.getName().equals(varName)) {
                return field.getType();
            }
        }

        // Check if class name
        if (varName.equals(table.getClassName())) {
            return newObjectType(table.getClassName());
        }

        // Check imports
        for (String imp : table.getImports()) {
            if (imp.endsWith(varName)) {
                return newObjectType(varName);
            }
        }

        return null;
    }

    /**
     * Gets the type of an expression node (2 arguments).
     */
    public static Type getType(JmmNode node, SymbolTable table) {
        return getType(node, table, null);  // Calls the new overloaded method with 3 arguments
    }

    /**
     * Gets the type of an expression node (3 arguments).
     */
    public static Type getType(JmmNode node, SymbolTable table, String methodName) {
        switch (node.getKind()) {
            case "IntegerLiteral":
                return newIntType();
            case "BooleanLiteral":
                return newBooleanType();
            case "StringLiteral":
                return newStringType();
            case "VarRefExpr":
                return getTypeFromName(node.get("name"), table, methodName);  // Use methodName if it's provided
            case "ThisExpr":
                return newObjectType(table.getClassName());
            case "NewArrayExpr":
                return newIntArrayType();
            case "ArrayAccessExpr":
                Type arrayType = getType(node.getChild(0), table, methodName);
                return arrayType != null ? new Type(arrayType.getName(), false) : null;
            case "ArrayInitExpr":
                if (node.getNumChildren() == 0) return newIntArrayType();
                Type firstType = getType(node.getChild(0), table, methodName);
                return firstType != null ? new Type(firstType.getName(), true) : null;
            case "NewObjectExpr":
                return newObjectType(node.get("name"));
            case "MethodCall":
                String tmp = node.get("name");
                return table.getMethods().contains(tmp)
                        ? table.getReturnType(tmp)
                        : null;
            case "BinaryExpr":
                String op = node.get("op");
                return op.equals("<") || op.equals(">") || op.equals("&&") || op.equals("||")
                        ? newBooleanType()
                        : newIntType();
            case "NegateExpr":
                return newBooleanType();
            case "LengthExpr":
                return newIntType();
            case "ParExpr":
                return getType(node.getChild(0), table, methodName);
            default:
                return null;
        }
    }

    /**
     * Checks type compatibility for assignments.
     */
    public static boolean areTypesCompatible(Type rightType, Type leftType, SymbolTable table) {
        // 1. Handle direct equality
        boolean ret = false;
        if (rightType.getName().equals(leftType.getName())) {
            return true; }

        // 2. Handle object inheritance (subclass -> superclass assignment)
        String currentClass = table.getClassName();
        String superClass = table.getSuper();
        if (superClass != null) { // Only valid if a superclass exists
            if (rightType.getName().equals(currentClass) && leftType.getName().equals(superClass)) {
                return true; // Subclass can be assigned to superclass
            }
        }

        boolean rightImported = table.getImports().stream().anyMatch(imp -> imp.endsWith(rightType.getName()));
        boolean leftImported = table.getImports().stream().anyMatch(imp -> imp.endsWith(leftType.getName()));

        System.out.println("Right imported (" + rightType.getName() + ") -> " + rightImported);
        System.out.println("Left imported (" + leftType.getName() + ") -> " + leftImported);
        // If both types are imported, assume they are compatible
        if (rightImported && leftImported) {
            return true;
        }

        // 4. Default: incompatible
        return false;

    }

    /**
     * Checks if a type is a primitive type (int, boolean, or void).
     * Note: String is not considered a primitive type in Java.
     */
    public static boolean isPrimitive(Type type) {
        if (type == null) {
            return false;
        }
        String typeName = type.getName();
        return typeName.equals("int") ||
                typeName.equals("boolean") ||
                typeName.equals("void");
    }

    /**
     * Checks if a type name string represents a primitive type.
     */
    public static boolean isPrimitive(String typeName) {
        if (typeName == null) {
            return false;
        }
        return typeName.equals("int") ||
                typeName.equals("boolean") ||
                typeName.equals("void");
    }

    // =========================
    // 🆕 ADDED UTILITY METHODS
    // =========================

    /**
     * Checks if a type is numeric (int but not array).
     */
    public static boolean isNumeric(Type type) {
        return type != null && type.getName().equals("int") && !type.isArray();
    }

    /**
     * Checks if a type is boolean (boolean but not array).
     */
    public static boolean isBoolean(Type type) {
        return type != null && type.getName().equals("boolean") && !type.isArray();
    }

    /**
     * Checks if a type is an object type (i.e., not primitive or array of primitive).
     */
    public static boolean isObjectType(Type type) {
        if (type == null) return false;
        String name = type.getName();
        return !name.equals("int") && !name.equals("boolean") && !name.equals("void");
    }

    /**
     * Converts a Type to a readable string.
     */
    public static String typeToString(Type type) {
        if (type == null) return "null";
        return type.getName() + (type.isArray() ? "[]" : "");
    }
}
