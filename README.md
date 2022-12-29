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
        `(` . exp m exp* `)` |   // call a method
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
