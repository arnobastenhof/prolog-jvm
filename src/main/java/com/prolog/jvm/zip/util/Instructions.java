package com.prolog.jvm.zip.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class describing the ZIP instruction set, defining operators by the
 * combination of an opcode with a processor mode.
 * <p>
 * Processor modes are defined by two-bit numeric constants, padded by trailing
 * zeroes to the size of a byte. With opcodes occupying only the lower-order
 * six bits of a byte, this allows for the two to be combined at runtime using
 * a bitwise or to arrive at the 'real' instruction to be dispatched on.
 *
 * @author Arno Bastenhof
 *
 */
public final class Instructions {

	// Private constructor to prevent instantiation.
	private Instructions() {
		throw new AssertionError();
	}

	private static final int OPCODE_MASK = 0x3F;
	private static final int MODE_MASK = 0xC0;

	// === Processor modes === //

	/**
	 * The MATCH processor mode, used during the processing of a head literal.
	 */
	public static final int MATCH = 1 << 6;

	/**
	 * The ARG processor mode, used during the processing of goal literals.
	 */
	public static final int ARG = 2 << 6;

	/**
	 * The COPY processor mode, used when writing on the global stack.
	 */
	public static final int COPY = 3 << 6;

	// === Opcodes ===

	/**
	 * Opcode for completing the unification of a compound term.
	 */
	public static final int POP = 1;

	/**
	 * Opcode for unifying a functor of some non-zero arity.
	 */
	public static final int FUNCTOR = 9;

	/**
	 * Opcode for unifying a constant (i.e., a functor of zero arity).
	 */
	public static final int CONSTANT = 11;

	/**
	 * Opcode for unifying the first occurrence of a variable within some
	 * clause.
	 */
	public static final int FIRSTVAR = 5;

	/**
	 * Opcode for unifying a subsequent occurrence of a variable within some
	 * clause.
	 */
	public static final int VAR = 4;

	/**
	 * Opcode for calling a predicate.
	 */
	public static final int CALL = 17;

	/**
	 * Opcode for executing the neck of a clause.
	 */
	public static final int ENTER = 12;

	/**
	 * Opcode for completion of a clause.
	 */
	public static final int EXIT = 25;

	// === String representations ===

	/**
	 * Unmodifiable map containing the modes' String representations.
	 */
	public static final Map<Integer,String> MODES;

	/**
	 * Unmodifiable map containing the opcode mnemonics.
	 */
	public static final Map<Integer,String> MNEMONICS;

	static {
		final Map<Integer,String> map = new HashMap<>();
		map.put(Integer.valueOf(MATCH), "MATCH");
		map.put(Integer.valueOf(ARG), "ARG");
		map.put(Integer.valueOf(COPY), "COPY");
		MODES = Collections.unmodifiableMap(map);
	}

	static {
		final Map<Integer,String> map = new HashMap<>();
		map.put(Integer.valueOf(POP), "POP");
		map.put(Integer.valueOf(FUNCTOR), "FUNCTOR");
		map.put(Integer.valueOf(CONSTANT), "CONSTANT");
		map.put(Integer.valueOf(FIRSTVAR), "FIRSTVAR");
		map.put(Integer.valueOf(VAR), "VAR");
		map.put(Integer.valueOf(CALL), "CALL");
		map.put(Integer.valueOf(ENTER), "ENTER");
		map.put(Integer.valueOf(EXIT), "EXIT");
		MNEMONICS = Collections.unmodifiableMap(map);
	}

	/**
	 * Extracts the mode and opcode from {@code operator} and returns the
	 * concatenation of their String representations, in accordance with
	 * {@link #modeToString(int)} and {@link #opcodeToString(int)}.
	 */
	public static final String toString(final int operator) {
		return toModeString(operator) + " | " + toOpcodeString(operator);
	}

	/**
	 * Extracts the opcode information from {@code operator} and returns
	 * its String representation, in accordance with
	 * {@link #opcodeToString(int)}.
	 */
	public static String toOpcodeString(final int operator) {
		return opcodeToString(getOpcode(operator));
	}

	/**
	 * Extracts the mode information from {@code operator} and returns its
	 * String representation, in accordance with {@link #modeToString(int)}.
	 */
	public static String toModeString(final int operator) {
		return modeToString(getMode(operator));
	}

	/**
	 * Returns the lower 6 bits of {@code operator}, comprising the opcode.
	 */
	public static int getOpcode(final int operator) {
		return operator & OPCODE_MASK;
	}

	/**
	 * Returns the higher 2 bits of {@code operator}, comprising the machine
	 * mode.
	 */
	public static int getMode(final int operator) {
		return operator & MODE_MASK;
	}

	/**
	 * Returns the String representation for the specified {@code mode} from
	 * {@link #MODES} if found therein, or that of its (hexadecimal) integer
	 * value otherwise.
	 */
	public static final String modeToString(final int mode) {
		return toString(MODES, mode);
	}

	/**
	 * Returns the String representation for the specified {@code opcode} from
	 * {@link #MNEMONICS} if found therein, or that of its (hexadecimal) integer
	 * value otherwise.
	 */
	public static final String opcodeToString(int opcode) {
		return toString(MNEMONICS, opcode);
	}

	private static final String toString(final Map<Integer,String> map,
			final int operatorPart) {
		final Integer key = Integer.valueOf(operatorPart);
		if (map.containsKey(key)) {
			return map.get(key);
		}
		return Integer.toHexString(operatorPart);
	}

}
