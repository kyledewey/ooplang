# OOPLang #

## Features ##

- Class-based inheritance
- Subtyping
- Method overloading

## Syntax ##

```
i is an integer
x is a variable
cls is a class name
m is a method name
type ::= `int` | `bool` | cls
exp ::= i | x | `this` | `true` | `false` |
        `(` `.` exp x `)` |         // access a field of an object
        `(` `new` cls exp* `)` |    // create a new object
        `(` `call` exp m exp* `)` | // call a method
        `(` op exp exp `)`          // binary operation
op ::= `+` | `*` | `<` | `&&` | `||`
lhs ::= x |
        `(` `.` `this` x `)` |
        `(` `.` lhs x `)`
stmt ::= `(` `vardec` type x exp `)` | // variable declaration
         `(` `=` lhs exp `)` |         // assignment
         `(` `while` exp stmt `)` |    // while loops
         `(` `progn` stmt* `)` |       // sequencing statements
         `(` `print` exp `)` |         // printing something
         `(` `if` exp stmt stmt `)` |  // conditionals
         `(` `return` exp `)`          // return from methods
param ::= `(` type x `)`
consdef ::= `(` `init` `(` param* `)` [`(` `super` exp* `)`] stmt `)`
classdef ::= `(` `class` cls [cls] `(` param* `)` consdef methoddef* `)`
methoddef ::= `(` `method` type m `(` param* `)` stmt `)`
program ::= classdef* stmt
```

## Tokens ##

- IntLiteralToken(int)
- IdentifierToken(String)
- IntToken: 0
- BoolToken: 1
- ThisToken: 2
- TrueToken: 3
- FalseToken: 4
- LeftParenToken: 5
- RightParenToken: 6
- NewToken: 7
- DotToken: 8
- PlusToken: 9
- MultToken: 10
- LessThanToken: 11
- LogicalAndToken: 12
- LogicalOrToken: 13
- VardecToken: 14
- SingleEqualsToken: 15
- WhileToken: 16
- PrognToken: 17
- PrintToken: 18
- IfToken: 19
- ReturnToken: 20
- InitToken: 21
- SuperToken: 22
- ClassToken: 23
- MethodToken: 24
- CallToken: 25

## AST ##

- Variable(String)
- ClassName(String)
- MethodName(String)
- Type
  - IntType: 0
  - BoolType: 1
  - ClassType(ClassName)
- Exp
  - IntLiteralExp(int)
  - VariableExp(Variable)
  - BooleanLiteralExp(boolean)
  - ThisExp: 0
  - AccessExp(Exp, Variable)
  - NewExp(ClassName, List<Exp>)
  - CallExp(Exp, MethodName, List<Exp>)
  - BinaryOpExp(Op, Exp, Exp)
- Lhs
  - VariableLhs(Variable)
  - AccessLhs(Lhs, Variable)
  - AccessThisLhs(Variable)
- Op
  - PlusOp: 0
  - MultOp: 1
  - LessThanOp: 2
  - LogicalAndOp: 3
  - LogicalOrOp: 4
- Stmt
  - VardecStmt(Type, Variable, Exp)
  - AssignStmt(Lhs, Exp)
  - WhileStmt(Exp, Stmt)
  - PrognStmt(List<Stmt>)
  - PrintStmt(Exp)
  - IfStmt(Exp, Stmt, Stmt)
  - ReturnStmt(Exp)
- Param(Type, Variable)
- ConsDef(List<Param>, Optional<List<Exp>>, Stmt)
- ClassDef(ClassName, Optional<ClassName>, List<Param>, ConsDef, List<MethodDef>)
- MethodDef(Type, MethodName, List<Param>, Stmt)
- Program(List<ClassDef>, Stmt)

## Running the Code ##

```console
mvn exec:java -Dexec.mainClass="ooplang.Ooplang" -Dexec.args="examples/class.ooplang class.c"
```
