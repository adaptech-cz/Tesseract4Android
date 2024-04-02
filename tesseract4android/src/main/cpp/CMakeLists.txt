##
# Copyright (C) 2019 Adaptech s.r.o., Robert Pösel
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
##

# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

project(tesseract4Android)

file(GLOB leptonica_src leptonica/*.cpp)
file(GLOB tesseract_src tesseract/*.cpp)

add_library( # Sets the name of the library.
        tesseract4android

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${leptonica_src}
        ${tesseract_src}
        jni.cpp
        )

find_package(leptonica REQUIRED CONFIG)
find_package(tesseract REQUIRED CONFIG)

target_link_libraries(tesseract4android

        # List libraries link to the target library
        PRIVATE
        log
        jnigraphics

        PUBLIC
        leptonica::leptonica
        tesseract::tesseract
        )
