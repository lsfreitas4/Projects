package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class AssignmentValidator extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.ASSIGN_STMT, this::validateAssignment);
        addVisit(Kind.METHOD_DECL, this::setContextForMethod);
    }

    private Void setContextForMethod(JmmNode method, SymbolTable table) {
        setCurrentMethod(method.get("name")); // Set the method context
        return null;
    }

    private Void validateAssignment(JmmNode node, SymbolTable table) {
        JmmNode left = node.getChild(0);
        JmmNode right = node.getChild(1);

        if (isArrayInitExpr(right)) {
            Type leftType = TypeUtils.getType(left, table, currentMethod);
            if (leftType != null && !leftType.isArray()) {
                addReport(newError(left, "Cannot assign an array initializer to a non-array variable."));
            }
            return null;
        }

        if (isArrayInitExpr(right)) {
            return null;
        }

        Type leftType = TypeUtils.getType(left, table, currentMethod);
        Type rightType = TypeUtils.getType(right, table, currentMethod);
        if (isArrayInitExpr(right) && (leftType == null || !leftType.isArray())) {
            addReport(newError(left, "Cannot assign an array initializer to a non-array variable."));
        }


        if (leftType == null) {
            addReport(newError(left, "Variable '" + left.get("name") + "' has an unknown type."));
            return null;
        }
        if (rightType == null) {
            if (right.getKind().equals("NewArrayExpr")) {
                addReport(newError(right, "Array creation expression has no type information."));
            } else {
                addReport(newError(right, "Expression has an unknown type."));
            }
            return null;
        }

        if (!TypeUtils.areTypesCompatible(leftType, rightType, table) && !doesExtend(rightType, leftType, table)) {
            addReport(newError(node,
                    "Incompatible types in assignment: cannot assign " +
                            TypeUtils.typeToString(rightType) + " to " + TypeUtils.typeToString(leftType)));
        }

        return null;
    }

    private boolean isArrayInitExpr(JmmNode node) {
        return node != null && node.getKind().equals("ArrayInitExpr");
    }

    private boolean doesExtend(Type subType, Type superType, SymbolTable table) {
        if (subType == null || superType == null) {
            return false;
        }

        String currentClass = table.getClassName();
        String superClass = table.getSuper();

        // Only need to check if we are dealing with the current class and its superclass
        if (subType.getName().equals(currentClass) && superClass != null) {
            return superClass.equals(superType.getName());
        }

        return false;
    }
}
