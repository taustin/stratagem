grammar Stratagem;


@header { package edu.sjsu.stratagem.parser; }

// Reserved words
FUNCTION : 'fn' ;
IF       : 'if' ;
ELSE     : 'else' ;
LET      : 'let' ;

// Literals
LIT_INT    : [1-9][0-9]* | '0' ;
LIT_BOOL   : 'true' | 'false' ;
LIT_STRING : '"' ( ESC | . )*? '"' ;
fragment ESC : '\\' [tnr"\\] ;  // Matches: \t \n \r \" \\

// Types
TYPE_INT       : 'Int' ;
TYPE_BOOL      : 'Bool' ;
TYPE_STRING    : 'String' ;
TYPE_FUN       : '->' ;

// Both a type and a literal
UNIT      : '()' ;

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

// Built-in functions
PRINT : 'print' ;

// Misc syntax & keywords
SEPARATOR : ';' ;
COLON : ':' ;
COMMA : ',' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;
ASSIGN : '=' ;
IN : 'in' ;

// Identifiers
ID : [a-zA-Z_] [a-zA-Z0-9_]* ;


// Whitespace and comments
NEWLINE       : '\r'? '\n' -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
LINE_COMMENT  : '//' ~[\n\r]* -> skip ;
WS            : [ \t]+ -> skip ;  // ignore whitespace



// *** Parsing rules ***

/** The start rule */
prog: seq ;

seq: expr (SEPARATOR expr)* ;

expr: LPAREN expr RPAREN                                                          # parens
    | expr args                                                                   # functionApp
    | FUNCTION params COLON type LBRACE seq RBRACE                                # functionDecl
    | LIT_INT                                                                     # int
    | LIT_BOOL                                                                    # bool
    | LIT_STRING                                                                  # string
    | UNIT                                                                        # unit
    | ID                                                                          # id
    | IF LPAREN expr RPAREN LBRACE seq RBRACE ELSE LBRACE seq RBRACE              # if
    | LET ID COLON type ASSIGN expr IN expr                                       # let
    | expr op=( ADD | SUB | MUL | DIV | MOD | GT | GE | LT | LE | EQ | NE ) expr  # binOp
    | PRINT args                                                                  # print
    ;

params: LPAREN (param (COMMA param)*)? RPAREN ;

param: ID COLON type ;

args: LPAREN (expr (COMMA expr)*)? RPAREN ;

type_prim : TYPE_INT | TYPE_BOOL | TYPE_STRING | UNIT ;

type_fun  : type_fun_piece TYPE_FUN type_fun_piece (TYPE_FUN type_fun_piece)* ;

type_fun_piece : type_prim | LPAREN type_fun RPAREN ;

type      : type_prim | type_fun ;
