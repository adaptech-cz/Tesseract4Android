package com.googlecode.tesseract.android;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Assets {

	/**
	 * Returns directory which contains the "tessdata" directory.
	 */
	@NonNull
	public static String getTessDataPath(@NonNull Context context) {
		// For testing we return app's external files dir, which is also directly
		// accessible when connected to the computer.
		// In this case it is: <sdcard>/Android/data/cz.adaptech.tesseract4android.test/
		return context.getExternalFilesDir(null).getAbsolutePath();
	}

	/**
	 * Returns directory where tests can write some data.
	 */
	@NonNull
	public static String getOutputPath(@NonNull Context context) {
		// For testing we return app's external files dir, which is also directly
		// accessible when connected to the computer.
		// In this case it is: <sdcard>/Android/data/cz.adaptech.tesseract4android.test/

		// Note we return same directory as what getTessDataPath() returns, because tess data
		// creates own "tessdata" subdirectory there, so there won't be any conflict.
		return context.getExternalFilesDir(null).getAbsolutePath();
	}

	@NonNull
	public static String getLanguage() {
		return "eng";
	}

	public static void extractAssets(@NonNull Context context) {
		AssetManager am = context.getAssets();

		File tessDir = new File(getTessDataPath(context), "tessdata");
		if (!tessDir.exists()) {
			tessDir.mkdir();
		}
		File engFile = new File(tessDir, "eng.traineddata");
		if (!engFile.exists()) {
			copyFile(am, "eng.traineddata", engFile);
		}
	}

	private static void copyFile(@NonNull AssetManager am, @NonNull String assetName,
								 @NonNull File outFile) {
		try (
				InputStream in = am.open(assetName);
				OutputStream out = new FileOutputStream(outFile)
		) {
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
