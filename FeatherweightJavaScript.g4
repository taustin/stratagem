grammar FeatherweightJavaScript;


@header { package edu.sjsu.fwjs.parser; }

// Reserved words
FUNCTION : 'fn' ;
IF       : 'if' ;
ELSE     : 'else' ;
LET      : 'let' ;

// Types
TYPE_INT       : 'Int' ;
TYPE_BOOL      : 'Bool' ;
TYPE_STRING    : 'String' ;
TYPE_FUN       : '->' ;
TYPE_UNIT      : '()' ;

// Literals
LIT_INT    : [1-9][0-9]* | '0' ;
LIT_BOOL   : 'true' | 'false' ;
LIT_STRING : '"' ( [^"] | '\\"' )* '"' ;
LIT_UNIT   : '()' ;

// Binary operators
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
NE        : '!=' ;

SEPARATOR : ';' ;

// Identifiers
ID : [a-zA-Z_] [a-zA-Z0-9_]* ;


// Whitespace and comments
NEWLINE       : '\r'? '\n' -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
LINE_COMMENT  : '//' ~[\n\r]* -> skip ;
WS            : [ \t]+ -> skip ; // ignore whitespace


// *** Parsing rules ***

/** The start rule */
prog: seq ;

seq: expr (SEPARATOR expr)* ;

expr: expr args                                         # functionApp
    | FUNCTION params ':' type '{' seq '}'              # functionDecl
    | LIT_INT                                           # int
    | LIT_BOOL                                          # bool
    | LIT_STRING                                        # string
    | LIT_UNIT                                          # unit
    | ID                                                # id
    | IF '(' expr ')' '{' seq '}' ELSE '{' seq '}'      # if
    | LET ID ':' type '=' expr 'in' expr                # let
    | expr op=( MUL | DIV | MOD ) expr                  # MulDivMod
    | expr op=( ADD | SUB ) expr                        # AddSub
    | expr op=( GT | GE | LT | LE | EQ | NE ) expr      # Comparison
    ;

params: '(' ID ':' type ')'
      ;

args: '(' expr ')'
    ;

type_prim : TYPE_INT | TYPE_BOOL | TYPE_STRING | TYPE_UNIT ;

type_fun  : type_prim TYPE_FUN type ;

type      : type_prim | type_fun ;
