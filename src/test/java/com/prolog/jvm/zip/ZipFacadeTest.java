package com.prolog.jvm.zip;

import static com.prolog.jvm.zip.util.PlWords.CONS;
import static com.prolog.jvm.zip.util.PlWords.FUNC;
import static com.prolog.jvm.zip.util.PlWords.LIS;
import static com.prolog.jvm.zip.util.PlWords.REF;
import static com.prolog.jvm.zip.util.PlWords.STR;
import static com.prolog.jvm.zip.util.PlWords.getWord;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.prolog.jvm.symbol.FunctorSymbol;
import com.prolog.jvm.zip.api.MemoryArea;

/**
 * Test class for {@link ZipFacadeImpl}.
 *
 * @author Arno Bastenhof
 *
 */
public final class ZipFacadeTest {

	private final AbstractZipFacadeBuilder<ZipFacadeMockImpl.Builder> builder =
			new ZipFacadeMockImpl.Builder();

	@Test
	public void pushFunctor() {
		// Keep a reference to the word store for post-asserts
		final int[] wordStore = new int[4];

		// Mock constant pool
		final FunctorSymbol symbol = FunctorSymbol.valueOf("f", 2);
		final List<Object> constants = new ArrayList<>();
		constants.add(symbol);

		// Build
		final ZipFacadeMockImpl facade = this.builder
				.setWordStore(new MemoryAreaMockImpl(wordStore))
				.setConstants(constants)
				.build();

		// Assert
		assertEquals(getWord(STR, 0), facade.pushFunctor(symbol));
		assertEquals(getWord(FUNC, 0), wordStore[0]);
		assertEquals(getWord(REF, 1), wordStore[1]);
		assertEquals(getWord(REF, 2), wordStore[2]);
	}

	@Test
	public void unwindTrail() {
		// Keep a reference to the word store for post-asserts
		final int[] wordStore = new int[]{
				getWord(REF, 3),  // Unbound variable
				getWord(REF, 2),  // Bound variable
				getWord(REF, 0),  // Bound variable
				getWord(REF, 3)}; // Unbound variable

		// Expected word store after the trail is unbound between addresses 1-3
		final int[] expected = Arrays.copyOf(wordStore, 4);
		expected[1] = getWord(REF, 1);
		expected[2] = getWord(REF, 2);

		// Build the facade
		final ZipFacadeMockImpl facade = this.builder
				.setWordStore(new MemoryAreaMockImpl(wordStore))
				.setTrailStack(new MemoryAreaMockImpl(new int[]{0, 2, 1, 3}))
				.build();

		// Assert
		final List<Integer> vars = new ArrayList<>();
		facade.unwindTrail(1, 3, vars);
		assertTrue(Arrays.equals(wordStore, expected));
		assertEquals(2, vars.size());
		assertTrue(vars.contains(1));
		assertTrue(vars.contains(2));
	}

	@Test
	public void getWordAt() {
		// Keep a reference to the word store for post-asserts
		final int[] wordStore = new int[]{
				getWord(REF, 5),
				getWord(REF, 3),
				getWord(REF, 0),
				getWord(STR, 4),
				getWord(FUNC, 1),
				getWord(REF, 5)
		};

		// Build the facade
		final ZipFacadeMockImpl facade = this.builder
				.setWordStore(new MemoryAreaMockImpl(wordStore))
				.build();

		// Asserts
		assertEquals(getWord(STR, 4), facade.getWordAt(3));
		assertEquals(getWord(FUNC, 1), facade.getWordAt(4));
		assertEquals(getWord(REF, 5), facade.getWordAt(5));
		assertEquals(getWord(REF, 5), facade.getWordAt(2));
		assertEquals(getWord(STR, 4), facade.getWordAt(1));
	}

