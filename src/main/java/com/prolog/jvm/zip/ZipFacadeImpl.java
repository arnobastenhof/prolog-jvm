package com.prolog.jvm.zip;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.prolog.jvm.zip.util.Instructions.ARG;
import static com.prolog.jvm.zip.util.Instructions.COPY;
import static com.prolog.jvm.zip.util.Instructions.MATCH;
import static com.prolog.jvm.zip.util.MemoryConstants.MAX_CODE_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MAX_LOCAL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_CODE_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_GLOBAL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_LOCAL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_PDL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_SCRATCHPAD_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_TRAIL_INDEX;
import static com.prolog.jvm.zip.util.PlWords.CONS;
import static com.prolog.jvm.zip.util.PlWords.FUNCTOR;
import static com.prolog.jvm.zip.util.PlWords.LIS;
import static com.prolog.jvm.zip.util.PlWords.REF;
import static com.prolog.jvm.zip.util.PlWords.STR;
import static com.prolog.jvm.zip.util.PlWords.getWord;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prolog.jvm.exceptions.BacktrackException;
import com.prolog.jvm.symbol.ClauseSymbol;
import com.prolog.jvm.symbol.FunctorSymbol;
import com.prolog.jvm.zip.api.ActivationRecord;
import com.prolog.jvm.zip.api.MemoryArea;
import com.prolog.jvm.zip.api.ZipFacade;
import com.prolog.jvm.zip.util.Instructions;
import com.prolog.jvm.zip.util.MemoryConstants;
import com.prolog.jvm.zip.util.PlWords;

/**
 * A {@link ZipFacade} implementation.
 * <p>
 * This class is designed and documented for extension. More specifically, it
 * contains several template methods that offer hooks into its implementations
 * of the various methods specified in the {@link ZipFacade} interface, as
 * further documented in this API. In particular, unit tests need not target
 * this class directly, but may be defined for a subclass wherein said
 * overridable methods are given mock implementations.
 *
 * @author Arno Bastenhof
 *
 */
public class ZipFacadeImpl implements ZipFacade {

	// Constant pool (unmodifiable)
	private final List<Object> constants;

	// Memory areas
	private final MemoryArea codeMemory;
	private final MemoryArea globalStack;
	private final MemoryArea localStack;
	private final MemoryArea wordStore;
	private final MemoryArea trailStack;
	private final MemoryArea pdl;
	private final MemoryArea scratchpad;

	// Machine registers
	private int mode;                      // Processor mode (PM)
	private int programctr;                // Program counter (PC)
	private ActivationRecordImpl targetfrm; // Target (local) frame (L)
	private ActivationRecordImpl sourcefrm; // Source (local) frame (CL)
	private int globalptr;                 // Global stack top (G0)
	private int trailptr;                  // Trail top (TR0)
	private ActivationRecordImpl choicepnt; // Backtrack (local) frame (BL)
	private int pdlptr;                    // Push-Down List top
	private int scratchpadptr;             // Scratchpad top

	/**
	 * Constructor. Note no null checks are done on any of the supplied
	 * parameters. Instead, the state of the constructed object is validated by
	 * {@link ZipFacadeImpl.Builder#build()}. This leafs room for subclasses
	 * that are used only for unit testing specific methods, and for which not
	 * all of the parameters below are actually needed.
	 *
	 * @param code the compiled Prolog program and -query
	 * @param globalStack the memory area used for the global stack
	 * @param localStack the memory area used for the local stack
	 * @param wordStore the combined memory areas for the global and local
	 * stacks
	 * @param trailStack the memory area used for the trail stack
	 * @param pdl the memory area used for the Push-Down List
	 * @param scratchpad the memory area used for the scratchpad
	 */
	protected ZipFacadeImpl(
			List<Object> constants, MemoryArea codeMemory,
			MemoryArea globalStack, MemoryArea localStack,
			MemoryArea wordStore, MemoryArea trailStack,
			MemoryArea pdl, MemoryArea scratchpad) {
		this.constants = constants;
		this.codeMemory = codeMemory;
		this.globalStack = globalStack;
		this.localStack = localStack;
		this.wordStore = wordStore;
		this.trailStack = trailStack;
		this.pdl = pdl;
		this.scratchpad = scratchpad;
	}

	// === Protected hooks ===

