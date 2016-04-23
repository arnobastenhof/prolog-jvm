package com.prolog.jvm.zip;

import static com.prolog.jvm.zip.util.Instructions.ARG;
import static com.prolog.jvm.zip.util.Instructions.CALL;
import static com.prolog.jvm.zip.util.Instructions.CONSTANT;
import static com.prolog.jvm.zip.util.Instructions.COPY;
import static com.prolog.jvm.zip.util.Instructions.ENTER;
import static com.prolog.jvm.zip.util.Instructions.EXIT;
import static com.prolog.jvm.zip.util.Instructions.FIRSTVAR;
import static com.prolog.jvm.zip.util.Instructions.FUNCTOR;
import static com.prolog.jvm.zip.util.Instructions.MATCH;
import static com.prolog.jvm.zip.util.Instructions.POP;
import static com.prolog.jvm.zip.util.Instructions.VAR;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_LOCAL_INDEX;
import static com.prolog.jvm.zip.util.PlWords.CONS;
import static com.prolog.jvm.zip.util.PlWords.FUNC;
import static com.prolog.jvm.zip.util.PlWords.REF;
import static com.prolog.jvm.zip.util.PlWords.STR;
import static com.prolog.jvm.zip.util.PlWords.getWord;
import static com.prolog.jvm.zip.util.ReplConstants.FAILURE;
import static com.prolog.jvm.zip.util.ReplConstants.NEXT_ANSWER;
import static com.prolog.jvm.zip.util.ReplConstants.SUCCESS;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.prolog.jvm.exceptions.BacktrackException;
import com.prolog.jvm.main.Factory;
import com.prolog.jvm.symbol.ClauseSymbol;
import com.prolog.jvm.symbol.FunctorSymbol;
import com.prolog.jvm.symbol.PredicateSymbol;
import com.prolog.jvm.symbol.Symbol;
import com.prolog.jvm.zip.api.StepEvent;
import com.prolog.jvm.zip.api.StepListener;
import com.prolog.jvm.zip.api.ZipFacade;
import com.prolog.jvm.zip.api.ZipInterpreter;
import com.prolog.jvm.zip.util.Instructions;
import com.prolog.jvm.zip.util.PlWords;

/**
 * Implementation of a {@link ZipInterpreter}, as described in [1] and [2].
 * <p>
 * [1] Bowen, David L., Lawrence Byrd, and William F. Clocksin. A portable
 * Prolog compiler. Department of Artificial Intelligence, University of
 * Edinburgh, 1983.
 * <p>
 * [2] Clocksin, William F. "Design and simulation of a sequential Prolog
 * machine." New Generation Computing 3.1 (1985): 101-120.
 *
 * @author Arno Bastenhof
 *
 */
/*
 * Implementation notes: for the outside world, the ZIP interpreter is an
 * internal iterator, exposed through the execute method. The latter, however,
 * iteratively calls a step method for executing the next instruction, which
 * additionally stores information in the interpreter's instance fields about
 * how the VM state was mutated. Execute, in turn, may read these instance
 * fields in between steps in order to, say, output debugging information. In
 * other words, the interpreter is implemented as an external iterator, while at
 * the same time being its own (and only) client (via execute).
 */
public final class ZipInterpreterImpl implements ZipInterpreter {

    private final ZipFacade facade;
    private StepEventImpl event;
    private final Set<StepListener> listeners;

    /**
     *
     * @param facade a facade for the ZIP's internals; not allowed to be null
     */
    public ZipInterpreterImpl(final ZipFacade facade) {
        this.facade = requireNonNull(facade);
        this.event = new StepEventImpl();
        this.listeners = new HashSet<>();
    }

    // === Listener registration API ===

    @Override
    public void register(final StepListener listener) {
        this.listeners.add(requireNonNull(listener));
    }

    @Override
    public void unregister(final StepListener listener) {
        this.listeners.remove(requireNonNull(listener));
    }

    // === Fetch/Decode/Execute ===

