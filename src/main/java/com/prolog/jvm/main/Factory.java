package com.prolog.jvm.main;

import static com.prolog.jvm.zip.util.MemoryConstants.MAX_CODE_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MAX_GLOBAL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MAX_LOCAL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MAX_PDL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MAX_SCRATCHPAD_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MAX_TRAIL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MEMORY_SIZE;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_CODE_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_GLOBAL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_LOCAL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_PDL_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_SCRATCHPAD_INDEX;
import static com.prolog.jvm.zip.util.MemoryConstants.MIN_TRAIL_INDEX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.prolog.jvm.compiler.Compiler;
import com.prolog.jvm.compiler.ast.Ast;
import com.prolog.jvm.compiler.ast.AstWalker;
import com.prolog.jvm.compiler.parser.PrologParser;
import com.prolog.jvm.compiler.parser.Tokens;
import com.prolog.jvm.compiler.visitor.PrologVisitor;
import com.prolog.jvm.compiler.visitor.SourcePass;
import com.prolog.jvm.exceptions.RecognitionException;
import com.prolog.jvm.symbol.Scope;
import com.prolog.jvm.zip.PrologBytecodeImpl;
import com.prolog.jvm.zip.PrologBytecodeImpl.MementoImpl;
import com.prolog.jvm.zip.ZipFacadeImpl;
import com.prolog.jvm.zip.ZipInterpreterImpl;
import com.prolog.jvm.zip.api.MemoryArea;
import com.prolog.jvm.zip.api.PrologBytecode;
import com.prolog.jvm.zip.api.ZipFacade;
import com.prolog.jvm.zip.api.ZipInterpreter;

/**
 * Utility class containing static factory methods for obtaining references
 * to the various components of the ZIP machine.
 *
 * @author Arno Bastenhof
 *
 */
public final class Factory {

	private static final List<Object> CONSTANT_POOL;
	private static final PrologBytecode<MementoImpl> PROLOG_BYTECODE;
	private static final MementoImpl BYTECODE_MEMENTO;
	private static final ZipFacade ZIP_FACADE;
	private static final ZipInterpreter ZIP_INTERPRETER;

	/*
	 * During compilation, clause-, functor- and predicate symbols are resolved
	 * against the 'global' root scope. Since, however, programs and queries
	 * are compiled separately, we have to cache this scope in between.
	 */
	private static Scope rootScope;

	static {
		CONSTANT_POOL = new ArrayList<>();
		// First element of constant pool is reserved
		CONSTANT_POOL.add(null);

		PROLOG_BYTECODE = new PrologBytecodeImpl(CONSTANT_POOL,
				MemoryAreas.CODE_MEMORY);

		// Keep a memento of PROLOG_BYTECODE in still pristine condition
		BYTECODE_MEMENTO = PROLOG_BYTECODE.createMemento();

		ZIP_FACADE = new ZipFacadeImpl.Builder().
				setConstants(Collections.unmodifiableList(CONSTANT_POOL)).
				setCodeMemory(MemoryAreas.CODE_MEMORY).
				setGlobalStack(MemoryAreas.GLOBAL_STACK).
				setLocalStack(MemoryAreas.LOCAL_STACK).
				setWordStore(MemoryAreas.WORD_STORE).
				setTrailStack(MemoryAreas.TRAIL_STACK).
				setPdl(MemoryAreas.PDL).
				setScratchpad(MemoryAreas.SCRATCHPAD).build();

		ZIP_INTERPRETER = new ZipInterpreterImpl(ZIP_FACADE);
	}

	// Private constructor to prevent instantiation.
	private Factory() {
		throw new AssertionError();
	}

	/**
	 * Returns a representation of the compiled bytecode instructions for a
	 * program and query, using addresses {@link ProcessorModes#MIN_CODE_INDEX}
	 * up to and including {@link ProcessorModes#MAX_CODE_INDEX} for its code
	 * memory.
	 * <p>
	 * This method is guaranteed to return the same instance upon each of its
	 * invocations.
	 */
	public static final PrologBytecode<MementoImpl> getBytecode() {
		return PROLOG_BYTECODE;
	}

