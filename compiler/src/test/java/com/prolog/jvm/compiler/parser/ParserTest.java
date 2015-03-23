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
			Parser parser = Parser.newInstance(is);
			parser.program();
			assertEquals(parser.lookahead, Token.EOF);
		}
	}

	@Test
	public final void query() throws IOException {
		Parser parser = Parser.newInstance("ancestor(zeus, X).");
		parser.query();
		assertEquals(parser.lookahead, Token.EOF);
	}

	// TODO Add more tests

}
