package com.prolog.jvm.zip.api;

/**
 * Callback interface listening for state changes in the ZIP machine in between
 * execution of individual instructions (steps). Intended for debugging
 * purposes.
 *
 * @author Arno Bastenhof
 *
 */
public interface StepListener {

	/**
	 * Called by the {@link ZipInterpreter} after execution of each instruction.
	 */
	void handleEvent(StepEvent event) throws Exception;

}
