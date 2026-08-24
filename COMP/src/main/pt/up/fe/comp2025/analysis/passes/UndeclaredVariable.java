package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.List;

public class UndeclaredVariable extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        String methodName = method.get("name");
        setCurrentMethod(methodName);

        //validateMethodReturns(method, table);

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
}
