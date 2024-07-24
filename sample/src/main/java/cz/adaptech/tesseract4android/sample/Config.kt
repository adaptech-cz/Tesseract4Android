package cz.adaptech.tesseract4android.sample

import com.googlecode.tesseract.android.TessBaseAPI

object Config {
	const val TESS_ENGINE: Int = TessBaseAPI.OEM_LSTM_ONLY

	const val TESS_LANG: String = "eng"

	const val IMAGE_NAME: String = "sample.jpg"
}
