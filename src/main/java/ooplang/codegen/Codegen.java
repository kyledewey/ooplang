package ooplang.codegen;

import ooplang.parser.*;
import ooplang.typechecker.ClassInformation;
import ooplang.typechecker.SingleClassInformation;
import ooplang.typechecker.MethodSignature;
import ooplang.typechecker.MethodInformation;
import ooplang.typechecker.TypeErrorException;

import java.util.List;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

// File structure:
// 1.) imports (#include ...)
// 2.) struct prototypes
// 3.) structs (the data for classes)
// 4.) function prototypes
// 5.) vtables
// 6.) typedefs
// 7.) init functions (the body of constructors)
// 8.) new functions (allocation + call to init)
// 9.) methods (functions as methods)
// 10.) virtual functions (call out to methods)
// 11.) main
//
// In an ideal world, we'd define an AST for C code,
// generate a C AST, and then write that AST to a file.
// To cut down on the amount of code to write, we'll
// write strings instead.
//
public class Codegen {
    private final Program program;
    private final ClassInformation classInformation;
    private final VTable vtable;
    
    // ---begin parts for what goes in the file---
    private final StringBuffer imports;
    private final StringBuffer structPrototypes;
    private final StringBuffer structs;
    private final StringBuffer functionPrototypes;
    private final StringBuffer vtables;
    private final StringBuffer typedefs;
    private final StringBuffer initFunctions;
    private final StringBuffer newFunctions;
    private final StringBuffer methods;
    private final StringBuffer virtualFunctions;
    private final StringBuffer main;
    private final StringBuffer[] sections;
    // ---end parts for what goes in the file---
    
    public Codegen(final Program program,
                   final ClassInformation classInformation) throws TypeErrorException, CodegenException {
        this.program = program;
        this.classInformation = classInformation;
        vtable = new VTable(classInformation);
        imports = new StringBuffer();
        structPrototypes = new StringBuffer();
        structs = new StringBuffer();
        functionPrototypes = new StringBuffer();
        vtables = new StringBuffer();
        typedefs = new StringBuffer();
        initFunctions = new StringBuffer();
        newFunctions = new StringBuffer();
        methods = new StringBuffer();
        virtualFunctions = new StringBuffer();
        main = new StringBuffer();
        sections = new StringBuffer[]{
            imports,
            structPrototypes,
            structs,
            functionPrototypes,
            vtables,
            typedefs,
            initFunctions,
            newFunctions,
            methods,
            virtualFunctions,
            main
        };
        handleProgram();
    }

    private void handleImports() {
        imports.append("#include \"stdio.h\"\n");
        imports.append("#include \"stdlib.h\"\n");
        imports.append("#include \"stdbool.h\"\n");
    } // handleImports
    
    public static String typeToString(final Type type) throws CodegenException {
        if (type instanceof IntType) {
            return "int";
        } else if (type instanceof BoolType) {
            return "bool";
        } else if (type instanceof ClassType) {
            return "struct " + ((ClassType)type).name.name + "*";
        } else {
            throw new CodegenException("Unknown type: " + type);
        }
    } // typeToString

    public static String typeToName(final Type type) throws CodegenException {
        if (type instanceof IntType) {
            return "int";
        } else if (type instanceof BoolType) {
            return "bool";
        } else if (type instanceof ClassType) {
            return ((ClassType)type).name.name;
        } else {
            throw new CodegenException("Unknown type: " + type);
        }
    } // typeToName

    public static String paramToString(final Param param) throws CodegenException {
        return typeToString(param.type) + " " + param.variable.name;
    } // paramToString
    
    private void handleClassStruct(final ClassDef classDef) throws CodegenException {
        final String structHeader =
            "struct " + classDef.className.name;
        structPrototypes.append(structHeader + ";");
        structs.append(" {\n");
        if (classDef.extendsName.isPresent()) {
            structs.append("  struct ");
            structs.append(classDef.extendsName.get().name);
            structs.append(" super;\n");
        } else {
            structs.append("  void** _vtable;\n");
        }
        for (final Param instanceVariable : classDef.instanceVariables) {
            structs.append("  ");
            structs.append(paramToString(instanceVariable));
            structs.append(";\n");
        }
        structs.append("};\n");
    } // handleClassStruct

    public static String cast(final ClassName className,
                              final String value) {
        return "((struct " + className.name + "*)" + value + ")";
    } // cast

