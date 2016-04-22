package com.prolog.jvm.zip;

import static com.prolog.jvm.zip.util.Instructions.ARG;
import static com.prolog.jvm.zip.util.Instructions.COPY;
import static com.prolog.jvm.zip.util.Instructions.MATCH;
import static com.prolog.jvm.zip.util.MemoryConstants.MAX_HEAP_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MAX_LOCAL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_GLOBAL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_HEAP_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_LOCAL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_PDL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_SCRATCHPAD_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_TRAIL_INDEX;
import static com.prolog.jvm.zip.util.PlWords.CONS;
import static com.prolog.jvm.zip.util.PlWords.FUNC;
import static com.prolog.jvm.zip.util.PlWords.LIS;
import static com.prolog.jvm.zip.util.PlWords.REF;
import static com.prolog.jvm.zip.util.PlWords.STR;
import static com.prolog.jvm.zip.util.PlWords.getWord;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import com.prolog.jvm.exceptions.BacktrackException;
import com.prolog.jvm.symbol.ClauseSymbol;
import com.prolog.jvm.symbol.FunctorSymbol;
import com.prolog.jvm.zip.api.ActivationRecord;
import com.prolog.jvm.zip.api.MemoryArea;
import com.prolog.jvm.zip.api.ZipFacade;
import com.prolog.jvm.zip.util.Instructions;
import com.prolog.jvm.zip.util.MemoryConstants;
import com.prolog.jvm.zip.util.PlWords;
import com.prolog.jvm.zip.util.Validate;

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
    private final MemoryArea heap;
    private final MemoryArea globalStack;
    private final MemoryArea localStack;
    private final MemoryArea wordStore;
    private final MemoryArea trailStack;
    private final MemoryArea pdl;
    private final MemoryArea scratchpad;

    // Machine registers
    private int mode;                       // Processor mode (PM)
    private int programctr;                 // Program counter (PC)
    private ActivationRecordImpl targetfrm; // Target (local) frame (L)
    private ActivationRecordImpl sourcefrm; // Source (local) frame (CL)
    private int globalptr;                  // Global stack top (G0)
    private int trailptr;                   // Trail top (TR0)
    private ActivationRecordImpl choicepnt; // Backtrack (local) frame (BL)
    private int pdlptr;                     // Push-Down List top
    private int scratchpadptr;              // Scratchpad top

    /**
     * Constructor. Note no null checks are done on any of the supplied
     * parameters. Instead, the state of the constructed object is validated by
     * {@link ZipFacadeImpl.Builder#build()}. This leafs room for subclasses
     * that are used only for unit testing specific methods, and for which not
     * all of the parameters below are actually needed.
     *
     * @param constants the constant pool
     * @param heap the memory area used for storing the bytecode instructions
     * @param globalStack the memory area used for the global stack
     * @param localStack the memory area used for the local stack
     * @param wordStore the combined memory areas for the global and local
     * stacks
     * @param trailStack the memory area used for the trail stack
     * @param pdl the memory area used for the Push-Down List
     * @param scratchpad the memory area used for the scratchpad
     */
    protected ZipFacadeImpl(final List<Object> constants,
            final MemoryArea heap, final MemoryArea globalStack,
            final MemoryArea localStack, final MemoryArea wordStore,
            final MemoryArea trailStack, final MemoryArea pdl,
            final MemoryArea scratchpad) {
        this.constants = constants;
        this.heap = heap;
        this.globalStack = globalStack;
        this.localStack = localStack;
        this.wordStore = wordStore;
        this.trailStack = trailStack;
        this.pdl = pdl;
        this.scratchpad = scratchpad;
    }

    // === Protected hooks ===

    /**
     * Returns whether the specified address is part of the local stack. Invoked
     * by {@link #bind(int, int)} to determine whether trailing is needed.
     * <p>
     * This method is intended to be overridden by mock implementations.
     *
     * @param address the address to be tested
     */
    protected boolean isLocal(final int address) {
        return address >= MIN_LOCAL_INDEX && address <= MAX_LOCAL_INDEX;
    }

    /**
     * Returns the backtrack global pointer, or
     * {@link ProcessorModes#MIN_GLOBAL_INDEX} if no choice point was pushed on
     * the local stack. Invoked by {@link #bind(int, int)} to determine whether
     * trailing is needed.
     * <p>
     * This method is intended to be overridden by mock implementations.
     */
    protected int getBacktrackGlobalPointer() {
        return this.choicepnt != null ? this.choicepnt.globalptr
                : MIN_GLOBAL_INDEX;
    }

    /**
     * Returns the smallest address in virtual memory for use by the Push-Down
     * List, being invoked by {@link #unifiable(int, int)} to determine whether
     * unification has succeeded. By default,
     * {@link MemoryConstants#MIN_PDL_INDEX} is returned, though mock
     * implementations used solely for testing the various memory areas in
     * isolation could override this method to return {@code 0} instead.
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
    public final void reset(final int queryAddr) {
        if (queryAddr < MIN_HEAP_INDEX || queryAddr > MAX_HEAP_INDEX) {
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
    public final void setMode(final int mode) {
        // API sacrifices preconditions for performance, so use asserts instead
        assert Instructions.MODES.keySet().contains(mode);

        this.mode = mode;
    }

    // === Constant pool ===

    @Override
    public final <T> T getConstant(final int index, final Class<T> clazz) {
        requireNonNull(clazz);
        if (index < 0 || index >= this.constants.size()) {
            throw new IndexOutOfBoundsException();
        }
        final Object obj = this.constants.get(index);
        // Next line throws ClassCastException if !(clazz.isInstance(obj))
        return clazz.cast(obj);
    }

    // Returns the index for the specified constant pool entry, throwing
    // and exception if not found.
    private int getConstantPoolIndex(final Object obj) {
        assert obj != null;
        final int index = this.constants.indexOf(obj);
        if (index == -1) {
            throw new IllegalArgumentException();
        }
        return index;
    }

    // === Code memory ===

    @Override
    public final int fetchOperator() {
        return this.mode | this.heap.readFrom(this.programctr++);
    }

    @Override
    public final int fetchOperand(final boolean isVariable) {
        final int result = this.heap.readFrom(this.programctr++);
        if (!isVariable) {
            return result;
        }
        int m = this.mode;
        while (true) {
            switch (m) {
            case MATCH: {
                return result + this.targetfrm.localptr;
            }
            case ARG: {
                return result + this.sourcefrm.localptr;
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
    public final int jump(final int address) {
        // API sacrifices preconditions for performance, so use asserts instead
        assert this.targetfrm != null;
        assert address >= MIN_HEAP_INDEX && address <= MAX_HEAP_INDEX;

        this.targetfrm.programctr = this.programctr;
        this.programctr = address;
        return this.targetfrm.localptr;
    }

    // === Global stack ===

    @Override
    public final int pushFunctor(final FunctorSymbol symbol) {
        // API sacrifices preconditions for performance, so use asserts instead
        assert symbol != null;
        assert symbol.getArity() > 0;

        final int result = getWord(STR, this.globalptr);
        this.wordStore.writeTo(this.globalptr++,
                getWord(FUNC, getConstantPoolIndex(symbol)));
        // Push arguments as unbound variables
        // (needed when executing FIRSTVAR in COPY mode)
        for (int i = 0; i < symbol.getArity(); i++) {
            final int word = getWord(REF, this.globalptr);
            this.wordStore.writeTo(this.globalptr++, word);
        }
        return result;
    }

    @Override
    public final void setWord(final int address, final FunctorSymbol symbol) {
        // API sacrifices preconditions for performance, so use asserts instead
        assert symbol != null;
        assert symbol.getArity() == 0;
        assert address >= MIN_GLOBAL_INDEX;
        assert address <= MAX_LOCAL_INDEX;

        final int word = getWord(CONS, getConstantPoolIndex(symbol));
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
            } else {
                address = this.sourcefrm.localptr + this.sourcefrm.size;
            }
        }
        this.targetfrm = new ActivationRecordImpl(address);
        return address;
    }

    @Override
    public final void pushChoicePoint(final ClauseSymbol clause) {
        // API sacrifices preconditions for performance, so use asserts instead
        assert clause != null;

        this.targetfrm.clause = clause;
        this.targetfrm.globalptr = this.globalptr;
        this.targetfrm.trailptr = this.trailptr;
        this.targetfrm.backtrackfrm = this.choicepnt;
        this.choicepnt = this.targetfrm;
    }

    @Override
    public final void pushSourceFrame(final int size) {
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
    public final void pushOnScratchpad(final int address) {
        // API sacrifices preconditions for performance, so use asserts instead
        assert address >= MIN_GLOBAL_INDEX;
        assert address <= MAX_LOCAL_INDEX;

        this.scratchpad.writeTo(this.scratchpadptr++, address); // push
        this.scratchpad.writeTo(this.scratchpadptr++, this.mode); // push
    }

    @Override
    public final int popFromScratchpad() {
        this.mode = this.scratchpad.readFrom(--this.scratchpadptr); // pop
        return this.scratchpad.readFrom(--this.scratchpadptr); // pop
    }

    // === Trail methods ===

    @Override
    public void trail(final int address) {
        assert address >= MIN_GLOBAL_INDEX && address <= MAX_LOCAL_INDEX;
        if (address < getBacktrackGlobalPointer() || isLocal(address)) {
            this.trailStack.writeTo(this.trailptr++, address);
        }
    }

    /*
     * Performs garbage collection on the trail stack between the specified
     * addresses from (inclusive) and to (exclusive), resetting the bindings
     * found therebetween on the global- and local local stacks. Made package-
     * private for testing purposes.
     */
    final void unwindTrail(final int from, final int to,
            final List<Integer> vars) {
        assert from > 0;
        assert from <= to;
        assert vars != null;
        assert vars.isEmpty();
        for (int i = from; i < to; i++) {
            final int address = this.trailStack.readFrom(i);
            this.wordStore.writeTo(address, getWord(REF, address));
            vars.add(address);
        }
        this.trailptr = from;
    }

    // === Dereferencing, binding and unification ===

    private int deref(final int address) {
        assert address >= MIN_GLOBAL_INDEX && address <= MAX_LOCAL_INDEX;
        final int word = this.wordStore.readFrom(address);
        final int tag = PlWords.getTag(word);
        final int value = PlWords.getValue(word);
        return (tag == REF && value != address) ? deref(value) : address;
    }

    @Override
    public final int getWordAt(final int address) {
        // API sacrifices preconditions for performance, so use asserts instead
        assert address >= MIN_GLOBAL_INDEX && address <= MAX_LOCAL_INDEX;

        return this.wordStore.readFrom(deref(address));
    }

    @Override
    public final void setWord(final int address, final int word) {
        // API sacrifices preconditions for performance, so use asserts instead
        assert address >= MIN_GLOBAL_INDEX && address <= MAX_LOCAL_INDEX;
        assert PlWords.TAGS.keySet().contains(PlWords.getTag(word));

        this.wordStore.writeTo(address, word);
    }

    @Override
    public final int bind(int address1, int address2) {
        // API sacrifices preconditions for performance, so use asserts instead
        assert address1 >= MIN_GLOBAL_INDEX && address1 <= MAX_LOCAL_INDEX;
        assert address2 >= MIN_GLOBAL_INDEX && address2 <= MAX_LOCAL_INDEX;

        address1 = deref(address1);
        address2 = deref(address2);
        final int t1 = PlWords.getTag(this.wordStore.readFrom(address1));
        final int t2 = PlWords.getTag(this.wordStore.readFrom(address2));
        if (t1 == REF && (t2 != REF || address2 < address1)) {
            final int word = this.wordStore.readFrom(address2);
            this.wordStore.writeTo(address1, word);
            trail(address1);
            return address1;
        }
        if (t2 == REF) {
            final int word = this.wordStore.readFrom(address1);
            this.wordStore.writeTo(address2, word);
            trail(address2);
            return address2;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final List<Integer> unifiable(final int a1, final int a2) {
        // API sacrifices preconditions for performance, so use asserts instead
        assert a1 >= MIN_GLOBAL_INDEX && a1 <= MAX_LOCAL_INDEX;
        assert a2 >= MIN_GLOBAL_INDEX && a2 <= MAX_LOCAL_INDEX;

        final List<Integer> bindings = new ArrayList<>();
        this.pdl.writeTo(this.pdlptr++, a1); // push
        this.pdl.writeTo(this.pdlptr++, a2); // push
        while (this.pdlptr != getMinPdlIndex()) {
            final int d1 = deref(this.pdl.readFrom(--this.pdlptr)); // pop
            final int d2 = deref(this.pdl.readFrom(--this.pdlptr)); // pop
            final int w1 = this.wordStore.readFrom(d1);
            final int t1 = PlWords.getTag(w1);
            if (t1 == REF) {
                bindings.add(bind(d1, d2));
                continue;
            }
            final int w2 = this.wordStore.readFrom(d2);
            final int t2 = PlWords.getTag(w2);
            final int v1 = PlWords.getValue(w1);
            final int v2 = PlWords.getValue(w2);
            switch (t2) {
            case REF: {
                bindings.add(bind(d1, d2));
                continue;
            }
            case CONS: {
                if (t1 != CONS || v1 != v2) {
                    return null;
                }
                continue;
            }
            case LIS: {
                if (t1 != LIS) {
                    return null;
                }
                this.pdl.writeTo(this.pdlptr++, v1); // push
                this.pdl.writeTo(this.pdlptr++, v2); // push
                this.pdl.writeTo(this.pdlptr++, v1 + 1); // push
                this.pdl.writeTo(this.pdlptr++, v2 + 1); // push
                continue;
            }
            case STR: {
                if (t1 != STR) {
                    return null;
                }
                final int f1 = PlWords.getValue(this.wordStore.readFrom(v1));
                final int f2 = PlWords.getValue(this.wordStore.readFrom(v2));
                if (f1 != f2) {
                    return null;
                }
                final int arity = getConstant(f1, FunctorSymbol.class)
                        .getArity();
                for (int i = 1; i <= arity; i++) {
                    this.pdl.writeTo(this.pdlptr++, v1 + i); // push
                    this.pdl.writeTo(this.pdlptr++, v2 + i); // push
                }
                continue;
            }
            default:
                throw new AssertionError();
            }
        }
        return bindings;
    }

    // === Backtracking ===

    @Override
    public final int backtrack(final List<Integer> vars)
            throws BacktrackException {
        // Validate preconditions
        requireNonNull(vars);
        Validate.argument(vars.isEmpty());

        // No choice point means nowhere to backtrack to
        if (this.choicepnt == null) {
            throw new BacktrackException();
        }

        // Restore machine state and unwind the trail
        this.mode = MATCH;
        this.programctr = this.choicepnt.clause.getHeapptr();
        if (this.choicepnt.sourcefrm != null) { // choicepnt != targetfrm
            this.sourcefrm = this.choicepnt.sourcefrm;
            this.targetfrm = this.choicepnt;
        }
        unwindTrail(this.choicepnt.trailptr, this.trailptr, vars);
        this.globalptr = this.choicepnt.globalptr;
        this.trailptr = this.choicepnt.trailptr;

        // See if there's a next clause alternative
        final ClauseSymbol next = this.choicepnt.clause.getNext();
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

    // === Debugging ===

    @Override
    public int getProgramCounter() {
        return this.programctr;
    }

    private static final class ActivationRecordImpl implements ActivationRecord {

        private int size;                     // No. of arguments and local vars
        private int programctr;             // Continuation program counter (CP)
        private ActivationRecordImpl sourcefrm; // Continuation local frame (CL)
        private ClauseSymbol clause;            // Backtrack clause pointer (BP)
        private int globalptr;                // Backtrack global stack top (BG)
        private ActivationRecordImpl backtrackfrm; // Backtrack local frame (BL)
        private int trailptr;                        // Backtrack trail top (BT)
        private final int localptr;               // Memory offset for var slots

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
    public static final class Builder extends AbstractZipFacadeBuilder<Builder> {

        public Builder() {
            this.instance = this;
        }

        /**
         * Builds a {@link ZipFacadeImpl} instance.
         *
         * @throws IllegalStateException if any of the protected members in
         * {@link AbstractZipFacadeBuilder} are null.
         */
        @Override
        public ZipFacadeImpl build() {
            // Build the facade
            ZipFacadeImpl facade = new ZipFacadeImpl(this.constants, this.heap,
                    this.globalStack, this.localStack, this.wordStore,
                    this.trailStack, this.pdl, this.scratchpad);

            // Validate
            Validate.state(facade.constants != null);
            Validate.state(facade.heap != null);
            Validate.state(facade.globalStack != null);
            Validate.state(facade.localStack != null);
            Validate.state(facade.wordStore != null);
            Validate.state(facade.trailStack != null);
            Validate.state(facade.pdl != null);
            Validate.state(facade.scratchpad != null);

            // If the build instance is in a consistent state, return it
            return facade;
        }
    }
}
