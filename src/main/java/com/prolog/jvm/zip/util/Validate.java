package com.prolog.jvm.zip.util;

/**
 * Replicates the validation logic from Guava's {@code Preconditions} and
 * Apache Commons' {@code Validate}, obviating the need to include either as
 * dependencies to help keep down the size of the generated jar.
 *
 * @author Arno Bastenhof
 *
 */
public final class Validate {

	// Private constructor to prevent instantiation.
	private Validate() {
		throw new AssertionError();
	}

	/**
	 *
	 * @param cond the condition to be validated
	 * @throws IllegalArgumentException if {@code cond} is false
	 */
	public static void argument(boolean cond) {
		exception(cond, IllegalArgumentException.class);
	}

	/**
	 *
	 * @param cond the condition to be validated
	 * @throws IllegalStateException if {@code cond} is false
	 */
	public static void state(boolean cond) {
		exception(cond, IllegalStateException.class);
	}

	private static void exception(boolean cond,
			Class<? extends RuntimeException> clazz) {
		if (!cond) {
			try {
				throw clazz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
