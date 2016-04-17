package com.prolog.jvm.main;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import com.prolog.jvm.exceptions.InternalCompilerException;
import com.prolog.jvm.exceptions.RecognitionException;

/**
 * Main (executable) class for Prolog-JVM, kicking off the {@link Repl}.
 *
 * @author Arno Bastenhof
 *
 */
public final class PrologJvm {

	private static final String HELP = "Usage: java PrologJvm <file name>.";

	/**
	 * Main method.
	 *
	 * @param args command-line parameter for the program to be loaded
	 */
	public static final void main(String[] args) {
		if (args.length == 0) {
			System.out.println(HELP); // print help message
			return;
		}
		try (final Reader program = new FileReader(args[0])) {
			Factory.newProgramCompiler().compile(program);
		}
		catch (IOException | RecognitionException e) {
			e.printStackTrace();
			return;
		}
		catch (Exception e) {
			throw new InternalCompilerException(e);
		}
		try (final Reader reader = new InputStreamReader(System.in);
				Writer writer = new PrintWriter(System.out)) {
			Repl.INSTANCE.run(reader, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
