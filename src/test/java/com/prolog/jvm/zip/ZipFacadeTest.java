package com.prolog.jvm.zip;

import static com.prolog.jvm.zip.util.PlWords.CONS;
import static com.prolog.jvm.zip.util.PlWords.FUNC;
import static com.prolog.jvm.zip.util.PlWords.LIS;
import static com.prolog.jvm.zip.util.PlWords.REF;
import static com.prolog.jvm.zip.util.PlWords.STR;
import static com.prolog.jvm.zip.util.PlWords.getWord;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.prolog.jvm.symbol.FunctorSymbol;
import com.prolog.jvm.zip.api.MemoryArea;
import com.prolog.jvm.zip.api.ZipFacade;

/**
 * Test class for {@link ZipFacadeImpl}.
 *
 * @author Arno Bastenhof
 *
 */
public final class ZipFacadeTest {

	private final ZipFacade.Builder<ZipFacadeMockImpl> builder =
			new ZipFacadeMockImpl.Builder();

	@Test
	public final void pushFunctor() {
		// Keep a reference to the word store for post-asserts
		int[] wordStore = new int[4];

		// Mock constant pool
		FunctorSymbol symbol = FunctorSymbol.valueOf("f", 2);
		List<Object> constants = new ArrayList<>();
		constants.add(symbol);

		// Build
		ZipFacadeMockImpl facade = this.builder
				.setWordStore(new MemoryAreaMockImpl(wordStore))
				.setConstants(constants)
				.build();

		// Assert
		assertEquals(facade.pushFunctor(symbol), getWord(STR, 0));
		assertEquals(wordStore[0], getWord(FUNC, 0));
		assertEquals(wordStore[1], getWord(REF, 1));
		assertEquals(wordStore[2], getWord(REF, 2));
	}

	@Test
	public final void unwindTrail() {
		// Keep a reference to the word store for post-asserts
		int[] wordStore = new int[]{
				getWord(REF, 3),  // Unbound variable
				getWord(REF, 2),  // Bound variable
				getWord(REF, 0),  // Bound variable
				getWord(REF, 3)}; // Unbound variable

		// Expected word store after the trail is unbound between addresses 1-3
		int[] expected = Arrays.copyOf(wordStore, 4);
		expected[1] = getWord(REF, 1);
		expected[2] = getWord(REF, 2);

		// Build the facade
		ZipFacadeMockImpl facade = this.builder
				.setWordStore(new MemoryAreaMockImpl(wordStore))
				.setTrailStack(new MemoryAreaMockImpl(new int[]{0, 2, 1, 3}))
				.build();

		// Assert
		facade.unwindTrail(1, 3);
		assertTrue(Arrays.equals(wordStore, expected));
	}

	@Test
	public final void getWordAt() {
		// Keep a reference to the word store for post-asserts
		int[] wordStore = new int[]{
				getWord(REF, 5),
				getWord(REF, 3),
				getWord(REF, 0),
				getWord(STR, 4),
				getWord(FUNC, 1),
				getWord(REF, 5)
		};

		// Build the facade
		ZipFacadeMockImpl facade = this.builder
				.setWordStore(new MemoryAreaMockImpl(wordStore))
				.build();

		// Asserts
		assertEquals(facade.getWordAt(3), getWord(STR, 4));
		assertEquals(facade.getWordAt(4), getWord(FUNC, 1));
		assertEquals(facade.getWordAt(5), getWord(REF, 5));
		assertEquals(facade.getWordAt(2), getWord(REF, 5));
		assertEquals(facade.getWordAt(1), getWord(STR, 4));
	}

