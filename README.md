# Tesseract4Android

Fork of tess-two rewritten from scratch to build with CMake and support latest Android Studio and Tesseract 4.

The Java/JNI wrapper files and tests for Leptonica / Tesseract are based on the [tess-two project][tess-two], which is based on [Tesseract Tools for Android][tesseract-android-tools].

## Dependencies

This project uses additional libraries (with their own specific licenses):

 - [Tesseract][tesseract-ocr] 4.0.0
 - [Leptonica][leptonica] 1.77.0
 - [libjpeg][jpeg] v9c
 - [libpng][png] 1.6.36

## Prerequisites

 -  Android 4.1 (API 16) or higher
 -  A v4.0.0 [trained data file(s)][tessdata] for language(s) you want to use. Data files must be
copied to the Android device to a directory named `tessdata`.
 - For working PDF Renderer must be in `tessdata` directory also [pdf.ttf][pdffile] file.

## Building

You can use Android Studio 3.3 (or later) to open the project and build the AAR. Or you can use `gradlew` from command line.

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
