package pt.up.fe.comp2025.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable; // Import adicionado
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

import static pt.up.fe.comp2025.ast.Kind.*;

public class JmmSymbolTableBuilder {

    private List<Report> reports;

    public List<Report> getReports() {
        return this.reports;
    }

    private static Report newError(JmmNode node, String message) {
        return Report.newError(
                Stage.SEMANTIC,
                Integer.parseInt(node.get("line")),
                Integer.parseInt(node.get("col")),
                message,
                null);
    }

    public static JmmSymbolTable build(JmmNode root) {
        List<JmmNode> classDecls = root.getChildren(CLASS_DECL);
        var classDecl = classDecls.getFirst();
        String className = classDecl.get("name");
        String superClass = classDecl.hasAttribute("parent") ? classDecl.get("parent") : "";

        var imports = buildImports(root);

        JmmSymbolTable tempTable = new JmmSymbolTable(className, superClass,
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyMap(), Collections.emptyMap(),
                Collections.emptyMap(), imports, Collections.emptySet());

        var fields = getFields(classDecl, tempTable);
        var methods = buildMethods(classDecl);
        var returnTypes = buildReturnTypes(classDecl, tempTable);
        var params = buildParams(classDecl, tempTable);
        var locals = buildLocals(classDecl, tempTable);
        var staticMethods = buildStaticMethods(classDecl);


        return new JmmSymbolTable(className, superClass, fields, methods,
                returnTypes, params, locals, imports, staticMethods);
    }

    private static Set<String> buildStaticMethods(JmmNode classDecl) {
        Set<String> result = new HashSet<>();
        for (JmmNode method : classDecl.getChildren(METHOD_DECL)) {
            if (!method.getChildren(STATIC).isEmpty()) {
                result.add(method.get("name"));
            }
        }
        return result;
    }

    private static Map<String, Type> buildReturnTypes(JmmNode classDecl, SymbolTable table) {
        Map<String, Type> map = new HashMap<>();
        for (JmmNode method : classDecl.getChildren(METHOD_DECL)) {
            String methodName = method.get("name");
            Type returnType;

            if (method.getKind().equals("MainMethod")) {
                returnType = TypeUtils.newVoidType();
            } else {
                JmmNode typeNode = method.getChild(0);
                returnType = TypeUtils.convertType(typeNode); // Using static method
            }
            map.put(methodName, returnType);

        }
        return map;
    }


    private static Map<String, List<Symbol>> buildParams(JmmNode classDecl, SymbolTable table) {
        Map<String, List<Symbol>> map = new HashMap<>();
        for (JmmNode method : classDecl.getChildren(METHOD_DECL)) {
            String methodName = method.get("name");
            List<Symbol> paramsList = new ArrayList<>();

            for (JmmNode param : method.getChildren(PARAM)) {
                Type type = TypeUtils.convertType(param.getChild(0));
                String typename = type.getName();
                if (check(param, VAR_ARGS)){
                    type = new Type(typename, true);
                }
                // Using static method
                paramsList.add(new Symbol(type, param.get("name")));
            }
            map.put(methodName, paramsList);
        }
        return map;
    }


    private static Map<String, List<Symbol>> buildLocals(JmmNode classDecl, SymbolTable table) {
        Map<String, List<Symbol>> map = new HashMap<>();
        List<Symbol> fields = getFields(classDecl, table);

        for (JmmNode method : classDecl.getChildren(METHOD_DECL)) {
            String methodName = method.get("name");
            List<Symbol> localsList = new ArrayList<>();

            for (JmmNode varDecl : method.getChildren(VAR_DECL)) {
                Type type = TypeUtils.convertType(varDecl.getChild(0)); // Changed from getType()
                localsList.add(new Symbol(type, varDecl.get("name")));

            }
            //localsList.addAll(fields);
            map.put(methodName, localsList);
        }
        return map;
    }


    private static List<String> buildMethods(JmmNode classDecl) {
        List<String> methodsList = new ArrayList<>();
        for (JmmNode method : classDecl.getChildren(METHOD_DECL)) {
            // 1) Detect the “static” modifier
            boolean isStatic = !method.getChildren(STATIC).isEmpty();
            method.put("isStatic", String.valueOf(isStatic));

            // 2) Keep track of the name as before
            String methodName = method.get("name");
            methodsList.add(methodName);
        }
        return methodsList;
    }


    private static List<String> buildImports(JmmNode root) {
        List<String> imports = new ArrayList<>();

        // Iterate through all the children of the root node
        for (JmmNode child : root.getChildren()) {
            // Check if the node represents an import statement
            if (child.getKind().equals("ImportStmt")) {
                String importedClass = child.get("ID");
                // Only add the import if it's not already in the list (avoid duplicates)
                if (!imports.contains(importedClass)) {
                    imports.add(importedClass);
                }
            }

            // Additional checks for potential variations in AST structure (other cases)
            else if (child.getKind().equals("PackageStmt")) {
                // If a package statement exists, you might want to capture this as well
                String packageName = child.get("ID");
            }
        }
        return imports;
    }


    private static List<Symbol> getFields(JmmNode classDecl, SymbolTable table) {
        Set<String> fieldNames = new HashSet<>();
        List<Symbol> fields = new ArrayList<>();

        for (JmmNode fieldDecl : classDecl.getChildren(VAR_DECL)) {
            String fieldName = fieldDecl.get("name");

            if (fieldNames.contains(fieldName)) {
                continue;
            }

            Type fieldType = TypeUtils.convertType(fieldDecl.getChild(0)); // Changed from getType()
            fields.add(new Symbol(fieldType, fieldName));
            fieldNames.add(fieldName);
        }
        return fields;
    }
}