	/**
	 * Returns a {@link ZipFacade} instance based on the following bounds for
	 * its {@link MemoryArea}s:
	 * <ul>
	 * <li> Addresses {@link ProcessorModes#MIN_GLOBAL_INDEX} up to and
	 * including {@link ProcessorModes#MAX_GLOBAL_INDEX} for the global stack.
	 * <li> Addresses (@link ZipConstants#MIN_LOCAL_INDEX} up to and including
	 * {@link ProcessorModes#MAX_LOCAL_INDEX} for the local stack.
	 * <li> Addresses {@link ProcessorModes#MIN_TRAIL_INDEX} up to and
	 * including {@link ProcessorModes#MAX_TRAIL_INDEX} for the trail stack.
	 * <li> Addresses {@link ProcessorModes#MIN_PDL_INDEX} up to and including
	 * {@link ProcessorModes#MAX_PDL_INDEX} for the Push-Down List.
	 * <li> Addresses {@link ProcessorModes#MIN_SCRATCHPAD_INDEX} up to and
	 * including {@link ProcessorModes#MAX_SCRATCHPAD_INDEX} for the
	 * scratchpad.
	 * </ul>
	 * This method is guaranteed to return the same instance upon each of its
	 * invocations.
	 */
	public static final ZipFacade getMachine() {
		return ZIP_FACADE;
	}

	/**
	 * Returns a {@link ZipInterpreter} instance, guaranteed to be the same
	 * upon each invocation.
	 */
	public static final ZipInterpreter getInterpreter() {
		return ZIP_INTERPRETER;
	}

	/**
	 * Returns a new {@link Compiler} instance for Prolog programs.
	 */
	public static final Compiler newProgramCompiler() {
		rootScope = Scope.newRootInstance();
		PROLOG_BYTECODE.setMemento(BYTECODE_MEMENTO);
		return new Compiler(PROLOG_BYTECODE, rootScope) {

			@Override
			protected SourcePass createSourcePassVisitor() {
				return new SourcePass(Tokens.PROGRAM);
			}

			@Override
			protected void parseSource(PrologParser parser)
					throws IOException, RecognitionException {
				parser.parseProgram();
			}

			@Override
			protected void walkAst(Ast root, PrologVisitor<Ast> visitor) {
				AstWalker.INSTANCE.walkProgram(root, visitor);
			}
		};
	}

	/**
	 * Returns a new {@link Compiler} instance for Prolog queries.
	 */
	public static final Compiler newQueryCompiler() {
		return new Compiler(PROLOG_BYTECODE, Scope.copyOf(rootScope)) {

			@Override
			protected SourcePass createSourcePassVisitor() {
				return new SourcePass(Tokens.IMPLIES);
			}

			@Override
			protected void parseSource(PrologParser parser)
					throws IOException, RecognitionException {
				parser.parseQuery();
			}

			@Override
			protected void walkAst(Ast root, PrologVisitor<Ast> visitor) {
				AstWalker.INSTANCE.walkQuery(root, visitor);
			}

		};
	}

	private enum MemoryAreas implements MemoryArea {

		GLOBAL_STACK(MIN_GLOBAL_INDEX, MAX_GLOBAL_INDEX),

		LOCAL_STACK(MIN_LOCAL_INDEX, MAX_LOCAL_INDEX),

		WORD_STORE(MIN_GLOBAL_INDEX, MAX_LOCAL_INDEX),

		TRAIL_STACK(MIN_TRAIL_INDEX, MAX_TRAIL_INDEX),

		PDL(MIN_PDL_INDEX, MAX_PDL_INDEX),

		SCRATCHPAD(MIN_SCRATCHPAD_INDEX, MAX_SCRATCHPAD_INDEX),

		CODE_MEMORY(MIN_CODE_INDEX, MAX_CODE_INDEX);

		// Virtual memory
		private static final int[] memory = new int[MEMORY_SIZE];

		private final int lower, upper;

		private MemoryAreas(int lower, int upper) {
			this.lower = lower;
			this.upper = upper;
		}

		@Override
		public int readFrom(int address) {
			checkBounds(address);
			return memory[address];
		}

		@Override
		public void writeTo(int address, int value) {
			checkBounds(address);
			memory[address] = value;
		}

		private void checkBounds(int address) {
			if (address < this.lower || address > this.upper) {
				throw new IndexOutOfBoundsException();
			}
		}
	}

}
