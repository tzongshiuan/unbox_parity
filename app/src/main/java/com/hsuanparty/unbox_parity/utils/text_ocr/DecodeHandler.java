package com.hsuanparty.unbox_parity.utils.text_ocr;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.googlecode.leptonica.android.Pixa;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.hsuanparty.unbox_parity.R;
import com.hsuanparty.unbox_parity.model.OcrResult;
import com.hsuanparty.unbox_parity.model.OcrResultFailure;
import com.hsuanparty.unbox_parity.view.ui.scan.ScanFragment;
import com.hsuanparty.unbox_parity.view.ui.scan.ViewFinder;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * Class to send bitmap data for OCR.
 * <p>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
public final class DecodeHandler extends Handler {

    private final ScanFragment fragment;
    private boolean running = true;
    private final TessBaseAPI baseApi;
    //private BeepManager beepManager;
    private Bitmap bitmap;
    private static boolean isDecodePending;
    private long timeRequired;

    public DecodeHandler(ScanFragment fragment) {
        this.fragment = fragment;
        baseApi = fragment.getBaseApi();
//    beepManager = new BeepManager(activity);
//    beepManager.updatePrefs();
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case R.id.ocr_continuous_decode:
                // Only request a decode if a request is not already pending.
                if (!isDecodePending) {
                    isDecodePending = true;
                    ocrContinuousDecode((byte[]) message.obj, message.arg1, message.arg2);
                }
                break;
            case R.id.ocr_decode:
                ocrDecode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case R.id.quit:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    public static void resetDecodeState() {
        isDecodePending = false;
    }

    /**
     * Launch an AsyncTask to perform an OCR decode for single-shot mode.
     *
     * @param data   Image data
     * @param width  Image width
     * @param height Image height
     */
    private void ocrDecode(byte[] data, int width, int height) {
        //beepManager.playBeepSoundAndVibrate();
        fragment.displayProgressDialog();

        // test saved image
//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root + "/req_images");
//        if (!myDir.mkdirs()) {
//        }
//        Random generator = new Random();
//        int n = 10000;
//        n = generator.nextInt(n);
//        String fname = "Image-" + n + ".jpg";
//        File file = new File(myDir, fname);
//
//        try {
//            FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
//            BufferedOutputStream bos = new BufferedOutputStream(out);
//            byte[] test = encodeNV21toJPEG(data, width, height);
//            bos.write(test);
//            bos.flush();
//            bos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // Launch OCR asynchronously, so we get the dialog box displayed immediately
        new OcrRecognizeAsyncTask(fragment, baseApi, data, width, height).execute();
    }

    /**
     * Perform an OCR decode for realtime recognition mode.
     *
     * @param data   Image data
     * @param width  Image width
     * @param height Image height
     */
    private void ocrContinuousDecode(byte[] data, int width, int height) {
        PlanarYUVLuminanceSource source = fragment.getCameraManager().buildLuminanceSource(data, width, height);
        if (source == null) {
            sendContinuousOcrFailMessage();
            return;
        }
        bitmap = source.renderCroppedGreyscaleBitmap();

        OcrResult ocrResult = getOcrResult();
        Handler handler = fragment.getHandler();
        if (handler == null) {
            return;
        }

        if (ocrResult == null) {
            try {
                sendContinuousOcrFailMessage();
            } catch (NullPointerException e) {
                fragment.stopHandler();
            } finally {
                bitmap.recycle();
                baseApi.clear();
            }
            return;
        }

        try {
            Message message = Message.obtain(handler, R.id.ocr_continuous_decode_succeeded, ocrResult);
            message.sendToTarget();
        } catch (NullPointerException e) {
            fragment.stopHandler();
        } finally {
            baseApi.clear();
        }
    }

    @SuppressWarnings("unused")
    private OcrResult getOcrResult() {
        OcrResult ocrResult;
        String textResult;
        long start = System.currentTimeMillis();

        try {
            baseApi.setImage(ReadFile.readBitmap(bitmap));
            textResult = baseApi.getUTF8Text();
            timeRequired = System.currentTimeMillis() - start;

            // Check for failure to recognize text
            if (textResult == null || textResult.equals("")) {
                return null;
            }
            ocrResult = new OcrResult();
            ocrResult.setWordConfidences(baseApi.wordConfidences());
            ocrResult.setMeanConfidence(baseApi.meanConfidence());
            if (ViewFinder.DRAW_REGION_BOXES) {
                Pixa regions = baseApi.getRegions();
                ocrResult.setRegionBoundingBoxes(regions.getBoxRects());
                regions.recycle();
            }
            if (ViewFinder.DRAW_TEXTLINE_BOXES) {
                Pixa textlines = baseApi.getTextlines();
                ocrResult.setTextlineBoundingBoxes(textlines.getBoxRects());
                textlines.recycle();
            }
            if (ViewFinder.DRAW_STRIP_BOXES) {
                Pixa strips = baseApi.getStrips();
                ocrResult.setStripBoundingBoxes(strips.getBoxRects());
                strips.recycle();
            }

            // Always get the word bounding boxes--we want it for annotating the bitmap after the user
            // presses the shutter button, in addition to maybe wanting to draw boxes/words during the
            // continuous mode recognition.
            Pixa words = baseApi.getWords();
            ocrResult.setWordBoundingBoxes(words.getBoxRects());
            words.recycle();

//      if (ViewfinderView.DRAW_CHARACTER_BOXES || ViewfinderView.DRAW_CHARACTER_TEXT) {
//        ocrResult.setCharacterBoundingBoxes(baseApi.getCharacters().getBoxRects());
//      }
        } catch (RuntimeException e) {
            Log.e("OcrRecognizeAsyncTask", "Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.");
            e.printStackTrace();
            try {
                baseApi.clear();
                fragment.stopHandler();
            } catch (NullPointerException e1) {
                // Continue
            }
            return null;
        }
        timeRequired = System.currentTimeMillis() - start;
        ocrResult.setBitmap(bitmap);
        ocrResult.setText(textResult);
        ocrResult.setRecognitionTimeRequired(timeRequired);
        return ocrResult;
    }

    private void sendContinuousOcrFailMessage() {
        Handler handler = fragment.getHandler();
        if (handler != null) {
            Message message = Message.obtain(handler, R.id.ocr_continuous_decode_failed, new OcrResultFailure(timeRequired));
            message.sendToTarget();
        }
    }

}












