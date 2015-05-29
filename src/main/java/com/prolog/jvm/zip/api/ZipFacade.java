package com.prolog.jvm.zip.api;

import java.util.List;

import com.prolog.jvm.exceptions.BacktrackException;
import com.prolog.jvm.main.Factory;
import com.prolog.jvm.symbol.ClauseSymbol;
import com.prolog.jvm.symbol.FunctorSymbol;

/**
 * Facade for accessing the ZIP machine's runtime data structures.
 * <p>
 * The idea of dissecting the specification of a virtual machine into familiar
 * object-oriented design patterns and hiding them behind a facade was explored
 * before at least in [1], though there having been pushed further in its
 * encapsulation of the machine state.
 * <p>
 * [1] Taivalsaari, Antero. "Implementing a Java Virtual Machine in the Java
 * Programming Language." (1998).
 *
 * @author Arno Bastenhof
 *
 */
public interface ZipFacade {

	// === Initialization ===

	/**
	 * (Re)sets the ZIP machine for executing the query at the specified {@code
	 * queryAddr}.
	 *
	 * @param queryAddr the address in code memory whereat the (compiled) query
	 * that is to be executed is stored
	 */
	void reset(int queryAddr);

	// === Machine mode ===

	/**
	 * Sets the machine mode.
	 *
	 * @param mode the machine mode; should be one of {@link ProcessorModes#ARG},
	 * {@link ProcessorModes#COPY} or {@link ProcessorModes#MATCH}
	 */
	void setMode(int mode);

	// === Constant pool ===

	/**
	 * Retrieves the constant for the specified index from the constant pool
	 * and casts it to the supplied class.
	 *
	 * @param index the index for the constant pool entry to retrieve
	 * @param clazz the class to cast the retrieved constant pool entry to
	 * @throws NullPointerException if {@code clazz == null}
	 * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index}
	 * exceeds the constant pool size - 1
	 * @throws ClassCastException if the constant for the specified index
	 * cannot be cast to the supplied class
	 */
	<T> T getConstant(int index, Class<T> clazz);

	// === Code memory ===

	/**
	 * Reads an instruction from code memory and returns its bit-wise
	 * disjunction with the machine mode, advancing the instruction pointer
	 * as a side effect.
	 */
	int readOperator();

	/**
	 * Reads an operand from code memory, advancing the instruction pointer as
	 * a side effect. By passing {@code true} for {@code isVariable}, this
	 * indicates the operand designates a variable, in which case its local
	 * stack address is returned. Otherwise, the operand is returned as is.
	 */
	int readOperand(boolean isVariable);

	/**
	 * Sets the instruction pointer to the specified {@code address} and saves
	 * the return address in the current target frame, if it exists.
	 *
	 * @return the local stack address at which the target frame has been
	 * allocated, or {@link ProcessorModes#MIN_LOCAL_INDEX} if it doesn't exist
	 */
	int jump(int address);

	// === Global stack ===

	/**
	 * Pushes the representation of a compound term on the global stack.
	 *
	 * @param symbol a symbol for a functor
	 * @return an STR-tagged word
	 */
	int pushFunctor(FunctorSymbol symbol);

	/**
	 * Writes the given constant {@code symbol} to the specified {@code
	 * address}.
	 */
	void writeConstant(int address, FunctorSymbol symbol);

	// === Local stack ===

	/**
	 * Pushes a target frame on the local stack, returning the address therein
	 * at which it has been allocated.
	 */
	int pushTargetFrame();

	/**
	 * Sets the last choice point to the current target frame, storing therein
	 * the current machine state.
	 *
	 * @param clause the backtrack clause pointer
	 */
	void pushChoicePoint(ClauseSymbol clause);

	/**
	 * Sets the last source frame to the current target frame, storing therein
	 * the specified frame {@code size}.
	 *
	 * @param Local stack addressfor the first local variable cell in the
	 * source frame to be pushed
	 * @param size the frame size, specifying the combined number of parameters
	 * and local variables
	 */
	void pushSourceFrame(int size);

	/**
	 * Pops the source frame.
	 *
	 * @return whether execution has finished
	 */
	boolean popSourceFrame();

	// === Scratchpad ===

