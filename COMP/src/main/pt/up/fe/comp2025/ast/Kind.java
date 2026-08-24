package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import static pt.up.fe.comp2025.ast.TypeUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum that mirrors the nodes that are supported by the AST for the Javamm grammar.
 */
public enum Kind {
    PROGRAM,
    CLASS_DECL,
    IMPORT_STMT,
    VAR_DECL,
    TYPE,
    METHOD_DECL,
    METHOD_CALL,
    PARAM,
    STMT,
    ASSIGN_STMT,
    RETURN_STMT,
    IF_STMT,
    WHILE_STMT,
    EXPR,
    BINARY_EXPR,
    UNARY_EXPR,
    ARRAY_ACCESS_EXPR,
    ARRAY_LENGTH_EXPR,
    PROPERTY_ACCESS_EXPR,
    METHOD_CALL_EXPR,
    NEW_ARRAY_EXPR,
    NEW_OBJECT_EXPR,
    PAREN_EXPR,
    ARRAY_INIT_EXPR,
    INTEGER_LITERAL,
    BOOLEAN_LITERAL,
    THIS_EXPR,
    ARRAY_TYPE,
    VOID,
    VAR_REF_EXPR,
    BLOCK_STMT,
    EXPR_STMT,
    ARRAY_STORE,
    ARG_LIST,
    STATIC,
    GRT,
    LESS,
    VAR_ARGS,
    ARRAY_ASSIGN;



    private final String name;

    private Kind(String name) {
        this.name = name;
    }

    private Kind() {
        this.name = SpecsStrings.toCamelCase(name(), "_", true);
    }

    public static Kind fromString(String kind) {

        for (Kind k : Kind.values()) {
            if (k.getNodeName().equals(kind)) {
                return k;
            }
        }
        throw new RuntimeException("Could not convert string '" + kind + "' to a Kind");
    }

    public static List<String> toNodeName(Kind firstKind, Kind... otherKinds) {
        var nodeNames = new ArrayList<String>();
        nodeNames.add(firstKind.getNodeName());

        for(Kind kind : otherKinds) {
            nodeNames.add(kind.getNodeName());
        }

        return nodeNames;
    }

    public String getNodeName() {
        return name;
    }

    @Override
    public String toString() {
        return getNodeName();
    }

    /**
     * Tests if the given JmmNode has the same kind as this type.
     *
     * @param node
     * @return
     */
    public boolean check(JmmNode node) {
        return node.isInstance(this);
    }

    /**
     * Performs a check and throws if the test fails. Otherwise, does nothing.
     *
     * @param node
     */
    public void checkOrThrow(JmmNode node) {

        if (!check(node)) {
            throw new RuntimeException("Node '" + node + "' is not a '" + getNodeName() + "'");
        }
    }

    /**
     * Performs a check on all kinds to test and returns false if none matches. Otherwise, returns true.
     *
     * @param node
     * @param kindsToTest
     * @return
     */
    public static boolean check(JmmNode node, Kind... kindsToTest) {

        for (Kind k : kindsToTest) {

            // if any matches, return successfully
            if (k.check(node)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Performs a check on all kinds to test and throws if none matches. Otherwise, does nothing.
     *
     * @param node
     * @param kindsToTest
     */
    public static void checkOrThrow(JmmNode node, Kind... kindsToTest) {
        if (!check(node, kindsToTest)) {
            // throw if none matches
            throw new RuntimeException("Node '" + node + "' is not any of " + Arrays.asList(kindsToTest));
        }
    }

    public static Type getType(JmmNode node, SymbolTable table) {
        String kind = node.getKind();

        switch (kind) {
            case "VarRefExpr":
                return getTypeFromName(node.get("name"), table, null);
            case "IntegerLiteral":
                return newIntType();
            case "BooleanLiteral":
                return newBooleanType();
            case "ThisExpr":
                return newObjectType(table.getClassName());
            // Add more cases as needed
            default:
                return null;
        }
    }
}
