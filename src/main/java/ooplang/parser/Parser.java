package ooplang.parser;

import ooplang.tokenizer.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class Parser {    
    public final Token[] tokens;

    public Parser(final Token[] tokens) {
        this.tokens = tokens;
    }

    public Token getToken(final int position) throws ParseException {
        if (0 <= position && position < tokens.length) {
            return tokens[position];
        } else {
            throw new ParseException("Invalid position: " + position);
        }
    }

    public void assertTokenHereIs(final int position,
                                  final Token expected) throws ParseException {
        final Token received = getToken(position);
        if (!expected.equals(received)) {
            throw new ParseException("Expected: " + expected.toString() +
                                     "; received: " + received.toString());
        }
    }

    public ParseResult<Type> parseType(final int position) throws ParseException {
        final Token token = getToken(position);
        Type type = null;
        if (token instanceof IntToken) {
            type = new IntType();
        } else if (token instanceof BoolToken) {
            type = new BoolType();
        } else if (token instanceof IdentifierToken) {
            final String name = ((IdentifierToken)token).name;
            type = new ClassType(new ClassName(name));
        } else {
            throw new ParseException("Expected type; received: " + token.toString());
        }

        return new ParseResult<Type>(type, position + 1);
    } // parseType

    public ParseResult<Op> parseOp(final int position) throws ParseException {
        final Token token = getToken(position);
        Op op = null;
        if (token instanceof PlusToken) {
            op = new PlusOp();
        } else if (token instanceof MultToken) {
            op = new MultOp();
        } else if (token instanceof LessThanToken) {
            op = new LessThanOp();
        } else if (token instanceof LogicalAndToken) {
            op = new LogicalAndOp();
        } else if (token instanceof LogicalOrToken) {
            op = new LogicalOrOp();
        } else {
            throw new ParseException("Expected op; received: " + token.toString());
        }

        return new ParseResult<Op>(op, position + 1);
    }

    public ParseResult<List<Exp>> parseExps(final int position) throws ParseException {
        return new Disjunct<Exp>() {
            public ParseResult<Exp> parse(final int position) throws ParseException {
                return parseExp(position);
            }
        }.star().parse(position);
    }
    
    public ParseResult<Variable> parseVariable(final int position) throws ParseException {
        final Token token = getToken(position);
        if (token instanceof IdentifierToken) {
            return new ParseResult<Variable>(new Variable(((IdentifierToken)token).name),
                                              position + 1);
        } else {
            throw new ParseException("Expected variable; received: " + token.toString());
        }
    }

    public ParseResult<Optional<ClassName>> parseOptionalClassName(final int position) throws ParseException {
        return new Disjunct<ClassName>() {
            public ParseResult<ClassName> parse(final int position) throws ParseException {
                return parseClassName(position);
            }
        }.optional().parse(position);
    }
    
    public ParseResult<ClassName> parseClassName(final int position) throws ParseException {
        final Token token = getToken(position);
        if (token instanceof IdentifierToken) {
            return new ParseResult<ClassName>(new ClassName(((IdentifierToken)token).name),
                                              position + 1);
        } else {
            throw new ParseException("Expected class name; received: " + token.toString());
        }
    }

    public ParseResult<MethodName> parseMethodName(final int position) throws ParseException {
        final Token token = getToken(position);
        if (token instanceof IdentifierToken) {
            return new ParseResult<MethodName>(new MethodName(((IdentifierToken)token).name),
                                               position + 1);
        } else {
            throw new ParseException("Expected method name; received: " + token.toString());
        }
    }
    
    public ParseResult<Exp> parseExp(final int position) throws ParseException {
        final Token token = getToken(position);
        if (token instanceof IntLiteralToken) {
            return new ParseResult<Exp>(new IntLiteralExp(((IntLiteralToken)token).value),
                                        position + 1);
        } else if (token instanceof IdentifierToken) {
            return new ParseResult<Exp>(new VariableExp(new Variable(((IdentifierToken)token).name)),
                                        position + 1);
        } else if (token instanceof ThisToken) {
            return new ParseResult<Exp>(new ThisExp(), position + 1);
        } else if (token instanceof TrueToken) {
            return new ParseResult<Exp>(new BooleanLiteralExp(true),
                                        position + 1);
        } else if (token instanceof FalseToken) {
            return new ParseResult<Exp>(new BooleanLiteralExp(false),
                                        position + 1);
        } else if (token instanceof LeftParenToken) {
            final Token nextToken = getToken(position + 1);
            if (nextToken instanceof DotToken) {
                final ParseResult<Exp> exp = parseExp(position + 2);
                final ParseResult<Variable> field = parseVariable(exp.nextPosition);
                assertTokenHereIs(field.nextPosition, new RightParenToken());
                return new ParseResult<Exp>(new AccessExp(exp.result,
                                                          field.result),
                                            field.nextPosition + 1);
            } else if (nextToken instanceof NewToken) {
                final ParseResult<ClassName> className = parseClassName(position + 2);
                final ParseResult<List<Exp>> exps = parseExps(className.nextPosition);
                assertTokenHereIs(exps.nextPosition, new RightParenToken());
                return new ParseResult<Exp>(new NewExp(className.result,
                                                       exps.result),
                                            exps.nextPosition + 1);
            } else if (nextToken instanceof CallToken) {
                final ParseResult<Exp> target = parseExp(position + 2);
                final ParseResult<MethodName> method = parseMethodName(target.nextPosition);
                final ParseResult<List<Exp>> params = parseExps(method.nextPosition);
                assertTokenHereIs(params.nextPosition, new RightParenToken());
                return new ParseResult<Exp>(new CallExp(target.result,
                                                        method.result,
                                                        params.result),
                                            params.nextPosition + 1);
            } else {
                final ParseResult<Op> op = parseOp(position + 1);
                final ParseResult<Exp> left = parseExp(op.nextPosition);
                final ParseResult<Exp> right = parseExp(left.nextPosition);
                assertTokenHereIs(right.nextPosition, new RightParenToken());
                return new ParseResult<Exp>(new BinaryOpExp(op.result,
                                                            left.result,
                                                            right.result),
                                            right.nextPosition + 1);
            }
        } else {
            throw new ParseException("Expected expression; received: " + token.toString());
        }
    } // parseExp

    public ParseResult<Lhs> parseLhs(final int position) throws ParseException {
        final Token token = getToken(position);
        if (token instanceof IdentifierToken) {
            return new ParseResult<Lhs>(new VariableLhs(new Variable(((IdentifierToken)token).name)),
                                        position + 1);
        } else if (token instanceof LeftParenToken) {
            assertTokenHereIs(position + 1, new DotToken());
            final Token nextToken = getToken(position + 2);
            if (nextToken instanceof ThisToken) {
                final ParseResult<Variable> variable = parseVariable(position + 3);
                assertTokenHereIs(variable.nextPosition, new RightParenToken());
                return new ParseResult<Lhs>(new AccessThisLhs(variable.result),
                                            variable.nextPosition + 1);
            } else {
                final ParseResult<Lhs> lhs = parseLhs(position + 2);
                final ParseResult<Variable> variable = parseVariable(lhs.nextPosition);
                assertTokenHereIs(variable.nextPosition, new RightParenToken());
                return new ParseResult<Lhs>(new AccessLhs(lhs.result,
                                                          variable.result),
                                            variable.nextPosition + 1);
            }
        } else {
            throw new ParseException("Unknown lhs: " + token.toString());
        }
    } // parseLhs
    
    public ParseResult<List<Stmt>> parseStmts(final int position) throws ParseException {
        return new Disjunct<Stmt>() {
            public ParseResult<Stmt> parse(final int position) throws ParseException {
                return parseStmt(position);
            }
        }.star().parse(position);
    }
    
    public ParseResult<Stmt> parseStmt(final int position) throws ParseException {
        assertTokenHereIs(position, new LeftParenToken());
        final Token token = getToken(position + 1);
        if (token instanceof VardecToken) {
            // `(` `vardec` type x exp `)`
            final ParseResult<Type> type = parseType(position + 2);
            final ParseResult<Variable> variable = parseVariable(type.nextPosition);
            final ParseResult<Exp> exp = parseExp(variable.nextPosition);
            assertTokenHereIs(exp.nextPosition, new RightParenToken());
            return new ParseResult<Stmt>(new VardecStmt(type.result,
                                                        variable.result,
                                                        exp.result),
                                         exp.nextPosition + 1);
        } else if (token instanceof SingleEqualsToken) {
            // `(` `=` lhs exp `)`
            final ParseResult<Lhs> lhs = parseLhs(position + 2);
            final ParseResult<Exp> exp = parseExp(lhs.nextPosition);
            assertTokenHereIs(exp.nextPosition, new RightParenToken());
            return new ParseResult<Stmt>(new AssignStmt(lhs.result,
                                                        exp.result),
                                         exp.nextPosition + 1);
        } else if (token instanceof WhileToken) {
            // `(` `while` exp stmt `)`
            final ParseResult<Exp> exp = parseExp(position + 2);
            final ParseResult<Stmt> stmt = parseStmt(exp.nextPosition);
            assertTokenHereIs(stmt.nextPosition, new RightParenToken());
            return new ParseResult<Stmt>(new WhileStmt(exp.result,
                                                       stmt.result),
                                         stmt.nextPosition + 1);
        } else if (token instanceof PrognToken) {
            // `(` `progn` stmt* `)`
            final ParseResult<List<Stmt>> stmts = parseStmts(position + 2);
            assertTokenHereIs(stmts.nextPosition, new RightParenToken());
            return new ParseResult<Stmt>(new PrognStmt(stmts.result),
                                         stmts.nextPosition + 1);
        } else if (token instanceof PrintToken) {
            // `(` `print` exp `)`
            final ParseResult<Exp> exp = parseExp(position + 2);
            assertTokenHereIs(exp.nextPosition, new RightParenToken());
            return new ParseResult<Stmt>(new PrintStmt(exp.result),
                                         exp.nextPosition + 1);
        } else if (token instanceof IfToken) {
            // `(` `if` exp stmt stmt `)`
            final ParseResult<Exp> exp = parseExp(position + 2);
            final ParseResult<Stmt> trueBranch = parseStmt(exp.nextPosition);
            final ParseResult<Stmt> falseBranch = parseStmt(trueBranch.nextPosition);
            assertTokenHereIs(falseBranch.nextPosition, new RightParenToken());
            return new ParseResult<Stmt>(new IfStmt(exp.result,
                                                    trueBranch.result,
                                                    falseBranch.result),
                                         falseBranch.nextPosition + 1);
        } else if (token instanceof ReturnToken) {
            // `(` `return` exp `)`
            final ParseResult<Exp> exp = parseExp(position + 2);
            assertTokenHereIs(exp.nextPosition, new RightParenToken());
            return new ParseResult<Stmt>(new ReturnStmt(exp.result),
                                         exp.nextPosition + 1);
        } else {
            throw new ParseException("Expected statement; received: " + token.toString());
        }
    } // parseStmt

    public ParseResult<Param> parseParam(final int position) throws ParseException {
        assertTokenHereIs(position, new LeftParenToken());
        final ParseResult<Type> type = parseType(position + 1);
        final ParseResult<Variable> variable = parseVariable(type.nextPosition);
        assertTokenHereIs(variable.nextPosition, new RightParenToken());
        return new ParseResult<Param>(new Param(type.result,
                                                variable.result),
                                      variable.nextPosition + 1);
    }

    public ParseResult<List<Param>> parseParams(final int position) throws ParseException {
        return new Disjunct<Param>() {
            public ParseResult<Param> parse(final int position) throws ParseException {
                return parseParam(position);
            }
        }.star().parse(position);
    }

    public ParseResult<Optional<List<Exp>>> parseOptionalSuper(final int position) throws ParseException {
        // [`(` `super` exp* `)`]
        return new Disjunct<List<Exp>>() {
            public ParseResult<List<Exp>> parse(final int position) throws ParseException {
                assertTokenHereIs(position, new LeftParenToken());
                assertTokenHereIs(position + 1, new SuperToken());
                final ParseResult<List<Exp>> exps = parseExps(position + 2);
                assertTokenHereIs(exps.nextPosition, new RightParenToken());
                return new ParseResult<List<Exp>>(exps.result, exps.nextPosition + 1);
            }
        }.optional().parse(position);
    }
    
    public ParseResult<ConsDef> parseConsDef(final int position) throws ParseException {
        // consdef ::= `(` `init` `(` param* `)` [`(` `super` exp* `)`] stmt `)`
        assertTokenHereIs(position, new LeftParenToken());
        assertTokenHereIs(position + 1, new InitToken());
        assertTokenHereIs(position + 2, new LeftParenToken());
        final ParseResult<List<Param>> params = parseParams(position + 3);
        assertTokenHereIs(params.nextPosition, new RightParenToken());
        final ParseResult<Optional<List<Exp>>> superParams = parseOptionalSuper(params.nextPosition + 1);
        assertTokenHereIs(superParams.nextPosition, new RightParenToken());
        final ParseResult<Stmt> stmt = parseStmt(superParams.nextPosition + 1);
        assertTokenHereIs(stmt.nextPosition, new RightParenToken());
        return new ParseResult<ConsDef>(new ConsDef(params.result,
                                                    superParams.result,
                                                    stmt.result),
                                        stmt.nextPosition + 1);
    }

    public ParseResult<List<ClassDef>> parseClassDefs(final int position) throws ParseException {
        return new Disjunct<ClassDef>() {
            public ParseResult<ClassDef> parse(final int position) throws ParseException {
                return parseClassDef(position);
            }
        }.star().parse(position);
    }
    
    public ParseResult<ClassDef> parseClassDef(final int position) throws ParseException {
        // `(` `class` cls [cls] `(` param* `)` consdef methoddef* `)`
        assertTokenHereIs(position, new LeftParenToken());
        assertTokenHereIs(position + 1, new ClassToken());
        final ParseResult<ClassName> className = parseClassName(position + 2);
        final ParseResult<Optional<ClassName>> extendsName = parseOptionalClassName(className.nextPosition);
        assertTokenHereIs(extendsName.nextPosition, new LeftParenToken());
        final ParseResult<List<Param>> instanceVariables = parseParams(extendsName.nextPosition + 1);
        assertTokenHereIs(instanceVariables.nextPosition, new RightParenToken());
        final ParseResult<ConsDef> consDef = parseConsDef(instanceVariables.nextPosition + 1);
        final ParseResult<List<MethodDef>> methodDefs = parseMethodDefs(consDef.nextPosition);
        assertTokenHereIs(methodDefs.nextPosition, new RightParenToken());
        return new ParseResult<ClassDef>(new ClassDef(className.result,
                                                      extendsName.result,
                                                      instanceVariables.result,
                                                      consDef.result,
                                                      methodDefs.result),
                                         methodDefs.nextPosition + 1);
    }

    public ParseResult<List<MethodDef>> parseMethodDefs(final int position) throws ParseException {
        return new Disjunct<MethodDef>() {
            public ParseResult<MethodDef> parse(final int position) throws ParseException {
                return parseMethodDef(position);
            }
        }.star().parse(position);
    }

    public ParseResult<MethodDef> parseMethodDef(final int position) throws ParseException {
        // methoddef ::= `(` `method` type m `(` param* `)` stmt `)`
        assertTokenHereIs(position, new LeftParenToken());
        assertTokenHereIs(position + 1, new MethodToken());
        final ParseResult<Type> type = parseType(position + 2);
        final ParseResult<MethodName> methodName = parseMethodName(type.nextPosition);
        assertTokenHereIs(methodName.nextPosition, new LeftParenToken());
        final ParseResult<List<Param>> params = parseParams(methodName.nextPosition + 1);
        assertTokenHereIs(params.nextPosition, new RightParenToken());
        final ParseResult<Stmt> stmt = parseStmt(params.nextPosition + 1);
        assertTokenHereIs(stmt.nextPosition, new RightParenToken());
        return new ParseResult<MethodDef>(new MethodDef(type.result,
                                                        methodName.result,
                                                        params.result,
                                                        stmt.result),
                                          stmt.nextPosition + 1);
    }

    public ParseResult<Program> parseProgram(final int position) throws ParseException {
        // program ::= classdef* stmt
        final ParseResult<List<ClassDef>> classDefs = parseClassDefs(position);
        final ParseResult<Stmt> stmt = parseStmt(classDefs.nextPosition);
        return new ParseResult<Program>(new Program(classDefs.result,
                                                    stmt.result),
                                        stmt.nextPosition);
    }

    public static Program parseProgram(final Token[] tokens) throws ParseException {
        final Parser parser = new Parser(tokens);
        final ParseResult<Program> program = parser.parseProgram(0);
        if (program.nextPosition == tokens.length) {
            return program.result;
        } else {
            throw new ParseException("Extra tokens at end: " +
                                     parser.getToken(program.nextPosition).toString());
        }
    }
}
