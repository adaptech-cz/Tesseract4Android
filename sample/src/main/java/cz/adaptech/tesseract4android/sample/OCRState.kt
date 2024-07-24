package cz.adaptech.tesseract4android.sample


/**
 * Represents the various states that the OCR can be in.
 *
 * @since 2024/07/22
 * @author Clocks
 */
sealed interface OCRState {
	/**
	 * OCR is loading up.
	 */
	data object Loading : OCRState

	/**
	 * OCR is prepared.
	 *
	 * @param version Version of tesseract
	 * @param flavour Build flavour of tesseract
	 */
	data class StartUp(val version: String, val flavour: String) : OCRState

	/**
	 * OCR has been stopped.
	 */
	data object Stopped : OCRState

	/**
	 * OCR is being stopped.
	 */
	data object Stopping : OCRState

	/**
	 * OCR is starting up.
	 */
	data object Processing : OCRState

	/**
	 * OCR is currently in process.
	 *
	 * @param progress 0-100 progress indication.
	 */
	data class Progress(val progress: Int) : OCRState

	/**
	 * OCR has completed its task.
	 *
	 * @param time How many seconds it took to process the image.
	 */
	data class Finished(val time: Float) : OCRState
}