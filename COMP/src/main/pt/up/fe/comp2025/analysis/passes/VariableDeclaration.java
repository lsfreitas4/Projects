package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class VariableDeclaration extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.VAR_DECL, this::validateVarDecl);
        addVisit(Kind.METHOD_DECL, this::setContextForMethod);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
        addVisit(Kind.ASSIGN_STMT, this::validateAssignStmt);
        addVisit(Kind.IF_STMT, this::validateIfOrWhile);
        addVisit(Kind.WHILE_STMT, this::validateIfOrWhile);
        addVisit(Kind.VAR_DECL, this::validateVoidVariableDeclaration);
    }

    private Void setContextForMethod(JmmNode method, SymbolTable table) {
        setCurrentMethod(method.get("name")); // Set the method context
        return null;
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        String varRefName = varRefExpr.get("name");

        if (table.getParameters(currentMethod).stream().anyMatch(p -> p.getName().equals(varRefName))) return null;
        if (table.getLocalVariables(currentMethod).stream().anyMatch(v -> v.getName().equals(varRefName))) return null;
        if (table.getFields().stream().anyMatch(f -> f.getName().equals(varRefName))) return null;
        if (table.getImports().contains(varRefName)) return null;

        addReport(Report.newError(
                Stage.SEMANTIC,
                varRefExpr.getLine(),
                varRefExpr.getColumn(),
                String.format("Variable '%s' does not exist.", varRefName),
                null
        ));

        return null;
    }

    private Void validateVarDecl(JmmNode varDecl, SymbolTable table) {
        // Get the type node and the variable name
        JmmNode typeNode = varDecl.getChild(0);
        List<Symbol> locals = null;
        if (!(currentMethod == null))
            locals = table.getLocalVariables(currentMethod);
        String typeName = typeNode.get("name");

        // Check for void type
        if ("void".equals(typeName)) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    varDecl.getLine(),
                    varDecl.getColumn(),
                    "Variable cannot be declared with type 'void'.",
                    null
            ));
        }

        if (locals != null && isMutipleLocal(locals, varDecl, table)) {
            addReport(newError(varDecl, "Invalid type '" + typeName + "' for variable"));
       }

        // Check for array type (array of non-valid types)
        if (typeNode.getKind().equals(Kind.ARRAY_TYPE.toString())) {
            // Extract base type (without the '[]')
            String baseType = typeName.replace("[]", "");
            if (isValidType(baseType, table)) {
                addReport(newError(typeNode, "Invalid array base type '" + baseType + "'"));
            }
        }

        // General type validation (non-array types)
        if (isValidType(typeName, table)) {
            addReport(newError(typeNode, "Invalid type '" + typeName + "' for variable"));
        }

        if (currentMethod != null) {
            checkDuplicateSymbols(table.getLocalVariables(currentMethod), varDecl, "local variable");
            checkDuplicateSymbols(table.getParameters(currentMethod), varDecl, "parameter");
        }
        checkDuplicateSymbols(table.getFields(), varDecl, "field");

        return null;
    }

    private boolean isMutipleLocal(List<Symbol> locals, JmmNode name, SymbolTable table) {
        int counter = 0;
        for (Symbol local : locals) {
            if(local.getName().equals(name.get("name")) && local.getType().getName().equals(table.getClassName())) {
                counter++;
            }
        }
        if (counter > 1) return true;
        return false;
    }
    private boolean isValidType(String typeName, SymbolTable table) {
        // Primitive types are always valid
        if ("int".equals(typeName) || "boolean".equals(typeName) || "String".equals(typeName)) {
            return false;
        }

        // Check if it's a known class or the current class
        return !table.getImports().contains(typeName) && !typeName.equals(table.getClassName()) &&
                (table.getSuper() == null || !typeName.equals(table.getSuper()));
    }

    private Void validateAssignStmt(JmmNode assignStmt, SymbolTable table) {
        JmmNode lhs = assignStmt.getChild(0); // geralmente VarRefExpr
        JmmNode rhs = assignStmt.getChild(1); // qualquer expressão

        Type lhsType = TypeUtils.getType(lhs, table, currentMethod);
        Type rhsType = TypeUtils.getType(rhs, table, currentMethod);

        if (lhsType == null || rhsType == null) {
            addReport(newError(assignStmt, "Cannot resolve types in assignment."));
            return null;
        }

        if (!TypeUtils.areTypesCompatible(rhsType, lhsType, table)) {
            addReport(newError(assignStmt, "Incompatible types in assignment: expected '" +
                    TypeUtils.typeToString(lhsType) + "', got '" + TypeUtils.typeToString(rhsType) + "'."));
        }

        return null;
    }

    private Void validateIfOrWhile(JmmNode node, SymbolTable table) {
        JmmNode condition = node.getChild(0); // first child is condition
        Type condType = TypeUtils.getType(condition, table, currentMethod);

        if (condType == null) {
            addReport(newError(condition, "Cannot determine type of conditional expression."));
            return null;
        }

        if (!TypeUtils.isBoolean(condType)) {
            addReport(newError(condition, "Condition must be of type boolean, but got '" +
                    TypeUtils.typeToString(condType) + "'."));
        }

        return null;
    }

    private Void validateVoidVariableDeclaration(JmmNode node, SymbolTable table) {
        JmmNode typeNode = node.getChild(0);
        Type type = TypeUtils.convertType(typeNode);

        if ("void".equals(type.getName()) && !type.isArray()) {
            System.out.println("Error: Variable '" + node.get("name") + "' cannot have type void.");
            addReport(newError(node, "Variables cannot be declared with type 'void'."));
        }

        return null;
    }

    private void checkDuplicateSymbols(List<Symbol> symbols, JmmNode node, String symbolType) {
        String name = node.get("name");
        long count = symbols.stream().filter(s -> s.getName().equals(name)).count();
        if (count > 1) {
            addReport(newError(node, "Duplicate " + symbolType + ": " + name));
        }
    }

    private Void validateCondition(JmmNode node, SymbolTable table) {
        JmmNode condition = node.getChild(0);
        Type condType = TypeUtils.getType(condition, table, currentMethod);

        if (condType == null) {
            addReport(newError(condition, "Cannot determine type of condition"));
            return null;
        }

        if (!TypeUtils.isBoolean(condType)) {
            addReport(newError(condition,
                    "Condition must be boolean, found " + TypeUtils.typeToString(condType)));
        }

        if (condType.isArray()) {
            addReport(newError(condition, "Cannot use array as condition"));
        }

        return null;
    }
}
