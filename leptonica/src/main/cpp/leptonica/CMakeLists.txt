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

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(LEPT_PATH "src/src/")
set(JPEG_PATH "../libjpeg/src/")
set(PNG_PATH "../libpng/src/")

configure_file(
        endianness.h.in
        ${CMAKE_CURRENT_SOURCE_DIR}/${LEPT_PATH}endianness.h @ONLY)

file(GLOB leptonica_src "${LEPT_PATH}*.c")
file(GLOB leptonica_hdr "${LEPT_PATH}*.h")

add_library( # Sets the name of the library.
        leptonica

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${leptonica_src}
        ${leptonica_hdr}
        box.cpp
        boxa.cpp
        jni.cpp
        pix.cpp
        pixa.cpp
        readfile.cpp
        utilities.cpp
        writefile.cpp
        common.h
        )

# Specifies a path to native header files.
include_directories(${LEPT_PATH})
include_directories(${JPEG_PATH})
include_directories(${PNG_PATH})

# Define preprocessor constants
add_definitions(-DANDROID_BUILD)
add_definitions(-DHAVE_LIBJPEG=1)
add_definitions(-DHAVE_LIBPNG=1)
add_definitions(-DHAVE_LIBZ=1)
#TODO: Enable this when we use minSdkVersion 23 or higher
#add_definitions(-DHAVE_FMEMOPEN=1)

# Ignore some warnings during build
target_compile_options(leptonica PRIVATE "-Wno-address-of-packed-member")

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        leptonica

        # Links the target library to the z library included in the NDK.
        log
        jnigraphics
        z
        jpeg
        pngx
        )
