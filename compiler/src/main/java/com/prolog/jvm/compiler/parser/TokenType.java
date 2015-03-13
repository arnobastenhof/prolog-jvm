package com.prolog.jvm.compiler.parser;

/**
 * Utility class defining token types.
 *
 * @author Arno Bastenhof
 *
 */
public final class TokenType {

	/**
	 * The token type for the end of file character.
	 */
	public static final int EOF = 0;

	/**
	 * The token type for variables. A variable starts with an underscore
	 * or capital letter, which can be followed by any number of letters
	 * (whether lower case or capital), digits and underscores.
	 */
	public static final int VAR = 1;

	/**
	 * The token type for an atom, otherwise referred to by a constant. An
	 * atom starts with a small letter, which can be followed by any number
	 * of letters (whether lower case or capital), digits and underscores.
	 * Alternatively, limited support is offered for 'graphic tokens'. See
	 * the Javadoc for the {@link Lexer} for more details.
	 */
	public static final int ATOM = 2;

	/**
	 * The token type for the Cut.
	 */
	public static final int CUT = 3;

	/**
	 * The token type for the empty list.
	 */
	public static final int NIL = 4;

	/**
	 * The token type for the implication sign ':-' used in rules.
	 */
	public static final int IMPLIES = 5;

	/**
	 * The token type for the comma, used for combining goal literals
	 * into a clause body.
	 */
	public static final int COMMA = 6;

	/**
	 * The token type for a period, used as a terminating delimited for
	 * all clauses.
	 */
	public static final int PERIOD = 7;

	/**
	 * The token type for the left bracket '(', indicating the start of
	 * a functor's argument list.
	 */
	public static final int LBRACK = 8;

	/**
	 * The token type for the right bracket ')', indicating the end of
	 * a functor's argument list.
	 */
	public static final int RBRACK = 9;

	/**
	 * An array of token names, s.t. for each token type T defined by this
	 * class, tokenNames[T] yields the name of T.
	 */
	public static final String[] tokenNames = {
		"EOF", "VAR", "ATOM", "CUT", "NIL", "IMPLIES", "COMMA", "PERIOD",
		"LBRACK", "RBRACK"
	};

	// Private constructor to prevent instantiation.
	private TokenType() {}
}
