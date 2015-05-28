package com.prolog.jvm.zip.api;

import com.prolog.jvm.zip.util.Instructions;
import com.prolog.jvm.zip.util.PlWords;

/**
 * Interface describing a runtime memory area.
 * <p>
 * Virtual memory is conceptualized as a large addressable array of 32-bit
 * words, being partitioned into areas like the code memory, the local and
 * global stacks, etc., with this interface describing the contract for each
 * single such area. In particular, these all share the same virtual address
 * space, and it is the responsibility of implementation classes to document
 * their bounds.
 * <p>
 * The following areas are assumed:
 * <ul>
 * <li> The global stack, used for storing compound terms.
 * <li> The local stack, where activation frames are allocated.
 * <li> The trail stack, tracking variable bindings that need to be reset upon
 * backtracking.
 * <li> The Push-Down List (PDL), used during unification.
 * <li> A small scratchpad area, used for storing return addresses and the
 * machine mode while executing bytecode instructions in between
 * {@link Instructions#FUNCTOR} and {@link Instructions#POP}.
 * <li> The code memory, storing the bytecode instructions for a compiled
 * program and for the compiled queries executed against it.
 * </ul>
 * The global and local stacks are to be placed together in the lower memory
 * region, as is necessary to ensure their cells can be addressed by {@link
 * PlWords} whose values are word pointers. Furthermore, Because of the chosen
 * representation of {@link PlWords}, the combined size of the global- and
 * local stacks is constrained by the largest array cell that can be addressed
 * using a 24-bit unsigned integer.
 *
 * @author Arno Bastenhof
 */
public interface MemoryArea {

	/**
	 * Reads a word from virtual memory.
	 *
	 * @param address the address in virtual memory to read from
	 * @throws IndexOutOfBoundsException if {@code address} falls outside of
	 * the bounds for this memory area
	 */
	int readFrom(int address);

	/**
	 * Writes a word to virtual memory.
	 *
	 * @param address the address in virtual memory to write to
	 * @param value the value to store at the specified {@code address}
	 * @throws IndexOutOfBoundsException if {@code address} falls outside of
	 * the bounds for this memory area
	 */
	void writeTo(int address, int value);

}
