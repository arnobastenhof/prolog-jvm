package com.prolog.jvm.zip.util;

import com.prolog.jvm.main.Repl;
import com.prolog.jvm.zip.api.ZipInterpreter;

/**
 * Utility class defining String constants used as in- and output by the {@link
 * Repl} and {@link ZipInterpreter}.
 *
 * @author Arno Bastenhof
 */
public final class ReplConstants {

	/**
	 * The REPL's prompt.
	 */
	public static final String PROMPT = "?- ";

	/**
	 * Indicates the query was found to be true.
	 */
	public static final String SUCCESS = "yes\n";

	/**
	 * Indicates the query was found to be false.
	 */
	public static final String FAILURE = "no\n";

	/**
	 * Input for initiating backtracking to find another answer.
	 */
	public static final String NEXT_ANSWER = ";";

	/**
	 * User command for exiting the REPL.
	 */
	public static final String HALT = "halt";

}
