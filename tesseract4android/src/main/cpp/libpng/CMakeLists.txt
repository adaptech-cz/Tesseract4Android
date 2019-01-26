# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(SRC_PATH "src/")

add_library( # Sets the name of the library.
        png

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${SRC_PATH}png.c
        ${SRC_PATH}pngerror.c
        ${SRC_PATH}pngget.c
        ${SRC_PATH}pngmem.c
        ${SRC_PATH}pngpread.c
        ${SRC_PATH}pngread.c
        ${SRC_PATH}pngrio.c
        ${SRC_PATH}pngrtran.c
        ${SRC_PATH}pngrutil.c
        ${SRC_PATH}pngset.c
        ${SRC_PATH}pngtrans.c
        ${SRC_PATH}pngwio.c
        ${SRC_PATH}pngwrite.c
        ${SRC_PATH}pngwtran.c
        ${SRC_PATH}pngwutil.c
        ${SRC_PATH}arm/arm_init.c
        ${SRC_PATH}arm/filter_neon_intrinsics.c
        ${SRC_PATH}arm/palette_neon_intrinsics.c)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        png

        # Links the target library to the z library included in the NDK.
        z)
