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
import android.graphics.Bitmap.CompressFormat;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author alanv@google.com (Alan Viverette)
 */
public class ReadFileTest {

	private static final String TAG = ReadFileTest.class.getSimpleName();

	@Test
	public void testReadBitmap_1x1() {
		testReadBitmap(1, 1, Bitmap.Config.ARGB_8888);
	}

	@Test
	public void testReadBitmap_100x100() {
		testReadBitmap(100, 100, Bitmap.Config.ARGB_8888);
	}

	@Test
	public void testReadBitmap_640x480() {
		testReadBitmap(640, 480, Bitmap.Config.ARGB_8888);
	}

	private void testReadBitmap(int width, int height, Bitmap.Config format) {
		Bitmap bmp = TestUtils.createTestBitmap(width, height, format);
		Pix pix = ReadFile.readBitmap(bmp);
		assertNotNull(pix);
		assertEquals(bmp.getWidth(), pix.getWidth());
		assertEquals(bmp.getHeight(), pix.getHeight());

		float match = TestUtils.compareImages(pix, bmp);
		assertTrue("Images do not match. match=" + match, (match >= 0.99f));

		bmp.recycle();
		pix.recycle();
	}

	@Test
	public void testReadFile_bmp() throws IOException {
		File file = File.createTempFile("testReadFile", ".bmp");
		FileOutputStream fileStream = new FileOutputStream(file);
		Bitmap bmp = TestUtils.createTestBitmap(100, 100, Bitmap.Config.RGB_565);
		boolean compressed = bmp.compress(CompressFormat.PNG, 100, fileStream);

		assertTrue(compressed);

		Pix pix = ReadFile.readFile(file);
		assertNotNull(pix);
		assertEquals(bmp.getWidth(), pix.getWidth());
		assertEquals(bmp.getHeight(), pix.getHeight());

		float match = TestUtils.compareImages(pix, bmp);
		assertTrue("Images do not match. match=" + match, (match >= 0.99f));

		fileStream.close();
		bmp.recycle();
		pix.recycle();
	}

	@Test
	public void testReadFile_jpg() throws IOException {
		File file = File.createTempFile("testReadFile", ".jpg");
		FileOutputStream fileStream = new FileOutputStream(file);
		Bitmap bmp = TestUtils.createTestBitmap(100, 100, Bitmap.Config.RGB_565);
		boolean compressed = bmp.compress(CompressFormat.JPEG, 85, fileStream);

		assertTrue(compressed);

		Pix pix = ReadFile.readFile(file);
		assertNotNull(pix);
		assertEquals(bmp.getWidth(), pix.getWidth());
		assertEquals(bmp.getHeight(), pix.getHeight());

		float match = TestUtils.compareImages(pix, bmp, 15);
		assertTrue("Images do not match. match=" + match, (match >= 0.98f));

		fileStream.close();
		bmp.recycle();
		pix.recycle();
	}

	@Test
	public void testReadFile_png() throws IOException {
		File file = File.createTempFile("testReadFile", ".png");
		FileOutputStream fileStream = new FileOutputStream(file);
		Bitmap bmp = TestUtils.createTestBitmap(100, 100, Bitmap.Config.RGB_565);
		boolean compressed = bmp.compress(CompressFormat.PNG, 100, fileStream);

		assertTrue(compressed);

		Pix pix = ReadFile.readFile(file);
		assertNotNull(pix);
		assertEquals(bmp.getWidth(), pix.getWidth());
		assertEquals(bmp.getHeight(), pix.getHeight());

		float match = TestUtils.compareImages(pix, bmp);
		assertTrue("Images do not match. match=" + match, (match >= 0.99f));

		fileStream.close();
		bmp.recycle();
		pix.recycle();
	}

	@Test
	public void testReadMem_jpg() throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		Bitmap bmp = TestUtils.createTestBitmap(100, 100, Bitmap.Config.RGB_565);
		boolean compressed = bmp.compress(CompressFormat.JPEG, 85, byteStream);

		assertTrue(compressed);

		byte[] encodedData = byteStream.toByteArray();
		Pix pix = ReadFile.readMem(encodedData);
		assertNotNull(pix);
		assertEquals(bmp.getWidth(), pix.getWidth());
		assertEquals(bmp.getHeight(), pix.getHeight());

		float match = TestUtils.compareImages(pix, bmp, 15);
		assertTrue("Images do not match. match=" + match, (match >= 0.98f));

		byteStream.close();
		bmp.recycle();
		//noinspection UnusedAssignment
		encodedData = null;
		pix.recycle();
	}

	@Test
	public void testReadMem_png() throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		Bitmap bmp = TestUtils.createTestBitmap(100, 100, Bitmap.Config.RGB_565);
		boolean compressed = bmp.compress(CompressFormat.PNG, 100, byteStream);

		assertTrue(compressed);

		byte[] encodedData = byteStream.toByteArray();
		Pix pix = ReadFile.readMem(encodedData);
		assertNotNull(pix);
		assertEquals(bmp.getWidth(), pix.getWidth());
		assertEquals(bmp.getHeight(), pix.getHeight());

		float match = TestUtils.compareImages(pix, bmp);
		assertTrue("Images do not match. match=" + match, (match >= 0.99f));

		byteStream.close();
		bmp.recycle();
		//noinspection UnusedAssignment
		encodedData = null;
		pix.recycle();
	}
}
