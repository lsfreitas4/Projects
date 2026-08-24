package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class ConditionValidator extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.IF_STMT, this::validateIfCondition);
        addVisit(Kind.WHILE_STMT, this::validateWhileCondition);
        addVisit(Kind.METHOD_DECL, this::setContextForMethod);
    }

    private Void setContextForMethod(JmmNode method, SymbolTable table) {
        setCurrentMethod(method.get("name")); // Set the method context
        return null;
    }

    private Void validateIfCondition(JmmNode node, SymbolTable table) {
        JmmNode condition = node.getChild(0);  // Get the condition of the if statement
        return validateCondition(condition, table, "if statement");
    }

    private Void validateWhileCondition(JmmNode node, SymbolTable table) {
        JmmNode condition = node.getChild(0);  // Get the condition of the while statement
        return validateCondition(condition, table, "while statement");
    }

    private Void validateCondition(JmmNode condition, SymbolTable table, String statementType) {
        Type conditionType = TypeUtils.getType(condition, table, currentMethod);  // Use TypeUtils to get the type of the condition

        if (conditionType == null) {
            addReport(newError(condition, "Condition has an unknown type."));
            return null;
        }

        // If the condition is not boolean, report an error
        if (!TypeUtils.isBoolean(conditionType)) {
            addReport(newError(condition, "Condition of " + statementType + " must be of type boolean, found " +
                    TypeUtils.typeToString(conditionType)));
        }

        // Check if the condition is an array
        if (conditionType.isArray()) {
            addReport(newError(condition, "Condition of " + statementType + " cannot be an array."));
        }

        return null;
    }
}
