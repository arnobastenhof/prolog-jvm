package com.prolog.jvm.zip.api;

import java.io.BufferedReader;
import java.io.Writer;

/**
 * Strategy interface for executing the ZIP's interpretation routine.
 * Implementations may differ with regard to the particular subset of the ZIP's
 * instruction set that they implement.
 *
 * @author Arno Bastenhof
 *
 */
public interface ZipInterpreter {

    /**
     * Commences the interpreter's fetch/decode/execute cycle after setting its
     * program counter to the supplied {@code queryAddress}.
     *
     * @param queryAddress the code memory address for a compiled query
     * @param in the source for reading in user commands
     * @param out the target for writing the answer to
     * @throws Exception
     */
    void execute(int queryAddress, BufferedReader in, Writer out)
            throws Exception;

    /**
     * Registers the specified {@code listener} to receive notifications for
     * each instruction executed. The order in which listeners are notified is
     * unspecified, and need in particular not be dependent on the order in
     * which they were added.
     *
     * @throws NullPointerException if {@code listener == null}
     */
    void register(StepListener listener);

    /**
     * Unregisters the specified {@code listener}.
     *
     * @throws NullPointerException if {@code listener == null}
     */
    void unregister(StepListener listener);

}