	@Test
	public final void bind() {
		// Keep references to the word store and trail stack for post-asserts
		int[] wordStore = new int[]{
				getWord(CONS, 0),
				getWord(REF, 1),
				getWord(REF, 3),
				getWord(STR, 4),
				getWord(FUNC, 1),
				getWord(REF, 5),
				getWord(REF, 6),
				getWord(REF, 7)
		};
		int[] trailStack = new int[2];

		// Build (Note: trailing disabled by default)
		ZipFacadeMockImpl facade = this.builder
				.setWordStore(new MemoryAreaMockImpl(wordStore))
				.setTrailStack(new MemoryAreaMockImpl(trailStack))
				.build();

		// Mock backtrack global- and trail stack pointers
		facade.backtrackGlobalptr = 5;
		facade.backtrackLocalptr = 7;
		facade.local = true;

		// #1: Bind a local unbound variable to a global unbound variable (no
		// trailing)
		facade.bind(7, 1);
		assertEquals(wordStore[7], getWord(REF, 1));
		assertEquals(wordStore[1], getWord(REF, 1));
		assertEquals(trailStack[0], 0);

		// #2: Bind a global unbound variable to another global unbound
		// variable (no trailing)
		facade.local = false;
		facade.bind(5, 1);
		assertEquals(wordStore[5], getWord(REF, 1));
		assertEquals(wordStore[1], getWord(REF, 1));
		assertEquals(trailStack[0], 0);

		// #3: Bind a global variable to an atom (with trailing)
		facade.bind(1, 0);
		assertEquals(wordStore[1], getWord(CONS, 0));
		assertEquals(trailStack[0], 1);

		// #4: Bind a local variable to a global bound variable (with trailing)
		facade.local = true;
		facade.bind(6, 2);
		assertEquals(wordStore[6], getWord(REF, 3));
		assertEquals(trailStack[1], 6);
	}

	@Test
	public final void unify() {
		// Keep a reference to the word store for post-asserts
		int[] wordStore = new int[]{
				getWord(LIS, 1),    // First term
				getWord(CONS, 1),
				getWord(STR, 3),
				getWord(FUNC, 0),
				getWord(REF, 6),
				getWord(REF, 5),
				getWord(STR, 7),
				getWord(FUNC, 2),
				getWord(REF, 8),
				getWord(LIS, 10),   // Second term
				getWord(REF, 10),
				getWord(STR, 12),
				getWord(FUNC, 0),
				getWord(REF, 13),
				getWord(CONS, 3),
				getWord(LIS, 16),   // Third term
				getWord(CONS, 4),
				getWord(REF, 17)
		};

		// Mock constant pool
		List<Object> constants = new ArrayList<>();
		constants.add(FunctorSymbol.valueOf("f", 2));

		// Build (Note: trailing disabled by default)
		ZipFacadeMockImpl facade = this.builder
				.setConstants(constants)
				.setWordStore(new MemoryAreaMockImpl(wordStore))
				.setPdl(new MemoryAreaMockImpl(new int[6]))
				.build();

		// #1: Unification succeeds
		assertTrue(facade.unifiable(0, 9));
		assertEquals(wordStore[10], getWord(CONS, 1));
		assertEquals(wordStore[5], getWord(CONS, 3));
		assertEquals(wordStore[13], getWord(STR, 7));

		// #2: Unification fails
		assertFalse(facade.unifiable(0, 15));
		assertEquals(wordStore[17], getWord(STR, 3));
	}

	// Memory area mock implementation backed by an array supplied by the
	// client code
	private static final class MemoryAreaMockImpl implements MemoryArea {

		private final int[] memory;

		private MemoryAreaMockImpl(int[] memory) {
			assert memory != null;
			this.memory = memory;
		}

		@Override
		public int readFrom(int address) {
			return this.memory[address];
		}

		@Override
		public void writeTo(int address, int value) {
			this.memory[address] = value;
		}

	}

	// ZipFacade implementation overriding its protected hooks to allow them
	// to be mocked.
	private static final class ZipFacadeMockImpl extends ZipFacadeImpl {

		private ZipFacadeMockImpl(
				List<Object> constants, MemoryArea heap,
				MemoryArea globalStack, MemoryArea localStack,
				MemoryArea wordStore, MemoryArea trailStack,
				MemoryArea pdl, MemoryArea scratchpad) {
			super(constants, heap, globalStack, localStack,
					wordStore, trailStack, pdl, scratchpad);
		}

		private boolean local;
		private int backtrackGlobalptr;
		private int backtrackLocalptr;

		@Override
		protected boolean isLocal(int address) {
			return this.local;
		}

		@Override
		protected int getBacktrackGlobalPointer() {
			return this.backtrackGlobalptr;
		}

		@Override
		protected int getBacktrackLocalPointer() {
			return this.backtrackLocalptr;
		}

		@Override
		protected int getMinPdlIndex() {
			return 0;
		}

		private static final class Builder
		extends AbstractZipFacadeBuilder<ZipFacadeMockImpl> {

			@Override
			public ZipFacadeMockImpl build() {
				return new ZipFacadeMockImpl(this.constants, this.heap,
						this.globalStack, this.localStack, this.wordStore,
						this.trailStack, this.pdl, this.scratchpad);
			}

		}

	}

}
