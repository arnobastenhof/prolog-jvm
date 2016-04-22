package com.prolog.jvm.zip.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class InstructionsTest {

    @Test
    public void getMode() {
        assertEquals(0xC0, Instructions.getMode(0xC0));
        assertEquals(0x0, Instructions.getMode(0x3F));
    }

    @Test
    public void getOpcode() {
        assertEquals(0x0, Instructions.getOpcode(0xC0));
        assertEquals(0x3F, Instructions.getOpcode(0x3F));
    }
}
