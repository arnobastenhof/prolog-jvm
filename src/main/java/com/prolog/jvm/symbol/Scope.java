package com.prolog.jvm.symbol;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class representing a possibly nested scope for storing Prolog {@link
 * Symbol}s.
 * <p>
 * The design of this class is based loosely on Pattern 17 of Parr (2010),
 * Language Implementation Patterns: Symbol Table for Nested Scopes. The
 * primary differences lies in the logic for resolving symbols: whereas Parr
 * recursively searches the parent scope until a symbol is found, the current
 * class instead delegates this responsibility to the client code, searching
 * only the current scope.
 * <p>
 * With {@link Symbol}s identified as such only through a marker interface,
 * this class allows for the typesafe retrieval of specific implementation
 * classes by parameterizing the type of the key whereby they are stored, thus
 * adopting the typesafe heterogeneous container pattern described in Item 29
 * from Bloch (2008), Effective Java, 2nd Edition.
 *
 * @author Arno Bastenhof
 */
public final class Scope {

	private final Scope parent;
	private final Map<SymbolKey<?>,Symbol> symbols = new HashMap<>();

	/**
	 * Static factory method for obtaining a root scope; i.e., with no parent.
	 */
	public static Scope newRootInstance() {
		return new Scope(null);
	}

	/**
	 * Static factory method for obtaining an intermediate-level scope; i.e.,
	 * with a non-null reference to a parent scope.
	 *
	 * @param parent the parent scope; not allowed to be null
	 * @throws NullPointerException if {@code parent == null}
	 */
	public static Scope newIntermediateInstance(Scope parent) {
		return new Scope(checkNotNull(parent));
	}

	/**
	 * Returns a deep copy of the specified {@code original}.
	 *
	 * @param original the scope to be copied; not allowed to be null
	 * @throws NullPointerException if {@code original == null}
	 */
	public static Scope copyOf(Scope original) {
		checkNotNull(original);
		// Copy the parent.
		Scope parent = null;
		if (original.parent != null) {
			parent = Scope.copyOf(original.parent);
		}
		// Create a new instance using the parent copy.
		Scope result = new Scope(parent);
		// Copy the symbol entries and return the result.
		result.symbols.putAll(original.symbols);
		return result;
	}

	// Package-private to force instantiation through static factory methods.
	private Scope(Scope parent) {
		this.parent = parent;
	}

	/**
	 * Returns the parent scope, if it exists, or null otherwise.
	 */
	public Scope getParent() {
		return this.parent;
	}

	/**
	 * Returns the number of symbols stored in this scope.
	 */
	public int getSize() {
		return this.symbols.keySet().size();
	}

	/**
	 * Returns the {@link SymbolKey}s for which a {@link Symbol} is stored in
	 * this scope.
	 */
	public Set<SymbolKey<?>> getKeys() {
		return this.symbols.keySet();
	}

	/**
	 * Returns the symbol for the given key in the root scope, or null if not
	 * found therein.
	 *
	 * @param key the search key; not allowed to be null
	 * @throws NullPointerException if {@code key == null}
	 */
	public <T extends Symbol> T resolveGlobal(SymbolKey<T> key) {
		return resolveRecursive(checkNotNull(key));
	}

	/**
	 * Defines the given symbol in the root scope, replacing the previous
	 * definition for the same (i.e., equivalent) key if already present.
	 *
	 * @param key the key; not allowed to be null
	 * @param symbol the symbol to be stored; not allowed to be null
	 * @throws NullPointerException if {@code key == null || symbol == null}
	 */
	public <T extends Symbol> void defineGlobal(SymbolKey<T> key, T symbol) {
		defineRecursive(checkNotNull(key), checkNotNull(symbol));
	}

	/**
	 * Returns the symbol for the given {@code key} in the current scope, or
	 * null if not found therein. In particular, this method will <i>not</i>
	 * search the parent scope, leaving this instead as a responsibility for
	 * the client code.
	 *
	 * @param key the search key; not allowed to be null
	 * @throws NullPointerException if {@code key == null}
	 */
	public <T extends Symbol> T resolveLocal(SymbolKey<T> key) {
		return resolve(checkNotNull(key));
	}

	/**
	 * Defines the given symbol in the current scope, replacing the previous
	 * definition for the same (i.e., equivalent) key if already present.
	 *
	 * @param key the key; not allowed to be null
	 * @param symbol the symbol to be stored; not allowed to be null
	 * @throws NullPointerException if {@code key == null || symbol == null}
	 */
	public <T extends Symbol> void defineLocal(SymbolKey<T> key, T symbol) {
		this.symbols.put(checkNotNull(key), checkNotNull(symbol));
	}

	// Recursively calls itself on the parent scope until found to be null
	private <T extends Symbol> T resolveRecursive(SymbolKey<T> key) {
		assert key != null;
		if (this.parent != null) {
			return this.parent.resolveRecursive(key);
		}
		return resolve(key);
	}

	// Searches the current scope
	private <T extends Symbol> T resolve(SymbolKey<T> key) {
		assert key != null;
		return key.getSymbolClass().cast(this.symbols.get(key));
	}

	// Recursively calls itself on the parent scope until found to be null
	private <T extends Symbol> void defineRecursive(SymbolKey<T> key, T symbol) {
		assert key != null;
		assert symbol != null;
		if (this.parent != null) {
			this.parent.defineRecursive(key, symbol);
		}
		this.symbols.put(key, symbol);
	}
}