	/**
	 * Returns whether the specified address is part of the local stack.
	 * Invoked by {@link #bind(int, int)} to determine whether trailing is
	 * needed.
	 * <p>
	 * This method is intended to be overridden by mock implementations.
	 *
	 * @param address the address to be tested
	 */
	protected boolean isLocal(int address) {
		return address >= MIN_LOCAL_INDEX && address <= MAX_LOCAL_INDEX;
	}

	/**
	 * Returns the backtrack global pointer, or {@link
	 * ProcessorModes#MIN_GLOBAL_INDEX} if no choice point was pushed on the
	 * local stack. Invoked by {@link #bind(int, int)} to determine whether
	 * trailing is needed.
	 * <p>
	 * This method is intended to be overridden by mock implementations.
	 */
	protected int getBacktrackGlobalPointer() {
		return this.choicepnt != null
				? this.choicepnt.globalptr : MIN_GLOBAL_INDEX;
	}

	/**
	 * Returns the local stack address of the last choice point, or {@link
	 * ProcessorModes#MIN_LOCAL_INDEX} if none exists. Invoked by {@link
	 * #bind(int, int)} to determine whether trailing is needed.
	 * <p>
	 * This method is intended to be overridden by mock implementations.
	 */
	protected int getBacktrackLocalPointer() {
		return this.choicepnt != null
				? this.choicepnt.localptr : MIN_LOCAL_INDEX;
	}

	/**
	 * Returns the smallest address in virtual memory for use by the Push-Down
	 * List, being invoked by {@link #unifiable(int, int)} to determine whether
	 * unification has succeeded. By default, {@link
	 * MemoryConstants#MIN_PDL_INDEX} is returned, though mock implementations
	 * used solely for testing the various memory areas in isolation could
	 * override this method to return {@code 0} instead.
	 */
	protected int getMinPdlIndex() {
		return MIN_PDL_INDEX;
	}

	// === Initialization ===

	/**
	 * @throws IndexOutOfBoundsException if {@code queryAddr <
	 * MemoryConstants#MIN_CODE_INDEX || queryAddr >
	 * MemoryConstants#MAX_CODE_INDEX}
	 */
	@Override
	public final void reset(int queryAddr) {
		if (queryAddr < MIN_CODE_INDEX || queryAddr > MAX_CODE_INDEX) {
			throw new IndexOutOfBoundsException();
		}
		this.mode = MATCH;
		this.programctr = queryAddr;
		this.targetfrm = null;
		this.sourcefrm = null;
		this.globalptr = MIN_GLOBAL_INDEX;
		this.trailptr = MIN_TRAIL_INDEX;
		this.choicepnt = null;
		this.pdlptr = MIN_PDL_INDEX;
		this.scratchpadptr = MIN_SCRATCHPAD_INDEX;

		pushTargetFrame();
	}

	// === Machine mode ===

	@Override
	public final void setMode(int mode) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert Instructions.MODES.keySet().contains(mode);

