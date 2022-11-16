package cz.adaptech.tesseract4android.sample.ui.main;

import android.app.Application;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.util.Locale;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = "MainViewModel";

    private final TessBaseAPI tessApi;

    private final MutableLiveData<Boolean> processing = new MutableLiveData<>(false);

    private final MutableLiveData<String> progress = new MutableLiveData<>();

    private final MutableLiveData<String> result = new MutableLiveData<>();

    private boolean tessInit;
    private boolean stopped;

    public MainViewModel(@NonNull Application application) {
        super(application);

        tessApi = new TessBaseAPI(progressValues -> {
            progress.postValue("Progress: " + progressValues.getPercent() + " %");
        });

        // Show Tesseract version and library flavor at startup
        progress.setValue(String.format(Locale.ENGLISH, "Tesseract %s (%s)",
                tessApi.getVersion(), tessApi.getLibraryFlavor()));
    }

    @Override
    protected void onCleared() {
        if (isProcessing()) {
            tessApi.stop();
        }
        // Don't forget to release TessBaseAPI
        tessApi.recycle();
    }

    public void initTesseract(@NonNull String dataPath, @NonNull String language, int engineMode) {
        Log.i(TAG, "Initializing Tesseract with: dataPath = [" + dataPath + "], " +
                "language = [" + language + "], engineMode = [" + engineMode + "]");
        try {
            tessInit = tessApi.init(dataPath, language, engineMode);
        } catch (IllegalArgumentException e) {
            tessInit = false;
            Log.e(TAG, "Cannot initialize Tesseract:", e);
        }
    }

    public void recognizeImage(@NonNull File imagePath) {
        if (!tessInit) {
            Log.e(TAG, "recognizeImage: Tesseract is not initialized");
            return;
        }
        if (isProcessing()) {
            Log.e(TAG, "recognizeImage: Processing is in progress");
            return;
        }
        result.setValue("");
        processing.setValue(true);
        progress.setValue("Processing...");
        stopped = false;

        // Start process in another thread
        new Thread(() -> {
            tessApi.setImage(imagePath);
            // Or set it as Bitmap, Pix,...
            // tessApi.setImage(imageBitmap);

            long startTime = SystemClock.uptimeMillis();

            // Use getHOCRText(0) method to trigger recognition with progress notifications and
            // ability to cancel ongoing processing.
            tessApi.getHOCRText(0);

            // Then get just normal UTF8 text as result. Using only this method would also trigger
            // recognition, but would just block until it is completed.
            String text = tessApi.getUTF8Text();

            result.postValue(text);
            processing.postValue(false);
            if (stopped) {
                progress.postValue("Stopped.");
            } else {
                long duration = SystemClock.uptimeMillis() - startTime;
                progress.postValue(String.format(Locale.ENGLISH,
                        "Completed in %.3fs.", (duration / 1000f)));
            }
        }).start();
    }

    public void stop() {
        if (!isProcessing()) {
            return;
        }
        tessApi.stop();
        progress.setValue("Stopping...");
        stopped = true;
    }

    public boolean isProcessing() {
        return Boolean.TRUE.equals(processing.getValue());
    }

    public boolean isInitialized() {
        return tessInit;
    }

    @NonNull
    public LiveData<Boolean> getProcessing() {
        return processing;
    }

    @NonNull
    public LiveData<String> getProgress() {
        return progress;
    }

    @NonNull
    public LiveData<String> getResult() {
        return result;
    }
}