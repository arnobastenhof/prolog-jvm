package com.prolog.jvm.zip.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Prolog machine words, based on the exposition of their
 * realization in the Warren Abstract Machine (WAM) found in [1]. The WAM's
 * take on words differs in several respects from that found in the literature
 * on the ZIP (see [2]), but both adopt the basic premise of their
 * decomposition into a tag and a value, making the two notions largely
 * interchangeable. Besides some differences in the naming conventions for
 * tags, the deviations from [2] are outlined in the list below. In summary,
 * the WAM's specification for words is mostly a simpler one, motivating our
 * preference towards it.
 * <ul>
 * <li> As with [2], words are defined as consisting of 32-bit quantities.
 * However, we have used an 8 bit tag (comprising the word's most significant
 * byte) and a 24 bit value, as opposed to a 4 bit tag and 26 bit value. Note
 * that in this we differ from the WAM as well.
 * <li> Only a subset of the tags proposed in [1] are here used. Mostly this is
 * due to our restriction to only a very modest subset of (pure) Prolog,
 * although we have also neglected to introduce separate tags for words
 * representing bound and unbound variables. Instead, as with the WAM, we use
 * only a single tag, representing unbound variables by self references.
 * <ul>
 *
 * [1] Aït-Kaci, Hassan. "Warren's Abstract Machine A Tutorial Reconstruction."
 * (1999).
 * <p>
 * [2] Clocksin, William F. "Design and simulation of a sequential Prolog
 * machine." New Generation Computing 3.1 (1985): 101-120.
 *
 * @author Arno Bastenhof
 *
 */
public final class PlWords {

	// Private constructor to prevent instantiation.
	private PlWords() {
		throw new AssertionError();
	}

	// Bitmask for extracting the value of a word
	private static final int VAL_MASK = ~(0xFF << 24);

	// === Tags ===

	/**
	 * The tag used for representing variables. The value points to the
	 * instantiation. Note unbound variables are representing by the same tag,
	 * with their values pointing to themselves
	 */
	public static final int REF = 1;

	/**
	 * The tag used for representing compound terms (also called structures).
	 * The value points to a group of adjacent machine words, composed of a
	 * functor with some arity n > 0 followed by its n arguments.
	 */
	public static final int STR = 2;

	/**
	 * Tag used for representing lists. The value points to a cell of two
	 * adjacent machine words, representing the head resp. the tail of the list.
	 */
	// TODO Currently unused.
	public static final int LIS = 3;

	/**
	 * Tag used for functors. The value of a functor consists of a pointer to
	 * to a data aggregate containing both the atom and arity.
	 */
	public static final int FUNC = 4;

	/**
	 * Tag used for representing constants.
	 */
	public static final int CONS = 5;

	/**
	 * Unmodifiable map containing the String representations for tags.
	 */
	public static final Map<Integer,String> TAGS;

	static {
		final Map<Integer,String> map = new HashMap<>();
		map.put(REF,"REF");
		map.put(STR, "STR");
		map.put(LIS, "LIS");
		map.put(FUNC, "FUNCTOR");
		map.put(CONS, "CONS");
		TAGS = Collections.unmodifiableMap(map);
	}

	// === Static factory methods ===

	/**
	 * Returns a word with the specified {@code tag} and {@code value}, said
	 * values being truncated to their lower-order 8, respectively 24 bits.
	 */
	public static int getWord(final int tag, final int value) {
		return (tag << 24) | ((value << 8) >>> 8);
	}

	// === Accessors for extracting the tag and value from a word ===

	/**
	 * Returns the value of the supplied {@code word}, consisting of its
	 * 28 lower-order bits.
	 */
	public static int getValue(final int word) {
		return word & VAL_MASK;
	}

	/**
	 * Returns the tag of the supplied {@code word}, consisting of its most
	 * significant byte.
	 */
	public static int getTag(final int word) {
		return word >>> 24;
	}

	/**
	 * Returns whether the supplied {@code word} has the given {@code tag}.
	 */
	public static boolean hasTag(final int word, final int tag) {
		return getTag(word) == tag;
	}

	// === String ===

	/**
	 * Returns the String representation for the specified {@code word}'s tag
	 * and value, looking up the former in {@link #TAGS} while if not found
	 * therein resorting to its numeric value.
	 */
	public static final String toString(final int word) {
		final StringBuilder buffer = new StringBuilder("<");
		final int tag = getTag(word);
		final Integer key = Integer.valueOf(tag);
		if (TAGS.containsKey(key)) {
			buffer.append(TAGS.get(key));
		}
		else {
			buffer.append(Integer.toString(tag));
		}
		buffer.append(',');
		buffer.append(getValue(word));
		buffer.append('>');
		return buffer.toString();
	}
}
