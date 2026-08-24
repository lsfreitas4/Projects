package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp2025.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;




import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends PreorderJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    public OllirExprGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
    }


    @Override
    protected void buildVisitor() {
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(BINARY_EXPR, this::visitBinExpr);
        addVisit(INTEGER_LITERAL, this::visitInteger);
        addVisit(BOOLEAN_LITERAL, this::visitBoolean);
        addVisit(NEW_ARRAY_EXPR, this::visitNewArrayExpr);
        addVisit(METHOD_CALL, this::visitMethodCall);
        addVisit(ARRAY_ACCESS_EXPR, this::visitArrayAccess);
        addVisit(ARRAY_LENGTH_EXPR, this::visitArrayLength);
        addVisit(NEW_OBJECT_EXPR, this::visitNewObjectExpr);
        addVisit(ARG_LIST, this::visitArgList);
        addVisit(THIS_EXPR, this::visitThisExpr);

        setDefaultVisit(this::defaultVisit);
    }


    private OllirExprResult visitThisExpr(JmmNode node, Void unused) {
        String name = "";
        if (node.hasAttribute("name")) {
            name = node.get("name");
        }
        String ollir = "this." + name;
        return new OllirExprResult(ollir);
    }
    private OllirExprResult visitNewObjectExpr(JmmNode node, Void unused) {
        // Get the class name from the node attribute "name"
        String className = node.get("name");

        // The OLLIR instruction to create a new object is: new(className).classType
        // You need to convert the class name to OLLIR type, which for objects is just ".className"
        String ollirType = "." + className;

        // Compose the OLLIR code for new object creation
        String exprCode = "new(" + className + ")" + ollirType;

        // No extra computation needed (no children)
        return new OllirExprResult(exprCode);
    }

    private OllirExprResult visitArrayLength(JmmNode node, Void unused) {

        OllirExprResult arrayRes = visit(node.getChild(0));

        String tempVar = ollirTypes.nextTemp();

        StringBuilder computation = new StringBuilder();
        computation.append(arrayRes.getComputation());
        computation.append(tempVar).append(".i32 :=.i32 arraylength(")
                .append(arrayRes.getCode()).append(").i32;\n");
        return new OllirExprResult(tempVar + ".i32", computation.toString());
    }



    private OllirExprResult visitArgList(JmmNode node, Void unused) {
        StringBuilder computation = new StringBuilder();
        StringBuilder argCodes = new StringBuilder();

        for (int i = 0; i < node.getNumChildren(); i++) {
            JmmNode arg = node.getChild(i);
            OllirExprResult argRes = visit(arg);

            computation.append(argRes.getComputation());

            // Force complex args into temporaries
            if (argRes.getCode().contains("invoke") || argRes.getCode().matches(".*[+\\-*/].*")) {
                String tempVar = ollirTypes.nextTemp() + ollirTypes.toOllirType(types.getExprType(arg));
                computation.append(tempVar).append(" :=.i32 ").append(argRes.getCode()).append(";\n");
                argRes = new OllirExprResult(tempVar, "");
            }

            if (i > 0) argCodes.append(", ");
            argCodes.append(argRes.getCode());
        }

        return new OllirExprResult(argCodes.toString(), computation.toString());
    }



    /** Lowers methodCall → getfield + invokevirtual, consuming a single ARG_LIST child */
    private OllirExprResult visitMethodCall(JmmNode node, Void unused) {
        String methodName = node.get("name");
        boolean isStatic = true;

        if (table.getMethods().contains(methodName)) {
            isStatic = Objects.equals(table.get(methodName), "1");
        }

        String instr = isStatic ? "invokestatic" : "invokevirtual";

        StringBuilder code = new StringBuilder();
        OllirExprResult obj = null;
        String receiverOrClass;

        if (!isStatic) {
            obj = visit(node.getChild(0));
            code.append(obj.getComputation());

            // Handle anonymous 'new'
            if (check(node.getChild(0), NEW_OBJECT_EXPR)) {
                String tempVar = ollirTypes.nextTemp() + "." + table.getClassName();
                code.append(tempVar).append(" :=.").append(table.getClassName())
                        .append(" ").append(obj.getCode()).append(";\n");
                receiverOrClass = tempVar;
            }
            // Default case
            else {
                receiverOrClass = obj.getCode();
            }
        } else {
            receiverOrClass = table.getClassName();
        }

        // Process arguments (using updated visitArgList)
        OllirExprResult args = OllirExprResult.EMPTY;
        args = visit(node.getChildren(ARG_LIST).get(0));
        code.append(args.getComputation());

        // Build the call
        code.append(instr).append("(").append(receiverOrClass).append(", \"")
                .append(methodName).append("\"");

        if (!args.getCode().isEmpty()) {
            code.append(", ").append(args.getCode());
        }

        Type returnType = table.getReturnType(methodName);
        String ollirReturnType = returnType != null ? ollirTypes.toOllirType(returnType) : ".V";
        code.append(")").append(ollirReturnType).append(";\n");

        return new OllirExprResult(code.toString(), "");
    }



    private OllirExprResult visitArrayAccess(JmmNode node, Void unused) {
        OllirExprResult arrayResult = visit(node.getChild(0));
        OllirExprResult indexResult = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();
        computation.append(arrayResult.getComputation());
        computation.append(indexResult.getComputation());

        // Clean index code, remove trailing semicolon if present
        String indexCode = indexResult.getCode().trim();
        if (indexCode.endsWith(";")) {
            indexCode = indexCode.substring(0, indexCode.length() - 1);
        }

        // If the index computation contains method calls (invoke*), store it in a temp var first
        boolean indexIsComplex = indexResult.getCode().contains("invoke");

        String indexVar;
        if (indexIsComplex) {
            // Create temp var for index
            String tempIndexVar = ollirTypes.nextTemp();
            computation.append(tempIndexVar).append(".i32 :=.i32 ").append(indexCode).append(";\n");
            indexVar = tempIndexVar + ".i32";
        } else {
            indexVar = indexCode;
        }

        // Now generate the array access result
        String tempVar = ollirTypes.nextTemp();
        computation.append(tempVar).append(".i32 :=.i32 ")
                .append(arrayResult.getCode())
                .append("[")
                .append(indexVar)
                .append("].i32;\n");

        return new OllirExprResult(tempVar + ".i32", computation.toString());
    }



    private OllirExprResult visitNewArrayExpr(JmmNode node, Void unused) {
        // Assume: NewArrayExpr has one child - the size expression
        JmmNode sizeExpr = node.getChild(0);
        OllirExprResult sizeResult = visit(sizeExpr);

        // Determine the OLLIR type for the array (assuming only int[])
        String arrayType = ".array.i32";

        // Build the code for creating the array
        String exprCode = "new(array, " + sizeResult.getCode() + ")" + arrayType;

        // Return the result with any necessary computation + this expression
        return new OllirExprResult(exprCode, sizeResult.getComputation());
    }


    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        Type intType = TypeUtils.newIntType();
        String ollirIntType = ollirTypes.toOllirType(intType);
        String code = node.get("name") + ollirIntType;
        return new OllirExprResult(code);
    }

    private OllirExprResult visitBoolean(JmmNode node, Void unused) {
        Type boolType = TypeUtils.newBooleanType();
        String ollirIntType = ollirTypes.toOllirType(boolType);
        String code = node.get("name") + ollirIntType;
        return new OllirExprResult(code);
    }


    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {
        // First check if this was optimized to a literal
        if (node.getKind().equals("IntegerLiteral") || node.getKind().equals("BooleanLiteral")) {
            String type = node.getKind().equals("IntegerLiteral") ? ".i32" : ".bool";
            return new OllirExprResult(node.get("name") + type);
        }

        // Handle case where node was optimized but still marked as BinaryExpr
        if (node.getNumChildren() != 2) {
            if (node.hasAttribute("name")) { // If it has a literal value
                String type = node.get("type").equals("int") ? ".i32" : ".bool";
                return new OllirExprResult(node.get("name") + type);
            }
            throw new RuntimeException("Binary expression must have exactly two children at " + node);
        }

        String operator = node.get("op");

        // Special handling for short-circuit operators
        if ("&&".equals(operator)) {
            return handleShortCircuitAnd(node);
        } else if ("||".equals(operator)) {
            return handleShortCircuitOr(node);
        }

        // Process both children
        OllirExprResult lhs = visit(node.getChild(0));
        OllirExprResult rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        Type resType = types.getExprType(node);
        String resOllirType = ollirTypes.toOllirType(resType);
        String tempVar = ollirTypes.nextTemp() + resOllirType;

        computation.append(tempVar).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE)
                .append(operator).append(resOllirType).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(tempVar, computation.toString());
    }

    private OllirExprResult handleShortCircuitOr(JmmNode node) {
        OllirExprResult lhs = visit(node.getChild(0));
        OllirExprResult rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();
        computation.append(lhs.getComputation());

        String tempVar = ollirTypes.nextTemp() + ".bool";
        String rhsLabel = "or_rhs_" + ollirTypes.nextLabelId();
        String endLabel = "or_end_" + ollirTypes.nextLabelId();

        computation.append(tempVar).append(" :=.bool 1.bool;\n"); // default to true
        computation.append("if (").append(lhs.getCode()).append(") goto ").append(endLabel).append(";\n");
        computation.append("goto ").append(rhsLabel).append(";\n");

        // RHS evaluation
        computation.append(rhsLabel).append(":\n");
        computation.append(rhs.getComputation());
        computation.append(tempVar).append(" :=.bool ").append(rhs.getCode()).append(";\n");

        // End label
        computation.append(endLabel).append(":\n");

        return new OllirExprResult(tempVar, computation.toString());
    }


    private OllirExprResult handleShortCircuitAnd(JmmNode node) {
        OllirExprResult lhs = visit(node.getChild(0));
        OllirExprResult rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();
        computation.append(lhs.getComputation());

        String tempVar = ollirTypes.nextTemp() + ".bool";
        String rhsLabel = "and_rhs_" + ollirTypes.nextLabelId();
        String endLabel = "and_end_" + ollirTypes.nextLabelId();

        computation.append(tempVar).append(" :=.bool 0.bool;\n"); // default to false
        computation.append("if (").append(lhs.getCode()).append(") goto ").append(rhsLabel).append(";\n");
        computation.append("goto ").append(endLabel).append(";\n");

        // RHS evaluation
        computation.append(rhsLabel).append(":\n");
        computation.append(rhs.getComputation());
        computation.append(tempVar).append(" :=.bool ").append(rhs.getCode()).append(";\n");

        // End label
        computation.append(endLabel).append(":\n");

        return new OllirExprResult(tempVar, computation.toString());
    }

    private OllirExprResult visitVarRef(JmmNode node, Void unused) {
        String id = node.get("name");
        String currentMethod = table.get("currentMethod");

        // Skip numeric literals that were incorrectly marked as VarRef
        if (id.matches("\\d+")) {
            return new OllirExprResult(id + ".i32");
        }

        // Get the type of the variable
        Type type = types.getExprType(node);
        if (currentMethod != null) {
            type = TypeUtils.getTypeFromName(id, table, currentMethod);
        }

        String ollirType = ollirTypes.toOllirType(type);

        // Check if variable is local or parameter
        boolean isLocalOrParam = table.getLocalVariables(currentMethod).stream()
                .anyMatch(sym -> sym.getName().equals(id)) ||
                table.getParameters(currentMethod).stream()
                        .anyMatch(sym -> sym.getName().equals(id));

        if (isLocalOrParam) {
            // Local or param: no computation needed
            return new OllirExprResult(id + ollirType);
        } else {
            // Field: emit a getfield into a temp
            String tempVar = "tmp_" + id;
            String computation = tempVar + ollirType + " :=" + ollirType + " getfield(this, " + id + ollirType + ")" + ollirType + ";\n";
            return new OllirExprResult(tempVar + ollirType, computation);
        }
    }

    /**
     * Default visitor. Visits every child node and return an empty result.
     *
     * @param node
     * @param unused
     * @return
     */
    private OllirExprResult defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }
        return OllirExprResult.EMPTY;
    }
}