    @Override
    public void execute(final int queryAddr, final BufferedReader in,
            final Writer out) throws Exception {
        this.facade.reset(queryAddr); // initialize the ZIP machine
        int stackAddr = MIN_LOCAL_INDEX;
        try {
            while ((stackAddr = step(stackAddr, in, out)) >= 0) {
                // Notify listeners
                for (final StepListener listener : this.listeners) {
                    listener.handleEvent(this.event);
                }
                // Reset event
                this.event = new StepEventImpl();
            }
        } catch (final BacktrackException e) {
            out.write(FAILURE);
        }
    }

    private int step(final int stackAddr, final BufferedReader in,
            final Writer out) throws IOException, BacktrackException {
        final int operator = this.facade.fetchOperator();

        this.event.stackAddress = stackAddr;
        this.event.codeAddress = this.facade.getProgramCounter();
        this.event.opcode = Instructions.getOpcode(operator);
        this.event.mode = Instructions.getMode(operator);

        switch (operator) {
        case MATCH | FUNCTOR:
            return matchFunctor(stackAddr, fetchFunctorOperand());
        case MATCH | CONSTANT:
            return matchConstant(stackAddr, fetchFunctorOperand());
        case MATCH | FIRSTVAR:
            return matchVariable(true, stackAddr, fetchVarOperand());
        case MATCH | VAR:
            return matchVariable(false, stackAddr, fetchVarOperand());
        case MATCH | ENTER:
            return enterClause(fetchSizeOperand());
        case MATCH | POP:
            // Fall-through
        case COPY | POP:
            return this.facade.popFromScratchpad();
        case COPY | FUNCTOR:
            // Fall-through
        case ARG | FUNCTOR:
            return argFunctor(stackAddr, fetchFunctorOperand());
        case COPY | CONSTANT:
            // Fall-through
        case ARG | CONSTANT:
            return copyConstant(stackAddr, fetchFunctorOperand());
        case COPY | FIRSTVAR:
            return copyVariable(true, stackAddr, fetchVarOperand());
        case COPY | VAR:
            return copyVariable(false, stackAddr, fetchVarOperand());
        case ARG | FIRSTVAR:
            return argVariable(true, stackAddr, fetchVarOperand());
        case ARG | VAR:
            return argVariable(false, stackAddr, fetchVarOperand());
        case ARG | CALL:
            return callPredicate(fetchPredicateOperand());
        case ARG | EXIT: {
            return exitClause(in, out);
        }
        default:
            throw new IllegalArgumentException(Instructions.toString(operator));
        }
    }

    // === Convenience methods for reading operands from code memory ===

    // operand for FUNCTOR and CONSTANT
    private FunctorSymbol fetchFunctorOperand() {
        return fetchSymbolOperand(FunctorSymbol.class);
    }

    // operand for CALL
    private ClauseSymbol fetchPredicateOperand() {
        return fetchSymbolOperand(PredicateSymbol.class).getFirst();
    }

    // operand for FIRSTVAR and VAR
    private int fetchVarOperand() {
        return fetchIntOperand(true);
    }

    // operand for ENTER
    private int fetchSizeOperand() {
        return fetchIntOperand(false);
    }

    private <T extends Symbol> T fetchSymbolOperand(final Class<T> clazz) {
        final int index = this.facade.fetchOperand(false);
        final T symbol = this.facade.getConstant(index, clazz);
        this.event.operand = symbol;
        return symbol;
    }

    private int fetchIntOperand(boolean isVariable) {
        final int numeric = this.facade.fetchOperand(isVariable);
        this.event.operand = isVariable ? Integer.toHexString(numeric)
                : Integer.toString(numeric);
        return numeric;
    }

    // === Instruction implementations ===

