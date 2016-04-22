package com.prolog.jvm.compiler.visitor;

import com.prolog.jvm.compiler.ast.Ast;

/**
 * Basic implementation of a {@link PrologVisitor} that does nothing.
 *
 * @author Arno Bastenhof
 *
 * @param <P> the parameter type; typically {@link Token} (for the first
 * compiler pass) or {@link Ast} (for subsequent passes)
 */
public class BasicPrologVisitor<P> implements PrologVisitor<P> {

    @Override
    public void preVisitClause(P param) {
        // Does nothing.
    }

    @Override
    public void inVisitClause(P param) {
        // Does nothing.
    }

    @Override
    public void postVisitClause(P param) {
        // Does nothing.
    }

    @Override
    public void postVisitGoal(P param) {
        // Does nothing.
    }

    @Override
    public void preVisitCompound(P param) {
        // Does nothing.
    }

    @Override
    public void postVisitCompound(P param) {
        // Does nothing.
    }

    @Override
    public void visitConstant(P param) {
        // Does nothing.
    }

    @Override
    public void visitVariable(P param) {
        // Does nothing.
    }

}