		this.mode = mode;
	}

	// === Constant pool ===

	@Override
	public final <T> T getConstant(int index, Class<T> clazz) {
		checkNotNull(clazz);
		if (index < 0 || index >= this.constants.size()) {
			throw new IndexOutOfBoundsException();
		}
		Object obj = this.constants.get(index);
		// Next line throws ClassCastException if !(clazz.isInstance(obj))
		return clazz.cast(obj);
	}

	// Returns the index for the specified constant pool entry, throwing
	// and exception if not found.
	private int getConstantPoolIndex(Object obj) {
		assert obj != null;
		int index = this.constants.indexOf(obj);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		return index;
	}

	// === Code memory ===

	@Override
	public final int readOperator() {
		return this.mode | this.codeMemory.readFrom(this.programctr++);
	}

	@Override
	public final int readOperand(boolean isVariable) {
		int result = this.codeMemory.readFrom(this.programctr++);
		if (!isVariable) {
			return result;
		}
		int m = this.mode;
		while (true) {
			switch (m) {
			case MATCH: {
				result += this.targetfrm.localptr; // redundant, but aids debug
				return result;
			}
			case ARG: {
				result += this.sourcefrm.localptr; // redundant, but aids debug
				return result;
			}
			case COPY: {
				m = this.scratchpad.readFrom(MIN_SCRATCHPAD_INDEX + 1);
				if (m != COPY) {
					continue;
				} // Else, fall-through
			}
			default:
				throw new IllegalStateException(Instructions.modeToString(m));
			}
		}
	}

	@Override
	public final int jump(int address) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert this.targetfrm != null;
		assert address >= MIN_CODE_INDEX && address <= MAX_CODE_INDEX;

		this.targetfrm.programctr = this.programctr;
		this.programctr = address;
		return this.targetfrm.localptr;
	}

	// === Global stack ===

	@Override
	public final int pushFunctor(FunctorSymbol symbol) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert symbol != null;
		assert symbol.getArity() > 0;

		int result = getWord(STR,this.globalptr);
		this.wordStore.writeTo(this.globalptr++,getWord(FUNCTOR, getConstantPoolIndex(symbol)));
		for (int i = 0; i < symbol.getArity(); i++) {
			int word = getWord(REF, this.globalptr);
			this.wordStore.writeTo(this.globalptr++, word);
		}
		return result;
	}

	@Override
	public final void writeConstant(int address, FunctorSymbol symbol) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert symbol != null;
		assert symbol.getArity() == 0;
		assert address >= MIN_GLOBAL_INDEX;
		assert address <= MAX_LOCAL_INDEX;

		int word = getWord(CONS,getConstantPoolIndex(symbol));
		this.wordStore.writeTo(address, word);
	}

	// === Local stack ===

	@Override
	public final int pushTargetFrame() {
		// Determine the address in the local stack at which to push
		int address = MIN_LOCAL_INDEX;
		if (this.sourcefrm != null) {
			if (this.choicepnt != null
					&& this.sourcefrm.localptr < this.choicepnt.localptr) {
				address = this.choicepnt.localptr + this.choicepnt.size;
			}
			address = this.sourcefrm.localptr + this.sourcefrm.size;
		}
		this.targetfrm = new ActivationRecordImpl(address);
		return address;
	}

	@Override
	public final void pushChoicePoint(ClauseSymbol clause) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert clause != null;

		this.targetfrm.clause = clause;
		this.targetfrm.globalptr = this.globalptr;
		this.targetfrm.trailptr = this.trailptr;
		this.targetfrm.backtrackfrm = this.choicepnt;
		this.choicepnt = this.targetfrm;
	}

	@Override
	public final void pushSourceFrame(int size) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert size >= 0;

		this.targetfrm.size = size;
		this.targetfrm.sourcefrm = this.sourcefrm; // Can be null!
		this.sourcefrm = this.targetfrm;
	}

	@Override
	public final boolean popSourceFrame() {
		// No continuation local frame means we're done.
		if (this.sourcefrm.sourcefrm == null) {
			return true;
		}
		this.programctr = this.sourcefrm.programctr;
		this.sourcefrm = this.sourcefrm.sourcefrm;
		return false;
	}

	// === Scratchpad methods ===

	@Override
	public final void pushOnScratchpad(int address) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert address >= MIN_GLOBAL_INDEX;
		assert address <= MAX_LOCAL_INDEX;

		this.scratchpad.writeTo(this.scratchpadptr++, address);   // push
		this.scratchpad.writeTo(this.scratchpadptr++, this.mode); // push
	}

	@Override
	public final int popFromScratchpad() {
		this.mode = this.scratchpad.readFrom(--this.scratchpadptr); // pop
		return this.scratchpad.readFrom(--this.scratchpadptr);      // pop
	}

	// === Trail methods ===

	/*
	 * Trails the specified address if needed. I.e., if a choice point has been
	 * allocated on the local stack and either: (a) {@code address} is part of
	 * the global stack and occurs before the backtrack global stack top; or
	 * (b) it is part of the local stack and occurs before the backtrack local
	 * frame. If neither condition applies, trailing would have no effect as
	 * the contents at {@code address} would already be garbage-collected at
	 * backtracking.
	 */
	private void trail(int address) {
		assert address >= MIN_GLOBAL_INDEX && address <= MAX_LOCAL_INDEX;
		if (address < getBacktrackGlobalPointer() ||
				(isLocal(address) && address < getBacktrackLocalPointer())) {
			this.trailStack.writeTo(this.trailptr++, address);
		}
	}

	/*
	 * Performs garbage collection on the trail stack between the specified
	 * addresses fromAddress (inclusive) and toAddress (exclusive), resetting
	 * resetting the bindings found therebetween on the global- and local
	 * local stacks. Made package-private for testing purposes.
	 */
	final void unwindTrail(int fromAddress, int toAddress) {
		assert fromAddress > 0;
		assert fromAddress <= toAddress;
		for (int i = fromAddress; i < toAddress; i++) {
			int binding = this.trailStack.readFrom(i);
			this.wordStore.writeTo(binding, getWord(REF, binding));
		}
		this.trailptr = fromAddress;
	}

	// === Dereferencing, binding and unification ===

	private int deref(int address) {
		assert address >= MIN_GLOBAL_INDEX && address <= MAX_LOCAL_INDEX;
		int word = this.wordStore.readFrom(address);
		int tag = PlWords.getTag(word);
		int value = PlWords.getValue(word);
		return (tag == REF && value != address) ? deref(value) : address;
	}

	@Override
	public final int getWordAt(int address) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert address >= MIN_GLOBAL_INDEX && address <= MAX_LOCAL_INDEX;

		return this.wordStore.readFrom(deref(address));
	}

	@Override
	public final void setWord(int address, int word) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert address >= MIN_GLOBAL_INDEX && address <= MAX_LOCAL_INDEX;
		assert PlWords.TAGS.keySet().contains(PlWords.getTag(word));

		this.wordStore.writeTo(address, word);
	}

	@Override
	public final void bind(int address1, int address2) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert address1 >= MIN_GLOBAL_INDEX && address1 <= MAX_LOCAL_INDEX;
		assert address2 >= MIN_GLOBAL_INDEX && address2 <= MAX_LOCAL_INDEX;

		int t1 = PlWords.getTag(this.wordStore.readFrom(address1));
		int t2 = PlWords.getTag(this.wordStore.readFrom(address2));
		if (t1 == REF && (t2 != REF || address2 < address1)) {
			int word = this.wordStore.readFrom(address2);
			this.wordStore.writeTo(address1, word);
			trail(address1);
		}
		else if (t2 == REF) {
			int word = this.wordStore.readFrom(address1);
			this.wordStore.writeTo(address2, word);
			trail(address2);
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public final boolean unifiable(int a1, int a2) {
		// API sacrifices preconditions for performance, so use asserts instead
		assert a1 >= MIN_GLOBAL_INDEX && a1 <= MAX_LOCAL_INDEX;
		assert a2 >= MIN_GLOBAL_INDEX && a2 <= MAX_LOCAL_INDEX;

		this.pdl.writeTo(this.pdlptr++, a1); // push
		this.pdl.writeTo(this.pdlptr++, a2); // push
		while (this.pdlptr != getMinPdlIndex()) {
			int d1 = deref(this.pdl.readFrom(--this.pdlptr)); // pop
			int d2 = deref(this.pdl.readFrom(--this.pdlptr)); // pop
			int w1 = this.wordStore.readFrom(d1);
			int t1 = PlWords.getTag(w1);
			if (t1 == REF) {
				bind(d1, d2);
				continue;
			}
			int w2 = this.wordStore.readFrom(d2);
			int t2 = PlWords.getTag(w2);
			int v1 = PlWords.getValue(w1);
			int v2 = PlWords.getValue(w2);
			switch (t2) {
			case REF: {
				bind(d1, d2);
				continue;
			}
			case CONS: {
				if (t1 != CONS || v1 != v2) {
					return false;
				}
				continue;
			}
			case LIS: {
				if (t1 != LIS) {
					return false;
				}
				this.pdl.writeTo(this.pdlptr++, v1);   // push
				this.pdl.writeTo(this.pdlptr++, v2);   // push
				this.pdl.writeTo(this.pdlptr++, v1+1); // push
				this.pdl.writeTo(this.pdlptr++, v2+1); // push
				continue;
			}
			case STR: {
				if (t1 != STR) {
					return false;
				}
				int f1 = PlWords.getValue(this.wordStore.readFrom(v1));
				int f2 = PlWords.getValue(this.wordStore.readFrom(v2));
				if (f1 != f2) {
					return false;
				}
				int arity = getConstant(f1, FunctorSymbol.class).getArity();
				for (int i = 1; i <= arity; i++) {
					this.pdl.writeTo(this.pdlptr++, v1+i); // push
					this.pdl.writeTo(this.pdlptr++, v2+i); // push
				}
				continue;
			}
			default:
				throw new AssertionError();
			}
		}
		return true;
	}

	// === Backtracking ===

	@Override
	public final int backtrack() throws BacktrackException {
		// No choice point means nowhere to backtrack to
		if (this.choicepnt == null) {
			throw new BacktrackException();
		}

		// Restore machine state and unwind the trail
		this.programctr = this.choicepnt.clause.getCode();
		if (this.choicepnt.sourcefrm != null) { // choicepnt != targetfrm
			this.sourcefrm = this.choicepnt.sourcefrm;
			this.targetfrm = this.choicepnt;
		}
		unwindTrail(this.choicepnt.trailptr, this.trailptr);
		this.globalptr = this.choicepnt.globalptr;
		this.trailptr = this.choicepnt.trailptr;

		// See if there's a next clause alternative
		ClauseSymbol next = this.choicepnt.clause.getNext();
		// If so, record it in the current choice point
		if (next != null) {
			this.choicepnt.clause = next;
		}
		// Otherwise, pop the current choice point
		else {
			this.choicepnt = this.choicepnt.backtrackfrm; // Can be null!
		}

		// Return the local stack frame address for the target frame
		return this.targetfrm.localptr;
	}

	// === Answers ===

	private final Map<Integer,String> varNames = new HashMap<>();
	private int ctr;
	private final Writer out = null; // TODO

	private String getVarName(int addr) {
		String result = this.varNames.get(addr);
		if (result == null) {
			result = "?" + this.ctr;
			this.varNames.put(addr, result);
		}
		return result;
	}

	// TODO Unit test this!!!
	// Note: first call should ensure addr >= MIN_LOCAL_INDEX && addr <= MAX_LOCAL_INDEX
	private final void walkCode(final int addr) throws IOException {
		int word = getWordAt(addr);
		switch (PlWords.getTag(word)) {
		case REF:
			// Since we already dereferenced, value must be equal to addr
			this.out.write(getVarName(addr));
		case STR:
			walkCode(PlWords.getValue(addr));
		case LIS: {
			int headAddr = PlWords.getValue(word);
			this.out.write("[");
			walkCode(headAddr);
			this.out.write(", ");
			walkCode(headAddr + 1);
			this.out.write("]");
		}
		case FUNCTOR: {
			int index = PlWords.getValue(word);
			FunctorSymbol symbol = getConstant(index, FunctorSymbol.class);
			assert symbol.getArity() > 0;
			this.out.write(symbol.getName());
			this.out.write('(');
			for (int i = 1; i <= symbol.getArity(); i++) {
				walkCode(addr + i);
				if (i < symbol.getArity()) {
					this.out.write(", ");
				}
			}
			this.out.write(')');
		}
		case CONS: {
			int index = PlWords.getValue(word);
			FunctorSymbol symbol = getConstant(index, FunctorSymbol.class);
			assert symbol.getArity() == 0;
			this.out.write(symbol.getName());
		}
		default:
			throw new AssertionError();
		}
	}


	private static final class ActivationRecordImpl implements ActivationRecord {

		private int size;                  // No. of arguments and local vars
		private int programctr;            // Continuation program counter (CP)
		private ActivationRecordImpl sourcefrm; // Continuation local frame (CL)
		private ClauseSymbol clause;       // Backtrack clause pointer (BP)
		private int globalptr;             // Backtrack global stack top (BG)
		private ActivationRecordImpl backtrackfrm; // Backtrack local frame (BL)
		private int trailptr;              // Backtrack trail top (BT)
		private final int localptr;        // Memory offset for var slots

		// Creates and initializes a target frame
		private ActivationRecordImpl(int localptr) {
			assert localptr >= MIN_LOCAL_INDEX;
			assert localptr <= MAX_LOCAL_INDEX;
			this.localptr = localptr;
		}

	}

	/**
	 * {@link ZipFacade.Builder} implementation for a {@link ZipFacadeImpl}.
	 *
	 * @author Arno Bastenhof
	 */
	public static final class Builder
	extends AbstractZipFacadeBuilder<ZipFacadeImpl> {

		/**
		 * Builds a {@link ZipFacadeImpl} instance.
		 *
		 * @throws IllegalStateException if any of the protected members in
		 * {@link AbstractZipFacadeBuilder} are null.
		 */
		@Override
		public ZipFacadeImpl build() {
			// Build the facade
			ZipFacadeImpl facade = new ZipFacadeImpl(
					this.constants, this.codeMemory, this.globalStack,
					this.localStack, this.wordStore, this.trailStack,
					this.pdl, this.scratchpad);

			// Validate
			checkState(facade.constants != null);
			checkState(facade.codeMemory != null);
			checkState(facade.globalStack != null);
			checkState(facade.localStack != null);
			checkState(facade.wordStore != null);
			checkState(facade.trailStack != null);
			checkState(facade.pdl != null);
			checkState(facade.scratchpad != null);

			// If the build instance is in a consistent state, return it
			return facade;
		}

	}

}
