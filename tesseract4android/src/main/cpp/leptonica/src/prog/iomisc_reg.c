/*====================================================================*
 -  Copyright (C) 2001 Leptonica.  All rights reserved.
 -
 -  Redistribution and use in source and binary forms, with or without
 -  modification, are permitted provided that the following conditions
 -  are met:
 -  1. Redistributions of source code must retain the above copyright
 -     notice, this list of conditions and the following disclaimer.
 -  2. Redistributions in binary form must reproduce the above
 -     copyright notice, this list of conditions and the following
 -     disclaimer in the documentation and/or other materials
 -     provided with the distribution.
 -
 -  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 -  ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 -  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 -  A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL ANY
 -  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 -  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 -  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 -  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 -  OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 -  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 -  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *====================================================================*/

/*
 * iomisc_reg.c
 *
 *   Tests several special I/O operations:
 *      * special operations for handling 16 bpp png input
 *      * zlib compression quality in png
 *      * chroma sampling options in jpeg
 *      * read/write of alpha with png
 *      * i/o with colormaps
 *      * removal and regeneration of rgb and gray colormaps
 *      * tiff compression
 *
 *   This does not test these exotic formats:
 *      * multipage/custom tiff (tested by mtiff_reg)
 *      * pdf (tested by pdfiotest and pdfseg_reg)
 *      * PostScript (tested by psio_reg and psioseg_reg)
 *
 *   The large error values that are being used for tests 3-7 and 36
 *   are only required for older versions of tifflib (e.g., libtiff 4.0.7).
 *   For libtiff 4.0.11, small values like 10 will pass tests 6, 7 and 36.
 *   The issue was initially found on the AArch64 (ARM) processor.
 */

#ifdef HAVE_CONFIG_H
#include <config_auto.h>
#endif  /* HAVE_CONFIG_H */

#include "string.h"
#include "allheaders.h"

LEPT_DLL extern const char *ImageFileFormatExtensions[];

static const size_t  zlibsize[5] = {1047873, 215039, 195778, 189709, 180987};
static const size_t  tiffsize[8] = {65674, 34872, 20482, 20998, 11178,
                                    21500, 18472, 151885};

