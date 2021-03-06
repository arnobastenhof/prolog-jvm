package com.prolog.jvm.symbol;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import com.prolog.jvm.zip.util.Validate;

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
    public static final FunctorSymbol valueOf(final String name,
            final int arity) {
        Validate.argument(arity >= 0);
        return new FunctorSymbol(requireNonNull(name), arity);
    }

    /**
     * Static factory method for obtaining a functor symbol with arity 0.
     *
     * @param name the functor's name; not allowed to be null
     * @throws NullPointerException if {@code name == null}
     */
    public static final FunctorSymbol valueOf(final String name) {
        return valueOf(name, 0);
    }

    // Private to force instantiation through static factory methods
    private FunctorSymbol(final String name, final int arity) {
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
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FunctorSymbol)) {
            return false;
        }
        final FunctorSymbol other = (FunctorSymbol) obj;
        // note name cannot be null
        return this.arity == other.arity && this.name.equals(other.name);
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
