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

/*
 * Some common list operations, using cons/2 as a list constructor.
 */

append([],YS,YS).
append(cons(X,XS),YS,cons(X,ZS)) :- append(XS,YS,ZS).

% naïve reverse
reverse([],[]).
reverse(cons(X,XS),YS) :- reverse(XS,ZS), append(ZS,cons(X,[]),YS).