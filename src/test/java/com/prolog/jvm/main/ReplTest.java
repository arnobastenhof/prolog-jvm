package com.prolog.jvm.main;

import static com.prolog.jvm.zip.util.ReplConstants.FAILURE;
import static com.prolog.jvm.zip.util.ReplConstants.HALT;
import static com.prolog.jvm.zip.util.ReplConstants.NEXT_ANSWER;
import static com.prolog.jvm.zip.util.ReplConstants.SUCCESS;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import com.prolog.jvm.exceptions.RecognitionException;

/**
 * Integration tests.
 *
 * @author Arno Bastenhof
 *
 */
public final class ReplTest {

	// Class-path resources
	private static final String EXAMPLE_1 = "ancestry.pl";
	private static final String EXAMPLE_2 = "lists.pl";

	@Test
	public void ancestry() throws IOException {
		StringBuilder queries = new StringBuilder().
				append("grandparent(hera, harmonia).\n").
				append("grandparent(dionisius, zeus).\n").
				append("parent(zeus,X), parent(X,harmonia).\n\n").
				append("mother(X,dionisius).\n").
				append(NEXT_ANSWER).append('\n').
				append("father(zeus,Y).\n").
				append(NEXT_ANSWER).append('\n').
				append(NEXT_ANSWER).append('\n').
				append("ancestor(zeus,harmonia).\n").
				append("fathers(zeus,Y).\n").
				append(HALT).append('\n');

		StringBuilder answers = new StringBuilder().
				append(SUCCESS).
				append(FAILURE).
				append("X = ares\n").
				append("X = semele\n").
				append(FAILURE).
				append("Y = ares\n").
				append("Y = dionisius\n").
				append(FAILURE).
				append(SUCCESS).
				append("No clauses defined for predicate fathers/2\n");

		assertEquals(runQueries(EXAMPLE_1, queries.toString()),
				answers.toString());
	}

	@Test
	public void lists() throws IOException {
		StringBuilder queries = new StringBuilder().
				append("append(cons(a,[]),cons(b,[]),cons(a,cons(b,[]))).\n").
				append("reverse(cons(a,cons(b,[])),cons(b,cons(a,[]))).\n").
				append("reverse(cons(a,[]),X).\n").
				append(NEXT_ANSWER).append('\n').
				append("reverse(cons(a,cons(b,[])),X).\n").
				append(NEXT_ANSWER).append('\n').
				append("reverse(X,Y.\n").
				append("halt\n");

		StringBuilder answers = new StringBuilder().
				append(SUCCESS).
				append(SUCCESS).
				append("X = cons(a, [])\n").
				append(FAILURE).
				append("X = cons(b, cons(a, []))\n").
				append(FAILURE).
				append("<.;PERIOD> unexpected at line 1. Expected RBRACK.\n");

		assertEquals(runQueries(EXAMPLE_2, queries.toString()),
				answers.toString());
	}

	private String runQueries(String resource, String queries)
			throws IOException {
		String result = null;
		try (InputStream is = this.getClass().getResourceAsStream(resource);
				Reader file = new InputStreamReader(is);
				Reader reader = new StringReader(queries);
				StringWriter writer = new StringWriter()) {
			Factory.newProgramCompiler().compile(file);
			Repl.INSTANCE.run(reader, writer);
			result = writer.toString();
		}
		catch (RecognitionException e) {
			throw new AssertionError();
		}
		return result;
	}
}
