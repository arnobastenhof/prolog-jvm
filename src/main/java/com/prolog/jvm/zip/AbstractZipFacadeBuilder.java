package com.prolog.jvm.zip;

import java.util.List;

import com.prolog.jvm.zip.api.MemoryArea;
import com.prolog.jvm.zip.api.ZipFacade;

/**
 * Skeletal implementation for a {@link ZipFacade.Builder}.
 *
 * @author Arno Bastenhof
 *
 */
public abstract class AbstractZipFacadeBuilder<T extends ZipFacade>
implements ZipFacade.Builder<T> {

	/**
	 * The constant pool. Defaults to null.
	 */
	protected List<Object> constants;

	/**
	 * The memory area used for storing the bytecode instructions. Defaults to
	 * null.
	 */
	protected MemoryArea codeMemory;

	/**
	 * The memory area used for the global stack. Defaults to null.
	 */
	protected MemoryArea globalStack;

	/**
	 * The memory area used for the local stack. Defaults to null.
	 */
	protected MemoryArea localStack;

	/**
	 * The combined memory areas for the global and local stacks. Defaults
	 * to null.
	 */
	protected MemoryArea wordStore;

	/**
	 * The memory area used for the trail stack. Defaults to null.
	 */
	protected MemoryArea trailStack;

	/**
	 * The memory area used for the Push-Down List. Defaults to null.
	 */
	protected MemoryArea pdl;

	/**
	 * The memory area used for the scratchpad. Defaults to null.
	 */
	protected MemoryArea scratchpad;

	@Override
	public ZipFacade.Builder<T> setConstants(List<Object> constants) {
		this.constants = constants;
		return this;
	}

	@Override
	public ZipFacade.Builder<T> setCodeMemory(MemoryArea codeMemory) {
		this.codeMemory = codeMemory;
		return this;
	}

	@Override
	public ZipFacade.Builder<T> setGlobalStack(MemoryArea globalStack) {
		this.globalStack = globalStack;
		return this;
	}

	@Override
	public ZipFacade.Builder<T> setLocalStack(MemoryArea localStack) {
		this.localStack = localStack;
		return this;
	}

	@Override
	public ZipFacade.Builder<T> setWordStore(MemoryArea wordStore) {
		this.wordStore = wordStore;
		return this;
	}

	@Override
	public ZipFacade.Builder<T> setTrailStack(MemoryArea trailStack) {
		this.trailStack = trailStack;
		return this;
	}

	@Override
	public ZipFacade.Builder<T> setPdl(MemoryArea pdl) {
		this.pdl = pdl;
		return this;
	}

	@Override
	public ZipFacade.Builder<T> setScratchpad(MemoryArea scratchpad) {
		this.scratchpad = scratchpad;
		return this;
	}
}