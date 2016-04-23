package com.prolog.jvm.zip;

import java.util.List;

import com.prolog.jvm.zip.api.MemoryArea;
import com.prolog.jvm.zip.api.ZipFacade;

 /**
  * Abstract class describing a builder for a {@link ZipFacade}.
  * <p>
  * While neglecting to set all properties before calling build may result in an
  * object residing in an inconsistent state for the purpose of executing the
  * {@link ZipFacade}'s main use case (i.e., running a query against a compiled
  * program), depending on the implementation class, the resulting instance may
  * still be useful for e.g. unit testing. As such, this API does not prescribe
  * the validation of any invariants on the constructed object.
  * <p>
  * For production, it is recommended that a reference to a {@link ZipFacade} be
  * obtained through the corresponding static factory method on {@link Factory},
  * which uses a {@code Builder} under water and ensures all properties are
  * initialized before invoking build. Test classes, in contrast, can use a
  * {@code Builder} to create a new {@link ZipFacade} for each unit test,
  * tweaked to the particular conditions assessed thereby (e.g., through mocking
  * the various {@link MemoryArea}s).
  *
  * @author Arno Bastenhof
  *
  */
public abstract class AbstractZipBuilder<T extends AbstractZipBuilder<T>> {

    /**
     * An instance of an implementing class.
     */
    protected T instance;

    /**
     * The constant pool. Defaults to null.
     */
    protected List<Object> constants;

    /**
     * The memory area used for storing the bytecode instructions. Defaults to
     * null.
     */
    protected MemoryArea heap;

    /**
     * The memory area used for the global stack. Defaults to null.
     */
    protected MemoryArea globalStack;

    /**
     * The memory area used for the local stack. Defaults to null.
     */
    protected MemoryArea localStack;

    /**
     * The combined memory areas for the global and local stacks. Defaults to
     * null.
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

    /**
     * Builds a {@link ZipFacade} instance.
     */
    public abstract ZipFacade build();

    /**
     * Sets the constant pool.
     */
    public final T setConstants(final List<Object> constants) {
        this.constants = constants;
        return this.instance;
    }

    /**
     * Sets the memory area used for storing the bytecode instructions (allowed
     * to be null).
     */
    public final T setHeap(final MemoryArea heap) {
        this.heap = heap;
        return this.instance;
    }

    /**
     * Sets the memory area used for the global stack (allowed to be null).
     */
    public final T setGlobalStack(final MemoryArea globalStack) {
        this.globalStack = globalStack;
        return this.instance;
    }

    /**
     * Sets the memory area used for the local stack (allowed to be null).
     */
    public final T setLocalStack(final MemoryArea localStack) {
        this.localStack = localStack;
        return this.instance;
    }

    /**
     * Sets the combined memory areas of the local and global stacks (allowed to
     * be null). Practical considerations require these to appear adjacent in
     * the lower region of virtual memory in order for them to be addressable
     * through a 24-bit unsigned integer, used for encoding word values.
     * Moreover, operations like dereferencing, binding and unification are
     * agnostic as to whether the words they operate on have been allocated on
     * the global- or local stack, necessitating the current separate
     * {@link MemoryArea} instance beside those used for accessing the global-
     * and local stacks proper.
     */
    public final T setWordStore(final MemoryArea wordStore) {
        this.wordStore = wordStore;
        return this.instance;
    }

    /**
     * Sets the memory area used for the trail (allowed to be null).
     */
    public final T setTrailStack(final MemoryArea trailStack) {
        this.trailStack = trailStack;
        return this.instance;
    }

    /**
     * Sets the memory area used for the Push-Down List (allowed to be null).
     */
    public final T setPdl(final MemoryArea pdl) {
        this.pdl = pdl;
        return this.instance;
    }

    /**
     * Sets the memory area used for the scratchpad (allowed to be null).
     */
    public final T setScratchpad(final MemoryArea scratchpad) {
        this.scratchpad = scratchpad;
        return this.instance;
    }
}