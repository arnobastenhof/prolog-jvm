package com.prolog.jvm.common.parser;

/**
 * The type for Tokens, used by {@link Lexer}s for categorizing the input
 * text.
 *
 * @author Arno Bastenhof
 *
 * @param <T> The token type.
 */
public interface Token<T> {

	/**
	 * Returns this token's type.
	 */
	T getType();

	/**
	 * Returns the matched input text.
	 */
	String getText();

	/**
	 * Returns the String representation of this token, consisting of the
	 * matched text together with the token type's name, separated by a
	 * semicolon and delimited by angular brackets. E.g., &lt;,;COMMA>,
	 * &lt;append;ATOM>, etc.
	 */
	@Override
	String toString();

}
