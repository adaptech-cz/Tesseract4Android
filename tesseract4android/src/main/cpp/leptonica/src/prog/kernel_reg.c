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
 * kernel_reg.c
 */

#ifdef HAVE_CONFIG_H
#include <config_auto.h>
#endif  /* HAVE_CONFIG_H */

#include <string.h>
#include "allheaders.h"

static const char  *kdatastr = " 20.3    50   80  50   20 "
                               " 51.4   100  140  100  50 "
                               " 92.5   160  200  160  90 "
                               " 53.7   100  140  100  50 "
                               " 24.9    50   80   50  20 ";

int main(int    argc,
         char **argv)
{
char         *str;
l_int32       i, j, same, ok, plottype;
l_float32     sum, avediff, rmsdiff;
L_KERNEL     *kel1, *kel2, *kel3, *kel4, *kelx, *kely;
BOX          *box;
PIX          *pix, *pixs, *pixb, *pixg, *pixd, *pixp, *pixt;
PIX          *pixt1, *pixt2, *pixt3;
PIXA         *pixa;
PIXAA        *paa;
SARRAY       *sa;
L_REGPARAMS  *rp;

#if !defined(HAVE_LIBPNG)
    L_ERROR("This test requires libpng to run.\n", "kernel_reg");
    exit(77);
#endif

    if (regTestSetup(argc, argv, &rp))
        return 1;

    paa = pixaaCreate(0);

        /* Test creating from a string */
    pixa = pixaCreate(0);
    kel1 = kernelCreateFromString(5, 5, 2, 2, kdatastr);
    pixd = kernelDisplayInPix(kel1, 41, 2);
    pixWrite("/tmp/lept/regout/pixkern.png", pixd, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/pixkern.png");  /* 0 */
    pixaAddPix(pixa, pixd, L_INSERT);
    pixaaAddPixa(paa, pixa, L_INSERT);
    kernelDestroy(&kel1);

        /* Test read/write for kernel.  Note that both get
         * compared to the same golden file, which is
         * overwritten with a copy of /tmp/lept/regout/kern2.kel */
    kel1 = kernelCreateFromString(5, 5, 2, 2, kdatastr);
    kernelWrite("/tmp/lept/regout/kern1.kel", kel1);
    regTestCheckFile(rp, "/tmp/lept/regout/kern1.kel");  /* 1 */
    kel2 = kernelRead("/tmp/lept/regout/kern1.kel");
    kernelWrite("/tmp/lept/regout/kern2.kel", kel2);
    regTestCheckFile(rp, "/tmp/lept/regout/kern2.kel");  /* 2 */
    regTestCompareFiles(rp, 1, 2);  /* 3 */
    kernelDestroy(&kel1);
    kernelDestroy(&kel2);

        /* Test creating from a file */
    pixa = pixaCreate(0);
    sa = sarrayCreate(0);
    sarrayAddString(sa, "# small 3x3 kernel", L_COPY);
    sarrayAddString(sa, "3 5", L_COPY);
    sarrayAddString(sa, "1 2", L_COPY);
    sarrayAddString(sa, "20.5   50   80    50   20", L_COPY);
    sarrayAddString(sa, "82.    120  180   120  80", L_COPY);
    sarrayAddString(sa, "22.1   50   80    50   20", L_COPY);
    str = sarrayToString(sa, 1);
    l_binaryWrite("/tmp/lept/regout/kernfile.kel", "w", str, strlen(str));
    kel2 = kernelCreateFromFile("/tmp/lept/regout/kernfile.kel");
    pixd = kernelDisplayInPix(kel2, 41, 2);
    pixWrite("/tmp/lept/regout/ker1.png", pixd, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/ker1.png");  /* 4 */
    pixaAddPix(pixa, pixd, L_INSERT);
    sarrayDestroy(&sa);
    lept_free(str);
    kernelDestroy(&kel2);

        /* Test creating from a pix */
    pixt = pixCreate(5, 3, 8);
    pixSetPixel(pixt, 0, 0, 20);
    pixSetPixel(pixt, 1, 0, 50);
    pixSetPixel(pixt, 2, 0, 80);
    pixSetPixel(pixt, 3, 0, 50);
    pixSetPixel(pixt, 4, 0, 20);
    pixSetPixel(pixt, 0, 1, 80);
    pixSetPixel(pixt, 1, 1, 120);
    pixSetPixel(pixt, 2, 1, 180);
    pixSetPixel(pixt, 3, 1, 120);
    pixSetPixel(pixt, 4, 1, 80);
    pixSetPixel(pixt, 0, 0, 20);
    pixSetPixel(pixt, 1, 2, 50);
    pixSetPixel(pixt, 2, 2, 80);
    pixSetPixel(pixt, 3, 2, 50);
    pixSetPixel(pixt, 4, 2, 20);
    kel3 = kernelCreateFromPix(pixt, 1, 2);
    pixd = kernelDisplayInPix(kel3, 41, 2);
    pixWrite("/tmp/lept/regout/ker2.png", pixd, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/ker2.png");  /* 5 */
    pixaAddPix(pixa, pixd, L_INSERT);
    pixaaAddPixa(paa, pixa, L_INSERT);
    pixDestroy(&pixt);
    kernelDestroy(&kel3);

        /* Test convolution with kel1 */
    pixa = pixaCreate(0);
    pixs = pixRead("test24.jpg");
    pixg = pixScaleRGBToGrayFast(pixs, 3, COLOR_GREEN);
    pixaAddPix(pixa, pixg, L_INSERT);
    kel1 = kernelCreateFromString(5, 5, 2, 2, kdatastr);
    pixd = pixConvolve(pixg, kel1, 8, 1);
    pixWrite("/tmp/lept/regout/ker3.png", pixd, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/ker3.png");  /* 6 */
    pixaAddPix(pixa, pixd, L_INSERT);
    pixaaAddPixa(paa, pixa, L_INSERT);
    pixDestroy(&pixs);
    kernelDestroy(&kel1);

        /* Test convolution with flat rectangular kel; also test
         * block convolution with tiling. */
    pixa = pixaCreate(0);
    pixs = pixRead("test24.jpg");
    pixg = pixScaleRGBToGrayFast(pixs, 3, COLOR_GREEN);
    kel2 = makeFlatKernel(11, 11, 5, 5);
    pixd = pixConvolve(pixg, kel2, 8, 1);
    pixaAddPix(pixa, pixd, L_COPY);
    pixWrite("/tmp/lept/regout/ker4.png", pixd, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/ker4.png");  /* 7 */
    pixt = pixBlockconv(pixg, 5, 5);
    pixaAddPix(pixa, pixt, L_COPY);
    pixWrite("/tmp/lept/regout/ker5.png", pixt, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/ker5.png");  /* 8 */
    if (rp->display)
        pixCompareGray(pixd, pixt, L_COMPARE_ABS_DIFF, GPLOT_PNG, NULL,
                       NULL, NULL, NULL);
    pixt2 = pixBlockconvTiled(pixg, 5, 5, 3, 6);
    pixaAddPix(pixa, pixt2, L_INSERT);
    pixaaAddPixa(paa, pixa, L_INSERT);
    pixWrite("/tmp/lept/regout/ker5a.png", pixt2, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/ker5a.png");  /* 9 */

    ok = TRUE;
    for (i = 1; i <= 7; i++) {
        for (j = 1; j <= 7; j++) {
            if (i == 1 && j == 1) continue;
            pixt2 = pixBlockconvTiled(pixg, 5, 5, j, i);
            pixEqual(pixt2, pixd, &same);
            if (!same) {
                lept_stderr("Error for nx = %d, ny = %d\n", j, i);
                ok = FALSE;
            }
            pixDestroy(&pixt2);
        }
    }
    if (ok)
        lept_stderr("OK: Tiled results identical to pixConvolve()\n");
    else
        lept_stderr("ERROR: Tiled results not identical to pixConvolve()\n");

    pixDestroy(&pixs);
    pixDestroy(&pixg);
    pixDestroy(&pixd);
    pixDestroy(&pixt);
    kernelDestroy(&kel2);

        /* Do another flat rectangular test; this time with white at edge.
         * About 1% of the pixels near the image edge differ by 1 between
         * the pixConvolve() and pixBlockconv().  For what it's worth,
         * pixConvolve() gives the more accurate result; namely, 255 for
         * pixels at the edge. */
    pix = pixRead("pageseg1.tif");
    box = boxCreate(100, 100, 2260, 3160);
    pixb = pixClipRectangle(pix, box, NULL);
    pixs = pixScaleToGray4(pixb);

    pixa = pixaCreate(0);
    kel3 = makeFlatKernel(7, 7, 3, 3);
    startTimer();
    pixt = pixConvolve(pixs, kel3, 8, 1);
    lept_stderr("Generic convolution time: %5.3f sec\n", stopTimer());
    pixaAddPix(pixa, pixt, L_INSERT);
    pixWrite("/tmp/lept/regout/conv1.png", pixt, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/conv1.png");  /* 10 */

    startTimer();
    pixt2 = pixBlockconv(pixs, 3, 3);
    lept_stderr("Flat block convolution time: %5.3f sec\n", stopTimer());
    pixaAddPix(pixa, pixt2, L_INSERT);
    pixWrite("/tmp/lept/regout/conv2.png", pixt2, IFF_PNG);  /* ditto */
    regTestCheckFile(rp, "/tmp/lept/regout/conv2.png");  /* 11 */

    plottype = (rp->display) ? GPLOT_PNG : 0;
    pixCompareGray(pixt, pixt2, L_COMPARE_ABS_DIFF, plottype, NULL,
                   &avediff, &rmsdiff, NULL);
    pixp = pixRead("/tmp/lept/comp/compare_gray0.png");
    pixaAddPix(pixa, pixp, L_INSERT);
    pixaaAddPixa(paa, pixa, L_INSERT);
    pixWrite("/tmp/lept/regout/conv3.png", pixp, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/conv3.png");  /* 12 */
    lept_stderr("Ave diff = %6.4f, RMS diff = %6.4f\n", avediff, rmsdiff);
    if (avediff <= 0.01)
        lept_stderr("OK: avediff = %6.4f <= 0.01\n", avediff);
    else
        lept_stderr("Bad?: avediff = %6.4f > 0.01\n", avediff);

    pixDestroy(&pixs);
    pixDestroy(&pix);
    pixDestroy(&pixb);
    boxDestroy(&box);
    kernelDestroy(&kel3);

        /* Do yet another set of flat rectangular tests, this time
         * on an RGB image */
    pixs = pixRead("test24.jpg");
    kel4 = makeFlatKernel(7, 7, 3, 3);
    startTimer();
    pixt1 = pixConvolveRGB(pixs, kel4);
    lept_stderr("Time 7x7 non-separable: %7.3f sec\n", stopTimer());
    pixWrite("/tmp/lept/regout/conv4.jpg", pixt1, IFF_JFIF_JPEG);
    regTestCheckFile(rp, "/tmp/lept/regout/conv4.jpg");  /* 13 */

    kelx = makeFlatKernel(1, 7, 0, 3);
    kely = makeFlatKernel(7, 1, 3, 0);
    startTimer();
    pixt2 = pixConvolveRGBSep(pixs, kelx, kely);
    lept_stderr("Time 7x1,1x7 separable: %7.3f sec\n", stopTimer());
    pixWrite("/tmp/lept/regout/conv5.jpg", pixt2, IFF_JFIF_JPEG);
    regTestCheckFile(rp, "/tmp/lept/regout/conv5.jpg");  /* 14 */

    startTimer();
    pixt3 = pixBlockconv(pixs, 3, 3);
    lept_stderr("Time 7x7 blockconv: %7.3f sec\n", stopTimer());
    pixWrite("/tmp/lept/regout/conv6.jpg", pixt3, IFF_JFIF_JPEG);
    regTestCheckFile(rp, "/tmp/lept/regout/conv6.jpg");  /* 15 */
    regTestComparePix(rp, pixt1, pixt2);  /* 16 */
    regTestCompareSimilarPix(rp, pixt2, pixt3, 15, 0.0005, 0);  /* 17 */

    pixDestroy(&pixs);
    pixDestroy(&pixt1);
    pixDestroy(&pixt2);
    pixDestroy(&pixt3);
    kernelDestroy(&kel4);
    kernelDestroy(&kelx);
    kernelDestroy(&kely);

        /* Test generation and convolution with gaussian kernel */
    pixa = pixaCreate(0);
    pixs = pixRead("test8.jpg");
    pixaAddPix(pixa, pixs, L_COPY);
    kel1 = makeGaussianKernel(5, 5, 3.0, 5.0);
    kernelGetSum(kel1, &sum);
    lept_stderr("Sum for gaussian kernel = %f\n", sum);
    kernelWrite("/tmp/lept/regout/gauss.kel", kel1);
    pixt = pixConvolve(pixs, kel1, 8, 1);
    pixt2 = pixConvolve(pixs, kel1, 16, 0);
    pixaAddPix(pixa, pixt, L_INSERT);
    pixaAddPix(pixa, pixt2, L_INSERT);
    pixWrite("/tmp/lept/regout/ker6.png", pixt, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/ker6.png");  /* 18 */

    pixt = kernelDisplayInPix(kel1, 25, 2);
    pixaAddPix(pixa, pixt, L_INSERT);
    pixaaAddPixa(paa, pixa, L_INSERT);
    kernelDestroy(&kel1);
    pixDestroy(&pixs);

        /* Test generation and convolution with separable gaussian kernel */
    pixa = pixaCreate(0);
    pixs = pixRead("test8.jpg");
    pixaAddPix(pixa, pixs, L_INSERT);
    makeGaussianKernelSep(5, 5, 3.0, 5.0, &kelx, &kely);
    kernelGetSum(kelx, &sum);
    lept_stderr("Sum for x gaussian kernel = %f\n", sum);
    kernelGetSum(kely, &sum);
    lept_stderr("Sum for y gaussian kernel = %f\n", sum);
    kernelWrite("/tmp/lept/regout/gauss.kelx", kelx);
    kernelWrite("/tmp/lept/regout/gauss.kely", kely);

    pixt = pixConvolveSep(pixs, kelx, kely, 8, 1);
    pixt2 = pixConvolveSep(pixs, kelx, kely, 16, 0);
    pixaAddPix(pixa, pixt, L_INSERT);
    pixaAddPix(pixa, pixt2, L_INSERT);
    pixWrite("/tmp/lept/regout/ker7.png", pixt, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/ker7.png");  /* 19 */

    pixt = kernelDisplayInPix(kelx, 25, 2);
    pixaAddPix(pixa, pixt, L_INSERT);
    pixt = kernelDisplayInPix(kely, 25, 2);
    pixaAddPix(pixa, pixt, L_INSERT);
    pixaaAddPixa(paa, pixa, L_INSERT);
    kernelDestroy(&kelx);
    kernelDestroy(&kely);

        /* Test generation and convolution with diff of gaussians kernel */
/*    pixt = pixRead("marge.jpg");
    pixs = pixConvertRGBToLuminance(pixt);
    pixDestroy(&pixt); */
    pixa = pixaCreate(0);
    pixs = pixRead("test8.jpg");
    pixaAddPix(pixa, pixs, L_INSERT);
    kel1 = makeDoGKernel(7, 7, 1.5, 2.7);
    kernelGetSum(kel1, &sum);
    lept_stderr("Sum for DoG kernel = %f\n", sum);
    kernelWrite("/tmp/lept/regout/dog.kel", kel1);
    pixt = pixConvolve(pixs, kel1, 8, 0);
/*    pixInvert(pixt, pixt); */
    pixaAddPix(pixa, pixt, L_INSERT);
    pixWrite("/tmp/lept/regout/ker8.png", pixt, IFF_PNG);
    regTestCheckFile(rp, "/tmp/lept/regout/ker8.png");  /* 20 */

    pixt = kernelDisplayInPix(kel1, 20, 2);
    pixaAddPix(pixa, pixt, L_INSERT);
    pixaaAddPixa(paa, pixa, L_INSERT);
    kernelDestroy(&kel1);

    pixd = pixaaDisplayByPixa(paa, 10, 1.0, 20, 20, 0);
    pixDisplayWithTitle(pixd, 100, 100, NULL, rp->display);
    pixWrite("/tmp/lept/regout/kernel.jpg", pixd, IFF_JFIF_JPEG);
    pixDestroy(&pixd);
    pixaaDestroy(&paa);

    return regTestCleanup(rp);
}
