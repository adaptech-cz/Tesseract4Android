# Tesseract4Android

Fork of tess-two rewritten from scratch to build with CMake and support latest Android Studio and Tesseract OCR.

The Java/JNI wrapper files and tests for Leptonica / Tesseract are based on the [tess-two project][tess-two], which is based on [Tesseract Tools for Android][tesseract-android-tools].

## Dependencies

This project uses additional libraries (with their own specific licenses):

 - [Tesseract][tesseract-ocr] 4.1.1
 - [Leptonica][leptonica] 1.80.0
 - [libjpeg][jpeg] v9d
 - [libpng][png] 1.6.37

## Prerequisites

 - Android 4.1 (API 16) or higher
 - A v4.0.0 [trained data file(s)][tessdata] for language(s) you want to use. Data files must be
copied to the Android device to a directory named `tessdata`.
 - If you want to use PdfRenderer, copy also [pdf.ttf][pdffile] file to the `tessdata` directory.
 - Application must hold permission `READ_EXTERNAL_STORAGE` to access `tessdata` directory.

## Usage

To use Tesseract4Android in your project add dependency to your `build.gradle` file:

    dependencies {
        implementation 'cz.adaptech.android:tesseract4android:2.1.1'
    }

## Building

You can use Android Studio (tested on version 4.1.2) to open the project and build the AAR. Or you can use `gradlew` from command line.

To build the release version of the library, use task `tesseract4android:assembleRelease`. After successful build, you will have resulting `AAR` file in the `<project dir>/tesseract4Android/build/outputs/aar/` directory.

### Android Studio

 - Open this project in Android Studio.
 - Open Gradle panel, expand `Tesseract4Android / :tesseract4Android / Tasks / other` and run `assembleRelease`.

### GradleW

 - In project directory create `local.properties` file containing:

       sdk.dir=c\:\\your\\path\\to\\android\\sdk
       ndk.dir=c\:\\your\\path\\to\\android\\ndk

   Note for paths on Windows you must use `\` to escape some special characters, as in example above.

 - Call `gradlew tesseract4android:assembleRelease` from command line.

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
[pdffile]: https://github.com/tesseract-ocr/tesseract/blob/master/tessdata/pdf.ttf
