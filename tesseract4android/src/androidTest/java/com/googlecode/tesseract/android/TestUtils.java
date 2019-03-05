/*
 * Copyright (C) 2019 Adaptech s.r.o., Robert Pösel
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
