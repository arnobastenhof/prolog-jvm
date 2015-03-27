package com.prolog.jvm.compiler.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for the parser.
 *
 * @author Arno Bastenhof
 *
 */
public final class ParserTest {

	private static final String program = "gods.pl"; // Class-path resource

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void program() throws IOException {
		try (InputStream is = this.getClass().getResourceAsStream(program)) {
			PrologParser parser = PrologParser.newInstance(is);
			parser.program();
			assertEquals(parser.isDone(), true);
		}
	}

	@Test
	public final void query() throws IOException {
		PrologParser parser = PrologParser.newInstance("ancestor(zeus, X).");
		parser.query();
		assertEquals(parser.isDone(), true);
	}

	// TODO Add more tests

}
