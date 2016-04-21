package com.prolog.jvm.main;

import static com.prolog.jvm.zip.util.ReplConstants.FAILURE;
import static com.prolog.jvm.zip.util.ReplConstants.HALT;
import static com.prolog.jvm.zip.util.ReplConstants.NEXT_ANSWER;
import static com.prolog.jvm.zip.util.ReplConstants.PROMPT;
import static com.prolog.jvm.zip.util.ReplConstants.SUCCESS;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

import com.prolog.jvm.exceptions.RecognitionException;
import com.prolog.jvm.zip.StepLogger;
import com.prolog.jvm.zip.api.StepListener;

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
	public void ancestry() throws Exception {
		ZipAssert.forFile(EXAMPLE_1)
			.prompt("grandparent(hera, harmonia).")
			.yes()
			.prompt("grandparent(dionisius, zeus).")
			.no()
			.prompt("parent(zeus,X), parent(X,harmonia).")
			.binding("X", "ares")
			.enough()
			.yes()
			.prompt("mother(X,dionisius).")
			.binding("X", "semele")
			.more()
			.no()
			.prompt("father(zeus,Y).")
			.binding("Y", "ares")
			.more()
			.binding("Y", "dionisius")
			.more()
			.no()
			.prompt("ancestor(zeus,harmonia).")
			.yes()
			.prompt("fathers(zeus,Y).")
			.error("No clauses defined for predicate fathers/2")
			.halt();
	}

	@Test
	public void lists() throws Exception {
		ZipAssert.forFile(EXAMPLE_2)
			.prompt("append([],X,Y).")
			.binding("X", "X")
			.binding("Y", "X")
			.more()
			.no()
			.prompt("append(cons(a,[]),cons(b,[]),cons(a,cons(b,[]))).")
			.yes()
			.prompt("reverse(cons(a,cons(b,[])),cons(b,cons(a,[]))).")
			.yes()
			.prompt("reverse(cons(a,[]),X).")
			.binding("X", "cons(a, [])")
			.more()
			.no()
			.prompt("reverse(cons(a,cons(b,[])),X).")
			.binding("X", "cons(b, cons(a, []))")
			.more()
			.no()
			.prompt("reverse(X,Y.")
			.error("<.;PERIOD> unexpected at line 1. Expected RBRACK.")
			.halt();
	}

	private static class ZipAssert {

		private final StringBuilder in = new StringBuilder();
		private final StringBuilder out = new StringBuilder();
		private final String fileName;

		private ZipAssert(final String fileName) {
			assert fileName != null;
			this.fileName = fileName;
		}

		private static ZipAssert forFile(final String fileName) {
			return new ZipAssert(fileName);
		}

		// records a query
		private ZipAssert prompt(final String query) {
			this.out.append(PROMPT);
			this.in.append(query).append('\n');
			return this;
		}

		// records an alternative (i.e., a binding)
		private ZipAssert binding(final String var, final String val) {
			this.out.append(var).append(" = ").append(val).append(' ');
			return this;
		}

		// records an affirmative answer to a query
		private ZipAssert yes() {
			this.out.append(SUCCESS);
			return this;
		}

		// records a negative answer to a query
		private ZipAssert no() {
			this.out.append(FAILURE);
			return this;
		}

		// records a request for another alternative (;)
		private ZipAssert more() {
			this.in.append(NEXT_ANSWER).append('\n');
			return this;
		}

		// records no further alternatives need be sought for
		private ZipAssert enough() {
			this.in.append('\n');
			return this;
		}

		// records an error message
		private ZipAssert error(final String msg) {
			this.out.append(msg).append('\n');
			return this;
		}

		// records the end of the session and validates it
		private void halt() throws Exception {
			this.out.append(PROMPT);
			this.in.append(HALT).append('\n');
			assertEquals(this.out.toString(),
					runQueries(this.fileName, this.in.toString()));
		}

		private String runQueries(final String resource, final String queries)
				throws Exception {
			String result;
			try (final InputStream is = this.getClass().getResourceAsStream(resource);
					final Reader file = new InputStreamReader(is);
					final Reader reader = new StringReader(queries);
					final StringWriter writer = new StringWriter()) {
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

}
