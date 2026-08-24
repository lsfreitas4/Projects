package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class BinaryExpressions extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.BINARY_EXPR, this::visit_BinaryOp);
        addVisit(Kind.METHOD_DECL, this::setContextForMethod);
    }

    private Void setContextForMethod(JmmNode method, SymbolTable table) {
        setCurrentMethod(method.get("name")); // Set the method context
        return null;
    }

    private Void visit_BinaryOp(JmmNode expr, SymbolTable table) {
        if (!Kind.check(expr, Kind.BINARY_EXPR)) {
            return null;
        }

        String operator = expr.get("op");
        JmmNode left = expr.getChild(0);
        JmmNode right = expr.getChild(1);

        // Use TypeUtils.getType instead of custom logic
        Type leftType = TypeUtils.getType(left, table, currentMethod);
        Type rightType = TypeUtils.getType(right, table, currentMethod);

        if (leftType == null || rightType == null) {
            if (leftType == null && rightType == null) {
                addReport(newError(expr, "Cannot determine types of both operands for binary operation"));
            } else if (leftType == null) {
                addReport(newError(left, "Cannot determine type of the left operand"));
            } else {
                addReport(newError(right, "Cannot determine type of the right operand"));
            }
            return null;
        }

        // Check type compatibility based on operator
        switch (operator) {
            case "*", "/", "+", "-" -> {
                if (!TypeUtils.isNumeric(leftType) || !TypeUtils.isNumeric(rightType)) {
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            expr.getLine(),
                            expr.getColumn(),
                            "Arithmetic operations require numeric operands, found " +
                                    TypeUtils.typeToString(leftType) + " and " + TypeUtils.typeToString(rightType),
                            null));
                }
            }
            case "&&", "||" -> {
                if (!TypeUtils.isBoolean(leftType) || !TypeUtils.isBoolean(rightType)) {
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            expr.getLine(),
                            expr.getColumn(),
                            "Logical operations require boolean operands, found " +
                                    TypeUtils.typeToString(leftType) + " and " + TypeUtils.typeToString(rightType),
                            null));
                }
            }
            case "<", ">" -> {
                if (!TypeUtils.isNumeric(leftType) || !TypeUtils.isNumeric(rightType)) {
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            expr.getLine(),
                            expr.getColumn(),
                            "Comparison operations require numeric operands, found " +
                                    TypeUtils.typeToString(leftType) + " and " + TypeUtils.typeToString(rightType),
                            null));
                }
            }
            case "==", "!=" -> {
                if (TypeUtils.areTypesCompatible(leftType, rightType, table)) {
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            expr.getLine(),
                            expr.getColumn(),
                            "Equality operations require compatible types, found " +
                                    TypeUtils.typeToString(leftType) + " and " + TypeUtils.typeToString(rightType),
                            null));
                }
            }
        }

        return null;
    }
}
