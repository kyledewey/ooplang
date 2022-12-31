package ooplang.typechecker;

import ooplang.parser.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class Typechecker {
    public static final Map<Op, OpTypeInfo> OPS =
        new HashMap<Op, OpTypeInfo>() {{
            put(new PlusOp(), new OpTypeInfo(new IntType(), new IntType(), new IntType()));
            put(new MultOp(), new OpTypeInfo(new IntType(), new IntType(), new IntType()));
            put(new LessThanOp(), new OpTypeInfo(new BoolType(), new IntType(), new IntType()));
            put(new LogicalAndOp(), new OpTypeInfo(new BoolType(), new BoolType(), new BoolType()));
            put(new LogicalOrOp(), new OpTypeInfo(new BoolType(), new BoolType(), new BoolType()));
        }};
    
    private final Program program;
    private final Map<ClassName, ClassDef> classes;
    // Need to record:
    // - Methods on a class (mapping signature to definition)
    //     - The class that originally introduced the method
    //     - The class we are inheriting this methoed from
    // - Instance variables on a class (mapping variable to type)
    //     - The class we are inheriting this instance variable from
    private final Map<ClassName, Map<MethodSignature, MethodDef>> classesWithMethods;
    private final Map<ClassName, Map<Variable, Type>> classesWithInstanceVariables;
    
    public void superClasses(final Map<ClassName, ClassDef> classes,
                             final Set<ClassName> supers,
                             final ClassName forClass) throws TypeErrorException {
        supers.add(forClass);
        final ClassDef classDef = classes.get(forClass);
        if (classDef == null) {
            throw new TypeErrorException("No such class: " + forClass.toString());
        } else if (classDef.extendsName.isPresent()) {
            final ClassName superClass = classDef.extendsName.get();
            if (supers.contains(superClass)) {
                throw new TypeErrorException("Cyclic inheritance involving: " + forClass.toString());
            } else {
                superClasses(classes, supers, superClass);
            }
        }
    } // superClasses

    // gets all superclasses of the given class
    // throws an exception on cyclic inheritance
    public static Set<ClassName> superClasses(final Map<ClassName, ClassDef> classes,
                                              final ClassName forClass) throws TypeErrorException {
        final Set<ClassName> retval = new HashSet<ClassName>();
        superClasses(classes, retval, forClass);
    }
    
    public static void assertNoCyclicInheritance(final Map<ClassName, ClassDef> classes) throws TypeErrorException {
        for (final ClassName className : classes.keySet()) {
            superClasses(classes, className);
        }
    }
    
    // also fills in the given map
    public static Inherited allMethodsAndInstanceVariables(final Map<ClassName, Map<MethodSignature, MethodDef>> classesWithMethods,
                                                           final Map<ClassName, Map<Variable, Type>> allInstanceVariables,
                                                           final Map<ClassName, ClassDef> onlyClasses,
                                                           final ClassName forClass) throws TypeErrorException {
        // See if we've already computed it.
        // If so, use that.
        Map<MethodSignature, MethodDef> retvalMethods = classesWithMethods.get(forClass);
        Map<Variable, Type> retvalInstanceVariables = allInstanceVariables.get(forClass);
        if (retvalMethods != null) {
            assert(retvalInstanceVariables != null);
            return new Inherited(retvalMethods, retvalInstanceVariables);
        }
        assert(retvalMethods == null);
        assert(retvalInstanceVariables == null);
        
        // We need to compute it ourselves.
        // Figure out what we inherited, if anything.
        final ClassDef classDef = onlyClasses.get(forClass);
        if (classDef == null) {
            throw new TypeErrorException("No such class: " + classDef);
        }
        retvalMethods = new HashMap<MethodSignature, MethodDef>();
        retvalInstanceVariables = new HashMap<Variable, Type>();
        // add everything inherited
        if (classDef.extendsName.isPresent()) {
            final Inherited inherited = allMethodsAndInstanceVariables(classWithMethods,
                                                                       allInstanceVariables,
                                                                       onlyClasses,
                                                                       classDef.extendsName.get());
            retvalMethods.putAll(inherited.methods);
            retvalInstanceVariables.putAll(inherited.instanceVariables);
        }
        // add our own instance variables
        for (final Param instanceVariable : classDef.instanceVariables) {
            if (retvalInstanceVariables.containsKey(instanceVariable.variable)) {
                throw new TypeErrorException("Redeclaration of (possibly inherited) instance variable: " +
                                             instanceVariable.variable.toString());
            } else {
                retvalInstanceVariables.put(instanceVariable.variable, instanceVariable.type);
            }
        }

        // add our own methods
        final Set<MethodSignature> signaturesOnSelf = new HashSet<MethodSignature>();
        for (final MethodDef methodDef : classDef.methodDefs) {
            final MethodSignature signature = MethodSignature.getSignature(methodDef);
            // make sure it's not a duplicate within this class
            if (signaturesOnSelf.contains(signature)) {
                throw new TypeErrorException("Duplicate method signature on same class: " +
                                             signature.toString());
            } else {
                signaturesOnSelf.add(signature);
            }

            // add it, possibly overriding
            final MethodDef existingDef = retvalMethods.get(signature);
            if (existingDef == null ||
                existingDef.returnType.equals(methodDef.returnType)) {
                retvalMethods.put(signature, methodDef);
            } else {
                throw new TypeErrorException("Attempt to change return type: " +
                                             methodDef.methodName.toString());
            }
        }

        // add it to the memo table
        classesWithMethods.put(forClass, retvalMethods);
        allInstanceVariables.put(forClass, retvalInstanceVariables);

        return new Inherited(retvalMethods, retvalInstanceVariables);
    } // allMethodsAndInstanceVariables
    
    public Typechecker(final Program program) throws TypeErrorException {
        this.program = program;
        classes = new HashMap<ClassName, ClassDef>();
        for (final ClassDef classDef : program.classDefs) {
            if (!classes.containsKey(classDef.className)) {
                classes.put(classDef.className, classDef);
            } else {
                throw new TypeErrorException("Class with duplicate name: " +
                                             classDef.className.toString());
            }
        }
        assertNoCyclicInheritance(classes);
        classesWithMethods = new HashMap<ClassName, Map<MethodSignature, MethodDef>>();
        classesWithInstanceVariables = new HashMap<ClassName, Map<Variable, Type>>();
        for (final ClassDef classDef : program.classDefs) {
            // precompute all methods
            allMethodsAndInstanceVariables(classesWithMethods,
                                           classesWithInstanceVariables,
                                           classes,
                                           classDef.className);
        }
        typecheck();
    }

    private ClassDef getClass(final ClassName className) throws TypeErrorException {
        final ClassDef retval = classes.get(className);
        if (retval == null) {
            throw new TypeErrorException("No such class: " + className.toString());
        } else {
            return retval;
        }
    }

    private MethodDef getMethod(final ClassName target,
                                final MethodSignature signature) throws TypeErrorException {
        final Map<MethodSignature, MethodDef> methods = classesWithMethods.get(target);
        if (methods == null) {
            throw new TypeErrorException("No such class: " + target.toString());
        } else {
            final MethodDef method = methods.get(signature);
            if (method == null) {
                throw new TypeErrorException("No such method on class " +
                                             target.toString() +
                                             " with signature " +
                                             signature.toString());
            } else {
                return method;
            }
        }
    }
    
    private void throwTypeError(final Type expected,
                                final Type received) throws TypeErrorException {
        throw new TypeErrorException("Expected type: " + expected.toString() +
                                     "; received type: " + received.toString());
    }

    private void assertTypesCompatible(final List<Param> formalParams,
                                       final List<Exp> actualParams,
                                       final Map<Variable, Type> typeEnv,
                                       final Optional<ClassName> inClass) throws TypeErrorException {
        final Iterator<Param> formalIterator = formalParams.iterator();
        final Iterator<Exp> actualIterator = actualParams.iterator();
        while (formalIterator.hasNext() &&
               actualIterator.hasNext()) {
            assertTypesCompatible(formalIterator.next().type,
                                  typeof(actualIterator.next(),
                                         typeEnv,
                                         inClass));
        }
        if (formalIterator.hasNext() ||
            actualIterator.hasNext()) {
            throw new TypeErrorException("Number of params mismatch");
        }
    }
    
    private void assertTypesCompatible(final Type expected,
                                       final Type received) throws TypeErrorException {
        if (!expected.equals(received)) {
            if (expected instanceof ClassType &&
                received instanceof ClassType) {
                // still ok if received is a subtype of expected
                // get the type of the parent
                final ClassDef subClass = getClass(((ClassType)received).name);
                if (subClass.extendsName.isPresent()) {
                    // try up the chain
                    assertTypesCompatible(expected, new ClassType(subClass.extendsName));
                } else {
                    throwTypeError(expected, received);
                }
            } else {
                throwTypeError(expected, received);
            }
        }
    }

    
    private Type typeofCall(final CallExp callExp,
                            final Map<Variable, Type> typeEnv,
                            final Optional<ClassName> inClass) throws TypeErrorException {
        final Type targetType = typeof(callExp.target, typeEnv, inClass);
        if (!(targetType instanceof ClassType)) {
            throw new TypeErrorException("Calling method on non-class type: " + targetType.toString());
        }
        final ClassType targetAsClassType = (ClassType)targetType;
        callExp.targetType = Optional.of(targetAsClassType);
        final List<Type> paramTypes = new ArrayList<Type>();
        for (final Exp param : callExp.exps) {
            paramTypes.add(typeof(param, typeEnv, inClass));
        }
        final MethodDef methodDef = getMethod(targetAsClassType.name,
                                              new MethodSignature(callExp.methodName,
                                                                  paramTypes));
        return methodDef.returnType;
    } // typeofCall

    private Type typeofBin(final BinaryOpExp exp,
                           final Map<Variable, Type> typeEnv,
                           final Optional<ClassName> inClass) throws TypeErrorException {
        final OpTypeInfo info = OPS.get(exp.op);
        if (info == null) {
            assert(false);
            throw new TypeErrorException("Unknown op: " + exp.op.toString());
        }
        assertTypesCompatible(info.leftType,
                              typeof(exp.left, typeEnv, inClass));
        assertTypesCompatible(info.rightType,
                              typeof(exp.right, typeEnv, inClass));
        return info.returnType;
    }

    public static Type typeofVariable(final Map<Variable, Type> typeEnv,
                                      final Variable variable) throws TypeErrorException {
        final Type retval = typeEnv.get(variable);
        if (retval == null) {
            throw new TypeErrorException("Variable not in scope: " + variable.toString());
        } else {
            return retval;
        }
    }
    
    private Type typeof(final Exp exp,
                        final Map<Variable, Type> typeEnv,
                        final Optional<ClassName> inClass) throws TypeErrorException {
        if (exp instanceof IntLiteralExp) {
            return new IntType();
        } else if (exp instanceof VariableExp) {
            return typeofVariable(typeEnv,
                                  ((VariableExp)exp).variable);
        } else if (exp instanceof BooleanLiteralExp) {
            return new BoolType();
        } else if (exp instanceof ThisExp) {
            if (inClass.isPresent()) {
                return new ClassType(inClass.get());
            } else {
                throw new TypeErrorException("this used outside of class");
            }
        } else if (exp instanceof NewExp) {
            final NewExp asNew = (NewExp)exp;
            final ClassDef classDef = getClass(exp.className);
            assertTypesCompatible(getClass(asNew.className).consDef.params,
                                  asNew.exps,
                                  typeEnv,
                                  inClass);
            return new ClassType(exp.className);
        } else if (exp instanceof CallExp) {
            return typeofCall((CallExp)exp, typeEnv, inClass);
        } else if (exp instanceof BinaryOpExp) {
            return typeoofBin((BinaryOpExp)exp, typeEnv, inClass);
        } else {
            assert(false);
            throw new TypeErrorException("Unknown expression: " + exp.toString());
        }
    }

    public static Map<Variable, Type> addEnv(final Map<Variable, Type> typeEnv,
                                             final List<Param> params) {
        final Map<Variable, Type> retval = new HashMap<Variable, Type>();
        retval.putAll(typeEnv);
        for (final Param param : params) {
            retval.put(param.variable, param.type);
        }
        return retval;
    }
    
    public static Map<Variable, Type> addEnv(final Map<Variable, Type> typeEnv,
                                             final Variable variable,
                                             final Type type) {
        final Map<Variable, Type> retval = new HashMap<Variable, Type>();
        retval.putAll(typeEnv);
        retval.put(variable, type);
        return retval;
    }

    private Type fieldType(final ClassName className,
                           final Variable fieldName) throws TypeErrorException {
        final Map<Variable, Type> 
    private Type typeofLhs(final Lhs lhs,
                           final Map<Variable, Type> typeEnv) throws TypeErrorException {
        if (lhs instanceof VariableLhs) {
            return typeofVariable(typeEnv, ((VariableLhs)lhs).variable);
        } else if (lhs instanceof AccessLhs) {
            final AccessLhs asAccess = (AccessLhs)lhs;
            final Type lhsType = typeofLhs(
    // For return:
    // - Need to make sure all paths return
    // - The return itself needs to be of the return type
    private StmtResult typecheckStmt(final Stmt stmt,
                                     final Map<Variable, Type> typeEnv,
                                     final Optional<ClassName> inClass,
                                     final Optional<Type> returnType) throws TypeErrorException {
        if (stmt instanceof VardecStmt) {
            final VardecStmt asVardec = (VardecStmt)stmt;
            assertTypesCompatible(asVardec.type,
                                  typeof(asVardec.exp,
                                         typeEnv,
                                         inClass));
            return new StmtResult(addEnv(typeEnv,
                                         asVardec.variable,
                                         asVardec.type),
                                  false);
        } else if (stmt instanceof AssignStmt) {
            final AssignStmt asAssign = (AssignStmt)stmt;
            assertTypesCompatible(typeofLhs(typeEnv, asAssign.lhs),
                                  typeof(asAssign.exp,
                                         typeEnv,
                                         inClass));
            return new StmtResult(typeEnv, false);
        } else if (stmt instanceof WhileStmt) {
            final WhileStmt asWhile = (WhileStmt)stmt;
            assertTypesCompatible(new BoolType(),
                                  typeof(asWhile.exp,
                                         typeEnv,
                                         inClass));
            typecheckStmt(asWhile.stmt,
                          typeEnv,
                          inClass,
                          returnType);
            return new StmtResult(typeEnv, false);
        } else if (stmt instanceof PrognStmt) {
            Map<Variable, Type> env = typeEnv;
            boolean returned = false;
            for (final Stmt curStmt : ((PrognStmt)stmt).stmts) {
                if (returned) {
                    throw new TypeErrorException("Dead code: " + curStmt.toString());
                } else {
                    final StmtResult res = typecheckStmt(curStmt,
                                                         typeEnv,
                                                         inClass,
                                                         returnType);
                    env = res.typeEnv;
                    returned = res.returnsOnAllPaths;
                }
            }
            return new StmtResult(typeEnv, returned);
        } else if (stmt instanceof PrintStmt) {
            final Type type = typeof(((PrintStmt)stmt).exp,
                                     typeEnv,
                                     inClass);
            if (type instanceof IntType ||
                type instanceof BoolType) {
                return new StmtResult(typeEnv, false);
            } else {
                throw new TypeErrorException("Cannot print type " + type.toString());
            }
        } else if (stmt instanceof IfStmt) {
            final IfStmt asIf = (IfStmt)stmt;
            assertTypesCompatible(new BoolType(),
                                  typeof(asIf.exp,
                                         typeEnv,
                                         inClass));
            final boolean returnedOnTrue =
                typecheckStmt(asIf.trueBranch,
                              typeEnv,
                              inClass,
                              returnType).returnsOnAllPaths;
            final boolean returnedOnFalse =
                typecheckStmt(asIf.falseBranch,
                              typeEnv,
                              inClass,
                              returnType).returnsOnAllPaths;
            return new StmtResult(typeEnv, returnedOnTrue && returnedOnFalse);
        } else if (stmt instanceof ReturnStmt) {
            if (!returnType.isPresent()) {
                throw new TypeErrorException("Return outside of method: " + stmt.toString());
            } else {
                assertTypesCompatible(returnType.get(),
                                      typeof(((ReturnStmt)stmt).exp,
                                             typeEnv,
                                             inClass));
                return new StmtResult(typeEnv, true);
            }
        } else {
            throw new TypeErrorException("Unknown statement: " + stmt.toString());
        }
    } // typecheckStmt

    private void typecheckMethodDef(final MethodDef methodDef,
                                    final ClassName inClass) throws TypeErrorException {
        final Map<Variable, Type> typeEnv = addEnv(new HashMap<Variable, Type>(),
                                                   methodDef.params);
        final StmtResult body = typecheckStmt(methodDef.body,
                                              typeEnv,
                                              Optional.of(inClass),
                                              Optional.of(methodDef.returnType));
        if (!body.returnsOnAllPaths) {
            throw new TypeErrorException("Missing return in method: " + methodDef.toString());
        }
    }
    
    // specifically checks the ConsDef on the given classDef
    private void typecheckConsDef(final ClassDef classDef) throws TypeErrorException {
        // instance variables are only accessible through `this`, so
        // only the constructor parameters are added
        final Map<Variable, Type> typeEnv = addEnv(new HashMap<Variable, Type>(),
                                                   classDef.consDef.params);
        final Optional<ClassName> inClass = Optional.of(classDef.className);
        
        // The call to super needs to be there if we extend, and shouldn't be there
        // if we don't extend.  There is no base class Object in this language.
        if (classDef.extendsName.isPresent()) {
            if (!classDef.consDef.exps.isPresent()) {
                throw new TypeErrorException("Call to super is required to extend: " +
                                             classDef.className.toString());
            }
            // call to super needs to be the same type
            final ClassDef superClass = getClass(classDef.extendsName.get());
            assertTypesCompatible(superClass.consDef.params,
                                  classDef.consDef.exps.get(),
                                  typeEnv,
                                  inClass);
        } else {
            if (classDef.consDef.exps.isPresent()) {
                throw new TypeErrorException("Cannot call super if not extending: " +
                                             classDef.consDef.toString());
            }
        }

        typecheckStmt(classDef.consDef.stmt,
                      typeEnv,
                      inClass,
                      Optional.empty());
    } // typecheckConsDef
    
    private void typecheckClassDef(final ClassDef classDef) throws TypeErrorException {
        typecheckConsDef(classDef);
        for (final MethodDef methodDef : classDef.methodDefs) {
            typecheckMethodDef(methodDef, classDef.className);
        }
    }
    
    private void typecheckProgram() throws TypeErrorException {
        for (final ClassDef classDef : program.classDefs) {
            typecheckClassDef(classDef);
        }
        typecheckStmt(program.entryPoint,
                      new HashMap<Variable, Type>(),
                      Optional.empty(),
                      Optional.empty());
    }
    
    public static void typecheck(final Program program) throws TypeErrorException {
        new Typechecker(program).typecheckProgram();
    }
}
