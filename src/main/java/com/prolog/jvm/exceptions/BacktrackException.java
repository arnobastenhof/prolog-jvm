package com.prolog.jvm.exceptions;

/**
 * Thrown by the ZIP machine's head unification instructions in case
 * backtracking fails due to there being no choice point to backtrack to.
 *
 * @author Arno Bastenhof
 *
 */
public final class BacktrackException extends Exception {

    private static final long serialVersionUID = 8791880327296548364L;

}
