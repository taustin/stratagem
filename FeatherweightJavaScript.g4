grammar FeatherweightJavaScript;


@header { package edu.sjsu.fwjs.parser; }

// Reserved words
IF        : 'if' ;
ELSE      : 'else' ;
WHILE     : 'while' ;
FUNCTION  : 'function' ;
VAR       : 'var' ;
PRINT     : 'print' ;

// Literals
INT       : [1-9][0-9]* | '0' ;
BOOL      : 'true' | 'false' ;
NULL      : 'null' ;

// Symbols
MUL       : '*' ;
DIV       : '/' ;
ADD       : '+' ;
SUB       : '-' ;
MOD       : '%' ;
GT        : '>' ;
GE        : '>=' ;
LT        : '<' ;
LE        : '<=' ;
EQ        : '==' ;
SEPARATOR : ';' ;

// Identifiers
ID        : [a-zA-Z_] [a-zA-Z0-9_]* ;


// Whitespace and comments
NEWLINE   : '\r'? '\n' -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
LINE_COMMENT  : '//' ~[\n\r]* -> skip ;
WS            : [ \t]+ -> skip ; // ignore whitespace


// ***Paring rules ***

/** The start rule */
prog: stat+ ;

stat: expr SEPARATOR                                    # bareExpr
    | IF '(' expr ')' block ELSE block                  # ifThenElse
    | IF '(' expr ')' block                             # ifThen
    | WHILE '(' expr ')' block                          # while
    | PRINT '(' expr ')' SEPARATOR                      # printExpr
    | SEPARATOR                                         # blank
    ;

expr: expr args                                         # functionApp
    | FUNCTION params '{' stat* '}'                     # functionDecl
    | expr op=( '*' | '/' | '%' ) expr                  # MulDivMod
    | expr op=( '+' | '-' ) expr                        # AddSub
    | expr op=( '<' | '<=' | '>' | '>=' | '==' ) expr   # Comparison
    | VAR ID '=' expr                                   # varDecl
    | ID '=' expr                                       # assign
    | INT                                               # int
    | BOOL                                              # bool
    | NULL                                              # null
    | ID                                                # id
    | '(' expr ')'                                      # parens
    ;

block: '{' stat* '}'                                    # fullBlock
     | stat                                             # simpBlock
     ;

params: '(' ')'
      | '(' ID (',' ID)* ')'
      ;

args: '(' ')'
    | '(' expr (',' expr)* ')'
    ;

objLit: '{' (ID ':' expr)* '}'
       ;


