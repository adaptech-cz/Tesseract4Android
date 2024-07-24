package cz.adaptech.tesseract4android.sample.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.googlecode.tesseract.android.TessBaseAPI
import cz.adaptech.tesseract4android.sample.Assets
import cz.adaptech.tesseract4android.sample.Assets.extractAssets
import cz.adaptech.tesseract4android.sample.Assets.getTessDataPath
import cz.adaptech.tesseract4android.sample.Config
import cz.adaptech.tesseract4android.sample.OCRState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * View Model for Main View.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
	/**
	 * Tesseract API
	 */
	private val tessApi: TessBaseAPI

	/**
	 * Is the OCR in progress?
	 */
	private val processing = MutableStateFlow(false)

	/**
	 * The current state of the OCR
	 */
	private val _progress = MutableStateFlow<OCRState>(OCRState.Loading)

	/**
	 * The resulting text from the OCR.
	 */
	private val _result = MutableStateFlow("")

	/**
	 * Has the tesseract API been initialized?
	 */
	private var isInitialized = false

	/**
	 * If the OCR has been stopped by the user or not.
	 */
	private var stopped: Boolean = false

	/**
	 * Holds the bitmap of the sample image.
	 */
	private val _image = MutableStateFlow<Bitmap?>(null)

	/**
	 * Immutable version for view access.
	 */
	val status: StateFlow<OCRState> = _progress

	/**
	 * Immutable version for view access.
	 */
	val result: StateFlow<String> = _result

	/**
	 * Is the start button enabled or not.
	 */
	val isStartEnabled: StateFlow<Boolean> = processing.map { !it }
		.stateIn(viewModelScope, SharingStarted.Lazily, false)

	/**
	 * Is the stop button enabled or not.
	 */
	val isStopEnabled: StateFlow<Boolean> = processing

	/**
	 * Converts the sample image into an ImageBitmap for UI
	 */
	val image: StateFlow<ImageBitmap?> = _image.map {
		it?.asImageBitmap()
	}.stateIn(viewModelScope, SharingStarted.Eagerly, null)

	init {
		// Instantiate the API
		tessApi = TessBaseAPI { progressValues: TessBaseAPI.ProgressValues ->
			_progress.tryEmit(OCRState.Progress(progressValues.percent))
		}

		// IO Tasks
		viewModelScope.launch(Dispatchers.IO) {
			// Copy sample image and language data to storage
			extractAssets(application)

			// Load the image
			_image.emit(Assets.getImageBitmap(application))

			// Initialize tesseract
			initTesseract(getTessDataPath(application), Config.TESS_LANG, Config.TESS_ENGINE)
		}

		// Show Tesseract version and library flavor at startup
		_progress.value = OCRState.StartUp(tessApi.version, tessApi.libraryFlavor)
	}

	override fun onCleared() {
		tessApi.stop()
		tessApi.recycle()
	}

	private fun initTesseract(dataPath: String, language: String, engineMode: Int) {
		Log.i(
			TAG, "Initializing Tesseract with: dataPath = [" + dataPath + "], " +
					"language = [" + language + "], engineMode = [" + engineMode + "]"
		)
		try {
			this.isInitialized = tessApi.init(dataPath, language, engineMode)
		} catch (e: IllegalArgumentException) {
			this.isInitialized = false
			Log.e(TAG, "Cannot initialize Tesseract:", e)
		}
	}

	private fun recognizeImage() {
		if (!this.isInitialized) {
			Log.e(TAG, "recognizeImage: Tesseract is not initialized")
			return
		}
		if (processing.value) {
			Log.e(TAG, "recognizeImage: Processing is in progress")
			return
		}
		_result.value = ""
		processing.value = true
		_progress.value = OCRState.Processing
		stopped = false

		// Start process in another thread
		viewModelScope.launch(Dispatchers.IO) {
			tessApi.setImage(_image.value!!)
			// Or set it via a File.
			// tessApi.setImage(imageFile);
			val startTime = SystemClock.uptimeMillis()

			// Use getHOCRText(0) method to trigger recognition with progress notifications and
			// ability to cancel ongoing processing.
			tessApi.getHOCRText(0)

			// At this point the recognition has completed (or was interrupted by calling stop())
			// and we can get the results we want. In this case just normal UTF8 text.
			//
			// Note that calling only this method (without the getHOCRText() above) would also
			// trigger the recognition and return the same result, but we would received no progress
			// notifications and we wouldn't be able to stop() the ongoing recognition.
			val text = tessApi.utF8Text

			// We can free up the recognition results and any stored image data in the tessApi
			// if we don't need them anymore.
			tessApi.clear()

			// Publish the results
			_result.emit(text)
			processing.emit(false)
			if (stopped) {
				_progress.emit(OCRState.Stopped)
			} else {
				val duration = SystemClock.uptimeMillis() - startTime
				_progress.emit(OCRState.Finished(duration / 1000f))
			}
		}
	}

	/**
	 * Stops the OCR.
	 */
	fun stop() {
		if (!processing.value) {
			return
		}
		_progress.value = OCRState.Stopping
		stopped = true
		tessApi.stop()
	}

	/**
	 * Start the OCR
	 */
	fun start() {
		recognizeImage()
	}

	companion object {
		private const val TAG = "MainViewModel"
	}
}