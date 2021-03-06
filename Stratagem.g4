grammar Stratagem;


@header { package edu.sjsu.stratagem.parser; }

// Reserved words
FUNCTION : 'fn' ;
IF       : 'if' ;
ELSE     : 'else' ;
LET      : 'let' ;
REF      : 'ref' ;

// Literals
LIT_UNIT   : 'unit' ;
LIT_INT    : [1-9][0-9]* | '0' ;
LIT_BOOL   : 'true' | 'false' ;
LIT_STRING : '"' ( ESC | . )*? '"' ;
fragment ESC : '\\' [tnr"\\] ;  // Matches: \t \n \r \" \\

// Types
TYPE_UNIT      : 'Unit' ;
TYPE_INT       : 'Int' ;
TYPE_BOOL      : 'Bool' ;
TYPE_STRING    : 'String' ;
TYPE_REF       : 'Ref' ;
TYPE_FUN       : '->' ;
TYPE_ANY       : '?' ;

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
LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;
BIND : '=' ;
IN : 'in' ;
DEREF : '!' ;
ASSIGN : '<-' ;

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
    | FUNCTION params LBRACE seq RBRACE                                           # functionDecl
    | REF expr                                                                    # ref
    | DEREF expr                                                                  # deref
    | expr ASSIGN expr                                                            # assign
    | LIT_INT                                                                     # int
    | LIT_BOOL                                                                    # bool
    | LIT_STRING                                                                  # string
    | LIT_UNIT                                                                    # unit
    | ID                                                                          # id
    | IF LPAREN expr RPAREN LBRACE seq RBRACE ELSE LBRACE seq RBRACE              # if
    | LET ID (COLON type)? BIND expr IN expr                                      # let
    | expr op=( ADD | SUB | MUL | DIV | MOD | GT | GE | LT | LE | EQ | NE ) expr  # binOp
    | PRINT args                                                                  # print
    ;

params: LPAREN ID (COLON type)? RPAREN
      ;

args: LPAREN expr RPAREN
    ;

type_prim : TYPE_INT | TYPE_BOOL | TYPE_STRING | TYPE_UNIT | TYPE_ANY ;

type_ref  : TYPE_REF type ;

//          (domain                            ) TYPE_FUN codomain ;
type_fun  : (type_prim | LPAREN type_fun RPAREN) TYPE_FUN type ;

type      : type_prim | type_ref | type_fun ;
