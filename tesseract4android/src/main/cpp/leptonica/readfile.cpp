/*
 * Copyright (C) 2019 Adaptech s.r.o., Robert Pösel
 * Copyright 2011, Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "common.h"

#include <cstring>
#include <android/bitmap.h>

#ifdef __cplusplus
extern "C" {
#endif  /* __cplusplus */

/************
 * ReadFile *
 ************/

jlong Java_com_googlecode_leptonica_android_ReadFile_nativeReadMem(JNIEnv *env, jclass clazz,
                                                                   jbyteArray image, jint length) {
  jbyte *image_buffer = env->GetByteArrayElements(image, nullptr);
  int buffer_length = env->GetArrayLength(image);

  PIX *pix = pixReadMem((const l_uint8 *) image_buffer, buffer_length);

  env->ReleaseByteArrayElements(image, image_buffer, JNI_ABORT);

  return (jlong) pix;
}

jlong Java_com_googlecode_leptonica_android_ReadFile_nativeReadBytes8(JNIEnv *env, jclass clazz,
                                                                      jbyteArray data, jint w,
                                                                      jint h) {
  PIX *pix = pixCreateNoInit((l_int32) w, (l_int32) h, 8);
  if (!pix) {
    LOGE("Failed to create pix object with w=%d, h=%d", w, h);
    return 0;
  }

  l_uint8 **lineptrs = pixSetupByteProcessing(pix, nullptr, nullptr);
  if (!lineptrs) {
    LOGE("Failed to prepare pix for byte processing");
    return 0;
  }

  jbyte *data_buffer = env->GetByteArrayElements(data, nullptr);
  l_uint8 *byte_buffer = (l_uint8 *) data_buffer;

  for (int i = 0; i < h; i++) {
    memcpy(lineptrs[i], (byte_buffer + (i * w)), w);
  }

  env->ReleaseByteArrayElements(data, data_buffer, JNI_ABORT);
  pixCleanupByteProcessing(pix, lineptrs);

  l_int32 d;

  pixGetDimensions(pix, &w, &h, &d);

  LOGI("Created image with w=%d, h=%d, d=%d", w, h, d);

  return (jlong) pix;
}

jboolean Java_com_googlecode_leptonica_android_ReadFile_nativeReplaceBytes8(JNIEnv *env,
                                                                            jclass clazz,
                                                                            jlong nativePix,
                                                                            jbyteArray data,
                                                                            jint srcw, jint srch) {
  PIX *pix = (PIX *) nativePix;
  l_int32 w, h, d;

  pixGetDimensions(pix, &w, &h, &d);

  if (d != 8 || (l_int32) srcw != w || (l_int32) srch != h) {
    LOGE("Failed to replace bytes at w=%d, h=%d, d=%d with w=%d, h=%d", w, h, d, srcw, srch);

    return JNI_FALSE;
  }

  l_uint8 **lineptrs = pixSetupByteProcessing(pix, nullptr, nullptr);
  jbyte *data_buffer = env->GetByteArrayElements(data, nullptr);
  l_uint8 *byte_buffer = (l_uint8 *) data_buffer;

  for (int i = 0; i < h; i++) {
    memcpy(lineptrs[i], (byte_buffer + (i * w)), w);
  }

  env->ReleaseByteArrayElements(data, data_buffer, JNI_ABORT);
  pixCleanupByteProcessing(pix, lineptrs);

  return JNI_TRUE;
}

jlong Java_com_googlecode_leptonica_android_ReadFile_nativeReadFile(JNIEnv *env, jclass clazz,
                                                                    jstring fileName) {
  PIX *pixd = nullptr;

  const char *c_fileName = env->GetStringUTFChars(fileName, nullptr);
  if (c_fileName == nullptr) {
    LOGE("could not extract fileName string!");
    return (jlong) NULL;
  }

  pixd = pixRead(c_fileName);

  env->ReleaseStringUTFChars(fileName, c_fileName);

  return (jlong) pixd;
}

jlong Java_com_googlecode_leptonica_android_ReadFile_nativeReadBitmap(JNIEnv *env, jclass clazz,
                                                                      jobject bitmap) {
  AndroidBitmapInfo info;
  void* pixels;
  int ret;

  if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
    LOGE("AndroidBitmap_getInfo() failed! error=%d", ret);
    return (jlong) NULL;
  }

  if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
    LOGE("Bitmap format is not RGBA_8888!");
    return (jlong) NULL;
  }

  if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
    LOGE("AndroidBitmap_lockPixels() failed! error=%d", ret);
    return (jlong) NULL;
  }

  PIX *pixd = pixCreate(info.width, info.height, 32);

  l_uint32 *src = (l_uint32 *) pixels;
  l_uint32 *dst = pixGetData(pixd);
  l_int32 srcWpl = (info.stride / 4);
  l_int32 dstWpl = pixGetWpl(pixd);
  l_uint8 a, r, g, b;

  for (int y = 0; y < info.height; y++) {
    l_uint32 *dst_line = dst + (y * dstWpl);
    l_uint32 *src_line = src + (y * srcWpl);

    for (int x = 0; x < info.width; x++) {
      // Get pixel from RGBA_8888
      // NOTE: For some reason we have to swap R and B constants in order to get correct values!
      a = (*src_line >> SK_A32_SHIFT);
      r = (*src_line >> SK_B32_SHIFT); // intentionally use B shift, see NOTE above
      g = (*src_line >> SK_G32_SHIFT);
      b = (*src_line >> SK_R32_SHIFT); // intentionally use R shift, see NOTE above

      // Set pixel to Pix format
      *dst_line = a << L_ALPHA_SHIFT | r << L_RED_SHIFT | g << L_GREEN_SHIFT | b << L_BLUE_SHIFT;

      // Move to the next pixel
      src_line++;
      dst_line++;
    }
  }

/*
  // Alternative version from renard314
  // Result should be the same, but it iterates over pixels twice, because of the pixEndianByteSwap()
  PIX *pixd = pixCreate(info.width, info.height, 32);
  l_uint8 *src = (l_uint8 *) pixels;
  l_uint8 *dst = (l_uint8 *) pixGetData(pixd);
  l_int32 srcBpl = (info.stride);
  l_int32 dstBpl = pixGetWpl(pixd)*4;

  for (int dy = 0; dy < info.height; dy++) {
    memcpy(dst, src, 4 * info.width);
    dst += dstBpl;
    src += srcBpl;
  }
  pixEndianByteSwap(pixd);
*/

  AndroidBitmap_unlockPixels(env, bitmap);

  return (jlong) pixd;
}

#ifdef __cplusplus
}
#endif  /* __cplusplus */
