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
    private final ClassInformation classInformation;
            
    public Typechecker(final Program program) throws TypeErrorException {
        this.program = program;
        classInformation = new ClassInformation(program.classDefs);
        typecheckProgram();
    }

    public ClassInformation getClassInformation() {
        return classInformation;
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
                final ClassDef subClass = classInformation.getClass(((ClassType)received).name).classDef;
                if (subClass.extendsName.isPresent()) {
                    // try up the chain
                    assertTypesCompatible(expected, new ClassType(subClass.extendsName.get()));
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
        final MethodDef methodDef = classInformation.getMethod(targetAsClassType.name,
                                                               new MethodSignature(callExp.methodName,
                                                                                   paramTypes));
        callExp.expTypes = Optional.of(paramTypes);
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
        } else if (exp instanceof AccessExp) {
            final AccessExp asAccess = (AccessExp)exp;
            final Type targetType = typeof(asAccess.exp, typeEnv, inClass);
            if (targetType instanceof ClassType) {
                final ClassType asClass = (ClassType)targetType;
                asAccess.expType = Optional.of(asClass);
                return classInformation.typeofField(asClass.name, asAccess.variable);
            } else {
                throw new TypeErrorException("Can only access a field of a class; found: " +
                                             targetType.toString());
            }
        } else if (exp instanceof NewExp) {
            final NewExp asNew = (NewExp)exp;
            final ClassDef classDef = classInformation.getClass(asNew.className).classDef;
            assertTypesCompatible(classDef.consDef.params,
                                  asNew.exps,
                                  typeEnv,
                                  inClass);
            return new ClassType(asNew.className);
        } else if (exp instanceof CallExp) {
            return typeofCall((CallExp)exp, typeEnv, inClass);
        } else if (exp instanceof BinaryOpExp) {
            return typeofBin((BinaryOpExp)exp, typeEnv, inClass);
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

    private Type typeofLhs(final Lhs lhs,
                           final Map<Variable, Type> typeEnv,
                           final Optional<ClassName> inClass) throws TypeErrorException {
        if (lhs instanceof VariableLhs) {
            return typeofVariable(typeEnv, ((VariableLhs)lhs).variable);
        } else if (lhs instanceof AccessLhs) {
            final AccessLhs asAccess = (AccessLhs)lhs;
            final Type lhsType = typeofLhs(asAccess.lhs,
                                           typeEnv,
                                           inClass);
            if (lhsType instanceof ClassType) {
                final ClassType asClass = (ClassType)lhsType;
                asAccess.lhsType = Optional.of(asClass);
                return classInformation.typeofField(asClass.name,
                                                    asAccess.variable);
            } else {
                throw new TypeErrorException("Can only access fields of classes; found: " +
                                             lhsType.toString());
            }
        } else if (lhs instanceof AccessThisLhs) {
            if (inClass.isPresent()) {
                final AccessThisLhs asAccess = (AccessThisLhs)lhs;
                asAccess.targetType = Optional.of(new ClassType(inClass.get()));
                return classInformation.typeofField(inClass.get(),
                                                    asAccess.variable);
            } else {
                throw new TypeErrorException("Use of this outside of class");
            }
        } else {
            throw new TypeErrorException("Unknown lhs: " + lhs.toString());
        }
    } // typeofLhs

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
            assertTypesCompatible(typeofLhs(asAssign.lhs,
                                            typeEnv,
                                            inClass),
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
                                                         env,
                                                         inClass,
                                                         returnType);
                    env = res.typeEnv;
                    returned = res.returnsOnAllPaths;
                }
            }
            return new StmtResult(typeEnv, returned);
        } else if (stmt instanceof PrintStmt) {
            final PrintStmt asPrint = (PrintStmt)stmt;
            final Type type = typeof(asPrint.exp,
                                     typeEnv,
                                     inClass);
            if (type instanceof IntType ||
                type instanceof BoolType) {
                asPrint.expType = Optional.of(type);
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
            final ClassDef superClass = classInformation.getClass(classDef.extendsName.get()).classDef;
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
    
    public static ClassInformation typecheck(final Program program) throws TypeErrorException {
        return new Typechecker(program).getClassInformation();
    }
}