    private void handleVardecStmt(final VardecStmt stmt,
                                  final StringBuffer dest) throws TypeErrorException, CodegenException {
        dest.append("  ");
        dest.append(typeToString(stmt.type));
        dest.append(" ");
        dest.append(stmt.variable.name);
        dest.append(" = ");
        handleExp(stmt.exp, dest);
        dest.append(";\n");
    } // handleVardecStmt

    private void handleAssignStmt(final AssignStmt stmt,
                                  final StringBuffer dest) throws TypeErrorException, CodegenException {
        dest.append("  ");
        handleLhs(stmt.lhs, dest);
        dest.append(" = ");
        handleExp(stmt.exp, dest);
        dest.append(";\n");
    } // handleAssignStmt

    private void handleLhs(final Lhs lhs,
                           final StringBuffer dest) throws TypeErrorException, CodegenException {
        if (lhs instanceof VariableLhs) {
            dest.append(((VariableLhs)lhs).variable.name);
        } else if (lhs instanceof AccessLhs) {
            final AccessLhs asAccess = (AccessLhs)lhs;
            dest.append("((struct ");
            dest.append(asAccess.lhsType.get().name.name);
            dest.append("*)");
            handleLhs(asAccess.lhs, dest);
            dest.append(")->");
            dest.append(asAccess.variable.name);
        } else if (lhs instanceof AccessThisLhs) {
            final AccessThisLhs asAccess = (AccessThisLhs)lhs;
            dest.append(cast(asAccess.targetType.get().name, "this"));
            dest.append("->");
            dest.append(asAccess.variable.name);
        } else {
            throw new CodegenException("Unknown lhs: " + lhs.toString());
        }
    } // handleLhs

    private void handleWhileStmt(final WhileStmt stmt,
                                 final StringBuffer dest) throws TypeErrorException, CodegenException {
        dest.append("while (");
        handleExp(stmt.exp, dest);
        dest.append(") {\n");
        handleStmt(stmt.stmt, dest);
        dest.append("}\n");
    } // handleWhileStmt

    private void handlePrognStmt(final PrognStmt progn,
                                 final StringBuffer dest) throws TypeErrorException, CodegenException {
        dest.append("{");
        for (final Stmt stmt : progn.stmts) {
            dest.append("\n");
            handleStmt(stmt, dest);
        }
        dest.append("}\n");
    } // handlePrognStmt

    private void handlePrintStmt(final PrintStmt stmt,
                                 final StringBuffer dest) throws TypeErrorException, CodegenException {
        dest.append("  printf(\"%");
        final Type expType = stmt.expType.get();
        if (expType instanceof IntType) {
            dest.append("i\\n\", ");
            handleExp(stmt.exp, dest);
            dest.append(");\n");
        } else if (expType instanceof BoolType) {
            dest.append("s\\n\", (");
            handleExp(stmt.exp, dest);
            dest.append(") ? \"true\" : \"false\");\n");
        } else {
            throw new CodegenException("Unprintable type: " + expType.toString());
        }
    } // handlePrintStmt

    private void handleIfStmt(final IfStmt stmt,
                              final StringBuffer dest) throws TypeErrorException, CodegenException {
        dest.append("  if(");
        handleExp(stmt.exp, dest);
        dest.append(") {\n");
        handleStmt(stmt.trueBranch, dest);
        dest.append("  } else {\n");
        handleStmt(stmt.falseBranch, dest);
        dest.append("  }\n");
    } // handleIfStmt

    private void handleReturnStmt(final ReturnStmt stmt,
                                  final StringBuffer dest) throws TypeErrorException, CodegenException {
        dest.append("  return ");
        handleExp(stmt.exp, dest);
        dest.append(";\n");
    } // handleReturnStmt
    
    private void handleStmt(final Stmt stmt,
                            final StringBuffer dest) throws TypeErrorException, CodegenException {
        if (stmt instanceof VardecStmt) {
            handleVardecStmt((VardecStmt)stmt, dest);
        } else if (stmt instanceof AssignStmt) {
            handleAssignStmt((AssignStmt)stmt, dest);
        } else if (stmt instanceof WhileStmt) {
            handleWhileStmt((WhileStmt)stmt, dest);
        } else if (stmt instanceof PrognStmt) {
            handlePrognStmt((PrognStmt)stmt, dest);
        } else if (stmt instanceof PrintStmt) {
            handlePrintStmt((PrintStmt)stmt, dest);
        } else if (stmt instanceof IfStmt) {
            handleIfStmt((IfStmt)stmt, dest);
        } else if (stmt instanceof ReturnStmt) {
            handleReturnStmt((ReturnStmt)stmt, dest);
        } else {
            throw new CodegenException("Unknown statement: " + stmt.toString());
        }
    } // handleStmt
    
