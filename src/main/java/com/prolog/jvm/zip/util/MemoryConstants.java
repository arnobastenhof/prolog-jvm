package com.prolog.jvm.zip.util;

/**
 * Utility class containing constants delimiting the ZIP's various runtime
 * memory areas within a shared virtual address space.
 *
 * @author Arno Bastenhof
 *
 */
public final class MemoryConstants {

	// Private to prevent instantiation.
	private MemoryConstants() {
		throw new AssertionError();
	}

	/**
	 * The total number of words that can be allocated in virtual memory.
	 */
	public static final int MEMORY_SIZE = 25000512;

	/**
	 * The smallest address in virtual memory for use by the global stack.
	 */
	public static final int MIN_GLOBAL_INDEX = 0;

	/**
	 * The largest address in virtual memory for use by the global stack.
	 */
	public static final int MAX_GLOBAL_INDEX = 7999999;

	/**
	 * The smallest address in virtual memory for use by the local stack.
	 */
	public static final int MIN_LOCAL_INDEX = MAX_GLOBAL_INDEX + 1;

	/**
	 * The largest address in virtual memory for use by the local stack.
	 */
	public static final int MAX_LOCAL_INDEX = 15999999;

	/**
	 * The smallest address in virtual memory for use by the trail.
	 */
	public static final int MIN_TRAIL_INDEX = MAX_LOCAL_INDEX + 1;

	/**
	 * The largest address in virtual memory for use by the trail.
	 */
	public static final int MAX_TRAIL_INDEX = 23999999;

	/**
	 * The smallest address in virtual memory for use by the PDL (Push-Down
	 * List).
	 */
	public static final int MIN_PDL_INDEX = MAX_TRAIL_INDEX + 1;

	/**
	 * The largest address in virtual memory for use by the PDL (Push-Down
	 * List).
	 */
	public static final int MAX_PDL_INDEX = 24000255;

	/**
	 * The smallest address in virtual memory for use by the scratchpad area.
	 */
	public static final int MIN_SCRATCHPAD_INDEX = MAX_PDL_INDEX + 1;

	/**
	 * The largest address in virtual memory for use by the scratchpad area.
	 */
	public static final int MAX_SCRATCHPAD_INDEX = 24000511;

	/**
	 * The smallest address in virtual memory for use by the heap.
	 */
	public static final int MIN_HEAP_INDEX = MAX_SCRATCHPAD_INDEX + 1;

	/**
	 * The largest address in virtual memory for use by the heap.
	 */
	public static final int MAX_HEAP_INDEX = MEMORY_SIZE - 1;

}
