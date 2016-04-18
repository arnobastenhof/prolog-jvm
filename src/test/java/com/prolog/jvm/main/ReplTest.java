package com.prolog.jvm.main;

import static com.prolog.jvm.zip.util.ReplConstants.FAILURE;
import static com.prolog.jvm.zip.util.ReplConstants.HALT;
import static com.prolog.jvm.zip.util.ReplConstants.NEXT_ANSWER;
import static com.prolog.jvm.zip.util.ReplConstants.PROMPT;
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
		ZipAssert.forFile(EXAMPLE_1)
			.prompt("grandparent(hera, harmonia).")
			.yes()
			.prompt("grandparent(dionisius, zeus).")
			.no()
			.prompt("parent(zeus,X), parent(X,harmonia).")
			.binding("X", "ares")
			.done()
			.yes()
			.prompt("mother(X,dionisius).")
			.binding("X", "semele")
			.or()
			.no()
			.prompt("father(zeus,Y).")
			.binding("Y", "ares")
			.or()
			.binding("Y", "dionisius")
			.or()
			.no()
			.prompt("ancestor(zeus,harmonia).")
			.yes()
			.prompt("fathers(zeus,Y).")
			.error("No clauses defined for predicate fathers/2")
			.halt();
	}

	@Test
	public void lists() throws IOException {
		ZipAssert.forFile(EXAMPLE_2)
			.prompt("append(cons(a,[]),cons(b,[]),cons(a,cons(b,[]))).")
			.yes()
			.prompt("reverse(cons(a,cons(b,[])),cons(b,cons(a,[]))).")
			.yes()
			.prompt("reverse(cons(a,[]),X).")
			.binding("X", "cons(a, [])")
			.or()
			.no()
			.prompt("reverse(cons(a,cons(b,[])),X).")
			.binding("X", "cons(b, cons(a, []))")
			.or()
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

		private ZipAssert prompt(final String query) {
			this.out.append(PROMPT);
			this.in.append(query).append('\n');
			return this;
		}

		private ZipAssert binding(final String var, final String val) {
			this.out.append(var).append(" = ").append(val).append(' ');
			return this;
		}

		private ZipAssert yes() {
			this.out.append(SUCCESS);
			return this;
		}

		private ZipAssert no() {
			this.out.append(FAILURE);
			return this;
		}

		private ZipAssert or() {
			this.in.append(NEXT_ANSWER).append('\n');
			return this;
		}

		private ZipAssert done() {
			this.in.append('\n');
			return this;
		}

		private ZipAssert error(final String msg) {
			this.out.append(msg).append('\n');
			return this;
		}

		private void halt() throws IOException {
			this.out.append(PROMPT);
			this.in.append(HALT).append('\n');
			assertEquals(this.out.toString(),
					runQueries(this.fileName, this.in.toString()));
		}

		private String runQueries(final String resource, final String queries)
				throws IOException {
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
