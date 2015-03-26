package com.prolog.jvm.compiler.parser;

import java.util.Objects;

import com.prolog.jvm.common.parser.AbstractToken;
import com.prolog.jvm.common.parser.Token;

/**
 * Utility class containing constants and static factory methods for Prolog
 * tokens.
 *
 * @author Arno Bastenhof
 *
 */
public final class PrologTokens {

	/**
	 * The <code>Token</code> indicating the end of the source file.
	 */
	public static final Token<PlTokenType> EOF = new PrologToken(PlTokenType.EOF, "<EOF>");

	/**
	 * The <code>Token</code> corresponding to occurrences of Cut.
	 */
	public static final Token<PlTokenType> CUT = new PrologToken(PlTokenType.CUT, "!");

	/**
	 * The <code>Token</code> corresponding to occurrences of the empty list.
	 */
	public static final Token<PlTokenType> NIL = new PrologToken(PlTokenType.NIL, "[]");

	/**
	 * The <code>Token</code> corresponding to occurrences of the implication sign.
	 */
	public static final Token<PlTokenType> IMPLIES = new PrologToken(PlTokenType.IMPLIES, ":-");

	/**
	 * The <code>Token</code> corresponding to occurrences of a comma in the
	 * source program.
	 */
	public static final Token<PlTokenType> COMMA = new PrologToken(PlTokenType.COMMA, ",");

	/**
	 * The <code>Token</code> corresponding to occurrences of a period in the
	 * source program.
	 */
	public static final Token<PlTokenType> PERIOD = new PrologToken(PlTokenType.PERIOD, ".");

	/**
	 * The <code>Token</code> corresponding to occurrences of a left bracket
	 * in the source program.
	 */
	public static final Token<PlTokenType> LBRACK = new PrologToken(PlTokenType.LBRACK, "(");

	/**
	 * The <code>Token</code> corresponding to occurrences of a right bracket
	 * in the source program.
	 */
	public static final Token<PlTokenType> RBRACK = new PrologToken(PlTokenType.RBRACK, ")");


	/**
	 * Static factory method for obtaining a <code>Token</code> of type
	 * <code>ATOM</code>.
	 *
	 * @param text The matched input text.
	 */
	public static final Token<PlTokenType> getAtom(String text) {
		return new PrologToken(PlTokenType.ATOM, text);
	}
	/**
	 * Static factory method for obtaining a <code>Token</code> of type
	 * <code>VAR</code>.
	 *
	 * @param text The matched input text.
	 */
	public static final Token<PlTokenType> getVar(String text) {
		return new PrologToken(PlTokenType.VAR, text);
	}

	// Private constructor to prevent instantiation
	private PrologTokens() {}

	// Private implementation of the type Token
	private static final class PrologToken extends AbstractToken<PlTokenType> {

		private PrologToken(PlTokenType type, String text) {
			super(type, text);
		}

		@Override
		public final String toString() {
			return "<" + this.getText() + ";" + this.getType() + ">";
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.getType(), this.getText());
		}

		@Override
		public final boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof PrologToken)) {
				return false;
			}
			PrologToken other = (PrologToken)obj;
			return this.getType() == other.getType() &&
					(this.getText() == null
					? other.getText() == null
					: this.getText().equals(other.getText()));
		}
	}
}
