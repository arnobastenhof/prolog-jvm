package com.prolog.jvm.compiler.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import com.prolog.jvm.common.parser.AbstractParser;

/**
 * A parser for a subset of Prolog, based on Covington (1993), ISO Prolog:
 * A Summary of the Draft Proposed Standard.
 *
 * TODO: Deviations from proposed ISO standard
 *
 * @author Arno Bastenhof
 *
 */
public final class PrologParser extends AbstractParser<PlTokenType> {

	/**
	 * Static factory method for obtaining a new <code>Parser</code> instance
	 * based on an input String.
	 *
	 * @throws IOException
	 */
	public static final PrologParser newInstance(String input) throws IOException {
		return newInstance(new StringReader(input));
	}

	/**
	 * Static factory method for obtaining a new <code>Parser</code> instance
	 * based on an <code>InputStream</code>.
	 *
	 * @throws IOException
	 */
	public static final PrologParser newInstance(InputStream input) throws IOException {
		return newInstance(new InputStreamReader(input));
	}

	private static final PrologParser newInstance(Reader input) throws IOException {
		PrologLexer lexer = new PrologLexer(input);
		PrologParser parser = new PrologParser(lexer);
		return parser;
	}

	/**
	 *
	 * @param input A lexical analyzer for Prolog.
	 */
	public PrologParser(PrologLexer input) {
		super(input);
	}

	// === Start symbols ===

	/**
	 * Parses a Prolog program consisting of a sequence of one or more program
	 * clauses, being either facts (e.g., <code>father(zeus,ares).</code>) or rules
	 * (<code>grandparent(X,Y) :- parent(X,Z), parent(Z,Y).</code>).
	 *
	 * @throws IOException
	 */
	// program = {clause}- ;
	public void program() throws IOException {
		consume(); // Read the first token.
		do {
			clause();
		}
		while (getLookaheadType() == PlTokenType.ATOM);
		match(PlTokenType.EOF);
	}

	/**
	 * Parses a query consisting of a non-empty sequence of goals, the latter in
	 * turn being terms built from a functor (possibly of arity 0) and an optional
	 * list of term arguments.
	 *
	 * @throws IOException
	 */
	// query = structure, {",", structure}, "." ;
	public void query() throws IOException {
		consume(); // Read the first token.
		structure();
		while (getLookaheadType() == PlTokenType.COMMA) {
			consume();
			structure();
		}
		match(PlTokenType.PERIOD);
		match(PlTokenType.EOF);
	}

	// === Remaining nonterminals (private) ===

	// clause = structure, [":-", structure, {",", structure} ], "."
	private void clause() throws IOException {
		structure();
		if (getLookaheadType() == PlTokenType.IMPLIES) {
			consume();
			structure();
			while (getLookaheadType() == PlTokenType.COMMA) {
				consume();
				structure();
			}
		}
		match(PlTokenType.PERIOD);
	}

	// term = "[]" | variable | structure ;
	private void term() throws IOException {
		switch (getLookaheadType()) {
		case VAR:
			consume();
			break;
		case ATOM:
			structure();
			break;
		case NIL:
			consume();
			break;
		default:
			throw new RuntimeException(getErrorMsg(Arrays.asList(
					PlTokenType.VAR,
					PlTokenType.ATOM,
					PlTokenType.NIL)));
		}
	}

	// structure = atom, ["(", term, {",", term}, ")"] ;
	private void structure() throws IOException {
		match(PlTokenType.ATOM);
		if (getLookaheadType() == PlTokenType.LBRACK) {
			consume();
			term();
			while (getLookaheadType() == PlTokenType.COMMA) {
				consume();
				term();
			}
			match(PlTokenType.RBRACK);
		}
	}

	// === Package-private diagnostic methods, for testing purposes ===

	boolean isDone() {
		return getLookaheadType() == PlTokenType.EOF;
	}
}
