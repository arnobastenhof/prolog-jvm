package com.prolog.jvm.compiler.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;

import com.prolog.jvm.exceptions.RecognitionException;

/**
 * Skeletal implementation for a lexical analyzer based on Pattern 2, LL(1)
 * Recursive-Descent Lexer from Parr (2010), Language Implementation Patterns.
 *
 * @author Arno Bastenhof
 */
public abstract class AbstractLexer implements Lexer {

	// Private state
	private final Reader input; // reader for the input text
	private char lookahead = ' '; // lookahead character, initialized with whitespace
	private int line = 1; // current line number

	/**
	 * Constructs a {@link Lexer} instance reading from a {@link Reader}.
	 *
	 * @param input a reader for a Prolog source program or query; not
	 * allowed to be null
	 * @throws NullPointerException if {@code input == null}
	 */
	protected AbstractLexer(final Reader input) {
		this.input = checkNotNull(input);
	}

	@Override
	public final int getLine() {
		return this.line;
	}

	/**
	 * Returns the lookahead character.
	 */
	protected final char getLookahead() {
		return this.lookahead;
	}

	// === Character consumption ===

	/**
	 * Consumes a single character and increments the line number if needed.
	 *
	 * @throws IOException
	 */
	protected final void consume() throws IOException {
		if (this.lookahead == '\n') {
			this.line++;
		}
		this.lookahead = (char)this.input.read();
	}

	/**
	 * Convenience method serving as an alternative to {@link #consume()} in case
	 * it is known that the lookahead character is not a linefeed.
	 *
	 * @throws IOException
	 */
	protected final void consumeNonLinefeed() throws IOException {
		this.lookahead = (char)this.input.read();
	}

	/**
	 * Compares the lookahead character against the supplied parameter, throwing
	 * an exception in case of a mismatch.
	 *
	 * @param expected the expected character
	 * @throws IOException
	 * @throws RecognitionException if {@code expected} does not match {@link
	 * #getLookahead()}
	 */
	protected final void match(final char expected)
			throws IOException, RecognitionException {
		if (this.lookahead == expected) {
			consumeNonLinefeed();
		}
		else {
			throw RecognitionException.newInstance(
					this.lookahead,
					this.line,
					new String[]{String.valueOf(expected)});
		}
	}
}
