package com.prolog.jvm.common.parser;

/**
 * Skeletal implementation of a {@link Token}.
 *
 * @author Arno Bastenhof
 *
 * @param <T> The token type.
 */
public abstract class AbstractToken<T> implements Token<T> {

	private final T type;
	private final String text;

	/**
	 * Protected constructor.
	 * @param type The token type.
	 * @param text The matched input text.
	 */
	protected AbstractToken(T type, String text) {
		this.type = type;
		this.text = text;
	}

	/**
	 * Returns this token's type.
	 */
	@Override
	public final T getType() {
		return this.type;
	}

	/**
	 * Returns the matched input text for this token.
	 */
	@Override
	public final String getText() {
		return this.text;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public abstract String toString();

	/**
	 * @inheritDoc
	 */
	@Override
	public abstract int hashCode();

	/**
	 * @inheritDoc
	 */
	@Override
	public abstract boolean equals(Object obj);

}
