package com.prolog.jvm.common.parser;

import java.io.IOException;

/**
 * The type for lexical analyzers, used for converting input text into a sequence
 * of {@link Token}s.
 *
 * @author Arno Bastenhof
 *
 * @param <T> The token type.
 */
public interface Lexer<T> {

	/**
	 * Invoked by a parser to request a new token from the input.
	 *
	 * @return The next token read from the input.
	 * @throws IOException
	 */
	Token<T> nextToken() throws IOException;

	/**
	 * Returns the line number currently being processed.
	 */
	int getLine();

}
