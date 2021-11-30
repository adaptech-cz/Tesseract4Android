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

set(TESS_PATH "src/src/")
set(TESS_INCLUDE_PATH "src/include/")
set(LEPT_PATH "../leptonica/src/src/")

# Get version with components from VERSION file.
file(STRINGS "src/VERSION" VERSION_PLAIN)
string(REGEX REPLACE "^([^.]*)\\..*" "\\1" VERSION_MAJOR ${VERSION_PLAIN})
string(REGEX REPLACE "^[^.]*\\.([^.]*)\\..*" "\\1" VERSION_MINOR ${VERSION_PLAIN})
string(REGEX REPLACE "^[^.]*\\.[^.]*\\.([0-9]*).*" "\\1" VERSION_PATCH ${VERSION_PLAIN})
if(NOT PACKAGE_VERSION)
    set(PACKAGE_VERSION ${VERSION_PLAIN})
endif()

# Provide also same macro names as autoconf (see configure.ac).
set(GENERIC_MAJOR_VERSION ${VERSION_MAJOR})
set(GENERIC_MINOR_VERSION ${VERSION_MINOR})
set(GENERIC_MICRO_VERSION ${VERSION_PATCH})

configure_file(
        ${TESS_INCLUDE_PATH}tesseract/version.h.in
        ${CMAKE_CURRENT_SOURCE_DIR}/${TESS_INCLUDE_PATH}tesseract/version.h @ONLY)

configure_file(
        config_auto.h.in
        ${CMAKE_CURRENT_SOURCE_DIR}/${TESS_PATH}api/config_auto.h @ONLY)

file(GLOB tesseract_src
        ${TESS_PATH}api/*.cpp
        ${TESS_PATH}ccmain/*.cpp
        ${TESS_PATH}ccstruct/*.cpp
        ${TESS_PATH}ccutil/*.cpp
        ${TESS_PATH}classify/*.cpp
        ${TESS_PATH}cutil/*.cpp
        ${TESS_PATH}dict/*.cpp
        ${TESS_PATH}lstm/*.cpp
        ${TESS_PATH}opencl/*.cpp
        ${TESS_PATH}textord/*.cpp
        ${TESS_PATH}viewer/*.cpp
        ${TESS_PATH}wordrec/*.cpp
        )

list(APPEND arch_files
        ${TESS_PATH}arch/dotproduct.cpp
        ${TESS_PATH}arch/simddetect.cpp
        ${TESS_PATH}arch/intsimdmatrix.cpp
        # NEON support (we can include these files on any architecture without explicit checks)
        ${TESS_PATH}arch/intsimdmatrixneon.cpp
        ${TESS_PATH}arch/dotproductneon.cpp
        )

# Enable use of NEON on armeabi-v7a
if (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    add_definitions("-DHAVE_NEON")

    set_source_files_properties(${TESS_PATH}arch/intsimdmatrixneon.cpp
        PROPERTIES COMPILE_FLAGS -mfpu=neon)
    set_source_files_properties(${TESS_PATH}arch/dotproductneon.cpp
        PROPERTIES COMPILE_FLAGS -mfpu=neon)

    # Include cpu-features for runtime detection of NEON support
    include(AndroidNdkModules)
    android_ndk_import_module_cpufeatures()
    set(LIBRARY_CPU_FEATURES cpufeatures)
endif()

file(GLOB tesseract_hdr
        ${TESS_PATH}api/*.h
        ${TESS_PATH}arch/*.h
        ${TESS_PATH}ccmain/*.h
        ${TESS_PATH}ccstruct/*.h
        ${TESS_PATH}ccutil/*.h
        ${TESS_PATH}classify/*.h
        ${TESS_PATH}cutil/*.h
        ${TESS_PATH}dict/*.h
        ${TESS_PATH}lstm/*.h
        ${TESS_PATH}opencl/*.h
        ${TESS_PATH}textord/*.h
        ${TESS_PATH}viewer/*.h
        ${TESS_PATH}wordrec/*.h
        )

add_library( # Sets the name of the library.
        tesseract

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${tesseract_src}
        ${arch_files}
        ${tesseract_hdr}
        pageiterator.cpp
        resultiterator.cpp
        tessbaseapi.cpp
        common.h
        )

# Specifies a path to native header files.
include_directories(${LEPT_PATH})

include_directories(${TESS_INCLUDE_PATH})
include_directories(${TESS_PATH}api)
include_directories(${TESS_PATH}arch)
include_directories(${TESS_PATH}ccmain)
include_directories(${TESS_PATH}ccstruct)
include_directories(${TESS_PATH}ccutil)
include_directories(${TESS_PATH}classify)
include_directories(${TESS_PATH}cutil)
include_directories(${TESS_PATH}dict)
include_directories(${TESS_PATH}lstm)
include_directories(${TESS_PATH}opencl)
include_directories(${TESS_PATH}textord)
include_directories(${TESS_PATH}viewer)
include_directories(${TESS_PATH}wordrec)

# Define preprocessor constants
add_definitions(-DHAVE_CONFIG_H)
add_definitions(-DGRAPHICS_DISABLED)
add_definitions(-DFAST_FLOAT)

# Ignore some warnings during build
target_compile_options(tesseract PRIVATE "-Wno-null-dereference")

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        tesseract

        # Links the target library to the z library included in the NDK.
        atomic
        dl
        log
        ${LIBRARY_CPU_FEATURES}
        jnigraphics
        leptonica
        )
