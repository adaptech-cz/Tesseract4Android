/*
 * Copyright (C) 2019 Adaptech s.r.o., Robert Pösel
 * Copyright (C) 2010 Google Inc.
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

/**
 * Image rotation and skew detection methods.
 *
 * @author alanv@google.com (Alan Viverette)
 */
@SuppressWarnings("WeakerAccess")
public class Skew {
	static {
		System.loadLibrary("jpeg");
		System.loadLibrary("pngx");
		System.loadLibrary("leptonica");
	}

	// Text alignment defaults

	/**
	 * Default range for sweep, will detect rotation of + or - 30 degrees.
	 */
	public final static float SWEEP_RANGE = 30.0f;

	/**
	 * Default sweep delta, reasonably accurate within 0.05 degrees.
	 */
	public final static float SWEEP_DELTA = 5.0f;

	/**
	 * Default sweep reduction, one-eighth the size of the original image.
	 */
	public final static int SWEEP_REDUCTION = 8;

	/**
	 * Default sweep reduction, one-fourth the size of the original image.
	 */
	public final static int SEARCH_REDUCTION = 4;

	/**
	 * Default search minimum delta, reasonably accurate within 0.05 degrees.
	 */
	public final static float SEARCH_MIN_DELTA = 0.01f;

	/**
	 * Finds and returns the skew angle using default parameters.
	 *
	 * @param pixs Input pix (1 bpp).
	 * @return the detected skew angle, or 0.0 on failure
	 */
	public static float findSkew(Pix pixs) {
		return findSkew(pixs, SWEEP_RANGE, SWEEP_DELTA, SWEEP_REDUCTION, SEARCH_REDUCTION,
				SEARCH_MIN_DELTA);
	}

	/**
	 * Finds and returns the skew angle, doing first a sweep through a set of
	 * equal angles, and then doing a binary search until convergence.
	 * <p>
	 * Notes:
	 * <ol>
	 * <li>In computing the differential line sum variance score, we sum the
	 * result over scanlines, but we always skip:
	 * <ul>
	 * <li>at least one scanline
	 * <li>not more than 10% of the image height
	 * <li>not more than 5% of the image width
	 * </ul>
	 * </ol>
	 *
	 * @param pixs            Input pix (1 bpp).
	 * @param sweepRange      Half the full search range, assumed about 0; in
	 *                        degrees.
	 * @param sweepDelta      Angle increment of sweep; in degrees.
	 * @param sweepReduction  Sweep reduction factor = 1, 2, 4 or 8.
	 * @param searchReduction Binary search reduction factor = 1, 2, 4 or 8; and
	 *                        must not exceed sweepReduction.
	 * @param searchMinDelta  Minimum binary search increment angle; in degrees.
	 * @return the detected skew angle, or 0.0 on failure
	 */
	public static float findSkew(Pix pixs, float sweepRange, float sweepDelta, int sweepReduction,
								 int searchReduction, float searchMinDelta) {
		if (pixs == null)
			throw new IllegalArgumentException("Source pix must be non-null");

		return nativeFindSkew(pixs.getNativePix(), sweepRange, sweepDelta,
				sweepReduction, searchReduction, searchMinDelta);
	}

	/**
	 * Finds the skew angle and deskews the image using default parameters.
	 * <p>
	 * Notes:
	 * <ol>
	 * <li>If the angle is large enough and there is sufficient confidence,
	 * it returns a deskewed image; otherwise, it returns a clone.
	 * </ol>
	 *
	 * @param pixs   Input pix (any depth).
	 * @param result (Optional) A float array with at least two elements.
	 *               At [0] will be angle required to deskew, in degrees;
	 *               At [1] will be confidence as a ratio of max/min scores.
	 * @return the deskewed pix, or <code>null</code> on error
	 */
	public static Pix deskew(Pix pixs, float[] result) {
		return deskew(pixs, 0, SWEEP_RANGE, SWEEP_DELTA, 0, 0, result);
	}

	/**
	 * Finds the skew angle and deskews the image. Optionally gets the used angle and confidence.
	 * <p>
	 * Notes:
	 * <ol>
	 * <li>This binarizes if necessary and finds the skew angle. If the
	 * angle is large enough and there is sufficient confidence,
	 * it returns a deskewed image; otherwise, it returns a clone.
	 * <li>Typical values at 300 ppi for redSearch are 2 and 4.
	 * At 75 ppi, one should use redSearch = 1.
	 * </ol>
	 *
	 * @param pixs            Input pix (any depth).
	 * @param sweepReduction  For linear search: reduction factor = 1, 2 or 4;
	 *                        use 0 for default.
	 * @param sweepRange      In degrees in each direction from 0;
	 *                        use 0.0 for default.
	 * @param sweepDelta      In degrees; use 0.0 for default.
	 * @param searchReduction For binary search: reduction factor = 1, 2 or 4;
	 *                        use 0 for default.
	 * @param threshold       For binarizing the image; use 0 for default.
	 * @param result          (Optional) A float array with at least two elements.
	 *                        At [0] will be angle required to deskew, in degrees;
	 *                        At [1] will be confidence as a ratio of max/min scores.
	 * @return the deskewed pix, or NULL on error
	 */
	public static Pix deskew(Pix pixs, int sweepReduction, float sweepRange, float sweepDelta,
							 int searchReduction, int threshold, float[] result) {
		if (pixs == null)
			throw new IllegalArgumentException("Source pix must be non-null");

		long nativePix = nativeDeskew(pixs.getNativePix(), sweepReduction, sweepRange, sweepDelta,
				searchReduction, threshold, result);

		if (nativePix == 0)
			throw new RuntimeException("Failed to deskew pix");

		return new Pix(nativePix);
	}

	// ***************
	// * NATIVE CODE *
	// ***************

	private static native float nativeFindSkew(long nativePix, float sweepRange, float sweepDelta,
											   int sweepReduction, int searchReduction, float searchMinDelta);

	private static native long nativeDeskew(long nativePix, int sweepReduction, float sweepRange,
											float sweepDelta, int searchReduction, int threshold,
											float[] result);

}
