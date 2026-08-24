grammar Javamm;

@header {
    package pt.up.fe.comp2025;
}
// Keywords
CLASS     : 'class' ;
PUBLIC    : 'public' ;
STATIC    : 'static' ;
VOID      : 'void' ;
MAIN      : 'main' ; //not a keyword
RETURN    : 'return' ;
IF        : 'if' ;
ELSE      : 'else' ;
WHILE     : 'while' ;
NEW       : 'new' ;
THIS      : 'this' ;
IMPORT    : 'import' ;
BOOLEAN   : 'boolean' ;
INT       : 'int' ;
TRUE      : 'true' ;
FALSE     : 'false' ;
COMMENT : '/*' .*? '*/' -> skip;
LINE_COMMENT : '//' ~[\r\n]* -> skip;

// Operators and Symbols
ADD       : '+' ;
SUB       : '-' ;
MUL       : '*' ;
DIV       : '/' ;


AND       : '&&' ;
OR        : '||' ;
LESS      : '<' ;
GRT       : '>' ;
NOT       : '!' ;
EQUALS    : '=' ;


DOT       : '.' ;
LBRACE    : '{' ;
RBRACE    : '}' ;
LPAREN    : '(' ;
RPAREN    : ')' ;
LBRACK    : '[' ;
RBRACK    : ']' ;
SEMICOMMA : ';' ;
COMMA     : ',' ;


INTEGER : [0] | ([1-9][0-9]*) ;
ID : [a-zA-Z_$] [a-zA-Z0-9_$]* ;

LIBNAME: [a-z][a-zA-Z0-9]*;
WS : [ \t\n\r\f]+ -> skip ;

program
    : (importDecl)* classDecl+ EOF
    ;

importDecl
    : IMPORT lib+=ID (DOT lib+=ID)* SEMICOMMA #ImportStmt
    ;


classDecl
    : CLASS name=ID
    ('extends' parent=ID)? LBRACE varDecl* methodDecl* RBRACE
    ;

varDecl
    : type name=ID SEMICOMMA
    ;

type
    : name=INT                        # IntType
    | name=BOOLEAN                    # BooleanType
    | name=VOID                       # VoidType
    | name='String'                   # StringType
    | name=ID                         # ClassType
    | name=INT LBRACK RBRACK          # ArrayType
    ;

methodDecl
    : (PUBLIC)? (stat=STATIC)? VOID name=MAIN LPAREN 'String' LBRACK RBRACK arg=ID RPAREN
      LBRACE varDecl* stmt* RBRACE   # MainMethod
    | (PUBLIC)? (stat=STATIC)? type name=ID LPAREN (param? | (param COMMA)+ param) RPAREN
      LBRACE varDecl* stmt* RBRACE  # RegularMethod
    ;

param
    : type name=ID            # RegularParam
    | type ('...') name=ID    # VarArgs
    ;

stmt
    : type name=ID                             # VarDeclStmt
    | expr EQUALS expr SEMICOMMA               # AssignStmt
    | IF LPAREN expr RPAREN stmt (ELSE stmt)?  # IfStmt
    | WHILE LPAREN expr RPAREN stmt            # WhileStmt
    | RETURN expr SEMICOMMA                    # ReturnStmt
    | LBRACE stmt* RBRACE                      # BlockStmt
    | expr SEMICOMMA                           # ExprStmt
    ;

expr
    : expr op=(MUL | DIV) expr                     # BinaryExpr
    | expr op=(ADD | SUB) expr                     # BinaryExpr
    | expr op=(LESS | GRT) expr                    # BinaryExpr
    | expr op=AND expr                             # BinaryExpr
    | NOT expr                                     # UnaryExpr
    | LBRACK (expr (COMMA expr)*)? RBRACK          # ArrayInitExpr
    | NEW INT LBRACK expr RBRACK                   # NewArrayExpr
    | expr DOT 'length'                            # ArrayLengthExpr
    | expr LBRACK expr RBRACK                      # ArrayAccessExpr
    | expr DOT name=ID                             # PropertyAccessExpr
    | expr DOT name=ID LPAREN argList? RPAREN      # MethodCall
    | NEW name=ID LPAREN RPAREN                    # NewObjectExpr
    | LPAREN expr RPAREN                           # ParenExpr
    | name=INTEGER                                 # IntegerLiteral
    | name=TRUE                                    # BooleanLiteral
    | name=FALSE                                   # BooleanLiteral
    | THIS (DOT name=ID)?                          # ThisExpr
    | name=ID                                      # VarRefExpr
    ;

argList
    : expr (COMMA expr)*
    ;

