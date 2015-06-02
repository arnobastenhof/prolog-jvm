package com.prolog.jvm.symbol;

/**
 * Interface for objects that can be used as keys to search for {@link Symbol}s
 * inside a {@link Scope}.
 *
 * @author Arno Bastenhof
 *
 * @param <T> the type of Symbol this key is used for
 */
public interface SymbolKey<T extends Symbol> {

	/**
	 * Returns the {@link Symbol} implementation class for this key.
	 */
	Class<T> getSymbolClass();

	@Override
	String toString();

}
