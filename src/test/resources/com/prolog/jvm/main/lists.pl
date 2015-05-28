/*
 * This example contains some common list operations, using cons/2 as a list constructor.
 */

append([],YS,YS).
append(cons(X,XS),YS,cons(X,ZS)) :- append(XS,YS,ZS).

% naïve reverse
reverse([],[]).
reverse(cons(X,XS),YS) :- reverse(XS,ZS), append(ZS,cons(X,[]),YS).
