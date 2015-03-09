/** 
 * A grammar for a subset of pure Prolog, based on Covington (1993), ISO Prolog: A Summary of the Draft Proposed Standard. 
 * Since our intent is compilation, as opposed to interpretation, the query is made part of the program. Note this causes
 * non-standard usage of terminology, as a program is typically taken to consist only of facts and rules.
 * 
 * @author Arno Bastenhof 
 */
grammar PrologJvm;

// === Grammar rules ===

program : (fact | plRule)+ query ;

query   : '?-' literal (',' literal)* '.' ;

fact    : literal '.' ;                   // TODO: Should be followed by whitespace

plRule  : literal ':-' body '.' ;         // TODO: Should be followed by whitespace

body    : literal                            # singleGoal
        | literal (',' literal)* ',' literal # goalSequence
        ;

literal : struc ;                         // TODO: Add support for propositional variables.

term    : ATOM                            # constant
        | '_'                             # anonymous
        | VAR                             # variable
        | struc                           # structure
        ;

struc   : ATOM '(' terms? ')' ;           // TODO: No whitespace allowed between ATOM and '('

terms   : term (',' term)* ;

// === Lexical rules ===

VAR     : [A-Z_] ID ;                     // Variable names begin with an underscore or uppercase letter.
ATOM    : [a-z] ID                        // Identifiers used as atoms must begin with a lowercase letter.
        | [#$&*+\-./:<=>?@^~\\]+          // Graphic tokens.
        ;                                 // TODO: Arbitrary characters in single quotes.

WS      : [ \t\r\n]+         -> skip ;
INLINE  : '%' .*? '\r'? '\n' -> skip ;    // Single-line comments.
BLOCK   : '/*' .*? '*/'      -> skip ;    // Multiline comments.

fragment ALPHA : [a-zA-Z] ;
fragment DIGIT : [0-9] ;
fragment ID    : (ALPHA | DIGIT | '_')* ; // Identifiers may contain letters, digits and underscores