	/**
	 * Pushes the specified address together with the machine mode on the
	 * scratchpad.
	 *
	 * @param address a global- or local stack address
	 */
	void pushOnScratchpad(int address);

	/**
	 * Pops a mode and global- or local stack address from the scratchpad,
	 * using the former for restoring the machine mode while returning the
	 * latter.
	 */
	int popFromScratchpad();

	// === Dereferencing, binding and unification ===

	/**
	 * Returns the word found by dereferencing the specified {@code address}.
	 */
	int getWordAt(int address);

	/**
	 * Sets the given global- or local stack {@code address} to contain the
	 * specified {@code word}.
	 */
	void setWord(int address, int word);

	/**
	 * Performs binding on the specified addresses, at least one of which is to
	 * contain a reference-tagged word.
	 *
	 * @param address1 a local- or global stack address
	 * @param address2 a local- or global stack address
	 */
	void bind(int address1, int address2);

	/**
	 * Attempts unification on the specified addresses and returns whether said
	 * attempt was successful.
	 *
	 * @param address1 a local- or global stack address
	 * @param address2 a local- or global stack address
	 * @return whether unification was successful
	 */
	boolean unifiable(int address1, int address2);

	// === Backtracking ===

	/**
	 * Performs backtracking.
	 *
	 * @throws BacktrackException if there was no choice point to backtrack to
	 */
	int backtrack() throws BacktrackException;

	/**
	 * Interface describing a builder for a {@link ZipFacade}.
	 * <p>
	 * While neglecting to set all properties before calling build may result
	 * in an object residing in an inconsistent state for the purpose of
	 * executing the {@link ZipFacade}'s main use case (i.e., running a
	 * query against a compiled program), depending on the implementation
	 * class, the resulting instance may still be useful for e.g. unit
	 * testing. As such, this API does not prescribe the validation of any
	 * invariants on the constructed object.
	 * <p>
	 * For production, it is recommended that a reference to a {@link
	 * ZipFacade} be obtained through the corresponding static factory method
	 * on {@link Factory}, which uses a {@code Builder} under water and
	 * ensures all properties are initialized before invoking build. Test
	 * classes, in contrast, can use a {@code Builder} to create a new
	 * {@link ZipFacade} for each unit test, tweaked to the particular
	 * conditions assessed thereby (e.g., through mocking the various {@link
	 * MemoryArea}s).
	 *
	 * @author Arno Bastenhof
	 *
	 */
	public interface Builder<T extends ZipFacade> {

		/**
		 * Builds a {@link ZipFacade} instance.
		 */
		T build();

		/**
		 * Sets the constant pool.
		 */
		Builder<T> setConstants(List<Object> constants);

		/**
		 * Sets the memory area used for storing the bytecode instructions
		 * (allowed to be null).
		 */
		Builder<T> setHeap(MemoryArea heap);

		/**
		 * Sets the memory area used for the global stack (allowed to be null).
		 */
		Builder<T> setGlobalStack(MemoryArea globalStack);

		/**
		 * Sets the memory area used for the local stack (allowed to be null).
		 */
		Builder<T> setLocalStack(MemoryArea localStack);

		/**
		 * Sets the combined memory areas of the local and global stacks
		 * (allowed to be null). Practical considerations require these to
		 * appear adjacent in the lower region of virtual memory in order for
		 * them to be addressable through a 24-bit unsigned integer, used for
		 * encoding word values. Moreover, operations like dereferencing,
		 * binding and unification are agnostic as to whether the words they
		 * operate on have been allocated on the global- or local stack,
		 * necessitating the current separate {@link MemoryArea} instance
		 * beside those used for accessing the global- and local stacks proper.
		 */
		Builder<T> setWordStore(MemoryArea wordStore);

		/**
		 * Sets the memory area used for the trail (allowed to be null).
		 */
		Builder<T> setTrailStack(MemoryArea trailStack);

		/**
		 * Sets the memory area used for the Push-Down List (allowed to be
		 * null).
		 */
		Builder<T> setPdl(MemoryArea pdl);

		/**
		 * Sets the memory area used for the scratchpad (allowed to be null).
		 */
		Builder<T> setScratchpad(MemoryArea scratchpad);

	}

}
