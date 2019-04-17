package com.hsuanparty.unbox_parity.utils.text_ocr;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.hsuanparty.unbox_parity.utils.LogMessage;

import static com.hsuanparty.unbox_parity.utils.text_ocr.ImageUtils.rotateNV21_working;

/**
 * Called when the next preview frame is received.
 * <p>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
final class PreviewCallback implements Camera.PreviewCallback {

    private static final String TAG = PreviewCallback.class.getSimpleName();

    private final CameraConfigurationManager configManager;
    private Handler previewHandler;
    private int previewMessage;

    PreviewCallback(CameraConfigurationManager configManager) {
        this.configManager = configManager;
    }

    void setHandler(Handler previewHandler, int previewMessage) {
        this.previewHandler = previewHandler;
        this.previewMessage = previewMessage;
    }

    // Since we're not calling setPreviewFormat(int), the data arrives here in the YCbCr_420_SP
    // (NV21) format.
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Point cameraResolution = configManager.getCameraResolution();
        Handler thePreviewHandler = previewHandler;
        if (cameraResolution != null && thePreviewHandler != null) {
            // portrait
            Message message = thePreviewHandler.obtainMessage(previewMessage, cameraResolution.y,
                    cameraResolution.x, rotateNV21_working(data, cameraResolution.x, cameraResolution.y, 90));

//            Message message = thePreviewHandler.obtainMessage(previewMessage, cameraResolution.x,
//                    cameraResolution.y, data);
            message.sendToTarget();
            previewHandler = null;
        } else {
            Log.d(TAG, "Got preview callback, but no handler or resolution available");
        }
    }
}
