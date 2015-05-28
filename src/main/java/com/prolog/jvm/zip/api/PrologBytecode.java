package com.prolog.jvm.zip.api;

import com.prolog.jvm.zip.util.Instructions;

/**
 * Representation of a compiled Prolog program and its query, containing their
 * bytecode instructions and a constant pool. The latter serves much the same
 * function as the external reference tables from the original ZIP
 * specification, with the difference that the referenced works postulate a
 * separate table for each clause, whereas in the current implementation all
 * compiled clauses share a single constant pool.
 *
 * @author Arno Bastenhof
 *
 * @param <M> the supported implementation of {@link PrologBytecode.Memento}
 */
public interface PrologBytecode<M extends PrologBytecode.Memento> {

	/**
	 * Writes an instruction taking a single operand.
	 *
	 * @param opcode the instruction's opcode; must be one of
	 * {@link Instructions#FUNCTOR}, {@link Instructions#CONSTANT},
	 * {@link Instructions#VAR}, {@link Instructions#FISTVAR},
	 * {@link Instructions#CALL} or {@link Instructions#ENTER}
	 * @param operand the instruction's operand
	 * @throws IndexOutOfBoundsException if the code area has grown to its
	 * maximum size, as defined by {@link ProcessorModes#CODE_SIZE}
	 * @throws IllegalArgumentException if {@code opcode} is not one of the
	 * permitted opcodes
	 */
	void writeIns(int opcode, int operand);

	/**
	 * Writes an instruction taking no operands.
	 *
	 * @param opcode the instruction's opcode; must be one of
	 * {@link Instructions#POP} or {@link Instructions#EXIT}
	 * @throws IndexOutOfBoundsException if the code area has grown to its
	 * maximum size, as defined by {@link ProcessorModes#CODE_SIZE}
	 * @throws IllegalArgumentException if {@code opcode} is not one of the
	 * permitted opcodes
	 */
	void writeIns(int opcode);

	/**
	 * Reads an integer from the specified address in the code area.
	 *
	 * @param address the address in the code area to read from
	 * @throws IndexOutOfBoundsException if {@code address < 0 ||
	 * address >= ZipConstants.CODE_SIZE}
	 */
	int read(int address);

	/**
	 * Returns the size (measured in 32-bit words) of the written bytecode.
	 */
	int getCodeSize();

	/**
	 * Returns the index for the specified constant pool entry. If not found,
	 * the constant is added first.
	 *
	 * @param obj a constant pool entry; not allowed to be null
	 * @throws NullPointerException if {@code obj == null}
	 * @throws IllegalArgumentException if {@code obj} was not found and
	 * {@code mustBePresent == true}
	 */
	int getConstantPoolIndex(Object obj);

	/**
	 * Creates a memento, containing the state of the implementation class
	 * at the moment this method was invoked.
	 * <p>
	 * Meant for invocation immediately after compilation of a program, and
	 * prior to compiling the first query. The returned memento may then be
	 * (re)used through {@link #setMemento(Memento)} each time after a query
	 * has been executed to prevent the code area from overflowing.
	 */
	M createMemento();

	/**
	 * Restores the state to that found in the specified {@code memento}.
	 */
	void setMemento(M memento);

	/**
	 * Marker interface for a memento, capturing a snapshot of the state of a
	 * {@link PrologBytecode} implementation.
	 *
	 * @author Arno Bastenhof
	 *
	 */
	interface Memento {
		// Nothing.
	}

}
