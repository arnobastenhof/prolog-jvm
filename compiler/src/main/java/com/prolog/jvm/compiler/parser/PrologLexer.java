package com.prolog.jvm.compiler.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.prolog.jvm.common.parser.AbstractLexer;
import com.prolog.jvm.common.parser.LexerUtils;
import com.prolog.jvm.common.parser.Token;

/**
 * A tokenizer for a subset of Prolog, based on Covington (1993), ISO Prolog:
 * A Summary of the Draft Proposed Standard.
 *
 * The following simplifications were made compared to the proposed ISO standard:
 * <ul>
 * <li> Graphic tokens are not allowed to begin with <code>.</code>, <code>/</code>
 * or <code>:</code>. This ensures we can make do with a single lookahead character.
 * To compare, the proposed ISO standard only prohibited graphic tokens from
 * beginning with <code>/*</code>.
 * <li> No support for <code>{}</code> as an atom.
 * <li> No support for arbitrary characters inside single quotes as an atom.
 * <li> No support for numbers or character strings.
 * <li> No reserved identifiers.
 * <li> Whitespace is always ignored. In particular, we do not prohibit it from
 * occurring between a functor and its opening bracket, nor do we demand a period
 * be followed by it.
 * </ul>
 *
 * @author Arno Bastenhof
 *
 */
public final class PrologLexer extends AbstractLexer<PlTokenType> {

	// Package-private constructor used by static factory methods for the Parser
	PrologLexer(Reader input) {
		super(input);
	}

	/**
	 * Constructs a <code>Lexer</code> instance reading from a
	 * <code>String</code> and initializes the lookahead character.
	 * @throws IOException
	 */
	public PrologLexer(String input) {
		super(new StringReader(input));
	}

	/**
	 * Constructs a <code>Lexer</code> instance reading from an
	 * {@link InputStream} and initializes the lookahead character.
	 * @throws IOException
	 */
	public PrologLexer(InputStream input) {
		super(new InputStreamReader(input));
	}

	/**
	 * Invoked by the parser to request a new token from the input.
	 *
	 * @return The next token read from the input.
	 * @throws IOException
	 */
	@Override
	public Token<PlTokenType> nextToken() throws IOException {
		while (getLookahead() != LexerUtils.EOF) {
			if (isWhitespace()) {
				ws();
				continue;
			}
			if (isCapitalLetter() || isSmallLetter() || getLookahead() == '_') {
				return id();
			}
			switch (getLookahead()) {
			case '!'  :
				consumeNonLinefeed();
				return PrologTokens.CUT;
			case '%'  :
				single(); // single-line comment
				continue;
			case '#' :
			case '$' :
			case '&' :
			case '*' :
			case '+' :
			case '-' :
			case '<' :
			case '=' :
			case '>' :
			case '?' :
			case '@' :
			case '^' :
			case '~' :
			case '\\' :
				return graphic();
			case '('  :
				consumeNonLinefeed();
				return PrologTokens.LBRACK;
			case ')'  :
				consumeNonLinefeed();
				return PrologTokens.RBRACK;
			case ','  :
				consumeNonLinefeed();
				return PrologTokens.COMMA;
			case '.'  :
				consumeNonLinefeed();
				return PrologTokens.PERIOD;
			case '/'  :
				multi();
				continue;
			case ':'  :
				return implies();
			case '['  :
				return nil(); // Empty list
			default:
				throw new RuntimeException(getErrorMsg());
			}
		}
		return PrologTokens.EOF;
	}

	// === Lexer rules (Package private for testing purposes) ===
	// Comments reference the ISO standard for Extended Backus-Naur Form.

	// ws = {"\t" | "\n" | "\r" | " "}- ; (* whitespace *)
	void ws() throws IOException {
		do {
			consume();
		} while (isWhitespace());
	}

	// Inline comments.
	void single() throws IOException {
		consumeNonLinefeed(); // Consume '%'
		while (getLookahead() != '\n') {
			if (getLookahead() == -1) {
				return;
			}
			consumeNonLinefeed();
		}
		consume();
	}

	// Multiline comments.
	void multi() throws IOException {
		consumeNonLinefeed(); // Consume '/'
		match('*');
		while (true) {
			if (getLookahead() == LexerUtils.EOF) {
				throw new RuntimeException(getErrorMsg('*'));
			}
			if (getLookahead() == '*') {
				do {
					consumeNonLinefeed();
					if (getLookahead() == '/') {
						consumeNonLinefeed();
						return;
					}
				} while (getLookahead() == '*');
				continue;
			}
			consume();
		}
	}

	Token<PlTokenType> implies() throws IOException {
		consumeNonLinefeed(); // Consume ':'
		match('-');
		return PrologTokens.IMPLIES;
	}

	/*
	 * id = {"_" | small | capital | digit}- ; (* identifiers *)
	 *
	 * Note: tokenType must be ATOM if the first character is a small letter,
	 * while a VAR if the first character is a capital letter or underscore.
	 * In particular, identifiers are not allowed to begin with a digit.
	 */
	Token<PlTokenType> id() throws IOException {
		boolean smallLetter = isSmallLetter();
		StringBuilder buffer = new StringBuilder();
		do {
			buffer.append(getLookahead());
			consumeNonLinefeed();
		} while (isDigit()
				|| isCapitalLetter()
				|| isSmallLetter()
				|| getLookahead() == '_'
				);
		return smallLetter ? PrologTokens.getAtom(buffer.toString())
				: PrologTokens.getVar(buffer.toString());
	}

	// Graphic tokens
	Token<PlTokenType> graphic() throws IOException {
		StringBuilder buffer = new StringBuilder();
		do {
			buffer.append(getLookahead());
			consumeNonLinefeed();
		} while (isGraphic());
		return PrologTokens.getAtom(buffer.toString());
	}

	// nil = "[]" ;
	Token<PlTokenType> nil() throws IOException {
		consumeNonLinefeed(); // Consumes '['
		match(']');
		return PrologTokens.NIL;
	}

	private boolean isDigit() {
		return LexerUtils.isDigit(getLookahead());
	}

	private boolean isCapitalLetter() {
		return LexerUtils.isCapitalLetter(getLookahead());
	}

	private boolean isSmallLetter() {
		return LexerUtils.isSmallLetter(getLookahead());
	}

	private boolean isWhitespace() {
		return LexerUtils.isWhitespace(getLookahead());
	}

	private boolean isGraphic() {
		return LexerUtils.isGraphic(getLookahead());
	}
}