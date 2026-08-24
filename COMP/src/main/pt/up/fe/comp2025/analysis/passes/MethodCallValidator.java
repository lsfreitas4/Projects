package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

import static pt.up.fe.comp2025.ast.Kind.check;

public class MethodCallValidator extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_CALL, this::validateMethodCall);
        addVisit(Kind.METHOD_DECL, this::setContextForMethod);
        addVisit(Kind.RETURN_STMT, this::validateReturnStmt);
    }

    private Void setContextForMethod(JmmNode method, SymbolTable table) {
        if (method.hasAttribute("stat")){
            table.put(method.get("name"), "1");
        } else table.put(method.get("name"), "0");
        setCurrentMethod(method.get("name")); // Set the method context
        return null;
    }

    private void validateArrayAccess(JmmNode node, SymbolTable table) {
        JmmNode arrayExpr = node.getChild(0);
        JmmNode arrayIndex = node.getChild(1);

        Type arrayType = TypeUtils.getType(arrayExpr, table, currentMethod);
        Type indexType = TypeUtils.getType(arrayIndex, table, currentMethod);

        if (arrayType == null || !arrayType.isArray()) {
            addReport(newError(node, "Cannot access a non-array type."));
        }
        System.out.println("ArrayType -> " + arrayType.isArray());
        System.out.println("IndexType -> " + indexType.getName());

        if (!indexType.getName().equals("int")){
            addReport(newError(node, "Index type is not int."));
        }
    }

    private Void validateReturnStmt(JmmNode node, SymbolTable table) {
        JmmNode returnExpr = node.getChild(0);
        Type methodReturnType = table.getReturnType(currentMethod);
        Type returnType = TypeUtils.getType(returnExpr, table, currentMethod);
        if (check(returnExpr, Kind.METHOD_CALL)) {
            validateMethodCall(returnExpr, table);
        }
        else if (check(returnExpr, Kind.ARRAY_ACCESS_EXPR)){
            validateArrayAccess(returnExpr, table);
        }
        else if (returnType != null && !TypeUtils.areTypesCompatible(returnType, methodReturnType, table)){
            addReport(newError(node, "Return type not what was expected by the method."));
        }
        return null;
    }

    private Void validateMethodCall(JmmNode methodCall, SymbolTable table) {
        if (!methodCall.hasAttribute("name")) {
            addReport(newError(methodCall, "MethodCallExpr node does not contain attribute 'name'."));
            return null;
        }

        String methodName = methodCall.get("name");

        JmmNode objectRef = methodCall.getChild(0);
        Type objectType = getArgumentType(objectRef, table);

        if (objectType == null) {
            addReport(newError(methodCall, "Cannot determine the type of the object in method call."));
            return null;
        }
//        if (currentMethod != null) {
//            if (check(objectRef, Kind.THIS_EXPR) && table.get(methodName).equals("1")) {
//                addReport(newError(methodCall, "Static methods doesn't accept 'this' before a method call"));
//                return null;
//            }
//        }

        if (isImportedClass(objectType, table)) {
            // Method call on imported class, assume it is valid
            return null;
        }

        if (isCurrentClass(objectType, table)) {
            // Object is of current class
            if (currentClassExtendsImported(table)) {
                // If current class extends imported class, assume method exists
                return null;
            }
            if (!table.getMethods().contains(methodName)) {
                addReport(newError(methodCall, "Method '" + methodName + "' does not exist in the current class."));
                return null;
            }
        } else {
            // Other cases: object is not current class, not imported, cannot validate
            addReport(newError(methodCall, "Cannot call method '" + methodName + "' on unknown type '" + objectType.getName() + "'."));
            return null;
        }
        validateMethodArguments(methodCall, table);
        return null;
    }

    private void validateMethodArguments(JmmNode methodCall, SymbolTable table) {
        String methodName = methodCall.get("name");
        List<Symbol> params = table.getParameters(methodName);
        int expectedParams = params.size();
        int actualArgs = methodCall.getChildren().size() - 1; // excluding objectRef (child 0)

        if (params.isEmpty()) {
            if (actualArgs > 0) {
                addReport(newError(methodCall, "Method '" + methodName + "' does not take any arguments."));
            }
            return;
        }

        Symbol lastParam = params.getLast();
        boolean isVarargs = lastParam.getType().isArray();

        if (!isVarargs && expectedParams != actualArgs) {
            addReport(newError(methodCall, "Argument count mismatch for method '" + methodName +
                    "'. Expected " + expectedParams + ", got " + actualArgs + "."));
            return;
        }

        int fixedParamCount = isVarargs ? expectedParams - 1 : expectedParams;
        System.out.println("Parameters size -> " + fixedParamCount);

        // Check fixed parameters
        for (int i = 0; i < fixedParamCount; i++) {
            JmmNode argument = methodCall.getChild(i);
            Type argType = TypeUtils.getType(argument, table, currentMethod);
            Type paramType = params.get(i).getType();

            System.out.println("argType -> " + argType.getName());
            System.out.println("paramType -> " + argType.getName());


            if (argType == null) {
                addReport(newError(argument, "Cannot determine the type of the argument at index " + i + "."));
                continue;
            }

            if (!TypeUtils.areTypesCompatible(argType, paramType, table)) {
                addReport(newError(argument, "Argument type mismatch at index " + i +
                        ": expected " + TypeUtils.typeToString(paramType) +
                        ", got " + TypeUtils.typeToString(argType) + "."));
            }
        }

        // Check varargs parameters
        if (isVarargs) {
            Type elementType = new Type(lastParam.getType().getName(), false);

            for (int i = 1; i < fixedParamCount; i++) {
                JmmNode argument = methodCall.getChild(i + 1);
                Type argType = getArgumentType(argument, table);

                if (argType == null) {
                    addReport(newError(argument, "Cannot determine the type of the varargs argument at index " + (i - fixedParamCount) + "."));
                    continue;
                }

                if (TypeUtils.areTypesCompatible(argType, elementType, table)) {
                    addReport(newError(argument, "Varargs element type mismatch at index " + (i - fixedParamCount) +
                            ": expected " + TypeUtils.typeToString(elementType) +
                            ", got " + TypeUtils.typeToString(argType) + "."));
                }
            }
        }
    }

    private Type getArgumentType(JmmNode argument, SymbolTable table) {
        switch (argument.getKind()) {
            case "IntegerLiteral":
                return TypeUtils.newIntType();
            case "BooleanLiteral":
                return TypeUtils.newBooleanType();
            case "StringLiteral":
                return TypeUtils.getType(argument, table);
            case "VarRefExpr":
                return TypeUtils.getTypeFromName(argument.get("name"), table, currentMethod);
            default:
                return TypeUtils.getType(argument, table, currentMethod);
        }
    }

    private boolean isImportedClass(Type type, SymbolTable table) {
        if (type == null) {
            return false;
        }
        return table.getImports().contains(type.getName());
    }

    private boolean isCurrentClass(Type type, SymbolTable table) {
        if (type == null) {
            return false;
        }
        return type.getName().equals(table.getClassName());
    }

    private boolean currentClassExtendsImported(SymbolTable table) {
        String superClass = table.getSuper();
        return superClass != null && table.getImports().contains(superClass);
    }
}
