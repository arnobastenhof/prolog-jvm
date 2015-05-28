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
 * @see ProcessorModes
 *
 */
public final class Instructions {

	// Private constructor to prevent instantiation.
	private Instructions() {
		throw new AssertionError();
	}

	private static final int OPCODE_MASK = 0x3f;
	private static final int MODE_MASK = 0x70;

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
		Map<Integer,String> map = new HashMap<>();
		map.put(MATCH, "MATCH");
		map.put(ARG, "ARG");
		map.put(COPY, "COPY");
		MODES = Collections.unmodifiableMap(map);
	}

	static {
		Map<Integer,String> map = new HashMap<>();
		map = new HashMap<>();
		map.put(Integer.valueOf(POP), "pop");
		map.put(Integer.valueOf(FUNCTOR), "functor");
		map.put(Integer.valueOf(CONSTANT), "constant");
		map.put(Integer.valueOf(VAR), "var");
		map.put(Integer.valueOf(CALL), "call");
		map.put(Integer.valueOf(ENTER), "enter");
		map.put(Integer.valueOf(EXIT), "exit");
		MNEMONICS = Collections.unmodifiableMap(map);
	}

	/**
	 * Returns the String representations for the mode information and opcode
	 * in the specified {@code operator} from {@link #MODES} and
	 * {@link #MNEMONICS} respectively if found, or that of their integer
	 * values otherwise.
	 */
	public static final String toString(int operator) {
		StringBuilder buffer = new StringBuilder("Mode: ");
		buffer.append(toString(MODES, operator & MODE_MASK));
		buffer.append(". Opcode: ");
		buffer.append(toString(MNEMONICS, operator & OPCODE_MASK));
		return buffer.toString();
	}

	/**
	 * Returns the String representation for the specified {@code mode} from
	 * {@link #MODES} if found therein, or that of its integer value otherwise.
	 */
	public static final String modeToString(int mode) {
		return "Mode: " + toString(MODES, mode);
	}

	/**
	 * Returns the String representation for the specified {@code opcode} from
	 * {@link #MNEMONICS} if found therein, or that of its integer value
	 * otherwise.
	 */
	public static final String opcodeToString(int opcode) {
		return "Opcode: " + toString(MNEMONICS, opcode);
	}

	private static final String toString(Map<Integer,String> map,
			int operatorPart) {
		Integer key = Integer.valueOf(operatorPart);
		if (map.containsKey(key)) {
			return map.get(key);
		}
		return Integer.toString(operatorPart);
	}

}
