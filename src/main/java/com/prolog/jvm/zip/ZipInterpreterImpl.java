package com.prolog.jvm.zip;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.prolog.jvm.zip.util.Instructions.ARG;
import static com.prolog.jvm.zip.util.Instructions.CALL;
import static com.prolog.jvm.zip.util.Instructions.CONSTANT;
import static com.prolog.jvm.zip.util.Instructions.COPY;
import static com.prolog.jvm.zip.util.Instructions.ENTER;
import static com.prolog.jvm.zip.util.Instructions.EXIT;
import static com.prolog.jvm.zip.util.Instructions.FIRSTVAR;
import static com.prolog.jvm.zip.util.Instructions.FUNCTOR;
import static com.prolog.jvm.zip.util.Instructions.MATCH;
import static com.prolog.jvm.zip.util.Instructions.POP;
import static com.prolog.jvm.zip.util.Instructions.VAR;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_LOCAL_INDEX;
import static com.prolog.jvm.zip.util.PlWords.CONS;
import static com.prolog.jvm.zip.util.PlWords.FUNC;
import static com.prolog.jvm.zip.util.PlWords.REF;
import static com.prolog.jvm.zip.util.PlWords.STR;
import static com.prolog.jvm.zip.util.PlWords.getWord;
import static com.prolog.jvm.zip.util.ReplConstants.FAILURE;
import static com.prolog.jvm.zip.util.ReplConstants.NEXT_ANSWER;
import static com.prolog.jvm.zip.util.ReplConstants.SUCCESS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.prolog.jvm.exceptions.BacktrackException;
import com.prolog.jvm.main.Factory;
import com.prolog.jvm.symbol.ClauseSymbol;
import com.prolog.jvm.symbol.FunctorSymbol;
import com.prolog.jvm.symbol.PredicateSymbol;
import com.prolog.jvm.symbol.Symbol;
import com.prolog.jvm.zip.api.ZipFacade;
import com.prolog.jvm.zip.api.ZipInterpreter;
import com.prolog.jvm.zip.util.Instructions;
import com.prolog.jvm.zip.util.PlWords;

/**
 * Implementation of a {@link ZipInterpreter}, as described in [1] and [2].
 * <p>
 * [1] Bowen, David L., Lawrence Byrd, and William F. Clocksin. A portable
 * Prolog compiler. Department of Artificial Intelligence, University of
 * Edinburgh, 1983.
 * <p>
 * [2] Clocksin, William F. "Design and simulation of a sequential Prolog
 * machine." New Generation Computing 3.1 (1985): 101-120.
 *
 * @author Arno Bastenhof
 *
 */
public final class ZipInterpreterImpl implements ZipInterpreter {

	private final ZipFacade facade;

	/**
	 *
	 * @param facade a facade for the ZIP's internals; not allowed to be null
	 */
	public ZipInterpreterImpl(ZipFacade facade) {
		this.facade = checkNotNull(facade);
	}

