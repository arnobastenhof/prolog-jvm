package com.prolog.jvm.symbol;

import com.prolog.jvm.zip.util.Instructions;
import com.prolog.jvm.zip.util.Validate;

/**
 * A data aggregate collecting information about the occurrences of a variable
 * within a single clause.
 *
 * @author Arno Bastenhof
 *
 */
public final class VariableSymbol implements Symbol {

	private final int offset;
	private boolean seenBefore;

	/**
	 * Creates a new variable symbol with the specified offset into the
	 * activation record.
	 *
	 * @param offset the offset into the activation record; must be
	 * {@code >= 0}
	 * @throws IllegalArgumentException if {@code offset < 0}
	 */
	public VariableSymbol(final int offset) {
		Validate.argument(offset >= 0);
		this.offset = offset;
	}

	/**
	 * Returns whether the variable represented by this symbol was previously
	 * encountered during code generation. Used for determining whether to emit
	 * {@link Instructions#FIRSTVAR} or {@link Instructions#VAR} instructions.
	 */
	public boolean hasBeenSeenBefore() {
		return this.seenBefore;
	}

	/**
	 * Marks this variable symbol as having been previously encountered during
	 * code generation.
	 */
	public void setAsSeenBefore() {
		this.seenBefore = true;
	}

	/**
	 * Returns the offset in an activation record, identifying the address
	 * where this variable is stored.
	 */
	public int getOffset() {
		return this.offset;
	}
}
