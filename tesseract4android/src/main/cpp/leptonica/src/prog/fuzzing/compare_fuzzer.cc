#include "leptfuzz.h"

extern "C" int
LLVMFuzzerTestOneInput(const uint8_t *data, size_t size)
{
    if(size<3) return 0;

    leptSetStdNullHandler();

    l_float32 fract;
    PIX *pixs1, *pixs2;
    PIX *pixs3, *pixs4, *pixs5, *pixs6;

    pixs1 = pixReadMemSpix(data, size);
    if(pixs1==NULL) return 0;

    pixs2 = pixCopy(NULL, pixs1);
    if(pixs2==NULL) {
	    pixDestroy(&pixs1);
	    return 0;
    }

    pixs3 = pixConvertTo8(pixs1, 0);
    pixs4 = pixConvertTo8(pixs2, 0);

    pixGetPerceptualDiff(pixs3, pixs4, 1, 3, 20, &fract, &pixs5, &pixs6);

    pixDestroy(&pixs1);
    pixDestroy(&pixs2);
    pixDestroy(&pixs3);
    pixDestroy(&pixs4);
    pixDestroy(&pixs5);
    pixDestroy(&pixs6);
    return 0;
}
