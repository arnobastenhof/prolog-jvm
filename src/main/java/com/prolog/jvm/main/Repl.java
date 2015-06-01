package com.prolog.jvm.main;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.prolog.jvm.zip.util.ReplConstants.HALT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import com.prolog.jvm.compiler.AbstractCompiler;
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

		final AbstractCompiler compiler = Factory.newQueryCompiler();

		try (BufferedReader reader = new BufferedReader(in)) {
			String userInput = null;
			while (true) {
				userInput = reader.readLine();
				if (userInput != null && !userInput.equals(HALT)) {
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
					Factory.getInterpreter().execute(queryAddr, reader, out);
					Factory.getBytecode().setMemento(m);
					continue;
				}
				return;
			}
		}
	}
}