    private void handleAccessExp(final AccessExp exp,
                                 final StringBuffer dest) throws TypeErrorException, CodegenException {
        dest.append("((");
        dest.append(typeToString(exp.expType.get()));
        dest.append(")");
        handleExp(exp.exp, dest);
        dest.append(")->");
        dest.append(exp.variable.name);
    } // handleAccessExp

    private void handleNewExp(final NewExp exp,
                              final StringBuffer dest) throws TypeErrorException, CodegenException {
        dest.append("_new_");
        dest.append(exp.className.name);
        dest.append("(");
        int numRemaining = exp.exps.size();
        for(final Exp curExp : exp.exps) {
            handleExp(curExp, dest);
            if (numRemaining > 1) {
                dest.append(", ");
            }
            numRemaining--;
        }
        dest.append(")");
    } // handleNewExp

    private void handleMethodName(final String nameKind,
                                  final ClassName definer,
                                  final MethodSignature signature,
                                  final StringBuffer dest) throws CodegenException {
        dest.append("_");
        dest.append(nameKind);
        dest.append("_");
        dest.append(definer.name);
        dest.append("_");
        dest.append(signature.methodName.name);
        for (final Type type : signature.params) {
            dest.append("_");
            dest.append(typeToName(type));
        }
    } // handleMethodName

    // TODO: stopped here.
    // We need helper methods (another kind of block in the output C...)
    // specifically for making the virtual call.  This avoids double evaluation
    // of target.
    private void handleCallExp(final CallExp exp,
                               final StringBuffer dest) throws TypeErrorException, CodegenException {
        final MethodSignature signature =
            new MethodSignature(exp.methodName,
                                exp.expTypes.get());
        final MethodInformation methodInfo =
            classInformation.getMethodInformation(exp.targetType.get().name,
                                                  signature);
        handleMethodName("virtual",
                         methodInfo.originalDefiner,
                         signature,
                         dest);
        dest.append("(struct ");
        dest.append(methodInfo.originalDefiner.name);
        dest.append("*)");
        handleExp(exp.target, dest);
        for (final Exp actualParam : exp.exps) {
            dest.append(", ");
            handleExp(actualParam, dest);
        }
        dest.append(")");
    } // handleCallExp

    private void handleOp(final Op op,
                          final StringBuffer dest) throws CodegenException {
        if (op instanceof PlusOp) {
            dest.append("+");
        } else if (op instanceof MultOp) {
            dest.append("*");
        } else if (op instanceof LessThanOp) {
            dest.append("<");
        } else if (op instanceof LogicalAndOp) {
            dest.append("&&");
        } else if (op instanceof LogicalOrOp) {
            dest.append("||");
        } else {
            throw new CodegenException("No such op: " + op.toString());
        }
    } // handleOp
    
    private void handleBinExp(final BinaryOpExp exp,
                              final StringBuffer dest) throws TypeErrorException, CodegenException {
        dest.append("(");
        handleExp(exp.left, dest);
        dest.append(" ");
        handleOp(exp.op, dest);
        dest.append(" ");
        handleExp(exp.right, dest);
        dest.append(")");
    } // handleBinExp
    
    private void handleExp(final Exp exp,
                           final StringBuffer dest) throws TypeErrorException, CodegenException {
        if (exp instanceof IntLiteralExp) {
            dest.append(((IntLiteralExp)exp).value);
        } else if (exp instanceof VariableExp) {
            dest.append(((VariableExp)exp).variable.name);
        } else if (exp instanceof BooleanLiteralExp) {
            dest.append(((BooleanLiteralExp)exp).value);
        } else if (exp instanceof ThisExp) {
            dest.append("this");
        } else if (exp instanceof AccessExp) {
            handleAccessExp((AccessExp)exp, dest);
        } else if (exp instanceof NewExp) {
            handleNewExp((NewExp)exp, dest);
        } else if (exp instanceof CallExp) {
            handleCallExp((CallExp)exp, dest);
        } else if (exp instanceof BinaryOpExp) {
            handleBinExp((BinaryOpExp)exp, dest);
        } else {
            throw new CodegenException("Unknown exp: " + exp.toString());
        }
    } // handleExp
    
