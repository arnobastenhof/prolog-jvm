package com.prolog.jvm.zip;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Writer;

import com.prolog.jvm.zip.api.StepEvent;
import com.prolog.jvm.zip.api.StepListener;
import com.prolog.jvm.zip.api.ZipFacade;
import com.prolog.jvm.zip.util.Instructions;
import com.prolog.jvm.zip.util.PlWords;

/**
 * Step listener implementation that logs step events. Meant for debugging
 * purposes.
 *
 * @author Arno Bastenhof
 *
 */
public final class StepLogger implements StepListener {

    private final ZipFacade facade;
    private final Writer out;

    /**
     *
     * @throws NullPointerException if {@code facade == null || out == null}
     */
    public StepLogger(final ZipFacade facade, final Writer out) {
        this.facade = requireNonNull(facade);
        this.out = requireNonNull(out);
    }

    @Override
    public void handleEvent(StepEvent event) throws IOException {
        this.out.write(String.format("%07x  %07x %9s %14s  %5s  ",
                event.getStackAddress(),
                event.getCodeAddress(),
                Instructions.opcodeToString(event.getOpcode()),
                event.getOperand() == null ? "" : event.getOperand().toString(),
                Instructions.modeToString(event.getMode())));
        for (final int addr : event.getBindings()) {
            this.out.append('[').append(Integer.toHexString(addr)).append('/')
                    .append(PlWords.toString(this.facade.getWordAt(addr)))
                    .append(']');
        }
        this.out.write('\n');
    }

}
