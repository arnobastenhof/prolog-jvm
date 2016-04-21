package com.prolog.jvm.symbol;

import static java.util.Objects.requireNonNull;

import com.prolog.jvm.zip.util.Validate;

/**
 * A data aggregate adhering to the JavaBeans pattern, used for collecting
 * information associated with a Prolog predicate. Here, a predicate is
 * understood to comprise the set of clauses in a program that share the same
 * head literal.
 * <p>
 * Instances of this class are used both during compilation of program clauses
 * into bytecode as well as during the subsequent execution thereof (residing
 * by then in the runtime constant pool).
 *
 * @author Arno Bastenhof
 */
public final class PredicateSymbol implements Symbol {

	private final String name;  // kept for debugging purposes

	private ClauseSymbol first; // first clause alternative

	public PredicateSymbol(final String text, final int arity) {
		this.name = requireNonNull(text) + "/" + Integer.toString(arity);
	}

	/**
	 * Sets the first clause alternative for the predicate represented by this
	 * symbol.
	 *
	 * @param first first clause alternative; not allowed to be null
	 * @throws NullPointerException if {@code first == null}
	 * @throws IllegalStateException if the first clause alternative was
	 * already set
	 */
	public void setFirst(final ClauseSymbol first) {
		Validate.state(this.first == null);
		this.first = requireNonNull(first);
	}

	/**
	 * Returns the first clause alternative for the predicate represented by
	 * this symbol.
	 */
	public ClauseSymbol getFirst() {
		return this.first;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