    private void handleClassConstructorInit(final ClassDef classDef) throws TypeErrorException, CodegenException {
        final StringBuffer header = new StringBuffer();
        header.append("void _init_");
        header.append(classDef.className.name);
        header.append("(");
        header.append(typeToString(new ClassType(classDef.className)));
        header.append(" this");
        for (final Param param : classDef.consDef.params) {
            header.append(", ");
            header.append(paramToString(param));
        }
        header.append(")");
        functionPrototypes.append(header.toString());
        functionPrototypes.append(";\n");
        initFunctions.append(header.toString());
        initFunctions.append(" {\n");
        if (classDef.consDef.exps.isPresent()) {
            final ClassName extendsName = classDef.extendsName.get();
            initFunctions.append("  _init_");
            initFunctions.append(extendsName.name);
            initFunctions.append("(");
            initFunctions.append(cast(extendsName, "this"));
            for (final Exp exp : classDef.consDef.exps.get()) {
                initFunctions.append(", ");
                handleExp(exp, initFunctions);
            }
            initFunctions.append(");\n");
        }
        handleStmt(classDef.consDef.stmt, initFunctions);
        initFunctions.append("}\n");
    } // handleClassConstructorInit

    private void handleClassConstructorNew(final ClassDef classDef) throws TypeErrorException, CodegenException {
        final StringBuffer header = new StringBuffer();
        final String thisType = typeToString(new ClassType(classDef.className));
        header.append(thisType);
        header.append(" _new_");
        header.append(classDef.className.name);
        int numParamsLeft = classDef.consDef.params.size();
        for (final Param param : classDef.consDef.params) {
            header.append(paramToString(param));
            if (numParamsLeft > 1) {
                header.append(", ");
            }
            numParamsLeft--;
        }
        header.append(")");
        functionPrototypes.append(header.toString());
        functionPrototypes.append(";\n");
        newFunctions.append(header.toString());
        newFunctions.append(" {\n  ");
        newFunctions.append(thisType);
        newFunctions.append(" this = (");
        newFunctions.append(thisType);
        newFunctions.append(")malloc(sizeof(struct ");
        newFunctions.append(classDef.className.name);
        newFunctions.append("));\n");
        final SingleClassInformation base =
            classInformation.baseClass(classDef.className);
        newFunctions.append(cast(base.classDef.className, "this"));
        newFunctions.append("->_vtable = ");
        if (classInformation.getClass(classDef.className).methods.isEmpty()) {
            newFunctions.append("NULL");
        } else {
            newFunctions.append("_vtable_");
            newFunctions.append(classDef.className.name);
        }
        newFunctions.append(";\n  _init_");
        newFunctions.append(classDef.className.name);
        newFunctions.append("(this");
        for (final Param param : classDef.consDef.params) {
            newFunctions.append(", ");
            newFunctions.append(paramToString(param));
        }
        newFunctions.append(");\n  return this;\n}\n");
    } // handleClassConstructorNew

    private void handleClassConstructor(final ClassDef classDef) throws TypeErrorException, CodegenException {
        handleClassConstructorInit(classDef);
        handleClassConstructorNew(classDef);
    } // handleClassConstructor

    private void handleClassVTable(final ClassDef classDef) throws TypeErrorException, CodegenException {
        if (!classInformation.getClass(classDef.className).methods.isEmpty()) {
            vtables.append("void* _vtable_");
            vtables.append(classDef.className.name);
            vtables.append("[] = {\n");
            final MethodInformation[] wholeTable = vtable.getVTable(classDef.className);
            int numRemaining = wholeTable.length;
            for (final MethodInformation method : wholeTable) {
                vtables.append("  (void*)&");
                handleMethodName("method",
                                 method.mostRecentDefiner,
                                 MethodSignature.getSignature(method.methodDef),
                                 vtables);
                if (numRemaining > 1) {
                    vtables.append(",\n");
                }
                numRemaining--;
            }
            vtables.append("};\n");
        }
    } // handleClassVTable

    private List<MethodInformation> introducedMethods(final ClassDef classDef) throws TypeErrorException {
        return classInformation.getClass(classDef.className).introducedMethods();
    }
    
