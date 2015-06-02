package com.prolog.jvm.compiler;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import com.prolog.jvm.compiler.ast.Ast;
import com.prolog.jvm.compiler.parser.PrologParser;
import com.prolog.jvm.compiler.visitor.BytecodeGenerator;
import com.prolog.jvm.compiler.visitor.PrologVisitor;
import com.prolog.jvm.compiler.visitor.SourcePass;
import com.prolog.jvm.compiler.visitor.SymbolResolver;
import com.prolog.jvm.exceptions.InternalCompilerException;
import com.prolog.jvm.exceptions.RecognitionException;
import com.prolog.jvm.main.Factory;
import com.prolog.jvm.symbol.PredicateSymbol;
import com.prolog.jvm.symbol.Scope;
import com.prolog.jvm.symbol.Symbol;
import com.prolog.jvm.symbol.SymbolKey;
import com.prolog.jvm.zip.api.PrologBytecode;

/**
 * Abstract implementation of a Prolog compiler based on the Template method
 * design pattern, allowing for implementations targeting either programs
 * or queries. It is recommended that client code obtains instances through
 * one of the static factory methods on {@link Factory}.
 *
 * @author Arno Bastenhof
 */
public abstract class AbstractCompiler {

	private final PrologBytecode<?> code;
	private final Scope scope;

	/**
	 * The {@link Ast} that is built while executing {@link #compile(Reader)},
	 * made available for subclasses for the purpose of adding extra compiler
	 * passes (i.e., by overriding said method and first calling its super
	 * implementation).
	 */
	protected Ast root;

	/**
	 * The correspondence between {@link Ast}'s and the symbols to which they
	 * have been resolved while executing {@link #compile(Reader)}, made
	 * available for subclasses for the purpose of adding extra compiler passes
	 * (i.e., by overriding said method and first calling its super
	 * implementation).
	 */
	protected Map<Ast,Symbol> symbols;

	/**
	 *
	 * @param code the target for writing the generated bytecode to; not
	 * allowed to be null
	 * @param scope the ground scope to use when resolving symbols; not allowed
	 * to be null
	 * @throws NullPointerException if {@code code == null || scope == null}
	 *
	 */
	protected AbstractCompiler(PrologBytecode<?> code, Scope scope) {
		this.code = checkNotNull(code);
		this.scope = checkNotNull(scope);
	}

	/**
	 * Compiles the specified {@code source} by building an {@link Ast},
	 * resolving {@link Symbol}s and generating bytecode. Intermediate results
	 * are made available through {@link #root} and {@link #symbols}, allowing
	 * subclasses to add additional passes by overriding this method and first
	 * calling the current (super) implementation.
	 *
	 * @param source a reader for a program or -query; not allowed to be null
	 * @throws IOException
	 * @throws NullPointerException if {@code source == null}
	 * @throws RecognitionException if a lexer- or parsing error occurred
	 */
	public void compile(Reader source)
			throws IOException, RecognitionException {
		this.root = constructAst(checkNotNull(source));
		this.symbols = resolveSymbols();
		generateBytecode();
	}

	// First compiler pass.
	private Ast constructAst(Reader source)
			throws IOException, RecognitionException {
		SourcePass visitor = createSourcePassVisitor();
		PrologParser parser = PrologParser.newInstance(source, visitor);
		parseSource(parser);
		return visitor.getAst();
	}

	// Second compiler pass.
	private Map<Ast, Symbol> resolveSymbols() {
		SymbolResolver visitor = new SymbolResolver(this.scope);
		walkAst(this.root, visitor);
		verifySymbols();
		return visitor.getSymbols();
	}

	// Third compiler pass.
	private void generateBytecode() {
		BytecodeGenerator visitor = new BytecodeGenerator(
				this.symbols,this.code);
		walkAst(this.root, visitor);
	}

	// Checks for each declared predicate if it has any clauses.
	private void verifySymbols() {
		for (SymbolKey<?> key : this.scope.getKeys()) {
			if (key.getSymbolClass().equals(PredicateSymbol.class)) {
				PredicateSymbol symbol =
						(PredicateSymbol) this.scope.resolveLocal(key);
				if (symbol.getFirst() == null) {
					throw new InternalCompilerException(
							"No clauses defined for predicate "
									+ key.toString());
				}
			}
		}
	}

	/**
	 * Template method for returning a visitor for the initial source pass.
	 */
	protected abstract SourcePass createSourcePassVisitor();

	/**
	 * Template method for initiating a pass over the source code for a Prolog
	 * program or -query.
	 *
	 * @param parser the parser to be invoked
	 * @throws IOException
	 * @throws RecognitionException if a lexer- or parsing error occurred
	 */
	protected abstract void parseSource(PrologParser parser)
			throws IOException, RecognitionException;

	/**
	 * Template method for initiating a pass over the specified {@code root}
	 * node of an abstract syntax tree for a Prolog program or -query.
	 *
	 * @param root the {@link Ast} to be walked
	 * @param visitor the visitor containing the actions to be applied to the
	 * discovered nodes
	 */
	protected abstract void walkAst(Ast root, PrologVisitor<Ast> visitor);

}