    private int matchFunctor(final int stackAddr, final FunctorSymbol symbol)
            throws BacktrackException {
        final int word = this.facade.getWordAt(stackAddr);
        switch (PlWords.getTag(word)) {
        case REF: {
            final int address = PlWords.getValue(word);
            this.facade.trail(address);
            final int functor = this.facade.pushFunctor(symbol);
            this.facade.setWord(address, functor);
            this.event.bindings.add(address);
            this.facade.pushOnScratchpad(stackAddr + 1);
            this.facade.setMode(COPY);
            return PlWords.getValue(functor) + 1;
        }
        case STR: {
            final int globalAddr = PlWords.getValue(word);
            final int index = PlWords.getValue(this.facade
                    .getWordAt(globalAddr));
            if (symbol != this.facade.getConstant(index, FunctorSymbol.class)) {
                return this.facade.backtrack(this.event.bindings);
            }
            this.facade.pushOnScratchpad(stackAddr + 1);
            return globalAddr + 1;
        }
        default:
            return this.facade.backtrack(this.event.bindings);
        }
    }

    private int matchConstant(final int stackAddr, final FunctorSymbol symbol)
            throws BacktrackException {
        final int word = this.facade.getWordAt(stackAddr);
        switch (PlWords.getTag(word)) {
        case REF: {
            final int address = PlWords.getValue(word);
            this.facade.setWord(address, symbol);
            this.facade.trail(address);
            this.event.bindings.add(address);
            break;
        }
        case CONS: {
            final int index = PlWords.getValue(word);
            if (symbol != this.facade.getConstant(index, FunctorSymbol.class)) {
                return this.facade.backtrack(this.event.bindings);
            }
            break;
        }
        default:
            return this.facade.backtrack(this.event.bindings);
        }
        return stackAddr + 1;
    }

    private int matchVariable(final boolean firstOccurrence, final int addr,
            final int localAddr) throws BacktrackException {
        final List<Integer> unifier;
        if (firstOccurrence) {
            this.facade.setWord(localAddr, this.facade.getWordAt(addr));
            this.event.bindings.add(localAddr);
        } else if ((unifier = this.facade.unifiable(localAddr, addr)) == null) {
            return this.facade.backtrack(this.event.bindings);
        } else {
            this.event.bindings.addAll(unifier);
        }
        return addr + 1;
    }

    private int copyConstant(final int addr, final FunctorSymbol symbol) {
        this.facade.setWord(addr, symbol);
        this.event.bindings.add(addr);
        return addr + 1;
    }

    private int argVariable(final boolean firstOccurrence, final int addr,
            final int localAddr) {
        int word = 0;
        if (firstOccurrence) {
            word = getWord(REF, localAddr);
            this.facade.setWord(localAddr, word);
        } else {
            word = this.facade.getWordAt(localAddr);
        }
        this.facade.setWord(addr, word);
        this.event.bindings.add(addr);
        return addr + 1;
    }

    private int copyVariable(final boolean firstOccurrence,
            final int globalAddr, final int localAddr) {
        if (firstOccurrence) {
            this.facade.setWord(localAddr, this.facade.getWordAt(globalAddr));
            this.event.bindings.add(localAddr);
        } else {
            this.event.bindings.add(this.facade.bind(globalAddr, localAddr));
        }
        return globalAddr + 1;
    }

    private int argFunctor(final int stackAddr, final FunctorSymbol symbol) {
        final int word = this.facade.pushFunctor(symbol);
        this.facade.setWord(stackAddr, word);
        this.event.bindings.add(stackAddr);
        this.facade.pushOnScratchpad(stackAddr + 1);
        this.facade.setMode(COPY);
        return PlWords.getValue(word) + 1;
    }

    // localAddr contains the local stack frame address for the first local
    // variable cell in the source frame to be pushed
    private int enterClause(final int size) {
        // The old target frame becomes the new source frame
        this.facade.pushSourceFrame(size);

        // Set machine mode to ARG
        this.facade.setMode(ARG);

        // Push a new target frame for the first goal
        return this.facade.pushTargetFrame();
    }

