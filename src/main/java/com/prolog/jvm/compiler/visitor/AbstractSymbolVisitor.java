package com.prolog.jvm.compiler.visitor;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import com.prolog.jvm.compiler.ast.Ast;
import com.prolog.jvm.symbol.Symbol;

/**
 * Skeletal implementation of a {@link PrologVisitor}, to be used for
 * specifying compiler passes following that defined by {@link SymbolResolver}.
 *
 * @author Arno Bastenhof
 */
public abstract class AbstractSymbolVisitor extends BasicPrologVisitor<Ast> {

	private final Map<Ast,Symbol> symbols;

	/**
	 *
	 * @param symbols a mapping of {@link Ast} nodes to the {@link Symbol}s
	 * to which they have been resolved; not allowed to be null
	 * @throws NullPointerException if {@code symbols == null}
	 */
	public AbstractSymbolVisitor(final Map<Ast,Symbol> symbols) {
		this.symbols = requireNonNull(symbols);
	}

	/**
	 * Returns the symbol to which the specified {@code node} has been resolved
	 * and casts it to {@code clazz}.
	 *
	 * @param node the {@link Ast} node for which the resolved symbol is to be
	 * returned
	 * @param clazz the expected {@link Symbol} implementation class
	 * @throws NullPointerException if no symbol was found for {@code node}
	 * @throws ClassCastException if the symbol found for {@code node} cannot
	 * be cast to {@code clazz}
	 */
	protected <T extends Symbol> T getSymbol(final Ast node,
			final Class<T> clazz) {
		final Symbol symbol = this.symbols.get(node);
		return clazz.cast(requireNonNull(symbol));
	}

}
