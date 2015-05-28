package com.prolog.jvm.main;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import com.prolog.jvm.compiler.Compiler;
import com.prolog.jvm.exceptions.InternalCompilerException;
import com.prolog.jvm.exceptions.RecognitionException;
import com.prolog.jvm.zip.PrologBytecodeImpl.MementoImpl;

/**
 * Class implementing the Read-Eval-Print Loop.
 *
 * @author Arno Bastenhof
 *
 */
public enum Repl {

	/**
	 * The unique instance for this class.
	 */
	INSTANCE;

	private static final String HALT = "halt";

	/**
	 * Executes the Read-Eval-Print Loop.
	 *
	 * @param in source for reading in queries
	 * @param out target for writing answers to queries
	 * @throws NullPointerException if {@code in == null} or {@code out ==
	 * null}
	 * @throws IOException
	 */
	public void run(Reader in, Writer out) throws IOException {
		// check preconditions
		checkNotNull(in);
		checkNotNull(out);

		// the code address where compiled queries will be stored
		final int queryAddr = Factory.getBytecode().getCodeSize();

		// bytecode state prior to the compilation of any queries
		final MementoImpl m = Factory.getBytecode().createMemento();

		final Compiler compiler = Factory.newQueryCompiler();

		try (BufferedReader reader = new BufferedReader(in)) {
			String userInput = reader.readLine();
			while (!userInput.equals(HALT) && userInput != null) {
				try (StringReader sr = new StringReader(userInput)) {
					compiler.compile(sr);
				}
				catch (RecognitionException e) {
					out.write(e.getMessage());
					out.write('\n');
					continue;
				}
				catch (Exception e) {
					throw new InternalCompilerException(e);
				}
				Factory.getInterpreter().execute(queryAddr, out);
				Factory.getBytecode().setMemento(m);
				userInput = reader.readLine();
			}
		}
	}
}
