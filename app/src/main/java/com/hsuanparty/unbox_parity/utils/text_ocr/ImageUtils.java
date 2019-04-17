package com.hsuanparty.unbox_parity.utils.text_ocr;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/17
 * Description:
 */
public class ImageUtils {
    public static byte[] encodeNV21toJPEG(byte[] nv21, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        return out.toByteArray();
    }

    public static byte[] rotateNV21_working(final byte[] yuv,
                                            final int width,
                                            final int height,
                                            final int rotation)
    {
        if (rotation == 0) return yuv;
        if (rotation % 90 != 0 || rotation < 0 || rotation > 270) {
            throw new IllegalArgumentException("0 <= rotation < 360, rotation % 90 == 0");
        }

        final byte[]  output    = new byte[yuv.length];
        final int     frameSize = width * height;
        final boolean swap      = rotation % 180 != 0;
        final boolean xflip     = rotation % 270 != 0;
        final boolean yflip     = rotation >= 180;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                final int yIn = j * width + i;
                final int uIn = frameSize + (j >> 1) * width + (i & ~1);
                final int vIn = uIn       + 1;

                final int wOut     = swap  ? height              : width;
                final int hOut     = swap  ? width               : height;
                final int iSwapped = swap  ? j                   : i;
                final int jSwapped = swap  ? i                   : j;
                final int iOut     = xflip ? wOut - iSwapped - 1 : iSwapped;
                final int jOut     = yflip ? hOut - jSwapped - 1 : jSwapped;

                final int yOut = jOut * wOut + iOut;
                final int uOut = frameSize + (jOut >> 1) * wOut + (iOut & ~1);
                final int vOut = uOut + 1;

                output[yOut] = (byte)(0xff & yuv[yIn]);
                output[uOut] = (byte)(0xff & yuv[uIn]);
                output[vOut] = (byte)(0xff & yuv[vIn]);
            }
        }
        return output;
    }
}
