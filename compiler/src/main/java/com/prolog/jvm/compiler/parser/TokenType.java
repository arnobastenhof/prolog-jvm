package com.prolog.jvm.compiler.parser;

/**
 * An enumeration of the token types.
 *
 * @author Arno Bastenhof
 *
 */
public enum TokenType {

	/**
	 * The token type for the end of file character.
	 */
	EOF,

	/**
	 * The token type for variables. A variable starts with an underscore
	 * or capital letter, which can be followed by any number of letters
	 * (whether lower case or capital), digits and underscores.
	 */
	VAR,

	/**
	 * The token type for an atom, otherwise referred to by a constant. An
	 * atom starts with a small letter, which can be followed by any number
	 * of letters (whether lower case or capital), digits and underscores.
	 * Alternatively, limited support is offered for 'graphic tokens'. See
	 * the Javadoc for the {@link Lexer} for more details.
	 */
	ATOM,

	/**
	 * The token type for the cut <code>!</code>.
	 */
	CUT,

	/**
	 * The token type for the empty list <code>[]</code>.
	 */
	NIL,

	/**
	 * The token type for the implication sign <code>:-</code> used in rules.
	 */
	IMPLIES,

	/**
	 * The token type for the comma, used for combining goal literals
	 * into a clause body.
	 */
	COMMA,

	/**
	 * The token type for a period, used as a terminating delimited for
	 * all clauses.
	 */
	PERIOD,

	/**
	 * The token type for the left bracket <code>(</code>, indicating the start of
	 * a functor's argument list.
	 */
	LBRACK,

	/**
	 * The token type for the right bracket <code>)</code>, indicating the end of
	 * a functor's argument list.
	 */
	RBRACK;

}
