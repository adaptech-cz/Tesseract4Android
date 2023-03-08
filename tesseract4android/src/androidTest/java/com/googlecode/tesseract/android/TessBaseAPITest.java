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

package com.googlecode.tesseract.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Pair;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel;
import com.googlecode.tesseract.android.TessBaseAPI.ProgressNotifier;
import com.googlecode.tesseract.android.TessBaseAPI.ProgressValues;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class TessBaseAPITest {

	private static final int DEFAULT_PAGE_SEG_MODE = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK;

	private String tessDataPath;
	private String language;

	@Before
	public void setup() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

		// Copy language data to storage
		Assets.extractAssets(context);

		tessDataPath = Assets.getTessDataPath(context);
		language = Assets.getLanguage();
	}

	@Test
	public void testChoiceIterator() {
		final String inputText = "hello";
		final Bitmap bmp = TessBaseAPITest.getTextImage(inputText, 640, 480);

		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
		baseApi.setVariable(TessBaseAPI.VAR_SAVE_BLOB_CHOICES, TessBaseAPI.VAR_TRUE);

		// Ensure that text is recognized.
		baseApi.setImage(bmp);
		String recognizedText = baseApi.getUTF8Text();
		assertTrue("No recognized text found.", recognizedText != null && !recognizedText.equals(""));

		// Iterate through the results.
		ResultIterator iterator = baseApi.getResultIterator();
		List<Pair<String, Double>> choicesAndConfidences;
		iterator.begin();
		do {
			choicesAndConfidences = iterator.getSymbolChoicesAndConfidence();
			assertNotNull("Invalid result.", choicesAndConfidences);

			for (Pair<String, Double> choiceAndConfidence : choicesAndConfidences) {
				String choice = choiceAndConfidence.first;
				Double conf = choiceAndConfidence.second;
				assertTrue("No choice value found.", choice != null && !choice.equals(""));
				assertTrue("Found an incorrect confidence value.", conf >= 0 && conf <= 100);
			}
		} while (iterator.next(PageIteratorLevel.RIL_SYMBOL));
		iterator.delete();

		assertNotNull("No ChoiceIterator values found.", choicesAndConfidences);

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	private static Bitmap getTextImage(String text, int width, int height) {
		final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		final Canvas canvas = new Canvas(bmp);
		canvas.drawColor(Color.WHITE);
		drawTextNewLines(text, canvas);

		return bmp;
	}

	/**
	 * Draws text (with newlines) centered onto the canvas. If the text does not fit horizontally,
	 * it will be cut off. If the text does not fit vertically, the start of the text will be at
	 * the top of the image and whatever not fitting onto the image being cut off. If the text
	 * fits vertically it will be centered vertically.
	 *
	 * @param text   String to draw onto the canvas
	 * @param canvas Canvas to draw text onto
	 */
	private static void drawTextNewLines(String text, Canvas canvas) {
		final Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(24.0f);

		String[] textArray = text.split("\n");
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		int count = textArray.length;
		int lineSize = (int) (paint.descent() - paint.ascent());
		int maxLinesToPushUp = height / lineSize;
		maxLinesToPushUp = count < maxLinesToPushUp ? count : maxLinesToPushUp;
		int pixelsToPushUp = (maxLinesToPushUp - 1) / 2 * lineSize;

		int x = width / 2;
		int y = (height / 2) - pixelsToPushUp;

		for (String line : textArray) {
			canvas.drawText(line, x, y, paint);
			y += lineSize;
		}
	}

	@Test
	public void testClear() {
		final String inputText = "hello";
		final Bitmap bmp = getTextImage(inputText, 640, 480);

		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
		baseApi.setImage(bmp);

		// Ensure that the getUTF8Text() operation fails after clear() is called.
		baseApi.clear();
		String text = baseApi.getUTF8Text();

		assertNull("Received non-null result after clear().", text);

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testRecycle() {
		final String inputText = "hello";
		final Bitmap bmp = getTextImage(inputText, 640, 480);

		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
		baseApi.setImage(bmp);

		// Ensure that getUTF8Text() fails after recycle() is called.
		baseApi.recycle();
		try {
			baseApi.getUTF8Text();
			fail("IllegalStateException not thrown");
		} catch (IllegalStateException e) {
			// Continue
		} finally {
			bmp.recycle();
		}

		// Ensure that init() fails after recycle() is called.
		try {
			baseApi.init(tessDataPath, language);
			fail("IllegalStateException not thrown");
		} catch (IllegalStateException e) {
			// Continue
		} finally {
			bmp.recycle();
		}
	}

	@Test
	public void testGetHOCRText_combined() {
		testGetHOCRText(language, TessBaseAPI.OEM_TESSERACT_LSTM_COMBINED);
	}

	@Test
	public void testGetHOCRText_lstm() {
		testGetHOCRText(language, TessBaseAPI.OEM_LSTM_ONLY);
	}

	@Test
	public void testGetHOCRText_tesseract() {
		testGetHOCRText(language, TessBaseAPI.OEM_TESSERACT_ONLY);
	}

	private void testGetHOCRText(String language, int ocrEngineMode) {
		final String inputText = "hello";
		final Bitmap bmp = getTextImage(inputText, 640, 480);

		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language, ocrEngineMode);
		assertTrue(success);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
		baseApi.setImage(bmp);

		// Ensure that getHOCRText() produces a result.
		final String hOcr = baseApi.getHOCRText(0);
		assertNotNull("HOCR result not found.", hOcr);
		assertTrue(hOcr.length() > 0);

		final String outputText = Html.fromHtml(hOcr).toString().trim();
		assertEquals(inputText, outputText);

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testGetInitLanguagesAsString() {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		// Check the last-used language code.
		String lang = baseApi.getInitLanguagesAsString();
		assertEquals("Got incorrect init languages value.", lang, language);

		// Attempt to shut down the API.
		baseApi.recycle();
	}

	@Test
	public void testGetThresholdedImage() {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		// Set the image to a Bitmap.
		final Bitmap bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
		baseApi.setImage(bmp);

		// Check the size of the thresholded image.
		Pix pixd = baseApi.getThresholdedImage();
		assertNotNull("Thresholded image is null.", pixd);
		assertEquals(bmp.getWidth(), pixd.getWidth());
		assertEquals(bmp.getHeight(), pixd.getHeight());

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
		pixd.recycle();
	}

	@Test
	public void testGetUTF8Text_combined() {
		testGetUTF8Text(language, TessBaseAPI.OEM_TESSERACT_LSTM_COMBINED);
	}

	@Test
	public void testGetUTF8Text_lstm() {
		testGetUTF8Text(language, TessBaseAPI.OEM_LSTM_ONLY);
	}

	@Test
	public void testGetUTF8Text_tesseract() {
		testGetUTF8Text(language, TessBaseAPI.OEM_TESSERACT_ONLY);
	}

	private void testGetUTF8Text(String language, int ocrEngineMode) {
		final String inputText = "hello";
		final Bitmap bmp = getTextImage(inputText, 640, 480);

		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language, ocrEngineMode);
		assertTrue(success);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
		baseApi.setImage(bmp);

		// Ensure that the result is correct.
		final String outputText = baseApi.getUTF8Text();
		assertEquals("\"" + outputText + "\" != \"" + inputText + "\"", inputText, outputText);

		// Ensure getRegions() works.
		final Pixa regions = baseApi.getRegions();
		assertEquals("Found incorrect number of regions.", regions.size(), 1);
		regions.recycle();

		// Ensure getTextlines() works.
		final Pixa textlines = baseApi.getTextlines();
		assertEquals("Found incorrect number of textlines.", textlines.size(), 1);
		textlines.recycle();

		// Ensure getStrips() works.
		final Pixa strips = baseApi.getStrips();
		assertEquals("Found incorrect number of strips.", strips.size(), 1);
		strips.recycle();

		// Ensure getWords() works.
		final Pixa words = baseApi.getWords();
		assertEquals("Found incorrect number of words.", words.size(), 1);
		words.recycle();

		// Ensure getConnectedComponents() works.
		final Pixa connectedComponents = baseApi.getConnectedComponents();
		assertTrue("Connected components not found.", connectedComponents.size() > 0);
		connectedComponents.recycle();

		// Iterate through the results.
		final ResultIterator iterator = baseApi.getResultIterator();
		String lastUTF8Text;
		float lastConfidence;
		int[] lastBoundingBox;
		Rect lastBoundingRect;
		int count = 0;
		iterator.begin();
		do {
			lastUTF8Text = iterator.getUTF8Text(PageIteratorLevel.RIL_WORD);
			lastConfidence = iterator.confidence(PageIteratorLevel.RIL_WORD);
			lastBoundingBox = iterator.getBoundingBox(PageIteratorLevel.RIL_WORD);
			lastBoundingRect = iterator.getBoundingRect(PageIteratorLevel.RIL_WORD);
			count++;
		} while (iterator.next(PageIteratorLevel.RIL_WORD));
		iterator.delete();

		assertEquals("Found incorrect number of results.", count, 1);
		assertEquals("Found an incorrect result.", lastUTF8Text, outputText);
		assertTrue("Result was not high-confidence.", lastConfidence > 80);
		assertTrue("Result bounding box not found.", lastBoundingBox[2] > 0 && lastBoundingBox[3] > 0);

		boolean validBoundingRect = lastBoundingRect.left < lastBoundingRect.right
				&& lastBoundingRect.top < lastBoundingRect.bottom;
		assertTrue("Result bounding box Rect is incorrect.", validBoundingRect);

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testInit() {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		// Attempt to initialize the API again.
		success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		// Attempt to shut down the API.
		baseApi.recycle();
	}

	@Test
	public void testInit_ocrEngineMode() {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean result = baseApi.init(tessDataPath, language,
				TessBaseAPI.OEM_TESSERACT_ONLY);

		assertTrue("Init was unsuccessful.", result);

		// Attempt to shut down the API.
		baseApi.recycle();
	}

	@Test
	public void testInit_config() {
		final String inputText = "hello";
		final Bitmap bmp = getTextImage(inputText, 640, 480);

		// Ensure that setting the blacklist variable works.
		final String blacklistedCharacter = inputText.substring(1, 2);

		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();

		Map<String, String> config = new HashMap<>();
		config.put(TessBaseAPI.VAR_CHAR_BLACKLIST, blacklistedCharacter);

		boolean result = baseApi.init(tessDataPath, language,
				TessBaseAPI.OEM_TESSERACT_ONLY, config);

		assertTrue("Init was unsuccessful.", result);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
		baseApi.setImage(bmp);
		final String outputText = baseApi.getUTF8Text();
		assertFalse("Found a blacklisted character.", outputText.contains(blacklistedCharacter));

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testProgressValues() {
		final String inputText = "hello";
		final Bitmap bmp = getTextImage(inputText, 640, 480);
		final Rect imageBounds = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

		class Notifier implements ProgressNotifier {
			public boolean receivedProgress = false;

			@Override
			public void onProgressValues(ProgressValues progressValues) {
				receivedProgress = true;
				testProgressValues(progressValues, imageBounds);
			}
		}

		final Notifier notifier = new Notifier();

		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI(notifier);
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
		baseApi.setImage(bmp);

		// Ensure that we receive a progress callback.
		baseApi.getHOCRText(0);
		assertTrue(notifier.receivedProgress);

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testProgressValues_setRectangle() {
		class Notifier implements ProgressNotifier {
			public boolean receivedProgress = false;
			private Rect bounds;

			public void reset(Rect bounds) {
				this.bounds = bounds;
				receivedProgress = false;
			}

			@Override
			public void onProgressValues(ProgressValues progressValues) {
				receivedProgress = true;
				testProgressValues(progressValues, bounds);
			}
		}

		final Notifier notifier = new Notifier();

		final int width = 640;
		final int height = 480;
		final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		final Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(32.0f);

		// Draw separate text on the left and right halves of the image.
		final Canvas canvas = new Canvas(bmp);
		canvas.drawColor(Color.WHITE);
		final String leftInput = "A";
		final String rightInput = "B";
		canvas.drawText(leftInput, width / 4, height / 2, paint);
		canvas.drawText(rightInput, width * 3 / 4, height / 2, paint);

		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI(notifier);
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
		baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,
				leftInput + rightInput);
		baseApi.setImage(bmp);

		// Attempt to restrict recognition to a sub-rectangle of the image.
		final Rect left = new Rect(0, 0, width / 2, height);
		baseApi.setRectangle(left);
		notifier.reset(left);

		// Ensure a progress callback is received.
		baseApi.getHOCRText(0);
		assertTrue(notifier.receivedProgress);

		// Attempt to restrict recognition to a sub-rectangle of the image.
		final Rect right = new Rect(width / 2 + 5, 7, width - 5, height - 7);
		baseApi.setRectangle(right);
		notifier.reset(right);

		// Ensure a progress callback is received.
		baseApi.getHOCRText(0);
		assertTrue(notifier.receivedProgress);

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	private static void testProgressValues(ProgressValues progress, Rect bounds) {
		// Ensure that the percent progress is valid.
		assertTrue(progress.getPercent() >= 0);
		assertTrue(progress.getPercent() <= 100);

		// Ensure that the text rect is valid.
		final Rect textRect = progress.getCurrentRect();
		assertTrue(textRect.left <= textRect.right);
		assertTrue(textRect.top <= textRect.bottom);

		// Text rect must match the bounds of the image or sub-rectangle used.
		assertEquals(textRect.height(), bounds.height());
		assertEquals(textRect.width(), bounds.width());

		// Ensure that the word rect is valid.
		final Rect wordRect = progress.getCurrentWordRect();
		assertTrue(textRect.left <= textRect.right);
		assertTrue(textRect.top <= textRect.bottom);

		// Ensure the word rect falls within the text rect.
		// NOTE: Word rect will be empty during recognition's second pass.
		if (!wordRect.isEmpty()) {
			final Rect absoluteWordRect = new Rect(
					textRect.left + wordRect.left,
					textRect.top + wordRect.top,
					textRect.left + wordRect.right,
					textRect.top + wordRect.bottom);
			assertTrue(textRect.contains(absoluteWordRect));
		}
	}

	@Test
	public void testSetImage_bitmap() {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		// Set the image to a Bitmap.
		final Bitmap bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
		baseApi.setImage(bmp);

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testSetImage_file() throws IOException {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		// Create an image file.
		File file = File.createTempFile("testSetImage", ".bmp");
		FileOutputStream fileStream = new FileOutputStream(file);

		Bitmap bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
		bmp.compress(CompressFormat.JPEG, 85, fileStream);

		// Set the image to a File.
		baseApi.setImage(file);

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testSetImage_pix() throws IOException {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		// Set the image to a Pix.
		Pix pix = new Pix(640, 480, 32);
		baseApi.setImage(pix);

		// Attempt to shut down the API.
		baseApi.recycle();
		pix.recycle();
	}

	@Test
	public void testSetPageSegMode() {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		// Check the default page segmentation mode.
		assertEquals("Found unexpected default page segmentation mode.",
				baseApi.getPageSegMode(), DEFAULT_PAGE_SEG_MODE);

		// Ensure that the page segmentation mode can be changed.
		final int newPageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR;
		baseApi.setPageSegMode(newPageSegMode);
		assertEquals("Found unexpected page segmentation mode.",
				baseApi.getPageSegMode(), newPageSegMode);

		// Attempt to shut down the API.
		baseApi.recycle();
	}

	@Test
	public void testSetRectangle() {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);

		final int width = 640;
		final int height = 480;
		final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		final Paint paint = new Paint();
		final Canvas canvas = new Canvas(bmp);

		canvas.drawColor(Color.WHITE);

		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(32.0f);

		// Draw separate text on the left and right halves of the image.
		final String leftInput = "A";
		final String rightInput = "B";
		canvas.drawText(leftInput, width / 4, height / 2, paint);
		canvas.drawText(rightInput, width * 3 / 4, height / 2, paint);

		baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, leftInput + rightInput);
		baseApi.setImage(bmp);

		// Ensure the result is correct for a rectangle on the left half of the image.
		Rect left = new Rect(0, 0, width / 2, height);
		baseApi.setRectangle(left);
		String leftResult = baseApi.getUTF8Text();
		assertEquals("Found incorrect text.", leftInput, leftResult);

		// Ensure the result is correct for a rectangle on the right half of the image.
		Rect right = new Rect(width / 2, 0, width, height);
		baseApi.setRectangle(right);
		String rightResult = baseApi.getUTF8Text();
		assertEquals("Found incorrect text.", rightInput, rightResult);

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testGetVariable() {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		// Non existing variable should return null
		String nonExistingVariable = baseApi.getVariable("__non_existing_variable__");
		assertNull(nonExistingVariable);

		// Existing value should return real value
		String existingVariable = baseApi.getVariable("invert_threshold");
		assertNotNull(existingVariable);

		// Attempt to shut down the API.
		baseApi.recycle();
	}

	@Test
	public void testSetVariable() {
		final String inputText = "hello";
		final Bitmap bmp = getTextImage(inputText, 640, 480);

		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);

		// Ensure that setting the blacklist variable works.
		final String blacklistedCharacter = inputText.substring(1, 2);
		baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, blacklistedCharacter);
		baseApi.setImage(bmp);
		final String outputText = baseApi.getUTF8Text();
		assertFalse("Found a blacklisted character.", outputText.contains(blacklistedCharacter));

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testStop() throws InterruptedException {

		StringBuilder inputTextBuilder = new StringBuilder();
		for (int i = 0; i < 50; i++) {
			inputTextBuilder.append("The quick brown fox jumps over the lazy dog.\n");
		}
		final Bitmap bmp = getTextImage(inputTextBuilder.toString(), 640, 1000);

		final Semaphore progressSem = new Semaphore(0);
		final TessBaseAPI baseApi = new TessBaseAPI(new ProgressNotifier() {
			@Override
			public void onProgressValues(ProgressValues progressValues) {
				if (progressValues.getPercent() > 50) {
					fail("OCR recognition was too fast, try to increase the image size and amount of text?");
				}
				if (progressValues.getPercent() > 0) {
					progressSem.release();
				}
			}
		});

		class LongRecognitionTask extends AsyncTask<Void, Void, Void> {
			@Override
			protected Void doInBackground(Void... params) {
				baseApi.getHOCRText(0);
				progressSem.release();
				return null;
			}
		}

		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);
		baseApi.setImage(bmp);

		LongRecognitionTask task = new LongRecognitionTask();
		task.execute();

		// Wait for recognition to start
		progressSem.acquire();

		baseApi.stop();

		// Wait for getHOCRText() to complete, otherwise we may end() and recycle baseApi before
		// getHOCRText() finishes execution on the AsyncTask thread and cause an exception
		progressSem.acquire();

		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testWordConfidences() {
		final String inputText = "one two three";
		final Bitmap bmp = getTextImage(inputText, 640, 480);

		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);

		baseApi.setImage(bmp);
		String text = baseApi.getUTF8Text();

		assertNotNull("Recognized text is null.", text);

		// Ensure that a mean confidence value is returned.
		int conf = baseApi.meanConfidence();
		boolean validConf = conf > 0 && conf <= 100;
		assertTrue("Mean confidence value is incorrect.", validConf);

		// Ensure that word confidence values are returned.
		int numWords = text.split("\\s+").length;
		int[] wordConf = baseApi.wordConfidences();
		assertEquals("Found the wrong number of word confidence values.", numWords, wordConf.length);
		for (int confidence : wordConf) {
			boolean valid = 0 <= confidence && confidence <= 100;
			assertTrue("Found an invalid word confidence value.", valid);
		}

		// Attempt to shut down the API.
		baseApi.recycle();
		bmp.recycle();
	}

	@Test
	public void testGetVersion() {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();

		String version = baseApi.getVersion();
		assertNotNull("Version returned null", version);

		// Attempt to shut down the API.
		baseApi.recycle();
	}
}