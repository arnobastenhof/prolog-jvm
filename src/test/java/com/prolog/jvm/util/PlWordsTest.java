package com.prolog.jvm.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.prolog.jvm.zip.util.PlWords;

public class PlWordsTest {

	@Test
	public final void getWord() {
		assertEquals(PlWords.getWord(0x12,0xABCDEF),0x12ABCDEF);
		assertEquals(PlWords.getWord(0x34,0xF),0x3400000F);
		assertEquals(PlWords.getWord(0x56,0xAABCDEF),0x56ABCDEF);
		assertEquals(PlWords.getWord(0x789,0xABCDEF),0x89ABCDEF);
		assertEquals(PlWords.getWord(0x00,0xABCDEF),0xABCDEF);
	}

	@Test
	public final void getTag() {
		assertEquals(PlWords.getTag(0x12ABCDEF),0x12);
		assertEquals(PlWords.getTag(0xBCDEF),0x00);
	}

	@Test
	public final void getValue() {
		assertEquals(PlWords.getValue(0x12ABCDEF),0xABCDEF);
		assertEquals(PlWords.getValue(0xBCDEF),0xBCDEF);
	}

	@Test
	public final void hasTag() {
		assertEquals(PlWords.hasTag(0x12ABCDEF,0x12),true);
		assertEquals(PlWords.hasTag(0xBCDEF,0x00),true);
	}

}
