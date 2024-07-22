package cz.adaptech.tesseract4android.sample.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import cz.adaptech.tesseract4android.sample.OCRState
import cz.adaptech.tesseract4android.sample.ui.theme.Tesseract4AndroidTheme
import java.util.Locale


/**
 * @since 2024/07/22
 */
@Composable
fun MainView() {
	val viewModel = viewModel<MainViewModel>()
	val image by viewModel.image.collectAsState()
	val status by viewModel.status.collectAsState()
	val result by viewModel.result.collectAsState()

	val isStartEnabled by viewModel.isStartEnabled.collectAsState()
	val isStopEnabled by viewModel.isStopEnabled.collectAsState()

	val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
	val landscape = sizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

	Tesseract4AndroidTheme {
		Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
			MainContent(
				innerPadding,
				image,
				status,
				result,
				viewModel::start,
				viewModel::stop,
				isStartEnabled,
				isStopEnabled,
				landscape
			)
		}
	}
}

@Composable
fun MainContent(
	innerPadding: PaddingValues,
	bitmap: ImageBitmap?,
	status: OCRState,
	result: String,
	onStart: () -> Unit,
	onStop: () -> Unit,
	isStartEnabled: Boolean,
	isStopEnabled: Boolean,
	landscape: Boolean
) {
	if (landscape) {
		Row(
			Modifier
				.padding(innerPadding)
				.fillMaxSize(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Image(bitmap)

			Column(
				Modifier
					.verticalScroll(rememberScrollState())
					.weight(1f), // let it fill up space
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Status(status)

				Controls(onStart, onStop, isStartEnabled, isStopEnabled)

				Result(result)
			}
		}
	} else {
		Column(
			Modifier
				.padding(innerPadding)
				.verticalScroll(rememberScrollState())
				.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Image(bitmap)

			Status(status)

			Controls(onStart, onStop, isStartEnabled, isStopEnabled)

			Result(result)
		}
	}
}

@Composable
fun Result(result: String) {
	Text(text = result, Modifier.padding(16.dp))
}

@Composable
fun Image(bitmap: ImageBitmap?) {
	AnimatedVisibility(visible = bitmap != null) {
		Image(bitmap = bitmap!!, contentDescription = "Sample")
	}
}

@Composable
fun Controls(
	onStart: () -> Unit,
	onStop: () -> Unit,
	isStartEnabled: Boolean,
	isStopEnabled: Boolean
) {
	Row {
		Button(onClick = onStart, enabled = isStartEnabled) {
			Text(text = "START")
		}
		Button(onClick = onStop, enabled = isStopEnabled) {
			Text(text = "STOP")
		}
	}
}

@Composable
fun Status(status: OCRState) {
	Row {
		Text(text = "Status: ")
		Text(
			text = when (status) {
				is OCRState.Finished ->
					"Completed in %.3fs.".format(Locale.getDefault(), status.time)

				OCRState.Processing -> "Processing..."
				is OCRState.Progress -> "Processing ${status.progress}%"
				is OCRState.StartUp ->
					"Tesseract %s (%s)"
						.format(Locale.getDefault(), status.version, status.flavour)

				OCRState.Stopped -> "Stopped."
				OCRState.Stopping -> "Stopping..."
				OCRState.Loading -> "Loading..."
			}
		)
	}
}