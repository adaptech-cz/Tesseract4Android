package com.googlecode.tesseract.android.test;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;

import java.io.IOException;

import androidx.annotation.NonNull;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class TestUtils {

    public static void grantPermissions(@NonNull String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        boolean granted = false;

        Context context = getTargetContext();
        for (String permission : permissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                try (ParcelFileDescriptor pfd = getInstrumentation().getUiAutomation().executeShellCommand(
                        "pm grant " + context.getPackageName() + " " + permission)) {
                    granted = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (granted) {
            // Wait a while to make sure permission is granted
            SystemClock.sleep(2000);
        }
    }
}
