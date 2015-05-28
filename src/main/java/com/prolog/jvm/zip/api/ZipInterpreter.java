package com.prolog.jvm.zip.api;

import java.io.IOException;
import java.io.Writer;

/**
 * Strategy interface for executing the ZIP's interpretation routine.
 * Implementations may differ with regard to the particular subset of the
 * ZIP's instruction set that they implement.
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
	 * @param out the target for writing the answer to
	 * @throws IOException
	 */
	public void execute(int queryAddress, Writer out) throws IOException;

}
