package com.prolog.jvm.compiler.visitor;

import static com.prolog.jvm.zip.util.MemoryConstants.MIN_LOCAL_INDEX;
import static java.util.Objects.requireNonNull;

import java.util.Map;

import com.prolog.jvm.compiler.ast.Ast;
import com.prolog.jvm.main.Factory;
import com.prolog.jvm.symbol.Symbol;
import com.prolog.jvm.symbol.VariableSymbol;

/**
 * Visitor for traversing Prolog queries to register variable names and the
 * local stack addresses at which they are to be allocated, facilitating the
 * computation of answers.
 *
 * @author Arno Bastenhof
 */
public final class QueryVariableTracker extends AbstractSymbolVisitor {

	private final Map<Integer,String> queryVars;

	/**
	 * Constructor. Note the mapping {@code queryVars} from local stack
	 * addresses to (query) variable names is declared as an external
	 * dependency. It is recommended for client code not to instantiate this
	 * class directly, but rather invoke {@link Factory#newQueryCompiler()}
	 * to have its dependencies injected.
	 *
	 * @param symbols a mapping of {@link Ast} nodes to the {@link Symbol}s
	 * to which they have been resolved; not allowed to be null
	 * @param queryVars a mapping of local stack addresses to the names of
	 * the query variables allocated thereat
	 * @throws NullPointerException if {@code symbols == null || queryVars ==
	 * null}
	 */
	public QueryVariableTracker(final Map<Ast,Symbol> symbols,
			final Map<Integer,String> queryVars) {
		super(symbols);
		this.queryVars = requireNonNull(queryVars);
	}

	@Override
	public void visitVariable(Ast var) {
		// queryVars can safely be treated as a bidirectional map since
		// variables are scoped to the clause wherein they occur
		if (!this.queryVars.values().contains(var.getText())) {
			final VariableSymbol symbol = getSymbol(var, VariableSymbol.class);
			final Integer address = MIN_LOCAL_INDEX + symbol.getOffset();
			this.queryVars.put(address, var.getText());
		}
	}

}
