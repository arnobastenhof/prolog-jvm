package com.prolog.jvm.main;

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
		StringBuilder buffer = new StringBuilder();
		buffer.append("grandparent(hera, harmonia).\n");
		buffer.append("ancestor(zeus, harmonia).\n");
		buffer.append("grandparent(dionisius, zeus).\n");
		buffer.append("parent(zeus,X), parent(X,harmonia).\n");
		buffer.append("halt\n");

		assertEquals(runQueries(EXAMPLE_1,buffer.toString()),
				"yes\nyes\nno\nX = ares\n");
	}

	@Test
	public void lists() throws IOException {
		StringBuilder buffer = new StringBuilder();
		buffer.append("append(cons(a,[]),cons(b,[]),cons(a,cons(b,[]))).\n");
		buffer.append("reverse(cons(a,cons(b,[])),cons(b,cons(a,[]))).\n");
		buffer.append("reverse(cons(a,cons(b,[])),X).\n");
		buffer.append("halt\n");

		assertEquals(runQueries(EXAMPLE_2,buffer.toString()),
				"yes\nyes\nX = cons(b, cons(a, []))\n");
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