int main(int    argc,
         char **argv)
{
char         *text;
l_int32       w, h, d, level, wpl, format, xres, yres;
l_int32       bps, spp, res, iscmap;
size_t        size;
FILE         *fp;
PIX          *pixs, *pixg, *pix1, *pix2, *pix3, *pix4;
PIXA         *pixa;
PIXCMAP      *cmap;
L_REGPARAMS  *rp;

    if (regTestSetup(argc, argv, &rp))
        return 1;

    lept_mkdir("lept/io");

        /* Test 16 to 8 stripping */
    pixs = pixRead("test16.tif");
    pixWrite("/tmp/lept/io/test16.png", pixs, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/io/test16.png");  /* 0 */
    pix1 = pixRead("/tmp/lept/io/test16.png");
    d = pixGetDepth(pix1);
    regTestCompareValues(rp, 8, d, 0.0);  /* 1 */
    pixDestroy(&pix1);
    l_pngSetReadStrip16To8(0);
    pix1 = pixRead("/tmp/lept/io/test16.png");
    d = pixGetDepth(pix1);
    regTestCompareValues(rp, 16, d, 0.0);  /* 2 */
    pixDestroy(&pix1);
    pixDestroy(&pixs);

        /* Test zlib compression in png */
    pixs = pixRead("feyn.tif");
    for (level = 0; level < 5; level += 1) {
        pixSetZlibCompression(pixs, 2 * level);
        pixWrite("/tmp/lept/io/zlibtest.png", pixs, IFF_PNG);
        size = nbytesInFile("/tmp/lept/io/zlibtest.png");
        regTestCompareValues(rp, zlibsize[level], size, 3000.0);  /* 3 - 7 */
        if (rp->display)
            lept_stderr("zlib level = %d, file size = %ld\n",
                        level, (unsigned long)size);
    }
    pixDestroy(&pixs);

        /* Test chroma sampling options in jpeg */
    pixs = pixRead("marge.jpg");
    pixWrite("/tmp/lept/io/chromatest1.jpg", pixs, IFF_JFIF_JPEG);
    regTestCheckFile(rp, "/tmp/lept/io/chromatest1.jpg");  /* 8 */
    if (rp->display) {
        size = nbytesInFile("/tmp/lept/io/chromatest1.jpg");
        lept_stderr("chroma default: file size = %ld\n", (unsigned long)size);
    }
    pixSetChromaSampling(pixs, 0);
    pixWrite("/tmp/lept/io/chromatest2.jpg", pixs, IFF_JFIF_JPEG);
    regTestCheckFile(rp, "/tmp/lept/io/chromatest2.jpg");  /* 9 */
    if (rp->display) {
        size = nbytesInFile("/tmp/lept/io/chromatest2.jpg");
        lept_stderr("no ch. sampling: file size = %ld\n", (unsigned long)size);
    }
    pixSetChromaSampling(pixs, 1);
    pixWrite("/tmp/lept/io/chromatest3.jpg", pixs, IFF_JFIF_JPEG);
    regTestCheckFile(rp, "/tmp/lept/io/chromatest3.jpg");  /* 10 */
    if (rp->display) {
        size = nbytesInFile("/tmp/lept/io/chromatest3.jpg");
        lept_stderr("chroma default: file size = %ld\n", (unsigned long)size);
    }
    pixDestroy(&pixs);

        /* Test read/write of alpha with png */
    pixs = pixRead("books_logo.png");
    pixDisplayWithTitle(pixs, 0, 100, NULL, rp->display);
    pixg = pixGetRGBComponent(pixs, L_ALPHA_CHANNEL);
    regTestWritePixAndCheck(rp, pixg, IFF_PNG);  /* 11 */
    pixDisplayWithTitle(pixg, 300, 100, NULL, rp->display);
    pixDestroy(&pixg);
    pix1 = pixAlphaBlendUniform(pixs, 0xffffff00);  /* render rgb over white */
    regTestWritePixAndCheck(rp, pix1, IFF_PNG);  /* 12 */
    pixDisplayWithTitle(pix1, 0, 250, NULL, rp->display);
    pix2 = pixSetAlphaOverWhite(pix1);  /* regenerate alpha from white */
    pixWrite("/tmp/lept/io/logo2.png", pix2, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/io/logo2.png");  /* 13 */
    pixDisplayWithTitle(pix2, 0, 400, NULL, rp->display);
    pixg = pixGetRGBComponent(pix2, L_ALPHA_CHANNEL);
    regTestWritePixAndCheck(rp, pixg, IFF_PNG);  /* 14 */
    pixDisplayWithTitle(pixg, 300, 400, NULL, rp->display);
    pixDestroy(&pixg);
    pix3 = pixRead("/tmp/lept/io/logo2.png");
    pix4 = pixAlphaBlendUniform(pix3, 0x00ffff00);  /* render rgb over cyan */
    regTestWritePixAndCheck(rp, pix4, IFF_PNG);  /* 15 */
    pixDisplayWithTitle(pix3, 0, 550, NULL, rp->display);
    pixDestroy(&pix1);
    pixDestroy(&pix2);
    pixDestroy(&pix3);
    pixDestroy(&pix4);
    pixDestroy(&pixs);

        /* A little fun with rgb colormaps */
    pixs = pixRead("weasel4.11c.png");
    pixa = pixaCreate(6);
    pixaAddPix(pixa, pixs, L_CLONE);
    pixGetDimensions(pixs, &w, &h, &d);
    wpl = pixGetWpl(pixs);
    if (rp->display)
        lept_stderr("w = %d, h = %d, d = %d, wpl = %d\n", w, h, d, wpl);
    pixGetResolution(pixs, &xres, &yres);
    if (rp->display && xres != 0 && yres != 0)
        lept_stderr("xres = %d, yres = %d\n", xres, yres);
    cmap = pixGetColormap(pixs);
        /* Write and read back the colormap */
    if (rp->display) pixcmapWriteStream(stderr, pixGetColormap(pixs));
    fp = lept_fopen("/tmp/lept/io/cmap1", "wb");
    pixcmapWriteStream(fp, pixGetColormap(pixs));
    lept_fclose(fp);
    regTestCheckFile(rp, "/tmp/lept/io/cmap1");  /* 16 */
    fp = lept_fopen("/tmp/lept/io/cmap1", "rb");
    cmap = pixcmapReadStream(fp);
    lept_fclose(fp);
    fp = lept_fopen("/tmp/lept/io/cmap2", "wb");
    pixcmapWriteStream(fp, cmap);
    lept_fclose(fp);
    regTestCheckFile(rp, "/tmp/lept/io/cmap2");  /* 17 */
    pixcmapDestroy(&cmap);

        /* Remove and regenerate colormap */
    pix1 = pixRemoveColormap(pixs, REMOVE_CMAP_BASED_ON_SRC);
    regTestWritePixAndCheck(rp, pix1, IFF_PNG);  /* 18 */
    pixaAddPix(pixa, pix1, L_CLONE);
    pix2 = pixConvertRGBToColormap(pix1, 1);
    regTestWritePixAndCheck(rp, pix2, IFF_PNG);  /* 19 */
    pixaAddPix(pixa, pix2, L_CLONE);
    pixDestroy(&pixs);
    pixDestroy(&pix1);
    pixDestroy(&pix2);

       /* Remove and regnerate gray colormap */
    pixs = pixRead("weasel4.5g.png");
    pixaAddPix(pixa, pixs, L_CLONE);
    pix1 = pixRemoveColormap(pixs, REMOVE_CMAP_BASED_ON_SRC);
    regTestWritePixAndCheck(rp, pix1, IFF_PNG);  /* 20 */
    pixaAddPix(pixa, pix1, L_CLONE);
    pix2 = pixConvertGrayToColormap(pix1);
    regTestWritePixAndCheck(rp, pix2, IFF_PNG);  /* 21 */
    pixaAddPix(pixa, pix2, L_CLONE);
    pixDestroy(&pix1);
    pixDestroy(&pix2);
    pix3 = pixaDisplayTiled(pixa, 400, 0, 20);
    pixDisplayWithTitle(pix3, 0, 750, NULL, rp->display);
    pixDestroy(&pix3);
    pixaDestroy(&pixa);

        /* Other fields in the pix */
    format = pixGetInputFormat(pixs);
    regTestCompareValues(rp, format, IFF_PNG, 0.0);  /* 22 */
    if (rp->display)
        lept_stderr("Input format extension: %s\n",
                    ImageFileFormatExtensions[format]);
    pixSetText(pixs, "reconstituted 4-bit weasel");
    text = pixGetText(pixs);
    if (rp->display && text && strlen(text) != 0)
        lept_stderr("Text: %s\n", text);
    pixDestroy(&pixs);

        /* Some tiff compression and headers */
    readHeaderTiff("feyn-fract.tif", 0, &w, &h, &bps, &spp,
                   &res, &iscmap, &format);
    if (rp->display) {
        lept_stderr("w = %d, h = %d, bps = %d, spp = %d, res = %d, cmap = %d\n",
                    w, h, bps, spp, res, iscmap);
        lept_stderr("Input format extension: %s\n",
                    ImageFileFormatExtensions[format]);
    }
    pixs = pixRead("feyn-fract.tif");
    pixWrite("/tmp/lept/io/fract1.tif", pixs, IFF_TIFF);
    regTestCheckFile(rp, "/tmp/lept/io/fract1.tif");  /* 23 */
    size = nbytesInFile("/tmp/lept/io/fract1.tif");
    regTestCompareValues(rp, tiffsize[0], size, 0.0);  /* 24 */
    if (rp->display)
        lept_stderr("uncompressed: %ld\n", (unsigned long)size);
    pixWrite("/tmp/lept/io/fract2.tif", pixs, IFF_TIFF_PACKBITS);
    regTestCheckFile(rp, "/tmp/lept/io/fract2.tif");  /* 25 */
    size = nbytesInFile("/tmp/lept/io/fract2.tif");
    regTestCompareValues(rp, tiffsize[1], size, 0.0);  /* 26 */
    if (rp->display)
        lept_stderr("packbits: %ld\n", (unsigned long)size);
    pixWrite("/tmp/lept/io/fract3.tif", pixs, IFF_TIFF_RLE);
    regTestCheckFile(rp, "/tmp/lept/io/fract3.tif");  /* 27 */
    size = nbytesInFile("/tmp/lept/io/fract3.tif");
    regTestCompareValues(rp, tiffsize[2], size, 0.0);  /* 28 */
    if (rp->display)
        lept_stderr("rle: %ld\n", (unsigned long)size);
    pixWrite("/tmp/lept/io/fract4.tif", pixs, IFF_TIFF_G3);
    regTestCheckFile(rp, "/tmp/lept/io/fract4.tif");  /* 29 */
    size = nbytesInFile("/tmp/lept/io/fract4.tif");
    regTestCompareValues(rp, tiffsize[3], size, 0.0);  /* 30 */
    if (rp->display)
        lept_stderr("g3: %ld\n", (unsigned long)size);
    pixWrite("/tmp/lept/io/fract5.tif", pixs, IFF_TIFF_G4);
    regTestCheckFile(rp, "/tmp/lept/io/fract5.tif");  /* 31 */
    size = nbytesInFile("/tmp/lept/io/fract5.tif");
    regTestCompareValues(rp, tiffsize[4], size, 0.0);  /* 32 */
    if (rp->display)
        lept_stderr("g4: %ld\n", (unsigned long)size);
    pixWrite("/tmp/lept/io/fract6.tif", pixs, IFF_TIFF_LZW);
    regTestCheckFile(rp, "/tmp/lept/io/fract6.tif");  /* 33 */
    size = nbytesInFile("/tmp/lept/io/fract6.tif");
    regTestCompareValues(rp, tiffsize[5], size, 0.0);  /* 34 */
    if (rp->display)
        lept_stderr("lzw: %ld\n", (unsigned long)size);
    pixWrite("/tmp/lept/io/fract7.tif", pixs, IFF_TIFF_ZIP);
    regTestCheckFile(rp, "/tmp/lept/io/fract7.tif");  /* 35 */
    size = nbytesInFile("/tmp/lept/io/fract7.tif");
    regTestCompareValues(rp, tiffsize[6], size, 200.0);  /* 36 */
    if (rp->display)
        lept_stderr("zip: %ld\n", (unsigned long)size);
    pixg = pixConvertTo8(pixs, 0);
    pixWrite("/tmp/lept/io/fract8.tif", pixg, IFF_TIFF_JPEG);
    regTestCheckFile(rp, "/tmp/lept/io/fract8.tif");  /* 37 */
    size = nbytesInFile("/tmp/lept/io/fract8.tif");
    regTestCompareValues(rp, tiffsize[7], size, 100.0);  /* 38 */
    if (rp->display)
        lept_stderr("jpeg: %ld\n", (unsigned long)size);
    pixDestroy(&pixs);
    pixDestroy(&pixg);

        /* Test read/write of alpha with pnm */
    pixs = pixRead("books_logo.png");
    pixWrite("/tmp/lept/io/alpha1.pnm", pixs, IFF_PNM);
    regTestCheckFile(rp, "/tmp/lept/io/alpha1.pnm");  /* 39 */
    pix1 = pixRead("/tmp/lept/io/alpha1.pnm");
    regTestComparePix(rp, pixs, pix1);  /* 40 */
    pixDisplayWithTitle(pix1, 600, 100, NULL, rp->display);
    pixDestroy(&pixs);
    pixDestroy(&pix1);

    return regTestCleanup(rp);
}
