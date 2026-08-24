package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.type.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class JasminUtils {
    private final OllirResult ollirResult;
    private Method currentMethod;

    public JasminUtils(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
    }

    public void setCurrentMethod(Method method) {
        this.currentMethod = method;
    }

    public String getJasminType(String type) {
        return switch (type) {
            case "INT32" -> "I";
            case "INT32[]" -> "[I";
            case "VOID" -> "V";
            case "STRING" -> "Ljava/lang/String;";
            case "STRING[]" -> "[Ljava/lang/String;";
            case "BOOLEAN" -> "Z";
            default -> type.replace('.', '/');
        };
    }

    public String getStoreInstruction(Type type) {
        String typeStr = type.toString();
        String baseStore = switch (typeStr) {
            case "INT32", "BOOLEAN" -> "istore";
            case "STRING", "ARRAYREF" -> "astore";
            default -> {
                if (typeStr.startsWith("CLASS(") || typeStr.startsWith("OBJECTREF(") || typeStr.endsWith("]")) {
                    yield "astore";
                }
                throw new RuntimeException("Unsupported type for store: " + typeStr);
            }
        };
        return baseStore; // Note: No trailing space here
    }

    public String getLoadInstruction(Element element) {
        System.out.println("DEBUG: Loading element: " + element);
        System.out.println("DEBUG: Element type object: " + element.getType());
        String typeStr = element.getType().toString();
        System.out.println("DEBUG: element.getType().toString() = '" + typeStr + "'");

        String baseLoad;
        if ("INT32".equals(typeStr) || "BOOLEAN".equals(typeStr)) {
            baseLoad = "iload";
        } else {
            baseLoad = "aload";
        }
        System.out.println("DEBUG: baseLoad selected = '" + baseLoad + "'");

        if (element instanceof LiteralElement) {
            String literal = ((LiteralElement) element).getLiteral();
            System.out.println("DEBUG: LiteralElement detected with literal: " + literal);
            return "ldc " + literal;
        }

        if (element instanceof Operand operand) {
            System.out.println("DEBUG: Operand detected: " + operand.getName());
            int index = -1;

            if (operand.isParameter() && operand.getParamId() != -1) {
                // Only use paramId if the operand really is a parameter,
                // e.g., if your symbol table or some flag indicates so
                // Otherwise, ignore paramId and fallback to virtualReg
                // For safety, you can check if operand name equals parameter name(s)
                // or have an explicit isParameter flag
                // For now, ignore paramId to avoid the bug:
                index = operand.getParamId();
                if (currentMethod.isStaticMethod()) index++;
            }

            if (index == -1) {
                Descriptor descriptor = currentMethod.getVarTable().get(operand.getName());
                if (descriptor == null) {
                    throw new RuntimeException("Variable not found in table: " + operand.getName());
                }
                index = descriptor.getVirtualReg();
            }


            String loadInstr = (index <= 3) ? baseLoad + "_" + index : baseLoad + " " + index;
            System.out.println("DEBUG: Final load instruction: '" + loadInstr + "'");
            return loadInstr;
        }

        throw new RuntimeException("Unsupported element type: " + element.getClass());
    }


    public String getReturnInstruction(Type type) {
        String typeStr = type.toString();
        return switch (typeStr) {
            case "INT32", "BOOLEAN" -> "ireturn";
            case "STRING", "ARRAYREF" -> "areturn";
            case "VOID" -> "return";
            default -> {
                if (typeStr.startsWith("CLASS(") || typeStr.startsWith("OBJECTREF(") || typeStr.endsWith("]")) {
                    yield "areturn";
                }
                throw new RuntimeException("Unsupported type for return: " + typeStr);
            }
        };
    }

    public String getModifier(AccessModifier accessModifier) {
        return accessModifier != AccessModifier.DEFAULT ?
                accessModifier.name().toLowerCase() + " " :
                "";
    }

    public String getBinaryOpInstruction(Operation operation) {
        return switch (operation.getOpType()) {
            case OperationType.ADD -> "iadd";
            case OperationType.SUB -> "isub";
            case OperationType.MUL -> "imul";
            case OperationType.DIV -> "idiv";
            case OperationType.AND -> "iand";
            case OperationType.LTH -> "if_icmplt";
            case OperationType.GTH -> "if_icmpgt";
            case OperationType.EQ -> "if_icmpeq";
            case OperationType.NEQ -> "if_icmpne";
            default -> throw new RuntimeException("Unsupported binary operation: " + operation.getOpType());
        };
    }

    public String[] extractClassAndMethod(String callerTypeStr, String rawMethodName) {
        // Clean class name: strip "CLASS(" prefix and ")" suffix if present
        String className = callerTypeStr;
        if (className.startsWith("CLASS(") && className.endsWith(")")) {
            className = className.substring(6, className.length() - 1);
        } else if (className.startsWith("THIS(")) {
            className = className.substring(5, className.length() - 1);
        }
        // Convert dots to slashes for JVM internal name
        className = className.replace('.', '/');

        // Clean method name: strip "LiteralElement: " or similar prefix before the actual name
        String methodName = rawMethodName;
        if (methodName.contains(":")) {
            methodName = methodName.substring(methodName.indexOf(":") + 1).trim();
        }
        int dotIndex = methodName.indexOf('.');
        int parenIndex = methodName.indexOf('(');

        if (dotIndex != -1 && parenIndex != -1) {
            methodName = methodName.substring(dotIndex + 1, parenIndex) + methodName.substring(parenIndex);
        }
        return new String[]{className, methodName};
    }
}