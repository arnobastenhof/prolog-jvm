package com.prolog.jvm.compiler.visitor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.prolog.jvm.zip.util.Instructions.CALL;
import static com.prolog.jvm.zip.util.Instructions.CONSTANT;
import static com.prolog.jvm.zip.util.Instructions.ENTER;
import static com.prolog.jvm.zip.util.Instructions.EXIT;
import static com.prolog.jvm.zip.util.Instructions.FIRSTVAR;
import static com.prolog.jvm.zip.util.Instructions.FUNCTOR;
import static com.prolog.jvm.zip.util.Instructions.POP;
import static com.prolog.jvm.zip.util.Instructions.VAR;

import java.util.Map;

import com.prolog.jvm.compiler.ast.Ast;
import com.prolog.jvm.symbol.ClauseSymbol;
import com.prolog.jvm.symbol.FunctorSymbol;
import com.prolog.jvm.symbol.PredicateSymbol;
import com.prolog.jvm.symbol.Symbol;
import com.prolog.jvm.symbol.VariableSymbol;
import com.prolog.jvm.zip.api.PrologBytecode;

/**
 * Visitor specifying the semantic actions for the final compiler pass,
 * generating bytecode (along with a constant table) for the ZIP machine.
 * <p>
 * Note that the bytecode generated through use of this class targets only a
 * reduced (unoptimized) instruction set, described in detail in [1].
 * <p>
 * [1] Bowen, David L., Lawrence Byrd, and William F. Clocksin. A portable
 * Prolog compiler. Department of Artificial Intelligence, University of
 * Edinburgh, 1983.
 *
 * @author Arno Bastenhof
 *
 */
public final class BytecodeGenerator extends AbstractSymbolVisitor {

	private final PrologBytecode<?> code;

	/**
	 *
	 * @param symbols a mapping of {@link Ast} nodes to the {@link Symbol}s
	 * to which they have been resolved; not allowed to be null
	 * @param code the target for writing the bytecode instructions
	 * @throws NullPointerException if {@code symbols == null}
	 */
	public BytecodeGenerator(Map<Ast,Symbol> symbols, PrologBytecode<?> code) {
		super(symbols);
		this.code = checkNotNull(code);
	}

	@Override
	public void preVisitClause(Ast clause) {
		ClauseSymbol symbol = getSymbol(clause,ClauseSymbol.class);
		symbol.setCode(this.code.getCodeSize());
	}

	@Override
	public void inVisitClause(Ast clause) {
		ClauseSymbol symbol = getSymbol(clause,ClauseSymbol.class);
		this.code.writeIns(ENTER, symbol.getParams() + symbol.getLocals());
	}

	@Override
	public void postVisitClause(Ast param) {
		this.code.writeIns(EXIT);
	}

	@Override
	public void postVisitGoal(Ast goal) {
		writeGroundIns(PredicateSymbol.class, goal, CALL);
	}

	@Override
	public void preVisitCompound(Ast term) {
		writeGroundIns(FunctorSymbol.class, term, FUNCTOR);
	}

	@Override
	public void postVisitCompound(Ast param) {
		this.code.writeIns(POP);
	}

	@Override
	public void visitConstant(Ast constant) {
		writeGroundIns(FunctorSymbol.class, constant, CONSTANT);
	}

	@Override
	public void visitVariable(Ast var) {
		VariableSymbol symbol = getSymbol(var, VariableSymbol.class);
		int opcode = VAR;
		if (!symbol.hasBeenSeenBefore()) {
			opcode = FIRSTVAR;
			symbol.setAsSeenBefore();
		}
		this.code.writeIns(opcode, symbol.getOffset());
	}

	// Writes an instruction taking a constant pool entry as its parameter
	private <T extends Symbol> void writeGroundIns(Class<T> clazz, Ast node,
			int opcode) {
		T symbol = getSymbol(node, clazz);
		int index = this.code.getConstantPoolIndex(symbol);
		this.code.writeIns(opcode, index);
	}

}
