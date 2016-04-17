package com.prolog.jvm.compiler.ast;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.prolog.jvm.compiler.parser.Token;
import com.prolog.jvm.compiler.parser.TokenType;

/**
 * The type for Abstract Syntax Trees, based on Pattern 9 of Parr (2010),
 * Language Implementation Patterns: Homogeneous AST.
 * <p>
 * Instances of this class are immutable, allowing them to be used as
 * {@link Map} keys. Moreover, each instance is considered inherently unique,
 * meaning that for any two non-null {@link Ast}'s {@code a} and {@code b},
 * {@code a.equals(b) iff a == b}. In particular, when used as keys, this
 * implies {@link IdentityHashMap} may be used for increased performance.
 *
 * @see AstWalker
 *
 * @author Arno Bastenhof
 *
 */
public final class Ast implements Iterable<Ast> {

	private final Token token;
	private final List<Ast> children;

	/**
	 * Static factory method returning a leaf node (i.e., with empty child
	 * list).
	 *
	 * @param token the token to be stored at this node; not allowed to be
	 * null
	 * @throws NullPointerException if {@code token == null}
	 */
	public static Ast getLeaf(final Token token) {
		return new Ast(requireNonNull(token), Collections.<Ast>emptyList());
	}

	/**
	 * Static factory method returning a Builder for an internal node (i.e.,
	 * with non-empty child list).
	 *
	 * @param token the token to be stored at this node; not allowed to be
	 * null
	 * @throws NullPointerException if {@code token == null}
	 */
	public static ASTBuilder getInternal(final Token token) {
		return new ASTBuilder(requireNonNull(token));
	}

	// Instantiation through static factory methods.
	private Ast(final Token token, final List<Ast> children) {
		assert token != null;
		assert children != null;
		this.token = token;
		this.children = Collections.unmodifiableList(children);
	}

	/**
	 * Returns the type of the token stored at this node.
	 */
	public TokenType getNodeType() {
		return this.token.getType();
	}

	/**
	 * Returns the matched input text for the token stored at this node.
	 */
	public String getText() {
		return this.token.getText();
	}

	/**
	 * Returns the number of child nodes.
	 */
	public int getArity() {
		return this.children.size();
	}

	/**
	 * Returns an {@link Iterator} providing an umodifiable view  of the child
	 * nodes. In particular, the returned iterator will throw
	 * {@link UnsupportedOperationException} whenever {@link Iterator#remove()}
	 * is invoked on it.
	 */
	@Override
	public Iterator<Ast> iterator() {
		return this.children.iterator();
	}

	@Override
	public String toString() {
		if (this.children.isEmpty()) {
			return this.token.getText();
		}
		final StringBuilder buffer = new StringBuilder("(")
			.append(this.token.getText());
		for (Ast child : this.children) {
			buffer.append(' ').append(child.toString());
		}
		return buffer.append(')').toString();
	}

	/**
	 * Builder used for constructing internal AST nodes (i.e., with non-empty
	 * child list).
	 *
	 * @author Arno Bastenhof
	 */
	public static final class ASTBuilder {

		private final Token token;
		private final List<Ast> children = new ArrayList<>();

		// Instantiation through static factory methods.
		ASTBuilder(final Token token) {
			assert token != null;
			this.token = token;
		}

		/**
		 * Adds a child node.
		 *
		 * @param child the child node to add; not allowed to be null
		 * @throws NullPointerException if {@code child == null}
		 */
		public void addChild(final Ast child) {
			this.children.add(requireNonNull(child));
		}

		/**
		 * Returns an {@link Ast} with a defensive copy of the constructed
		 * child list.
		 */
		public Ast build() {
			return new Ast(this.token, new ArrayList<>(this.children));
		}
	}
}
