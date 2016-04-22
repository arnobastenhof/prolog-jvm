package com.prolog.jvm.zip.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PlWordsTest {

    @Test
    public void getWord() {
        assertEquals(0x12ABCDEF, PlWords.getWord(0x12, 0xABCDEF));
        assertEquals(0x3400000F, PlWords.getWord(0x34, 0xF));
        assertEquals(0x56ABCDEF, PlWords.getWord(0x56, 0xAABCDEF));
        assertEquals(0x89ABCDEF, PlWords.getWord(0x789, 0xABCDEF));
        assertEquals(0xABCDEF, PlWords.getWord(0x00, 0xABCDEF));
    }

    @Test
    public void getTag() {
        assertEquals(0x12, PlWords.getTag(0x12ABCDEF));
        assertEquals(0x00, PlWords.getTag(0xBCDEF));
    }

    @Test
    public void getValue() {
        assertEquals(0xABCDEF, PlWords.getValue(0x12ABCDEF));
        assertEquals(0xBCDEF, PlWords.getValue(0xBCDEF));
    }

    @Test
    public void hasTag() {
        assertTrue(PlWords.hasTag(0x12ABCDEF, 0x12));
        assertTrue(PlWords.hasTag(0xBCDEF, 0x00));
    }

}
