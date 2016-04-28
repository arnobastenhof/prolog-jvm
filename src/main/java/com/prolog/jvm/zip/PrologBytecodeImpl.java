package com.prolog.jvm.zip;

import static com.prolog.jvm.zip.util.Instructions.CALL;
import static com.prolog.jvm.zip.util.Instructions.CONSTANT;
import static com.prolog.jvm.zip.util.Instructions.ENTER;
import static com.prolog.jvm.zip.util.Instructions.EXIT;
import static com.prolog.jvm.zip.util.Instructions.FIRSTVAR;
import static com.prolog.jvm.zip.util.Instructions.FUNCTOR;
import static com.prolog.jvm.zip.util.Instructions.POP;
import static com.prolog.jvm.zip.util.Instructions.RETURN;
import static com.prolog.jvm.zip.util.Instructions.VAR;
import static java.util.Objects.requireNonNull;

import java.util.List;

import com.prolog.jvm.zip.PrologBytecodeImpl.MementoImpl;
import com.prolog.jvm.zip.api.MemoryArea;
import com.prolog.jvm.zip.api.PrologBytecode;
import com.prolog.jvm.zip.util.Instructions;
import com.prolog.jvm.zip.util.MemoryConstants;

/**
 * Implementation of {@link PrologBytecode}.
 *
 * @author Arno Bastenhof
 *
 */
public final class PrologBytecodeImpl implements PrologBytecode<MementoImpl> {

    private final MemoryArea code;
    private final List<Object> constants;

    private int codeptr = MemoryConstants.MIN_HEAP_INDEX;

    public PrologBytecodeImpl(final List<Object> constants,
            final MemoryArea code) {
        this.constants = requireNonNull(constants);
        this.code = requireNonNull(code);
    }

    @Override
    public int read(final int address) {
        return this.code.readFrom(address);
    }

    @Override
    public void writeIns(final int opcode, final int operand) {
        writeOpcode(opcode, FUNCTOR, CONSTANT, FIRSTVAR, VAR, CALL,
                ENTER, RETURN);
        this.code.writeTo(this.codeptr++, operand);
    }

    @Override
    public void writeIns(final int opcode) {
        writeOpcode(opcode, POP, EXIT);
    }

    /*
     * Writes the given opcode if it occurs in expected, while otherwise
     * throwing an exception.
     */
    private void writeOpcode(final int opcode, final int... expected) {
        for (int i : expected) {
            if (opcode == i) {
                this.code.writeTo(this.codeptr++, opcode);
                return;
            }
        }
        throw new IllegalArgumentException(Instructions.opcodeToString(opcode));
    }

    @Override
    public int getCodeSize() {
        return this.codeptr;
    }

    @Override
    public int getConstantPoolIndex(final Object obj) {
        requireNonNull(obj);
        int index = this.constants.indexOf(obj);
        if (index != -1) {
            return index;
        }
        this.constants.add(obj);
        return this.constants.size() - 1;
    }

    @Override
    public MementoImpl createMemento() {
        return new MementoImpl(this.codeptr, this.constants.size());
    }

    @Override
    public void setMemento(final MementoImpl memento) {
        this.codeptr = memento.codeptr;
        this.constants.subList(memento.poolSize, this.constants.size()).clear();
    }

    /**
     * Implementation of {@link PrologBytecode.Memento} for
     * {@link PrologBytecodeImpl}.
     *
     * @author Arno Bastenhof
     *
     */
    public static class MementoImpl implements PrologBytecode.Memento {
        private final int codeptr; // allocated heap top
        private final int poolSize; // constant pool size

        // Private constructor so only the surrounding class can invoke it
        private MementoImpl(final int codeptr, final int poolSize) {
            this.codeptr = codeptr;
            this.poolSize = poolSize;
        }
    }

}