	@Override
	public void execute(int queryAddress, BufferedReader in, Writer out)
			throws IOException {
		// Initialize the ZIP machine
		this.facade.reset(queryAddress);

		// In MATCH mode, some address in the global -or local stack must be
		// matched against. Similarly, in ARG and COPY modes, some such address
		// must be copied from.
		int address = MIN_LOCAL_INDEX;

		// Fetch/decode/execute cycle
		try {
			while (true) {
				int operator = this.facade.readOperator();
				switch (operator) {
				case MATCH | FUNCTOR: {
					address = matchFunctor(address);
					continue;
				}
				case MATCH | CONSTANT: {
					address = matchConstant(address);
					continue;
				}
				case MATCH | FIRSTVAR: {
					address = matchVariable(true, address);
					continue;
				}
				case MATCH | VAR: {
					address = matchVariable(false, address);
					continue;
				}
				case MATCH | ENTER: {
					address = enterClause();
					continue;
				}
				case MATCH | POP:
					// Fall-through
				case COPY | POP: {
					address = this.facade.popFromScratchpad();
					continue;
				}
				case COPY | FUNCTOR:
					// Fall-through
				case ARG | FUNCTOR: {
					address = copyFunctor(address);
					continue;
				}
				case COPY | CONSTANT:
					// Fall-through
				case ARG | CONSTANT: {
					address = copyConstant(address);
					continue;
				}
				case COPY | FIRSTVAR: {
					address = copyVariable(true, address);
					continue;
				}
				case COPY | VAR: {
					address = copyVariable(false, address);
					continue;
				}
				case ARG | FIRSTVAR: {
					address = argVariable(true, address);
					continue;
				}
				case ARG | VAR: {
					address = argVariable(false, address);
					continue;
				}
				case ARG | CALL: {
					address = callPredicate();
					continue;
				}
				case ARG | EXIT: {
					// If popSourceFrame returns true, that means we're done
					if (this.facade.popSourceFrame()) {
						if (writeAnswer(in, out)) {
							this.facade.backtrack();
							continue;
						}
						return;
					}
					// Else, push a new target frame
					// TODO Does double work if preceded by ENTER or jumps to
					// another EXIT (i.e., last call)
					address = this.facade.pushTargetFrame();
					continue;
				}
				default:
					throw new IllegalArgumentException(
							Instructions.toString(operator));
				}
			}
		}
		catch (BacktrackException e) {
			out.write(FAILURE);
		}
	}

	private int matchFunctor(int addr) throws BacktrackException {
		FunctorSymbol symbol = readSymbolOperand(FunctorSymbol.class);
		int word = this.facade.getWordAt(addr);
		switch (PlWords.getTag(word)) {
		case REF: {
			int address = PlWords.getValue(word);
			this.facade.trail(address);
			return writeFunctor(symbol, address);
		}
		case STR: {
			int globalAddr = PlWords.getValue(word);
			int index = PlWords.getValue(this.facade.getWordAt(globalAddr));
			if (symbol != this.facade.getConstant(index,FunctorSymbol.class)) {
				return this.facade.backtrack();
			}
			this.facade.pushOnScratchpad(++addr);
			return globalAddr + 1;
		}
		default:
			return this.facade.backtrack();
		}
	}

	private int matchConstant(int addr) throws BacktrackException {
		FunctorSymbol symbol = readSymbolOperand(FunctorSymbol.class);
		int word = this.facade.getWordAt(addr);
		switch (PlWords.getTag(word)) {
		case REF: {
			int address = PlWords.getValue(word);
			this.facade.setWord(address, symbol);
			this.facade.trail(address);
			break;
		}
		case CONS: {
			int index = PlWords.getValue(word);
			if (symbol != this.facade.getConstant(index,FunctorSymbol.class)) {
				return this.facade.backtrack();
			}
			break;
		}
		default:
			return this.facade.backtrack();
		}
		return addr + 1;
	}

	private int matchVariable(boolean firstOccurrence, int addr)
			throws BacktrackException {
		int localAddr = this.facade.readOperand(true);
		if (firstOccurrence) {
			this.facade.setWord(localAddr, this.facade.getWordAt(addr));
		}
		else if (!this.facade.unifiable(localAddr, addr)) {
			return this.facade.backtrack();
		}
		return addr + 1;
	}

	private int copyFunctor(int addr) {
		FunctorSymbol symbol = readSymbolOperand(FunctorSymbol.class);
		return writeFunctor(symbol, addr);
	}

	private int copyConstant(int addr) {
		FunctorSymbol symbol = readSymbolOperand(FunctorSymbol.class);
		this.facade.setWord(addr, symbol);
		return addr + 1;
	}

	private int argVariable(boolean firstOccurrence, int addr) {
		int localAddr = this.facade.readOperand(true);
		int word = 0;
		if (firstOccurrence) {
			word = getWord(REF, localAddr);
			this.facade.setWord(localAddr, word);
		}
		else {
			word = this.facade.getWordAt(localAddr);
		}
		this.facade.setWord(addr, word);
		return addr + 1;
	}

