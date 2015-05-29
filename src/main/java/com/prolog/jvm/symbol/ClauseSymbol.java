package com.prolog.jvm.symbol;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A data aggregate adhering to the JavaBeans pattern, used for storing and
 * retrieving information associated with a clause occurrence in a Prolog
 * program.
 * <p>
 * Instances of this class are used both during compilation of program clauses
 * into bytecode (at which point they are created and their state is set), as
 * well as during the subsequent execution thereof (residing by then in the
 * runtime constant pool).
 *
 * @author Arno Bastenhof
 *
 */
public final class ClauseSymbol implements Symbol {

	private int params;        // number of parameters
	private int locals;        // number of local variables
	private int code;          // offset into code area
	private ClauseSymbol next; // next clause alternative

	/**
	 * Sets the number of parameters for the clause represented by this symbol,
	 * coinciding with the arity of its head literal.
	 *
	 * @param params the number of parameters for this clause; must be {@code
	 * >= 0}
	 * @throws IllegalArgumentException if {@code params < 0}
	 */
	public void setParams(int params) {
		checkArgument(params >= 0);
		this.params = params;
	}

	/**
	 * Sets the number of local variables for the clause represented by this
	 * symbol.
	 *
	 * @param locals the number of local variables for this clause; must be
	 * {@code >= 0}
	 * @throws IllegalArgumentException if {@code locals < 0}
	 */
	public void setLocals(int locals) {
		checkArgument(locals >= 0);
		this.locals = locals;
	}

	/**
	 * Sets the offset into the code area of the bytecode block for the clause
	 * represented by this symbol.
	 *
	 * @param code an offset into the code area; must  be {@code >= 0}
	 * @throws IllegalArgumentException if {@code code < 0}
	 */
	public void setCode(int code) {
		checkArgument(code >= 0);
		this.code = code;
	}

	/**
	 * Sets the next clause alternative
	 *
	 * @param next clause symbol for the next alternative; not allowed to be
	 * null
	 * @throws NullPointerException if {@code next == null}.
	 */
	public void setNext(ClauseSymbol next) {
		this.next = checkNotNull(next);
	}

	/**
	 * Returns the number of parameters for the clause represented by this
	 * symbol, coinciding with the number of cells in an activation record
	 * needed for storing the parameters of its head literal.
	 */
	public int getParams() {
		return this.params;
	}

	/**
	 * Returns the number of local variables for the clause represented by this
	 * symbol.
	 */
	public int getLocals() {
		return this.locals;
	}

	/**
	 * Returns the offset into the code area of the bytecode block for the
	 * clause represented by this symbol.
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * Returns the next clause alternative. If there is none (which can only be
	 * the case if {@link #setNext(ClauseSymbol)} was never invoked), null is
	 * returned instead.
	 */
	public ClauseSymbol getNext() {
		return this.next;
	}

}