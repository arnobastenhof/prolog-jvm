package com.prolog.jvm.symbol;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

/**
 * The type of functors as they are represented in the constant pool. An atom
 * is in particular considered a functor of arity 0.
 *
 * @author Arno Bastenhof
 *
 */
public final class FunctorSymbol implements Symbol {

	/**
	 * A constant for the empty list.
	 */
	public static final FunctorSymbol NIL = new FunctorSymbol("[]", 0);

	private final String name;
	private final int arity;

	/**
	 * Static factory method for obtaining a functor symbol.
	 *
	 * @param name the functor's name; not allowed to be null
	 * @param arity the functor's arity; must be {@code >= 0}
	 * @throws NullPointerException if {@code name == null}
	 * @throws IllegalArgumentException if {@code arity < 0}
	 */
	public static final FunctorSymbol valueOf(String name, int arity) {
		checkArgument(arity >= 0);
		return new FunctorSymbol(checkNotNull(name), arity);
	}

	/**
	 * Static factory method for obtaining a functor symbol with arity 0.
	 *
	 * @param name the functor's name; not allowed to be null
	 * @throws NullPointerException if {@code name == null}
	 */
	public static final FunctorSymbol valueOf(String name) {
		return valueOf(name, 0);
	}

	// Private to force instantiation through static factory methods
	private FunctorSymbol(String name, int arity) {
		assert name != null;
		assert arity >= 0;
		this.name = name;
		this.arity = arity;
	}

	/**
	 * Returns this functor's name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns this functor's arity.
	 */
	public int getArity() {
		return this.arity;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.arity);
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof FunctorSymbol)) {
			return false;
		}
		FunctorSymbol other = (FunctorSymbol)obj;
		return this.arity == other.arity &&
				this.name.equals(other.name); // name cannot be null
	}

	/**
	 * Returns the String representation of this functor, being
	 * {@link #getName()}/{@link #getArity()}.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(this.name);
		buffer.append("/");
		buffer.append(this.arity);
		return buffer.toString();
	}
}
