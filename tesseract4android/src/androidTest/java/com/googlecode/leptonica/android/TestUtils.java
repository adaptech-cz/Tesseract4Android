/*
 * Copyright (C) 2019 Adaptech s.r.o., Robert Pösel
 * Copyright (C) 2012 Google Inc.
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.ColorInt;

/**
 * Utility methods for running Leptonica unit tests.
 *
 * @author alanv@google.com (Alan Viverette)
 */
@SuppressWarnings("WeakerAccess")
public class TestUtils {
	private static final String TAG = TestUtils.class.getSimpleName();
	private static final boolean LOG_DIFFERENCES = false;

	public static float compareBitmaps(Bitmap a, Bitmap b) {
		return compareBitmaps(a, b, 0);
	}

	public static float compareBitmaps(Bitmap a, Bitmap b, int tolerance) {
		int found = 0;

		for (int y = 0; y < a.getHeight(); y++) {
			for (int x = 0; x < a.getWidth(); x++) {
				int colorA = a.getPixel(x, y);
				int colorB = b.getPixel(x, y);

				if (compareColors(colorA, colorB, tolerance)) {
					found++;
				} else if (LOG_DIFFERENCES) {
					Log.v(TAG, String.format("compareBitmaps: Different pixel at [%d, %d]: a=%08X != b=%08X", x, y, colorA, colorB));
				}
			}
		}

		return found / (float) (a.getWidth() * a.getHeight());
	}

	public static float compareImages(Pix a, Bitmap b) {
		return compareImages(a, b, 0);
	}

	public static float compareImages(Pix a, Bitmap b, int tolerance) {
		int found = 0;

		for (int y = 0; y < a.getHeight(); y++) {
			for (int x = 0; x < a.getWidth(); x++) {
				int colorA = a.getPixel(x, y);
				int colorB = b.getPixel(x, y);

				if (compareColors(colorA, colorB, tolerance)) {
					found++;
				} else if (LOG_DIFFERENCES) {
					Log.v(TAG, String.format("compareImages: Different pixel at [%d, %d]: a=%08X != b=%08X", x, y, colorA, colorB));
				}
			}
		}

		return found / (float) (a.getWidth() * a.getHeight());
	}

	public static float comparePix(Pix a, Pix b) {
		return comparePix(a, b, 0);
	}

	public static float comparePix(Pix a, Pix b, int tolerance) {
		int found = 0;

		for (int y = 0; y < a.getHeight(); y++) {
			for (int x = 0; x < a.getWidth(); x++) {
				int colorA = a.getPixel(x, y);
				int colorB = b.getPixel(x, y);

				if (compareColors(colorA, colorB, tolerance)) {
					found++;
				} else if (LOG_DIFFERENCES) {
					Log.v(TAG, String.format("comparePix: Different pixel at [%d, %d]: a=%08X != b=%08X", x, y, colorA, colorB));
				}
			}
		}

		return found / (float) (a.getWidth() * a.getHeight());
	}

	private static boolean compareColors(@ColorInt int color1, @ColorInt int color2, int tolerance) {
		if (tolerance == 0) {
			return color1 == color2;
		}
		int r1 = Color.red(color1);
		int g1 = Color.green(color1);
		int b1 = Color.blue(color1);
		int a1 = Color.alpha(color1);

		int r2 = Color.red(color2);
		int g2 = Color.green(color2);
		int b2 = Color.blue(color2);
		int a2 = Color.alpha(color2);

		return Math.abs(r1 - r2) <= tolerance
				&& Math.abs(g1 - g2) <= tolerance
				&& Math.abs(b1 - b2) <= tolerance
				&& Math.abs(a1 - a2) <= tolerance;
	}

	public static Bitmap createTestBitmap(int width, int height, Bitmap.Config format) {
		Bitmap bmp = Bitmap.createBitmap(width, height, format);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();
		paint.setStyle(Style.FILL);

		if (width > 1 && height > 1) {
			// Paint the top left half white
			paint.setColor(Color.WHITE);
			canvas.drawRect(new Rect(0, 0, width / 2, height / 2), paint);

			// Paint the top right half black
			paint.setColor(Color.BLACK);
			canvas.drawRect(new Rect(width / 2, 0, width, height / 2), paint);

			// Paint the bottom left some color
			paint.setColor(Color.rgb(32, 64, 128));
			canvas.drawRect(new Rect(0, width / 2, width / 2, height), paint);

			// Paint the bottom right some other color
			paint.setColor(Color.rgb(128, 64, 32));
			canvas.drawRect(new Rect(width / 2, width / 2, width, height), paint);
		} else {
			// Paint the image white
			paint.setColor(Color.rgb(128, 64, 16));
			canvas.drawPaint(paint);
		}
		return bmp;
	}

	public static Pix createTestPix(int width, int height) {
		Bitmap bmp = TestUtils.createTestBitmap(width, height, Bitmap.Config.ARGB_8888);
		return ReadFile.readBitmap(bmp);
	}
}