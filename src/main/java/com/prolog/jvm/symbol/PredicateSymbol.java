package com.prolog.jvm.symbol;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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

	private ClauseSymbol first; // first clause alternative

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
		checkState(this.first == null);
		this.first = checkNotNull(first);
	}

	/**
	 * Returns the first clause alternative for the predicate represented by
	 * this symbol.
	 */
	public ClauseSymbol getFirst() {
		return this.first;
	}
}
