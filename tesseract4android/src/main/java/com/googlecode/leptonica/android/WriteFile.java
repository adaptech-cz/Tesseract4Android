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

import java.io.File;

/**
 * @author alanv@google.com (Alan Viverette)
 */
@SuppressWarnings("WeakerAccess")
public class WriteFile {
	static {
		System.loadLibrary("jpeg");
		System.loadLibrary("pngx");
		System.loadLibrary("leptonica");
	}

	/**
	 * Write an 8bpp Pix to a flat byte array.
	 *
	 * @param pixs The 8bpp source image.
	 * @return a byte array where each byte represents a single 8-bit pixel
	 */
	public static byte[] writeBytes8(Pix pixs) {
		if (pixs == null)
			throw new IllegalArgumentException("Source pix must be non-null");

		int size = pixs.getWidth() * pixs.getHeight();

		byte[] data = new byte[size];

		if (pixs.getDepth() != 8) {
			Pix pix8 = Convert.convertTo8(pixs);
			writeBytes8(pix8, data);
			pix8.recycle();
		} else {
			writeBytes8(pixs, data);
		}

		return data;
	}

	/**
	 * Write an 8bpp Pix to a flat byte array.
	 *
	 * @param pixs The 8bpp source image.
	 * @param data A byte array large enough to hold the pixels of pixs.
	 * @return the number of bytes written to data
	 */
	@SuppressWarnings("UnusedReturnValue")
	public static int writeBytes8(Pix pixs, byte[] data) {
		if (pixs == null)
			throw new IllegalArgumentException("Source pix must be non-null");

		int size = pixs.getWidth() * pixs.getHeight();

		if (data.length < size)
			throw new IllegalArgumentException("Data array must be large enough to hold image bytes");

		return nativeWriteBytes8(pixs.getNativePix(), data);
	}

	/**
	 * Writes a Pix to file using the file extension as the output format;
	 * supported formats are .bmp, .jpg, and .png.
	 * <p>
	 * Notes:
	 * <ol>
	 * <li>This determines the output format from the filename extension.
	 * <li>The last two args are ignored except for requests for jpeg files.</li>
	 * <li>The jpeg default quality is 75.</li>
	 * </ol>
	 *
	 * @param pixs        Source image.
	 * @param file        The file to write.
	 * @param quality     Compression quality (between 1 - 100, 0 = default). (only for JPEG files)
	 * @param progressive Progressive format. (only for JPEG files)
	 * @return <code>true</code> on success
	 */
	public static boolean writeImpliedFormat(Pix pixs, File file, int quality, boolean progressive) {
		if (pixs == null)
			throw new IllegalArgumentException("Source pix must be non-null");
		if (file == null)
			throw new IllegalArgumentException("File must be non-null");

		return nativeWriteImpliedFormat(pixs.getNativePix(),
				file.getAbsolutePath(), quality, progressive);
	}

	/**
	 * Writes a Pix to file using the file extension as the output format;
	 * supported formats are .bmp, .jpg, and .png.
	 * Jpeg files will have quality 75 and will be not progressive.
	 *
	 * @param pixs Source image.
	 * @param file The file to write.
	 * @return <code>true</code> on success
	 */
	public static boolean writeImpliedFormat(Pix pixs, File file) {
		return writeImpliedFormat(pixs, file, 0, false);
	}

	/**
	 * Writes a Pix to an Android Bitmap object. The output Bitmap will always
	 * be in ARGB_8888 format, but the input Pixs may be any bit-depth.
	 *
	 * @param pixs The source image.
	 * @return a Bitmap containing a copy of the source image, or <code>null
	 * </code> on failure
	 */
	public static Bitmap writeBitmap(Pix pixs) {
		if (pixs == null)
			throw new IllegalArgumentException("Source pix must be non-null");

		final int[] dimensions = pixs.getDimensions();
		if (dimensions != null) {
			final int width = dimensions[Pix.INDEX_W];
			final int height = dimensions[Pix.INDEX_H];

			final Bitmap.Config config = Bitmap.Config.ARGB_8888;
			final Bitmap bitmap = Bitmap.createBitmap(width, height, config);

			if (nativeWriteBitmap(pixs.getNativePix(), bitmap)) {
				return bitmap;
			}

			bitmap.recycle();
		}
		return null;
	}

	// ***************
	// * NATIVE CODE *
	// ***************

	private static native int nativeWriteBytes8(long nativePix, byte[] data);

	private static native boolean nativeWriteImpliedFormat(long nativePix, String fileName, int quality, boolean progressive);

	private static native boolean nativeWriteBitmap(long nativePix, Bitmap bitmap);
}
