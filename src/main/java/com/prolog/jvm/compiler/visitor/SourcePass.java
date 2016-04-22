package com.prolog.jvm.compiler.visitor;

import java.util.ArrayDeque;
import java.util.Deque;

import com.prolog.jvm.compiler.ast.Ast;
import com.prolog.jvm.compiler.ast.Ast.ASTBuilder;
import com.prolog.jvm.compiler.parser.Token;
import com.prolog.jvm.compiler.parser.TokenType;
import com.prolog.jvm.compiler.parser.Tokens;
import com.prolog.jvm.zip.util.Validate;

/**
 * Visitor detailing the first pass through a Prolog source program, converting
 * it to an {@link Ast}.
 *
 * @author Arno Bastenhof
 */
public final class SourcePass extends BasicPrologVisitor<Token> {

    // Stack of AST builders to construct the AST
    private final Deque<ASTBuilder> builders = new ArrayDeque<>();

    /**
     * @param token expected to be {@link Tokens#PROGRAM} for compiling programs
     * or {@link Tokens#IMPLIES} for queries.
     * @throws IllegalArgumentException if {@code token} does not have token
     * type {@link TokenType#PROGRAM} or {@link TokenType#IMPLIES}
     */
    public SourcePass(final Token token) {
        Validate.argument(token == Tokens.PROGRAM || token == Tokens.IMPLIES);
        push(token); // Create the AST root node
    }

    /**
     * Retrieves the {@link Ast} built by this visitor.
     *
     * @throws IllegalStateException if this pass has not yet completed
     */
    public Ast getAst() {
        Validate.state(this.builders.size() == 1);
        return this.builders.getLast().build();
    }

    @Override
    public void preVisitClause(Token clause) {
        push(clause);
    }

    @Override
    public void postVisitClause(Token param) {
        pop();
    }

    @Override
    public void preVisitCompound(Token param) {
        push(param);
    }

    @Override
    public void postVisitCompound(Token param) {
        pop();
    }

    @Override
    public void visitConstant(Token constant) {
        this.builders.getFirst().addChild(Ast.getLeaf(constant));
    }

    @Override
    public void visitVariable(Token variable) {
        this.builders.getFirst().addChild(Ast.getLeaf(variable));
    }

    // === Private implementation ===

    // Push a builder for a new intermediate AST node
    private void push(Token token) {
        this.builders.push(Ast.getInternal(token));
    }

    private void pop() {
        // Remove the builder on the top of the stack
        final ASTBuilder builder = this.builders.pop();
        // Build, and add as a child to the new top
        this.builders.getFirst().addChild(builder.build());
    }
}
