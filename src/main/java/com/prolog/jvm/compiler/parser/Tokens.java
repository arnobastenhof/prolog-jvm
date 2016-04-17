package com.prolog.jvm.compiler.parser;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import com.prolog.jvm.compiler.ast.Ast;

/**
 * Utility class containing constants and static factory methods for Prolog
 * tokens.
 *
 * @author Arno Bastenhof
 *
 */
public final class Tokens {

	/**
	 * The {@link Token} indicating the end of the source file.
	 */
	public static final Token EOF = new PrologToken(TokenType.EOF, "<EOF>");

	/**
	 * The {@link Token} corresponding to occurrences of the empty list.
	 */
	public static final Token NIL = new PrologToken(TokenType.NIL, "[]");

	/**
	 * The {@link Token} corresponding to occurrences of the implication sign.
	 */
	public static final Token IMPLIES = new PrologToken(TokenType.IMPLIES, ":-");

	/**
	 * The {@link Token} corresponding to occurrences of a comma in the source
	 * program.
	 */
	public static final Token COMMA = new PrologToken(TokenType.COMMA, ",");

	/**
	 * The {@link Token} corresponding to occurrences of a period in the source
	 * program.
	 */
	public static final Token PERIOD = new PrologToken(TokenType.PERIOD, ".");

	/**
	 * The {@link Token} corresponding to occurrences of a left bracket in the
	 * source program.
	 */
	public static final Token LBRACK = new PrologToken(TokenType.LBRACK, "(");

	/**
	 * The {@link Token} corresponding to occurrences of a right bracket in the
	 * source program.
	 */
	public static final Token RBRACK = new PrologToken(TokenType.RBRACK, ")");

	/**
	 * The imaginary {@link Token} corresponding to the root of an {@link Ast}.
	 */
	public static final Token PROGRAM = new PrologToken(TokenType.PROGRAM, "");

	/**
	 * Static factory method for obtaining a {@link Token} of type
	 * {@link TokenType#ATOM}.
	 *
	 * @param text the matched input text; not allowed to be null
	 * @throws NullPointerException if {@code text == null}
	 */
	public static final Token getAtom(final String text) {
		return new PrologToken(TokenType.ATOM, requireNonNull(text));
	}

	/**
	 * Static factory method for obtaining a {@link Token} of type
	 * {@link TokenType#VAR}.
	 *
	 * @param text the matched input text; not allowed to be null
	 * @throws NullPointerException if {@code text == null}
	 */
	public static final Token getVar(final String text) {
		return new PrologToken(TokenType.VAR, requireNonNull(text));
	}

	// Private constructor to prevent instantiation
	private Tokens() {
		throw new AssertionError();
	}

	// Private implementation of the type Token
	private static final class PrologToken implements Token {

		private final TokenType type;
		private final String text;

		private PrologToken(final TokenType type, final String text) {
			assert type != null;
			assert text != null;
			this.type = type;
			this.text = text;
		}

		@Override
		public TokenType getType() {
			return this.type;
		}

		@Override
		public String getText() {
			return this.text;
		}

		@Override
		public String toString() {
			return "<" + this.getText() + ";" + this.getType() + ">";
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.getType(), this.getText());
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof PrologToken)) {
				return false;
			}
			final PrologToken other = (PrologToken)obj;
			return this.getType() == other.getType() &&
					this.getText().equals(other.getText()); // text non-null
		}
	}
}