    private void handleClassTypedefs(final ClassDef classDef) throws TypeErrorException, CodegenException {
        // any methods that are introduced on this class need a typedef
        // overridden methods don't count
        for (final MethodInformation method : introducedMethods(classDef)) {
            typedefs.append("typedef ");
            typedefs.append(typeToString(method.methodDef.returnType));
            typedefs.append("(*");
            handleMethodName("typedef",
                             classDef.className,
                             MethodSignature.getSignature(method.methodDef),
                             typedefs);
            typedefs.append(")(");
            typedefs.append(typeToString(new ClassType(classDef.className)));
            for (final Param param : method.methodDef.params) {
                typedefs.append(", ");
                typedefs.append(typeToString(param.type));
            }
            typedefs.append(");\n");
        }
    } // handleClassTypedefs

    private void handleMethod(final ClassDef classDef,
                              final MethodDef methodDef) throws TypeErrorException, CodegenException {
        final StringBuffer header = new StringBuffer();
        header.append(typeToString(methodDef.returnType));
        header.append(" ");
        handleMethodName("method",
                         classDef.className,
                         MethodSignature.getSignature(methodDef),
                         header);
        header.append("(");
        header.append(typeToString(new ClassType(classDef.className)));
        for (final Param param : methodDef.params) {
            header.append(", ");
            header.append(paramToString(param));
        }
        header.append(")");
        functionPrototypes.append(header.toString());
        functionPrototypes.append(";\n");
        methods.append(header.toString());
        methods.append(" {\n");
        handleStmt(methodDef.body, methods);
        methods.append("}\n");
    } // handleMethod

    private void handleMethods(final ClassDef classDef) throws TypeErrorException, CodegenException {
        for (final MethodDef method : classDef.methodDefs) {
            handleMethod(classDef, method);
        }
    } // handleMethods

    private void handleVirtualFunctions(final ClassDef classDef) throws TypeErrorException, CodegenException {
        final SingleClassInformation base =
            classInformation.baseClass(classDef.className);
        for (final MethodInformation method : introducedMethods(classDef)) {
            final MethodSignature signature = method.getSignature();
            final StringBuffer header = new StringBuffer();
            handleMethodName("virtual",
                             classDef.className,
                             signature,
                             header);
            header.append("(");
            header.append(typeToString(new ClassType(classDef.className)));
            header.append(" this");
            for (final Param param : method.methodDef.params) {
                header.append(", ");
                header.append(paramToString(param));
            }
            header.append(")");
            functionPrototypes.append(header.toString());
            functionPrototypes.append(";\n");
            virtualFunctions.append(header.toString());
            virtualFunctions.append(" {\n");
            virtualFunctions.append("  return ((");
            handleMethodName("typedef",
                             classDef.className,
                             signature,
                             virtualFunctions);
            virtualFunctions.append(")((struct ");
            virtualFunctions.append(base.classDef.className.name);
            virtualFunctions.append("*)this)->_vtable[");
            virtualFunctions.append(vtable.methodIndex(base.classDef.className,
                                                       signature));
            virtualFunctions.append("])(this");
            for (final Param param : method.methodDef.params) {
                virtualFunctions.append(", ");
                virtualFunctions.append(param.variable.name);
            }
            virtualFunctions.append(");\n}\n");
        }
    } // handleVirtualFunctions
    
    private void handleClass(final ClassDef classDef) throws TypeErrorException, CodegenException {
        handleClassVTable(classDef);
        handleClassConstructor(classDef);
        handleClassVTable(classDef);
        handleClassTypedefs(classDef);
        handleMethods(classDef);
        handleVirtualFunctions(classDef);
    } // handleClass

    private void handleProgram() throws TypeErrorException, CodegenException {
        handleImports();
        for (final ClassDef classDef : program.classDefs) {
            handleClass(classDef);
        }
        methods.append("int main() {\n");
        handleStmt(program.entryPoint, main);
    } // handleProgram

    public void writeProgram(final File outputFile) throws IOException {
        final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
        try {
            for (final StringBuffer section : sections) {
                writer.println(section.toString());
            }
        } finally {
            writer.close();
        }
    } // writeProgram

    public static void writeProgram(final Program program,
                                    final ClassInformation classInfo,
                                    final File destination) throws IOException, TypeErrorException, CodegenException {
        new Codegen(program, classInfo).writeProgram(destination);
    }
} // Codegen
