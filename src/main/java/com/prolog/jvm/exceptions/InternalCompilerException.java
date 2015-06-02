package com.prolog.jvm.exceptions;

import com.prolog.jvm.compiler.ast.Ast;

/**
 * Exception class used for communicating exceptional conditions encountered
 * during the process of translating an {@link Ast} to bytecode.
 *
 * @author Arno Bastenhof
 *
 */
public final class InternalCompilerException extends RuntimeException {

	private static final long serialVersionUID = 7856350557692616483L;

	/**
	 *
	 * @param cause the lower-level exception to be wrapped
	 */
	public InternalCompilerException(Throwable cause) {
		super(cause);
	}

	/**
	 *
	 * @param msg the exception message
	 */
	public InternalCompilerException(String msg) {
		super(msg);
	}

}
