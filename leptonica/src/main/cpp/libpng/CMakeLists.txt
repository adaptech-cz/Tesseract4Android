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

set(SRC_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src)

configure_file(${SRC_PATH}/scripts/pnglibconf.h.prebuilt
        ${SRC_PATH}/pnglibconf.h
        COPYONLY)

add_library( # Sets the name of the library.
        pngx

        # Sets the library as a shared library.
        STATIC

        # Provides a relative path to your source file(s).
        ${SRC_PATH}/png.c
        ${SRC_PATH}/pngerror.c
        ${SRC_PATH}/pngget.c
        ${SRC_PATH}/pngmem.c
        ${SRC_PATH}/pngpread.c
        ${SRC_PATH}/pngread.c
        ${SRC_PATH}/pngrio.c
        ${SRC_PATH}/pngrtran.c
        ${SRC_PATH}/pngrutil.c
        ${SRC_PATH}/pngset.c
        ${SRC_PATH}/pngtrans.c
        ${SRC_PATH}/pngwio.c
        ${SRC_PATH}/pngwrite.c
        ${SRC_PATH}/pngwtran.c
        ${SRC_PATH}/pngwutil.c
        ${SRC_PATH}/arm/arm_init.c
        ${SRC_PATH}/arm/filter_neon.S
        ${SRC_PATH}/arm/filter_neon_intrinsics.c
        ${SRC_PATH}/arm/palette_neon_intrinsics.c)

# Enable NEON support
if (TARGET_ARCH MATCHES "^arm64" OR
        TARGET_ARCH MATCHES "^aarch64")
    add_definitions(-DPNG_ARM_NEON_OPT=2)

    # TODO: I don't know if these are really needed
    set_source_files_properties(${SRC_PATH}/arm/arm_init.c
            PROPERTIES COMPILE_FLAGS -mfpu=neon)
    set_source_files_properties(${SRC_PATH}/arm/filter_neon.S
            PROPERTIES COMPILE_FLAGS -mfpu=neon)
    set_source_files_properties(${SRC_PATH}/arm/filter_neon_intrinsics.c
            PROPERTIES COMPILE_FLAGS -mfpu=neon)
    set_source_files_properties(${SRC_PATH}/arm/palette_neon_intrinsics.c
            PROPERTIES COMPILE_FLAGS -mfpu=neon)
endif ()

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        pngx

        # Links the target library to the z library included in the NDK.
        z)

# Copy all header files to our include directory to be available for prefab
file(GLOB HEADER_FILES "${SRC_PATH}/*.h")
file(COPY ${HEADER_FILES} DESTINATION "${CMAKE_CURRENT_SOURCE_DIR}/include")
file(COPY ${HEADER_FILES} DESTINATION "${CMAKE_CURRENT_SOURCE_DIR}/include/png")
