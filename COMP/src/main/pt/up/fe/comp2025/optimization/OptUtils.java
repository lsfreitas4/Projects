package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.collections.AccumulatorMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import static pt.up.fe.comp2025.ast.Kind.TYPE;

/**
 * Utility methods related to the optimization middle-end.
 */
public class OptUtils {

    private final AccumulatorMap<String> temporaries;
    private final TypeUtils types;

    public OptUtils(TypeUtils types) {
        this.types = types;
        this.temporaries = new AccumulatorMap<>();
    }

    public String nextTemp() {
        return nextTemp("tmp");
    }

    public String nextTemp(String prefix) {
        var nextTempNum = temporaries.add(prefix) - 1;
        return prefix + nextTempNum;
    }

    public String toOllirType(JmmNode typeNode) {
        if (!TYPE.check(typeNode)) {
            Type resolvedType = types.getExprType(typeNode);
            if (resolvedType == null) {
                throw new RuntimeException("Cannot resolve type for node: " + typeNode);
            }
            return toOllirType(resolvedType);
        }

        return toOllirType(TypeUtils.convertType(typeNode));
    }

    public String toOllirType(Type type) {
        if (type == null) {
            return ".i32"; // default to int if type is null
        }
        if (type.isArray()) {
            return toOllirArrayType(type);
        }
        return toOllirBaseType(type.getName());
    }

    private String toOllirArrayType(Type type) {
        // This method assumes type.isArray() is true
        String baseType = type.getName();
        return ".array" + toOllirBaseType(baseType);
    }

    private String toOllirBaseType(String typeName) {
        return switch (typeName) {
            case "int" -> ".i32";
            case "boolean" -> ".bool";
            case "void" -> ".V";
            default -> "." + typeName; // class or user-defined types
        };
    }

    private int labelCounter = 0;

    public String nextLabelId() {
        return String.valueOf(labelCounter++);
    }
}
