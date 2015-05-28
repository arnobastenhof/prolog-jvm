package com.prolog.jvm.symbol;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

/**
 * Utility class containing static factory methods for obtaining {@link
 * SymbolKey} instances.
 *
 * @author Arno Bastenhof
 *
 */
public final class SymbolKeys {

	// Private to prevent instantiation.
	private SymbolKeys() {
		throw new AssertionError();
	}

	/**
	 * Returns a {@link SymbolKey} for use with {@link ClauseSymbol}s.
	 *
	 * @param name the name of the clause's head literal; not allowed to be
	 * null
	 * @param arity the arity of the clause's head literal; must be {@code >= 0}
	 * @throws NullPointerException if {@code name == null}
	 * @throws IllegalArgumentException if {@code arity < 0}
	 */
	public static SymbolKey<ClauseSymbol> ofClause(String name, int arity) {
		return ofRelationalSymbol(name, arity, ClauseSymbol.class);
	}

	/**
	 * Returns a {@link SymbolKey} for use with {@link PredicateSymbol}s.
	 *
	 * @param name the predicate name; not allowed to be null
	 * @param arity the predicate's arity; must be {@code >= 0}
	 * @throws NullPointerException if {@code name == null}
	 * @throws IllegalArgumentException if {@code arity < 0}
	 */
	public static SymbolKey<PredicateSymbol> ofPredicate(
			String name, int arity) {
		return ofRelationalSymbol(name, arity, PredicateSymbol.class);
	}

	/**
	 * Returns a {@link SymbolKey} for use with {@link FunctorSymbol}s.
	 *
	 * @param name the functor's name; not allowed to be null
	 * @param arity the functor's arity; must be {@code >= 0}
	 * @throws NullPointerException if {@code name == null}
	 * @throws IllegalArgumentException if {@code arity < 0}
	 */
	public static SymbolKey<FunctorSymbol> ofFunctor(String name, int arity) {
		return ofRelationalSymbol(name, arity, FunctorSymbol.class);
	}

	private static <T extends Symbol> SymbolKey<T> ofRelationalSymbol(
			String name, int arity, Class<T> clazz) {
		checkArgument(arity >= 0);
		return new RelationalKey<T>(checkNotNull(name), arity, clazz);
	}

	/**
	 * Returns a {@link SymbolKey} for use with {@link VariableSymbol}s.
	 *
	 * @param name the variable name; not allowed to be null
	 * @throws NullPointerException if {@code name == null}
	 */
	public static SymbolKey<VariableSymbol> ofVariable(String name) {
		return new VariableKey(checkNotNull(name));
	}

	// Skeletal implementation for SymbolKey
	private static abstract class AbstractKey<T extends Symbol>
	implements SymbolKey<T> {
		final String name;
		final Class<T> clazz;

		AbstractKey(String name, Class<T> clazz) {
			assert name != null;
			assert clazz != null;
			this.name = name;
			this.clazz = clazz;
		}

		@Override
		public Class<T> getSymbolClass() {
			return this.clazz;
		}
	}

	// Key for variable symbols
	private static final class VariableKey extends AbstractKey<VariableSymbol> {

		private VariableKey(String name) {
			super(name, VariableSymbol.class);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.name);
		}

		@Override
		public final boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof VariableKey)) {
				return false;
			}
			VariableKey other = (VariableKey)obj;
			return this.name.equals(other.name); // name cannot be null
		}
	}

	// Key for relational symbols; i.e., functors, clauses and predicates
	private static final class RelationalKey<T extends Symbol>
	extends AbstractKey<T> {
		private final int arity;

		private RelationalKey(String name, int arity, Class<T> clazz) {
			super(name,clazz);
			this.arity = arity;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.name, this.arity, this.clazz);
		}

		@Override
		public final boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof RelationalKey)) {
				return false;
			}
			RelationalKey<?> other = (RelationalKey<?>)obj;
			return this.arity == other.arity &&
					this.name.equals(other.name) &&  // name cannot be null
					this.clazz.equals(other.clazz);  // clazz cannot be null
		}
	}
}
