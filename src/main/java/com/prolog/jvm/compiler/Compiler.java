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
import com.prolog.jvm.exceptions.RecognitionException;
import com.prolog.jvm.symbol.Scope;
import com.prolog.jvm.symbol.Symbol;
import com.prolog.jvm.zip.api.PrologBytecode;

/**
 * Abstract implementation of a Prolog compiler based on the Template method
 * design pattern, allowing for implementations targeting either programs
 * or queries.
 *
 * @author Arno Bastenhof
 */
public abstract class Compiler {

	private final PrologBytecode<?> code;
	private final Scope scope;

	/**
	 *
	 * @param code the target for writing the generated bytecode to; not
	 * allowed to be null
	 * @param scope the ground scope to use when resolving symbols; not allowed
	 * to be null
	 * @throws NullPointerException if {@code code == null} if {@code scope ==
	 * null}
	 *
	 */
	protected Compiler(PrologBytecode<?> code, Scope scope) {
		this.code = checkNotNull(code);
		this.scope = checkNotNull(scope);
	}

	/**
	 * Compiles the specified {@code source}.
	 *
	 * @param source a reader for a program or -query; not allowed to be null
	 * @throws IOException
	 * @throws NullPointerException if {@code source == null}
	 * @throws RecognitionException if a lexer- or parsing error occurred
	 */
	public void compile(Reader source)
			throws IOException, RecognitionException {
		Ast ast = constructAst(checkNotNull(source));
		Map<Ast, Symbol> symbols = resolveSymbols(ast);
		generateBytecode(ast, symbols);
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
	private Map<Ast, Symbol> resolveSymbols(Ast root) {
		assert root != null;
		SymbolResolver visitor = new SymbolResolver(this.scope);
		walkAst(root, visitor);
		return visitor.getSymbols();
	}

	// Third compiler pass.
	private void generateBytecode(Ast root, Map<Ast, Symbol> symbols) {
		assert symbols != null;
		BytecodeGenerator visitor = new BytecodeGenerator(
				symbols,this.code);
		walkAst(root, visitor);
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