	@Test
	public final void bind() {
		// Keep references to the word store and trail stack for post-asserts
		final int[] wordStore = new int[]{
				getWord(CONS, 0),
				getWord(REF, 1),
				getWord(REF, 3),
				getWord(STR, 4),
				getWord(FUNC, 1),
				getWord(REF, 5),
				getWord(REF, 6),
				getWord(REF, 7)
		};
		final int[] trailStack = new int[2];

		// Build (Note: trailing disabled by default)
		final ZipFacadeMockImpl facade = this.builder
				.setWordStore(new MemoryAreaMockImpl(wordStore))
				.setTrailStack(new MemoryAreaMockImpl(trailStack))
				.build();

		// Mock backtrack global- and trail stack pointers
		facade.backtrackGlobalptr = 5;
		facade.local = true;

		// #1: Bind a global unbound variable to another global unbound
		// variable (no trailing)
		facade.local = false;
		facade.bind(5, 1);
		assertEquals(getWord(REF, 1), wordStore[5]);
		assertEquals(getWord(REF, 1), wordStore[1]);
		assertEquals(0, trailStack[0]);

		// #2: Bind a global variable to an atom (with trailing)
		facade.bind(1, 0);
		assertEquals(getWord(CONS, 0), wordStore[1]);
		assertEquals(1, trailStack[0]);

		// #3: Bind a local variable to a global bound variable (with trailing)
		facade.local = true;
		facade.bind(6, 2);
		assertEquals(getWord(STR, 4), wordStore[6]);
		assertEquals(6, trailStack[1]);
	}

	@Test
	public final void unify() {
		// Keep a reference to the word store for post-asserts
		final int[] wordStore = new int[]{
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
		final List<Object> constants = new ArrayList<>();
		constants.add(FunctorSymbol.valueOf("f", 2));

		// Build (Note: trailing disabled by default)
		final ZipFacadeMockImpl facade = this.builder
				.setConstants(constants)
				.setWordStore(new MemoryAreaMockImpl(wordStore))
				.setPdl(new MemoryAreaMockImpl(new int[6]))
				.build();

		// #1: Unification succeeds
		assertNotNull(facade.unifiable(0, 9));
		assertEquals(getWord(CONS, 1), wordStore[10]);
		assertEquals(getWord(CONS, 3), wordStore[5]);
		assertEquals(getWord(STR, 7), wordStore[13]);

		// #2: Unification fails
		assertNull(facade.unifiable(0, 15));
		assertEquals(getWord(STR, 3), wordStore[17]);
	}

	// Memory area mock implementation backed by an array supplied by the
	// client code
	private static final class MemoryAreaMockImpl implements MemoryArea {

		private final int[] memory;

		private MemoryAreaMockImpl(final int[] memory) {
			assert memory != null;
			this.memory = memory;
		}

		@Override
		public int readFrom(final int address) {
			return this.memory[address];
		}

		@Override
		public void writeTo(final int address, final int value) {
			this.memory[address] = value;
		}

	}

	// ZipFacade implementation overriding its protected hooks to allow them
	// to be mocked.
	private static final class ZipFacadeMockImpl extends ZipFacadeImpl {

		private ZipFacadeMockImpl(
				final List<Object> constants, final MemoryArea heap,
				final MemoryArea globalStack, final MemoryArea localStack,
				final MemoryArea wordStore, final MemoryArea trailStack,
				final MemoryArea pdl, final MemoryArea scratchpad) {
			super(constants, heap, globalStack, localStack,
					wordStore, trailStack, pdl, scratchpad);
		}

		private boolean local;
		private int backtrackGlobalptr;

		@Override
		protected boolean isLocal(final int address) {
			return this.local;
		}

		@Override
		protected int getBacktrackGlobalPointer() {
			return this.backtrackGlobalptr;
		}

		@Override
		protected int getMinPdlIndex() {
			return 0;
		}

		private static final class Builder
		extends AbstractZipFacadeBuilder<Builder> {

			private Builder() {
				this.instance = this;
			}

			@Override
			public ZipFacadeMockImpl build() {
				return new ZipFacadeMockImpl(this.constants, this.heap,
						this.globalStack, this.localStack, this.wordStore,
						this.trailStack, this.pdl, this.scratchpad);
			}

		}

	}

}
