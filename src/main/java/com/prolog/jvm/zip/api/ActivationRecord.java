package com.prolog.jvm.zip.api;

/**
 * Interface specifying the means for accessing the local variables of an
 * activation record. Note records may also be used for holding machine state
 * that is to be restored upon backtracking, and hence do double duty as a
 * memento for {@link ZipFacade} (in turn also acting as the caretaker).
 *
 * @author Arno Bastenhof
 *
 */
public interface ActivationRecord {
	// Nothing to specify.
}