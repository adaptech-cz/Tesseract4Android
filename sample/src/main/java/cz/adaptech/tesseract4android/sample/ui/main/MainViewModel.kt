package cz.adaptech.tesseract4android.sample.ui.main

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.util.Locale
import kotlin.concurrent.Volatile

class MainViewModel(application: Application) : AndroidViewModel(application) {
	private val tessApi: TessBaseAPI

	private val processing = MutableLiveData(false)

	private val progress = MutableLiveData<String>()

	private val result = MutableLiveData<String>()

	var isInitialized: Boolean = false
		private set

	@Volatile
	private var stopped = false

	@Volatile
	private var tessProcessing = false

	@Volatile
	private var recycleAfterProcessing = false

	private val recycleLock = Any()

	init {
		tessApi = TessBaseAPI { progressValues: TessBaseAPI.ProgressValues ->
			progress.postValue("Progress: " + progressValues.percent + " %")
		}

		// Show Tesseract version and library flavor at startup
		progress.value = String.format(
			Locale.ENGLISH, "Tesseract %s (%s)",
			tessApi.version, tessApi.libraryFlavor
		)
	}

	override fun onCleared() {
		synchronized(recycleLock) {
			if (tessProcessing) {
				// Processing is active, set flag to recycle tessApi after processing is completed
				recycleAfterProcessing = true
				// Stop the processing as we don't care about the result anymore
				tessApi.stop()
			} else {
				// No ongoing processing, we must recycle it here
				tessApi.recycle()
			}
		}
	}

	fun initTesseract(dataPath: String, language: String, engineMode: Int) {
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

	fun recognizeImage(imagePath: File) {
		if (!this.isInitialized) {
			Log.e(TAG, "recognizeImage: Tesseract is not initialized")
			return
		}
		if (tessProcessing) {
			Log.e(TAG, "recognizeImage: Processing is in progress")
			return
		}
		tessProcessing = true

		result.value = ""
		processing.value = true
		progress.value = "Processing..."
		stopped = false

		// Start process in another thread
		Thread {
			tessApi.setImage(imagePath)
			// Or set it as Bitmap, Pix,...
			// tessApi.setImage(imageBitmap);
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
			result.postValue(text)
			processing.postValue(false)
			if (stopped) {
				progress.postValue("Stopped.")
			} else {
				val duration = SystemClock.uptimeMillis() - startTime
				progress.postValue(
					String.format(
						Locale.ENGLISH,
						"Completed in %.3fs.", (duration / 1000f)
					)
				)
			}
			synchronized(recycleLock) {
				tessProcessing = false
				// Recycle the instance here if the view model is already destroyed
				if (recycleAfterProcessing) {
					tessApi.recycle()
				}
			}
		}.start()
	}

	fun stop() {
		if (!tessProcessing) {
			return
		}
		progress.value = "Stopping..."
		stopped = true
		tessApi.stop()
	}

	fun getProcessing(): LiveData<Boolean> {
		return processing
	}

	fun getProgress(): LiveData<String> {
		return progress
	}

	fun getResult(): LiveData<String> {
		return result
	}

	companion object {
		private const val TAG = "MainViewModel"
	}
}