package com.prolog.jvm.compiler.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import com.prolog.jvm.compiler.visitor.BasicPrologVisitor;
import com.prolog.jvm.compiler.visitor.PrologVisitor;
import com.prolog.jvm.exceptions.RecognitionException;

/**
 * Test class for the parser.
 *
 * @author Arno Bastenhof
 *
 */
public final class ParserTest {

	private static final String PROGRAM = "program.pl"; // Class-path resource
	private static final String QUERY = "ancestor(zeus, X).";
	private static final String WRONG_QUERY =
			"reverse(cons(a,cons(b,[])),cons(b,cons(a,[])).";

	private static final PrologVisitor<Token> VISITOR =
			new BasicPrologVisitor<>();

	@Test
	public final void program() throws IOException, RecognitionException {
		parseProgram(PROGRAM);
	}

	@Test
	public final void query() throws IOException, RecognitionException {
		parseQuery(QUERY);
	}

	@Test(expected = RecognitionException.class)
	public final void wrongQuery() throws IOException, RecognitionException {
		parseQuery(WRONG_QUERY);
	}

	// === Private implementation ===

	private final void parseProgram(String program)
			throws IOException, RecognitionException {
		try (InputStream is = this.getClass().getResourceAsStream(program);
				Reader reader = new InputStreamReader(is)) {
			PrologParser parser = PrologParser.newInstance(reader, VISITOR);
			parser.parseProgram();
			assertEquals(parser.isDone(), true);
		}
	}

	private final void parseQuery(String query)
			throws IOException, RecognitionException {
		try (Reader reader = new StringReader(query)) {
			PrologParser parser = PrologParser.newInstance(reader, VISITOR);
			parser.parseQuery();
			assertEquals(parser.isDone(), true);
		}
	}
}