	private int copyVariable(boolean firstOccurrence, int globalAddr) {
		int localAddr = this.facade.readOperand(true);
		if (firstOccurrence) {
			this.facade.setWord(localAddr, this.facade.getWordAt(globalAddr));
		}
		else {
			this.facade.bind(globalAddr, localAddr);
		}
		return globalAddr + 1;
	}

	private int writeFunctor(FunctorSymbol symbol, int addr) {
		int word = this.facade.pushFunctor(symbol);
		this.facade.setWord(addr, word);
		this.facade.pushOnScratchpad(++addr);
		this.facade.setMode(COPY);
		return PlWords.getValue(word) + 1;
	}

	// localAddr contains the local stack frame address for the first local
	// variable cell in the source frame to be pushed
	private int enterClause() {
		// The old target frame becomes the new source frame
		int size = this.facade.readOperand(false);
		this.facade.pushSourceFrame(size);

		// Set machine mode to ARG
		this.facade.setMode(ARG);

		// Push a new target frame for the first goal
		return this.facade.pushTargetFrame();
	}

	private int callPredicate() {
		ClauseSymbol symbol = readSymbolOperand(PredicateSymbol.class)
				.getFirst();

		// Push a choice point if necessary
		ClauseSymbol next = symbol.getNext();
		if (next != null) {
			this.facade.pushChoicePoint(next);
		}

		// Set the machine mode and jump to the first clause alternative for
		// the called predicate
		this.facade.setMode(MATCH);
		return this.facade.jump(symbol.getHeapptr());
	}

	private <T extends Symbol> T readSymbolOperand(Class<T> clazz) {
		int index = this.facade.readOperand(false);
		return this.facade.getConstant(index, clazz);
	}

	// == Answers ===

	// Returns whether to backtrack and look for more answers
	private boolean writeAnswer(BufferedReader in, Writer out)
			throws IOException {
		Set<Integer> addresses = new HashSet<>();
		addresses.addAll(Factory.getQueryVars().keySet());
		if (addresses.isEmpty()) {
			out.write(SUCCESS);
			return false;
		}
		for (Integer address : addresses) {
			out.append(Factory.getQueryVars().get(address)).write(" = ");
			walkCode(address.intValue(), out);
			out.write('\n');
		}
		out.flush();
		return in.readLine().equals(NEXT_ANSWER);
	}

	private String getVarName(int addr) {
		Map<Integer,String> queryVars = Factory.getQueryVars();
		Integer address = Integer.valueOf(addr);
		String result = queryVars.get(address);
		if (result == null) {
			result = "?" + queryVars.keySet().size();
			queryVars.put(address, result);
		}
		return result;
	}

	private final void walkCode(int addr, Writer out)
			throws IOException {
		int word = this.facade.getWordAt(addr);
		switch (PlWords.getTag(word)) {
		case REF: {
			// Since we already dereferenced, value must be equal to addr
			out.write(getVarName(addr));
			return;
		}
		case STR: {
			walkCode(PlWords.getValue(word), out);
			return;
		}
		case FUNC: {
			int index = PlWords.getValue(word);
			FunctorSymbol symbol =
					this.facade.getConstant(index, FunctorSymbol.class);
			assert symbol.getArity() > 0;
			out.append(symbol.getName()).write('(');
			for (int i = 1; i <= symbol.getArity(); i++) {
				walkCode(addr + i, out);
				if (i < symbol.getArity()) {
					out.write(", ");
				}
			}
			out.write(')');
			return;
		}
		case CONS: {
			int index = PlWords.getValue(word);
			FunctorSymbol symbol =
					this.facade.getConstant(index, FunctorSymbol.class);
			assert symbol.getArity() == 0;
			out.write(symbol.getName());
			return;
		}
		default:
			throw new AssertionError();
		}
	}
}