    private int callPredicate(final ClauseSymbol symbol) {
        // Push a choice point if necessary
        final ClauseSymbol next = symbol.getNext();
        if (next != null) {
            this.facade.pushChoicePoint(next);
        }

        // Set the machine mode and jump to the first clause alternative for
        // the called predicate
        this.facade.setMode(MATCH);
        return this.facade.jump(symbol.getHeapptr());
    }

    private int exitClause(final BufferedReader in, final Writer out)
            throws IOException, BacktrackException {
        // If popSourceFrame returns true, we have an answer
        if (this.facade.popSourceFrame()) {
            // If writeAnswer returns true, look for more
            if (writeAnswer(in, out)) {
                return this.facade.backtrack(this.event.bindings);
            }
            // else, we're done
            out.write(SUCCESS);
            return -1;
        }
        // If we're not done yet, push a new target frame
        // TODO Does double work if preceded by ENTER or jumps to
        // another EXIT (i.e., last call)
        return this.facade.pushTargetFrame();
    }

    // == Answers ===

    // Returns whether to backtrack and look for more answers
    private boolean writeAnswer(final BufferedReader in, final Writer out)
            throws IOException {
        Map<Integer,String> qVars = Factory.getQueryVars();

        // No query variables means nothing to print and no backtracking to do
        final Set<Integer> addresses = qVars.keySet();
        if (addresses.isEmpty()) {
            return false;
        }

        // qVars as returned by Factory is unmodifiable to guarantee that
        // multiple invocations of this method for printing alternative answers
        // to the same query are mutually independent. Thus, we should make a
        // copy here.
        qVars = new HashMap<>(qVars);

        for (final Integer address : addresses) {
            out.append(Factory.getQueryVars().get(address)).write(" = ");
            walkWord(qVars, address.intValue(), out);
            out.write(' ');
        }
        out.flush();
        return NEXT_ANSWER.equals(in.readLine());
    }

    private String getVarName(final Map<Integer,String> qVars, final int var) {
        final Integer address = Integer.valueOf(var);
        String result = qVars.get(address);
        if (result == null) {
            result = "?" + qVars.keySet().size();
            qVars.put(address, result);
        }
        return result;
    }

    private final void walkWord(final Map<Integer,String> qVars,
            final int addr, final Writer out) throws IOException {
        final int word = this.facade.getWordAt(addr);
        switch (PlWords.getTag(word)) {
        case REF: {
            out.write(getVarName(qVars, PlWords.getValue(word)));
            return;
        }
        case STR: {
            walkWord(qVars, PlWords.getValue(word), out);
            return;
        }
        case FUNC: {
            int index = PlWords.getValue(word);
            final FunctorSymbol symbol = this.facade.getConstant(index,
                    FunctorSymbol.class);
            assert symbol.getArity() > 0;
            out.append(symbol.getName()).write('(');
            for (int i = 1; i <= symbol.getArity(); i++) {
                walkWord(qVars, addr + i, out);
                if (i < symbol.getArity()) {
                    out.write(", ");
                }
            }
            out.write(')');
            return;
        }
        case CONS: {
            final int index = PlWords.getValue(word);
            final FunctorSymbol symbol = this.facade.getConstant(index,
                    FunctorSymbol.class);
            assert symbol.getArity() == 0;
            out.write(symbol.getName());
            return;
        }
        default:
            throw new IllegalArgumentException(PlWords.toString(word));
        }
    }

    // === Nested classes ===

    private static class StepEventImpl implements StepEvent {

        private int stackAddress;
        private int codeAddress;
        private int opcode;
        private Object operand;
        private int mode;
        private final List<Integer> bindings = new ArrayList<>();

        @Override
        public int getStackAddress() {
            return this.stackAddress;
        }

        @Override
        public int getCodeAddress() {
            return this.codeAddress;
        }

        @Override
        public int getOpcode() {
            return this.opcode;
        }

        @Override
        public Object getOperand() {
            return this.operand;
        }

        @Override
        public int getMode() {
            return this.mode;
        }

        @Override
        public Iterable<Integer> getBindings() {
            return this.bindings;
        }
    }

}
