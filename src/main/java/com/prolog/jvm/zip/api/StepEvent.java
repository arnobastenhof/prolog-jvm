package com.prolog.jvm.zip.api;

/**
 * Interface describing the information associated with the execution of a
 * single instruction (referred to by a step).
 *
 * @author Arno Bastenhof
 *
 */
public interface StepEvent {

	/**
	 * Returns the global- or local stack address that was matched against if
	 * {@link #getMode()} equals {@code MATCH}, or that was copied from if
	 * {@link #getMode()} equals {@code COPY} or {@code ARG}.
	 */
	int getStackAddress();

	/**
	 * Returns the address in code memory of the instruction that was
	 * executed during this event.
	 */
	int getCodeAddress();

	/**
	 * Returns the opcode of the instruction that was executed during this
	 * event.
	 */
	int getOpcode();

	/**
	 * Returns the operand of the instruction that was executed during this
	 * event.
	 */
	Object getOperand();

	/**
	 * Returns the machine mode at the time this step commenced.
	 */
	int getMode();

	/**
	 * Returns the addresses in the global- and local stack that were written
	 * to during this event, either because of variable bindings that were
	 * made, or because of backtracking.
	 */
	Iterable<Integer> getBindings();

}
