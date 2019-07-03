/*
 * Copyright (C) 2019 Adaptech s.r.o., Robert Pösel
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.leptonica.android;

import android.graphics.Bitmap;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WriteFileTest {
	@Test
	public void testWriteBitmap_1x1() {
		testWriteBitmap(1, 1);
	}

	@Test
	public void testWriteBitmap_100x100() {
		testWriteBitmap(100, 100);
	}

	@Test
	public void testWriteBitmap_640x480() {
		testWriteBitmap(640, 480);
	}

	private void testWriteBitmap(int width, int height) {
		Pix pix = TestUtils.createTestPix(width, height);
		Pix pcopy = pix.copy();                      // keep unchanged copy
		Bitmap bmp = WriteFile.writeBitmap(pix);     // pix changed

		assertNotNull(bmp);
		assertEquals(pix.getWidth(), bmp.getWidth());
		assertEquals(pix.getHeight(), bmp.getHeight());

		float match = TestUtils.compareImages(pcopy, bmp); // compare against the unchanged copy
		pix.recycle();
		bmp.recycle();

		assertTrue("Images do not match. match=" + match, (match >= 0.99f));
	}

	@Test
	public void testWriteBytes8_1x1() {
		testWriteBytes8(1, 1);
	}

	@Test
	public void testWriteBytes8_100x100() {
		testWriteBytes8(100, 100);
	}

	@Test
	public void testWriteBytes8_640x480() {
		testWriteBytes8(640, 480);
	}

	private static void testWriteBytes8(int width, int height) {
		Pix pixs = TestUtils.createTestPix(width, height);
		byte[] data = WriteFile.writeBytes8(pixs);
		Pix pixd = ReadFile.readBytes8(data, width, height);

		assertEquals(pixs.getWidth(), pixd.getWidth());
		assertEquals(pixs.getHeight(), pixd.getHeight());

		float match = TestUtils.comparePix(pixs, pixd);
		pixs.recycle();
		pixd.recycle();

		assertTrue("Images do not match. match=" + match, (match >= 0.99f));
	}

	@Test
	public void testWriteImpliedFormat_bmp() throws IOException {
		Pix pixs = TestUtils.createTestPix(100, 100);
		File file = File.createTempFile("testWriteImpliedFormat", ".bmp");
		testWriteImpliedFormat(pixs, file);
		pixs.recycle();
	}

	@Test
	public void testWriteImpliedFormat_jpg() throws IOException {
		Pix pixs = TestUtils.createTestPix(100, 100);
		File file = File.createTempFile("testWriteImpliedFormat", ".jpg");
		testWriteImpliedFormat(pixs, file);
		pixs.recycle();
	}

	@Test
	public void testWriteImpliedFormat_png() throws IOException {
		Pix pixs = TestUtils.createTestPix(100, 100);
		File file = File.createTempFile("testWriteImpliedFormat", ".png");
		testWriteImpliedFormat(pixs, file);
		pixs.recycle();
	}

	private void testWriteImpliedFormat(Pix pixs, File file) {
		boolean success = WriteFile.writeImpliedFormat(pixs, file);

		assertTrue("Writing to file failed.", success);
		assertTrue("File does not exist.", file.exists());
		assertTrue("File does not contain data.", file.length() > 0);

		Pix pixd = ReadFile.readFile(file);

		assertNotNull("Pix is null", pixd);

		float match = TestUtils.comparePix(pixs, pixd);
		pixd.recycle();

		assertTrue("Images do not match. match=" + match, (match >= 0.9999f));
	}
}
