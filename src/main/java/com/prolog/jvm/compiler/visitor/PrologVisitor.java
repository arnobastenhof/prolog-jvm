package com.prolog.jvm.compiler.visitor;

import java.nio.file.FileVisitor;

import com.prolog.jvm.compiler.ast.Ast;
import com.prolog.jvm.compiler.parser.PrologParser;

/**
 * A visitor interface for collecting the semantic actions associated with a
 * single pass through a Prolog source program. Implementations may be passed
 * as an argument to a {@link PrologParser} (for the first pass) or to {@link
 * ASTWalker} (for subsequent passes over the constructed {@link Ast}).
 * <p>
 * This interface draws its inspiration from the {@link FileVisitor} API
 * introduced in Java SE 7 and from the ParseTreeVisitor interface part of
 * ANTLR4.
 *
 * @author Arno Bastenhof
 *
 * @param <P> the parameter type; typically {@link Token} (for the first
 * compiler pass) or {@link Ast} (for subsequent passes)
 */
public interface PrologVisitor<P> {

	/**
	 * Called upon discovery of a clause, before its head and goal literals
	 * have been walked.
	 */
	void preVisitClause(P param);

	/**
	 * Used for visiting the neck of a clause. More specifically, this method
	 * will be called between the finishing of a clause's head literal and
	 * before walking its goal literals, if any.
	 */
	void inVisitClause(P param);

	/**
	 * Called when finishing a clause, after its head and goal literals have
	 * been walked.
	 */
	void postVisitClause(P param);

	/**
	 * Called when finishing a goal literal, after its arguments have been
	 * walked.
	 */
	void postVisitGoal(P param);

	/**
	 * Called upon discovery of a compound term, before its arguments have been
	 * walked.
	 */
	void preVisitCompound(P param);

	/**
	 * Called when finishing a compound term, after its arguments have been
	 * walked.
	 */
	void postVisitCompound(P param);

	/**
	 * Called between the discovery and finishing of a constant.
	 */
	void visitConstant(P param);

	/**
	 * Called between the discovery and finishing of a variable.
	 */
	void visitVariable(P param);

}
