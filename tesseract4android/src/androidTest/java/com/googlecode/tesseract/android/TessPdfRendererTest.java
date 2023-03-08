/*
 * Copyright (C) 2019 Adaptech s.r.o., Robert Pösel
 * Copyright 2015 Robert Theis
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

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class TessPdfRendererTest {

	private String tessDataPath;
	private String language;
	private String outputPath;

	@Before
	public void setup() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

		// Copy language data to storage
		Assets.extractAssets(context);

		tessDataPath = Assets.getTessDataPath(context);
		language = Assets.getLanguage();
		outputPath = Assets.getOutputPath(context);
	}

	@Test
	public void testCreate() {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		String pdfBasename = "testCreate";

		// Attempt to create a TessPdfRenderer instance.
		TessPdfRenderer pdfRenderer = new TessPdfRenderer(baseApi, outputPath
				+ pdfBasename);

		pdfRenderer.recycle();
		baseApi.recycle();
	}

	@Test
	public void testAddPageToDocument() throws IOException {
		// Attempt to initialize the API.
		final TessBaseAPI baseApi = new TessBaseAPI();
		boolean success = baseApi.init(tessDataPath, language);
		assertTrue(success);

		String pdfBasename = "testAddPageToDocument";

		// Attempt to create a TessPdfRenderer instance.
		TessPdfRenderer pdfRenderer = new TessPdfRenderer(baseApi, outputPath
				+ pdfBasename);

		// Start the PDF writing process.
		boolean beginSuccess = baseApi.beginDocument(pdfRenderer, "title");
		assertTrue(beginSuccess);

		// Add a page to the PDF.
		final Pix pixOne = getTextImage("page one", 640, 480);
		final File fileOne = File.createTempFile("testPageOne", ".png");
		WriteFile.writeImpliedFormat(pixOne, fileOne);
		boolean addedPageOne = baseApi.addPageToDocument(pixOne,
				fileOne.getAbsolutePath(), pdfRenderer);
		assertTrue(addedPageOne);

		// Add a second page.
		final Pix pixTwo = getTextImage("page two", 640, 480);
		final File fileTwo = File.createTempFile("testPageTwo", ".jpg");
		WriteFile.writeImpliedFormat(pixTwo, fileTwo);
		boolean addedPageTwo = baseApi.addPageToDocument(pixTwo,
				fileTwo.getAbsolutePath(), pdfRenderer);
		assertTrue(addedPageTwo);

		// Finish writing to the PDF document.
		boolean endSuccess = baseApi.endDocument(pdfRenderer);
		assertTrue(endSuccess);

		// Ensure that a PDF file was created.
		File pdf = new File(outputPath + pdfBasename + ".pdf");
		assertTrue(pdf.isFile());
		assertTrue(pdf.length() > 0);

		pdfRenderer.recycle();
		baseApi.recycle();
		pixOne.recycle();
		pixTwo.recycle();
	}

	private static Pix getTextImage(String text, int width, int height) {
		final Bitmap bmp = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		final Paint paint = new Paint();
		final Canvas canvas = new Canvas(bmp);

		canvas.drawColor(Color.WHITE);

		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(24.0f);
		canvas.drawText(text, width / 2, height / 2, paint);

		return ReadFile.readBitmap(bmp);
	}

}