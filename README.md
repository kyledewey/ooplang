# OOPLang #

## Syntax ##

```
i is an integer
x is a variable
cls is a class name
m is a method name
type ::= `int` | `bool` | cls
exp ::= i | x | `this` | `true` | `false` |
        `(` `new` cls exp* `)` | // create a new object
        `(` `.` exp m exp* `)` | // call a method
        `(` op exp exp `)`       // binary operation
op ::= `+` | `*` | `<` | `&&` | `||`
stmt ::= `(` `vardec` x type exp `)` | // variable declaration
         `(` `=` x exp `)` |           // assignment
         `(` `while` exp stmt `)` |    // while loops
         `(` `progn` stmt* `)` |       // sequencing statements
         `(` `print` exp `)` |         // printing something
         `(` `if` exp stmt stmt `)` |  // conditionals
         `(` `return` exp `)`          // return from methods
param ::= `(` type x `)`
consdef ::= `(` `init` `(` param* `)` `(` `super` exp* `)` stmt `)`
classdef ::= `(` `class` cls consdef methoddef* `)`
methoddef ::= `(` `method` type m `(` param* `)` stmt `)`
program ::= stmt
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
