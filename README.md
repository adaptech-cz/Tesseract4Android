[![](https://jitpack.io/v/cz.adaptech/tesseract4android.svg)](https://jitpack.io/#cz.adaptech/tesseract4android)

# Tesseract4Android

Fork of tess-two rewritten from scratch to build with CMake and support latest Android Studio and Tesseract OCR.

The Java/JNI wrapper files and tests for Leptonica / Tesseract are based on the [tess-two project][tess-two],
which is based on [Tesseract Tools for Android][tesseract-android-tools].

## Dependencies

This project uses additional libraries (with their own specific licenses):

 - [Tesseract OCR][tesseract-ocr] 5.3.2
 - [Leptonica][leptonica] 1.83.1
 - [libjpeg][jpeg] v9e
 - [libpng][png] 1.6.40

## Prerequisites

 - Android 4.1 (API 16) or higher
 - A v4.0.0 [trained data file(s)][tessdata] for language(s) you want to use.
   - These files must be placed in the (sub)directory named `tessdata` and the path must be readable
by the app. When targeting API >=29, only suitable places for this are app's private directories
(like `context.getFilesDir()` or `context.getExternalFilesDir()`).

## Variants

This library is available in two variants.

 - **Standard** - Single-threaded. Best for single-core processors or when using multiple Tesseract
instances in parallel.
 - **OpenMP** - Multi-threaded. Provides better performance on multi-core processors when using only
single instance of Tesseract.

## Usage

You can get compiled version of Tesseract4Android from JitPack.io.

1. Add the JitPack repository to your project root `build.gradle` file at the end of repositories:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency to your app module `build.gradle` file:

```gradle
dependencies {
    // To use Standard variant:
    implementation 'cz.adaptech.tesseract4android:tesseract4android:4.5.0'

    // To use OpenMP variant:
    implementation 'cz.adaptech.tesseract4android:tesseract4android-openmp:4.5.0'
}
```

3. Use the `TessBaseAPI` class in your code:

This is the simplest example you can have. In this case TessBaseAPI is always created, used to recognize the image and then destroyed.
Better would be to create and initialize the instance only once and use it to recognize multiple images instead. Look at the [sample](/sample)
project for such usage, additionally with progress notifications and a way to stop the ongoing processing.

```java
// Create TessBaseAPI instance (this internally creates the native Tesseract instance)
TessBaseAPI tess = new TessBaseAPI();

// Given path must contain subdirectory `tessdata` where are `*.traineddata` language files
// The path must be directly readable by the app
String dataPath = new File(context.getFilesDir(), "tesseract").getAbsolutePath();

// Initialize API for specified language
// (can be called multiple times during Tesseract lifetime)
if (!tess.init(dataPath, "eng")) { // could be multiple languages, like "eng+deu+fra"
    // Error initializing Tesseract (wrong/inaccessible data path or not existing language file(s))
    // Release the native Tesseract instance
    tess.recycle();
    return;
}

// Load the image (file path, Bitmap, Pix...)
// (can be called multiple times during Tesseract lifetime)
tess.setImage(image);

// Start the recognition (if not done for this image yet) and retrieve the result
// (can be called multiple times during Tesseract lifetime)
String text = tess.getUTF8Text();

// Release the native Tesseract instance when you don't want to use it anymore
// After this call, no method can be called on this TessBaseAPI instance
tess.recycle();
```

## Sample app

There is example application in the [sample](/sample) directory. It shows basic usage of the TessBaseAPI
inside ViewModel, showing progress indication, allowing stopping the processing and more.

It uses sample image and english traineddata, which are extracted from the assets in the APK
to app's private directory on device. This is simple, but you are keeping 2 instances of the data
file (first is kept in the APK file itself, second is kept on the storage) - wasting some space.
If you plan to use multiple traineddata files, it would be better to download them directly from
the internet rather than distributing them within the APK.

## Building

You can use Android Studio to open the project and build the AAR. Or you can use `gradlew` from command line.

To build the release version of the library, use task `tesseract4android:assembleRelease`.
After successful build, you will have resulting `AAR` files in the `<project dir>/tesseract4Android/build/outputs/aar/` directory.

Or you can publish the AAR directly to your local maven repository, by using task `tesseract4android:publishToMavenLocal`.
After successful build, you can consume your library as any other maven dependency. Just make sure
to add `mavenLocal()` repository in `repositories {}` block in your project's `build.gradle` file. 

### Android Studio

 - Open this project in Android Studio.
 - Open Gradle panel, expand `Tesseract4Android / :tesseract4Android / Tasks / other` and run `assembleRelease` (to get AAR).
 - Or in the same panel expand `Tesseract4Android / :tesseract4Android / Tasks / publishing` and run `publishToMavenLocal` (to publish AAR).

### GradleW

 - In project directory create `local.properties` file containing:

```properties
sdk.dir=c\:\\your\\path\\to\\android\\sdk
ndk.dir=c\:\\your\\path\\to\\android\\ndk
```

   Note for paths on Windows you must use `\` to escape some special characters, as in example above.

 - Call `gradlew tesseract4android:assembleRelease` from command line (to get AAR).
 - Or call `gradlew tesseract4android:publishToMavenLocal` from command line (to publish AAR).

## License

    Copyright 2019 Adaptech s.r.o., Robert Pösel

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[tess-two]: https://github.com/rmtheis/tess-two
[tesseract-android-tools]: https://github.com/alanv/tesseract-android-tools
[tesseract-ocr]: https://github.com/tesseract-ocr/tesseract
[leptonica]: https://github.com/DanBloomberg/leptonica
[jpeg]: http://libjpeg.sourceforge.net/
[png]: http://www.libpng.org/pub/png/libpng.html
[tessdata]: https://github.com/tesseract-ocr/tessdata/tree/4.0.0
