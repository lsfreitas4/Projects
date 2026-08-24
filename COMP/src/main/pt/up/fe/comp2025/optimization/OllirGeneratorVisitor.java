package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String NL = "\n";
    private final String L_BRACKET = " {\n";
    private final String R_BRACKET = "}\n";
    private String currentMethod;


    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    private final OllirExprGeneratorVisitor exprVisitor;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
        this.exprVisitor = new OllirExprGeneratorVisitor(table);
    }


    @Override
    protected void buildVisitor() {
        addVisit(PROGRAM, this::visitProgram);
        addVisit(CLASS_DECL, this::visitClass);
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(PARAM, this::visitParam);
        addVisit(RETURN_STMT, this::visitReturn);
        addVisit(ASSIGN_STMT, this::visitAssignStmt);
        addVisit(IMPORT_STMT, this::visitImportStmt);
        addVisit(EXPR_STMT, this::visitExprStmt);
        addVisit(IF_STMT, this::visitIfStmt);
        addVisit(BLOCK_STMT, this::visitBlockStmt);
        addVisit(WHILE_STMT, this::visitWhileStmt);
        addVisit(NEW_OBJECT_EXPR, this::visitNewObject);
        addVisit(NEW_ARRAY_EXPR, this::visitNewArray);
        addVisit(ARRAY_STORE, this::visitArrayStore);
        addVisit(ARRAY_ASSIGN, this::visitArrayAssignment);

        setDefaultVisit(this::defaultVisit);
    }

    private String visitArrayAssignment(JmmNode node, Void unused) {
        // Array assignment has 3 children: array, index, value
        OllirExprResult arrayResult = exprVisitor.visit(node.getChild(0));
        OllirExprResult indexResult = exprVisitor.visit(node.getChild(1));
        OllirExprResult valueResult = exprVisitor.visit(node.getChild(2));

        return arrayResult.getComputation() +
                indexResult.getComputation() +
                valueResult.getComputation() +
                arrayResult.getCode() + "[" + indexResult.getCode() + "]" +
                " :=.i32 " + valueResult.getCode() + ";\n";
    }

    private String visitNewObject(JmmNode node, Void unused) {
        String className = node.get("name");
        String tempVar = ollirTypes.nextTemp();
        return tempVar + "." + className + " :=." + className + " new(" + className + ")." + className + ";\n" +
                "invokespecial(" + tempVar + ", \"<init>\").V;\n";
    }

    private String visitNewArray(JmmNode node, Void unused) {
        OllirExprResult sizeResult = exprVisitor.visit(node.getChild(0));
        String tempVar = ollirTypes.nextTemp();
        return sizeResult.getComputation() +
                tempVar + ".array.i32 :=.array.i32 new(array, " + sizeResult.getCode() + ").array.i32;\n";
    }

    private String visitArrayStore(JmmNode node, Void unused) {
        OllirExprResult arrayResult = exprVisitor.visit(node.getChild(0));
        OllirExprResult indexResult = exprVisitor.visit(node.getChild(1));
        OllirExprResult valueResult = exprVisitor.visit(node.getChild(2));

        return arrayResult.getComputation() +
                indexResult.getComputation() +
                valueResult.getComputation() +
                arrayResult.getCode() + "[" + indexResult.getCode() + "].i32 :=.i32 " + valueResult.getCode() + ";\n";
    }



    private String visitIfStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        // Visit the condition
        OllirExprResult condition = exprVisitor.visit(node.getChild(0));
        code.append(condition.getComputation());

        // Labels
        String thenLabel = "if_then_" + ollirTypes.nextLabelId();
        String endLabel = "if_end_" + ollirTypes.nextLabelId();

        // Conditional jump to THEN (correct behavior)
        code.append("if (").append(condition.getCode()).append(") goto ").append(thenLabel).append(";\n");

        // ELSE block (falls through if condition is false)
        if (node.getNumChildren() > 2) {
            String elseBlock = visit(node.getChild(2));
            code.append(elseBlock);
        }

        // Jump over THEN after executing ELSE
        code.append("goto ").append(endLabel).append(";\n");

        // THEN label and block
        code.append(thenLabel).append(":\n");
        String thenBlock = visit(node.getChild(1));
        code.append(thenBlock);

        // END label
        code.append(endLabel).append(":\n");

        return code.toString();
    }


    private String visitBlockStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        // Optional: Add opening brace (though OLLIR doesn't need it)
        // code.append("{\n");

        // Visit all statements in the block
        for (var child : node.getChildren()) {
            String childCode = visit(child);
            // Add indentation to each line
            code.append(childCode.lines()
                    .map(line -> "    " + line)
                    .collect(Collectors.joining("\n")));
            code.append("\n");
        }

        // Optional: Add closing brace
        // code.append("}\n");

        return code.toString();
    }

    private String visitWhileStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        // Generate unique labels
        String startLabel = "while_start_" + ollirTypes.nextLabelId();
        String endLabel = "while_end_" + ollirTypes.nextLabelId();

        // Add start label
        code.append(startLabel).append(":\n");

        // Visit the condition (first child)
        OllirExprResult condition = exprVisitor.visit(node.getChild(0));

        // Convert condition to boolean if needed
        String conditionCode;
        if (condition.getCode().endsWith(".i32")) {
            // Create temp boolean variable for comparison
            String tempVar = ollirTypes.nextTemp();
            code.append(condition.getComputation());
            code.append(tempVar).append(".bool :=.bool ")
                    .append(condition.getCode()).append(" !=.bool 0.i32;\n");
            conditionCode = tempVar + ".bool";
        } else {
            code.append(condition.getComputation());
            conditionCode = condition.getCode();
        }

        // Add conditional jump to end if condition is false
        code.append("if (").append(conditionCode).append(") goto ").append(endLabel).append(";\n");

        // Visit the body (second child)
        String bodyCode = visit(node.getChild(1));
        code.append(bodyCode);

        // Add unconditional jump back to start
        code.append("goto ").append(startLabel).append(";\n");

        // Add end label
        code.append(endLabel).append(":\n");

        return code.toString();
    }

    private String visitExprStmt(JmmNode node, Void unused) {
        // Only delegate to the expression visitor:
        OllirExprResult exprResult = exprVisitor.visit(node.getChild(0));
        return exprResult.getCode();   // already ends in “;\n” for calls, etc.
    }


    private String visitImportStmt(JmmNode node, Void unused) {
        // No OLLIR output for import statements, just ignore them
        return "";
    }

    private String visitAssignStmt(JmmNode node, Void unused) {
        JmmNode lhs = node.getChild(0);
        OllirExprResult rhsResult = exprVisitor.visit(node.getChild(1));

        StringBuilder code = new StringBuilder();
        code.append(rhsResult.getComputation());

        if (lhs.getKind().equals("VarRefExpr")) {
            String id = lhs.get("name");

            // Skip if this is a numeric literal (invalid as a field name)
            if (id.matches("\\d+")) {
                return code.toString();
            }

            // Handle both regular types and literals
            String ollirType;
            if (node.getChild(1).hasAttribute("type")) {
                String type = node.getChild(1).get("type");
                ollirType = ollirTypes.toOllirType(new Type(type, false));
            } else {
                Type lhsType = types.getExprType(lhs, currentMethod);
                ollirType = ollirTypes.toOllirType(lhsType);
            }

            boolean isLocalOrParam = table.getLocalVariables(currentMethod).stream()
                    .anyMatch(sym -> sym.getName().equals(id)) ||
                    table.getParameters(currentMethod).stream()
                            .anyMatch(sym -> sym.getName().equals(id));

            if (types.isField(id) && !isLocalOrParam) {
                code.append("putfield(this, ").append(id).append(ollirType)
                        .append(", ").append(rhsResult.getCode())
                        .append(")").append(ollirType).append(";\n");
            } else {
                code.append(id).append(ollirType)
                        .append(" :=").append(ollirType)
                        .append(" ").append(rhsResult.getCode())
                        .append(";\n");
            }
        } else if (lhs.getKind().equals("ArrayAccessExpr")) {
            // Handle array assignments
            OllirExprResult arrayResult = exprVisitor.visit(lhs.getChild(0));
            OllirExprResult indexResult = exprVisitor.visit(lhs.getChild(1));

            code.append(arrayResult.getComputation());
            code.append(indexResult.getComputation());

            code.append(arrayResult.getCode())
                    .append("[")
                    .append(indexResult.getCode())
                    .append("].i32 :=.i32 ")
                    .append(rhsResult.getCode())
                    .append(";\n");
        }

        return code.toString();
    }

    private String visitReturn(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        if (node.getNumChildren() == 0) {
            code.append("ret.V;\n");
            return code.toString();
        }

        OllirExprResult expr = exprVisitor.visit(node.getChild(0));
        Type retType = types.getExprType(node.getChild(0), table.get("currentMethod"));
        String ollirType = ollirTypes.toOllirType(retType);

        // Add computation before the return
        code.append(expr.getComputation());
        code.append("ret").append(ollirType).append(" ").append(expr.getCode()).append(";\n");

        return code.toString();
    }

    private String visitParam(JmmNode node, Void unused) {

        var typeCode = ollirTypes.toOllirType(node.getChild(0));
        var id = node.get("name");

        String code = id + typeCode;

        return code;
    }

    private String visitMethodDecl(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder(".method ");

        boolean isPublic = node.getBoolean("isPublic", false);
        boolean isStatic = node.hasAttribute("stat");
        String name = node.get("name");

        if (isPublic) {
            code.append("public ");
        }

        this.currentMethod = name;

        if (isStatic) {
            table.put(name, "1");
        } else {
            table.put(name, "0");
        }
        // name
        code.append(name);
        this.table.put("currentMethod", name);

        // params
        // TODO: Hardcoded for a single parameter, needs to be expanded
        String paramsCode = node.getChildren(PARAM).stream()
                .map(this::visit)
                .collect(Collectors.joining(", "));
        code.append("(").append(paramsCode).append(")");

        // type
        // TODO: Hardcoded for int, needs to be expanded

        Type retType = table.getReturnType(table.get("currentMethod"));

        String ollirType = ollirTypes.toOllirType(retType);

        code.append(ollirType);
        code.append(L_BRACKET);


        // rest of its children stmts
        var stmtsCode = node.getChildren(STMT).stream()
                .map(this::visit)
                .collect(Collectors.joining("\n   ", "   ", ""));

        code.append(stmtsCode);
        // Append `ret.V;` if the return type is void
        if (".V".equals(ollirType)) {
            code.append("\n   ret.V;");
        }
        code.append(R_BRACKET);
        code.append(NL);

        return code.toString();
    }

    private String visitClass(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        code.append(NL);

        // Add the class declaration with optional extends clause

        code.append(table.getClassName());
        String superClass = table.getSuper();

        if (!superClass.isEmpty()) {
            code.append(" extends ").append(superClass).append(" ");
        }

        // Open the class body
        code.append(L_BRACKET);
        code.append(NL);
        for (Symbol field : table.getFields()) {
            String fieldName = field.getName();
            String fieldType = ollirTypes.toOllirType(field.getType());
            code.append(".field private ").append(fieldName).append(fieldType).append(";\n");
        }
        code.append(NL);

        // Build the constructor and add methods
        code.append(buildConstructor());
        code.append(NL);

        for (var child : node.getChildren(METHOD_DECL)) {
            var result = visit(child);
            code.append(result);
        }

        // Close the class body
        code.append(R_BRACKET);

        return code.toString();
    }

    private String buildConstructor() {

        return """
                .construct %s().V {
                    invokespecial(this, "<init>").V;
                }
                """.formatted(table.getClassName());
    }

    private String visitProgram(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        node.getChildren().stream()
                .map(this::visit)
                .forEach(code::append);

        return code.toString();
    }

    /**
     * Default visitor. Visits every child node and return an empty string.
     *
     * @param node
     * @param unused
     * @return
     */
    private String defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return "";
    }

    public String getCurrentMethod() {
        return currentMethod;
    }
}
