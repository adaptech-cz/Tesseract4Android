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

set(SRC_PATH "src/")

add_library( # Sets the name of the library.
        jpeg

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${SRC_PATH}jaricom.c
        ${SRC_PATH}jcapimin.c
        ${SRC_PATH}jcapistd.c
        ${SRC_PATH}jcarith.c
        ${SRC_PATH}jccoefct.c
        ${SRC_PATH}jccolor.c
        ${SRC_PATH}jcdctmgr.c
        ${SRC_PATH}jchuff.c
        ${SRC_PATH}jcinit.c
        ${SRC_PATH}jcmainct.c
        ${SRC_PATH}jcmarker.c
        ${SRC_PATH}jcmaster.c
        ${SRC_PATH}jcomapi.c
        ${SRC_PATH}jcparam.c
        ${SRC_PATH}jcprepct.c
        ${SRC_PATH}jcsample.c
        ${SRC_PATH}jctrans.c
        ${SRC_PATH}jdapimin.c
        ${SRC_PATH}jdapistd.c
        ${SRC_PATH}jdarith.c
        ${SRC_PATH}jdatadst.c
        ${SRC_PATH}jdatasrc.c
        ${SRC_PATH}jdcoefct.c
        ${SRC_PATH}jdcolor.c
        ${SRC_PATH}jddctmgr.c
        ${SRC_PATH}jdhuff.c
        ${SRC_PATH}jdinput.c
        ${SRC_PATH}jdmainct.c
        ${SRC_PATH}jdmarker.c
        ${SRC_PATH}jdmaster.c
        ${SRC_PATH}jdmerge.c
        ${SRC_PATH}jdpostct.c
        ${SRC_PATH}jdsample.c
        ${SRC_PATH}jdtrans.c
        ${SRC_PATH}jerror.c
        ${SRC_PATH}jfdctflt.c
        ${SRC_PATH}jfdctfst.c
        ${SRC_PATH}jfdctint.c
        ${SRC_PATH}jidctflt.c
        ${SRC_PATH}jidctfst.c
        ${SRC_PATH}jidctint.c
        ${SRC_PATH}jquant1.c
        ${SRC_PATH}jquant2.c
        ${SRC_PATH}jutils.c
        ${SRC_PATH}jmemmgr.c
        ${SRC_PATH}jmemansi.c)
