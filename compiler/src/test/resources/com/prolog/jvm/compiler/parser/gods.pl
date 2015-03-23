/*
 * This example is adapted from Kowalski (1979), Algorithm = Logic + Control.
 */

% Facts.
father(zeus, ares).
mother(hera, ares).
father(ares, harmonia).
mother(semele, dionisius).
father(zeus, dionisius).

% Chain rules.
parent(X,Y) :- mother(X,Y).
parent(X,Y) :- father(X,Y).
ancestor(X,Y) :- parent(X,Y).

% Rules with more than one goal.
grandparent(X,Y) :- parent(X,Z), parent(Z,Y).
ancestor(X,Y) :- ancestor(X,Z), ancestor(Z,Y).
