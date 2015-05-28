package com.prolog.jvm.compiler.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;

import com.prolog.jvm.compiler.ast.Ast;
import com.prolog.jvm.compiler.visitor.PrologVisitor;
import com.prolog.jvm.exceptions.RecognitionException;

/**
 * A parser for a subset of Prolog, based on Covington (1993), ISO Prolog:
 * A Summary of the Draft Proposed Standard.
 * <p>
 * As advocated by Parr (2010) in Language Design Patterns, the semantic
 * actions to be executed during parsing (e.g., for constructing an {@link Ast}
 * are decoupled from the parsing logic itself. In contrast with Parr, however,
 * we achieve this effect not through subclassing, but rather through
 * composition with a {@link PrologVisitor}.
 *
 * @author Arno Bastenhof
 *
 */
public final class PrologParser extends AbstractParser {

	private final PrologVisitor<Token> visitor;

	/**
	 * Static factory method for obtaining a new {@link Parser} instance
	 * based on the specified {@code input}.
	 *
	 * @param input a {@link Reader} for the input; not allowed to be null
	 * @param visitor a visitor containing the semantic actions to be invoked
	 * during parsing; not allowed to be null
	 * @throws NullPointerException if {@code input == null || visitor == null}
	 * @throws IOException
	 */
	public static final PrologParser newInstance(Reader input,
			PrologVisitor<Token> visitor) throws IOException {
		PrologLexer lexer = new PrologLexer(checkNotNull(input));
		return new PrologParser(lexer, checkNotNull(visitor));
	}

	// Instantiation through static factory methods
	private PrologParser(PrologLexer input, PrologVisitor<Token> visitor) {
		super(input);
		this.visitor = visitor;
	}

	// === Start symbols ===

	/**
	 * Parses a Prolog program consisting of a sequence of one or more program
	 * clauses, being either facts (e.g., {@code father(zeus,ares).}) or rules
	 * ({@code grandparent(X,Y) :- parent(X,Z), parent(Z,Y).}).
	 *
	 * @throws IOException
	 */
	// program = {clause}- ;
	public void parseProgram() throws IOException, RecognitionException {
		consume(); // Read the first token.
		do {
			clause();
		}
		while (getLookaheadType() == TokenType.ATOM);
		match(TokenType.EOF);
	}

	/**
	 * Parses a query consisting of a non-empty sequence of goals, the latter in
	 * turn being terms built from a functor (possibly of arity 0) and an optional
	 * list of term arguments.
	 *
	 * @throws IOException
	 */
	// query = goals, "." ;
	public void parseQuery() throws IOException, RecognitionException {
		consume(); // Read the first token.
		queryHead();
		goals();
		match(TokenType.PERIOD);
		match(TokenType.EOF);
	}

	// 'Parses' an imaginary token to stand in for the clause head
	private void queryHead() {
		Token functor = Tokens.getAtom("~"); // ~ not recognized by lexer
		this.visitor.visitConstant(functor);
	}

	// === Remaining nonterminals (private) ===

	// clause = structure, [":-", goals ], "." ;
	private void clause() throws IOException, RecognitionException {
		this.visitor.preVisitClause(Tokens.IMPLIES); // TODO Use imaginary token type?
		structure(); // match clause head
		this.visitor.inVisitClause(Tokens.IMPLIES);
		if (getLookaheadType() == TokenType.IMPLIES) {
			consume();
			goals();
		}
		match(TokenType.PERIOD);
		this.visitor.postVisitClause(Tokens.IMPLIES);
	}

	// goals = structure, {",", structure} ;
	private void goals() throws IOException, RecognitionException {
		structure(); // match first goal
		while (getLookaheadType() == TokenType.COMMA) {
			consume();
			Token functor = structure(); // match subsequent goals
			this.visitor.postVisitGoal(functor);
		}
	}

	// term = "[]" | variable | structure ;
	private void term() throws IOException, RecognitionException {
		switch (getLookaheadType()) {
		case VAR:
			this.visitor.visitVariable(getLookahead());
			consume();
			break;
		case ATOM:
			structure();
			break;
		case NIL:
			this.visitor.visitConstant(getLookahead());
			consume();
			break;
		default:
			throw RecognitionException.newInstance(
					getLookahead(),
					getLine(),
					new String[]{TokenType.VAR.toString(),
						TokenType.ATOM.toString(),
						TokenType.NIL.toString()});
		}
	}

	// structure = atom, ["(", term, {",", term}, ")"] ;
	private Token structure() throws IOException, RecognitionException {
		Token functor = getLookahead();
		match(TokenType.ATOM);
		if (getLookaheadType() == TokenType.LBRACK) {
			consume();
			this.visitor.preVisitCompound(functor);
			term();
			while (getLookaheadType() == TokenType.COMMA) {
				consume();
				term();
			}
			match(TokenType.RBRACK);
			this.visitor.postVisitCompound(functor);
		}
		else {
			this.visitor.visitConstant(functor);
		}
		return functor;
	}

	// === Package-private diagnostic methods, for testing purposes ===

	boolean isDone() {
		return getLookaheadType() == TokenType.EOF;
	}
}
