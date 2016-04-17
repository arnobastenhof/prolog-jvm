package com.prolog.jvm.compiler.ast;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;

import com.prolog.jvm.compiler.parser.TokenType;
import com.prolog.jvm.compiler.visitor.PrologVisitor;
import com.prolog.jvm.zip.util.Validate;

/**
 * Walker for {@link Ast} nodes based on Pattern 14 of of Parr (2010), Language
 * Implementation Patterns: Tree Grammar.
 * <p>
 * The walker discovers nodes by depth-first search, and finishes a node after
 * its children nodes have been walked. In between, it invokes callback methods
 * on a {@link PrologVisitor}, passing in the discovered node. In particular,
 * whether the AST is traversed in pre-, in- or post-order is determined by
 * which of the preVisit-, inVisit- or postVisit methods are overridden on the
 * {@link PrologVisitor} instance that was passed in as a parameter.
 * <p>
 * Our separation of the logic for walking a tree (discovering/finishing its
 * nodes) and for traversing it (applying actions in between) borrows heavily
 * from a similar setup found in the {@code ParseTreeWalker} class and
 * {@code ParseTreeLister} interface in ANTLR4, discussed in-depth by Parr
 * in The Definitive ANTLR4 Reference.
 * <p>
 * This class is made into a singleton through use of an enum type, as per the
 * advice found in Item 3 from Bloch (2008), Effective Java, 2nd edition.
 *
 * @author Arno Bastenhof
 *
 */
public enum AstWalker {

	/**
	 * The sole instance of this class.
	 */
	INSTANCE;

	/**
	 * Walks the supplied AST for a collection of program clauses, delegating
	 * the actions to be applied on nodes between their discovery and finishing
	 * to the specified {@code visitor}.
	 *
	 * @param root the root node of the AST to be walked; not allowed to be
	 * null and must have type {@link TokenType#PROGRAM}
	 * @param visitor the visitor containing the actions to be applied to the
	 * discovered nodes; not allowed to be null.
	 * @throws NullPointerException if {@code root == null || visitor == null}
	 * @throws IllegalArgumentException if root is not of type {@link
	 * TokenType#PROGRAM}
	 */
	public void walkProgram(final Ast root, final PrologVisitor<Ast> visitor) {
		requireNonNull(root);
		requireNonNull(visitor);
		Validate.argument(root.getNodeType() == TokenType.PROGRAM);
		final Iterator<Ast> it = root.iterator();
		while (it.hasNext()) {
			clause(match(it, TokenType.IMPLIES), visitor);
		}
	}

	/**
	 * Walks the supplied AST for a query, delegating the actions to be applied
	 * on nodes between their discovery and finishing to the specified {@code
	 * visitor}.
	 *
	 * @param root the {@link Ast} to be walked; not allowed to be null and
	 * must have type {@link TokenType#IMPLIES}
	 * @param visitor the visitor containing the actions to be applied to the
	 * discovered nodes; not allowed to be null.
	 * @throws NullPointerException if {@code root == null || visitor == null}
	 * @throws IllegalArgumentException if root is not of type {@link
	 * TokenType#IMPLIES}
	 */
	public void walkQuery(final Ast root, final PrologVisitor<Ast> visitor) {
		requireNonNull(root);
		requireNonNull(visitor);
		Validate.argument(root.getNodeType() == TokenType.IMPLIES);
		final Iterator<Ast> it = root.iterator();
		visitor.preVisitClause(root);
		match(it, TokenType.ATOM); // Match the imaginary clause head
		visitor.inVisitClause(root);
		walkGoals(it, visitor);
		visitor.postVisitClause(root);
	}

	private void clause(final Ast clause, final PrologVisitor<Ast> visitor) {
		assert clause != null;
		assert clause.getNodeType() == TokenType.IMPLIES;
		assert visitor != null;
		visitor.preVisitClause(clause);
		final Iterator<Ast> it = clause.iterator();
		literal(match(it, TokenType.ATOM), visitor); // Walk the head
		visitor.inVisitClause(clause);
		if (it.hasNext()) {
			walkGoals(it, visitor);
		}
		visitor.postVisitClause(clause);
	}

	private void walkGoals(final Iterator<Ast> it,
			final PrologVisitor<Ast> visitor) {
		assert it != null;
		assert visitor != null;
		assert it.hasNext();
		do { // Walk the goals (>= 1)
			final Ast goal = match(it, TokenType.ATOM);
			literal(goal, visitor);
			visitor.postVisitGoal(goal);
		} while (it.hasNext());
	}

	private void literal(final Ast structure,
			final PrologVisitor<Ast> visitor) {
		assert structure != null;
		assert visitor != null;
		final Iterator<Ast> it = structure.iterator();
		// Visit the arguments, if any
		while (it.hasNext()) {
			term(it.next(), visitor);
		}
	}

	private void term(final Ast term, final PrologVisitor<Ast> visitor) {
		assert term != null;
		assert visitor != null;
		switch (term.getNodeType()) {
		case VAR:
			visitor.visitVariable(term);
			break;
		case ATOM:
			structure(term, visitor);
			break;
		case NIL:
			visitor.visitConstant(term);
			break;
		default:
			throw new IllegalArgumentException(
					"Could not process " + term + " as a term.");
		}
	}

	private void structure(final Ast structure,
			final PrologVisitor<Ast> visitor) {
		assert structure != null;
		assert visitor != null;
		final Iterator<Ast> it = structure.iterator();
		if (it.hasNext()) {
			visitor.preVisitCompound(structure);
			do {
				term(it.next(), visitor);
			} while (it.hasNext());
			visitor.postVisitCompound(structure);
		}
		else {
			visitor.visitConstant(structure);
		}
	}

	private Ast match(final Iterator<Ast> it, final TokenType type) {
		assert it != null;
		assert it.hasNext();
		final Ast next = it.next();
		if (next.getNodeType() != type) {
			throw new IllegalStateException(
					"Expected node type " + type + ", but found "
							+ next.getNodeType() + " in " + next + ".");
		}
		return next;
	}

}
