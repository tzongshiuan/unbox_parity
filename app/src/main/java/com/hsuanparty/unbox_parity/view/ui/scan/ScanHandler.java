package com.hsuanparty.unbox_parity.view.ui.scan;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import com.hsuanparty.unbox_parity.R;
import com.hsuanparty.unbox_parity.model.OcrResult;
import com.hsuanparty.unbox_parity.model.OcrResultFailure;
import com.hsuanparty.unbox_parity.utils.text_ocr.CameraManager;
import com.hsuanparty.unbox_parity.utils.text_ocr.DecodeHandler;
import com.hsuanparty.unbox_parity.utils.text_ocr.DecodeThread;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 * <p>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
final class ScanHandler extends Handler {

    private static final String TAG = ScanFragment.class.getSimpleName();

    private final ScanFragment fragment;
    private final DecodeThread decodeThread;
    private static State state;
    private final CameraManager cameraManager;

    private enum State {
        PREVIEW,
        PREVIEW_PAUSED,
        CONTINUOUS,
        CONTINUOUS_PAUSED,
        SUCCESS,
        DONE
    }

    ScanHandler(ScanFragment fragment, CameraManager cameraManager, boolean isContinuousModeActive) {
        this.fragment = fragment;
        this.cameraManager = cameraManager;

        // Start ourselves capturing previews (and decoding if using continuous recognition mode).
        cameraManager.startPreview();

        decodeThread = new DecodeThread(fragment);
        decodeThread.start();

        if (isContinuousModeActive) {
            state = State.CONTINUOUS;

            // Show the shutter and torch buttons
            fragment.setButtonVisibility(true);

            // Display a "be patient" message while first recognition request is running
            fragment.setStatusViewForContinuous();

            restartOcrPreviewAndDecode();
        } else {
            state = State.SUCCESS;

            // Show the shutter and torch buttons
            fragment.setButtonVisibility(true);

            restartOcrPreview();
        }
    }

    @Override
    public void handleMessage(Message message) {

        switch (message.what) {
            case R.id.restart_preview:
                restartOcrPreview();
                break;
            case R.id.ocr_continuous_decode_failed:
                DecodeHandler.resetDecodeState();
                try {
                    fragment.handleOcrContinuousDecode((OcrResultFailure) message.obj);
                } catch (NullPointerException e) {
                    Log.w(TAG, "got bad OcrResultFailure", e);
                }
                if (state == State.CONTINUOUS) {
                    restartOcrPreviewAndDecode();
                }
                break;
            case R.id.ocr_continuous_decode_succeeded:
                DecodeHandler.resetDecodeState();
                try {
                    fragment.handleOcrContinuousDecode((OcrResult) message.obj);
                } catch (NullPointerException e) {
                    // Continue
                }
                if (state == State.CONTINUOUS) {
                    restartOcrPreviewAndDecode();
                }
                break;
            case R.id.ocr_decode_succeeded:
                state = State.SUCCESS;
                fragment.setShutterButtonClickable(true);
                fragment.handleOcrDecode((OcrResult) message.obj);
                break;
            case R.id.ocr_decode_failed:
                state = State.PREVIEW;
                fragment.setShutterButtonClickable(true);
                Toast toast = Toast.makeText(fragment.getContext(),
                        fragment.getContext().getString(R.string.msg_ocr_fail), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
                break;
        }
    }

    void stop() {
        // TODO See if this should be done by sending a quit message to decodeHandler as is done
        // below in quitSynchronously().

        Log.d(TAG, "Setting state to CONTINUOUS_PAUSED.");
        state = State.CONTINUOUS_PAUSED;
        removeMessages(R.id.ocr_continuous_decode);
        removeMessages(R.id.ocr_decode);
        removeMessages(R.id.ocr_continuous_decode_failed);
        removeMessages(R.id.ocr_continuous_decode_succeeded); // TODO are these removeMessages() calls doing anything?

        // Freeze the view displayed to the user.
//    CameraManager.get().stopPreview();
    }

    void resetState() {
        //Log.d(TAG, "in restart()");
        if (state == State.CONTINUOUS_PAUSED) {
            Log.d(TAG, "Setting state to CONTINUOUS");
            state = State.CONTINUOUS;
            restartOcrPreviewAndDecode();
        }
    }

    void quitSynchronously() {
        state = State.DONE;
        if (cameraManager != null) {
            cameraManager.stopPreview();
        }
        //Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        try {
            //quit.sendToTarget(); // This always gives "sending message to a Handler on a dead thread"

            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            Log.w(TAG, "Caught InterruptedException in quitSyncronously()", e);
            // continue
        } catch (RuntimeException e) {
            Log.w(TAG, "Caught RuntimeException in quitSyncronously()", e);
            // continue
        } catch (Exception e) {
            Log.w(TAG, "Caught unknown Exception in quitSynchronously()", e);
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.ocr_continuous_decode);
        removeMessages(R.id.ocr_decode);

    }

    /**
     * Start the preview, but don't try to OCR anything until the user presses the shutter button.
     */
    private void restartOcrPreview() {
        // Display the shutter and torch buttons
        fragment.setButtonVisibility(true);

        if (state == State.SUCCESS) {
            state = State.PREVIEW;

            // Draw the viewfinder.
            fragment.drawViewfinder();
        }
    }

    /**
     * Send a decode request for realtime OCR mode
     */
    private void restartOcrPreviewAndDecode() {
        // Continue capturing camera frames
        cameraManager.startPreview();

        // Continue requesting decode of images
        cameraManager.requestOcrDecode(decodeThread.getHandler(), R.id.ocr_continuous_decode);
        fragment.drawViewfinder();
    }

    /**
     * Request OCR on the current preview frame.
     */
    private void ocrDecode() {
        state = State.PREVIEW_PAUSED;
        cameraManager.requestOcrDecode(decodeThread.getHandler(), R.id.ocr_decode);
    }

    /**
     * Request OCR when the hardware shutter button is clicked.
     */
    void hardwareShutterButtonClick() {
        // Ensure that we're not in continuous recognition mode
        if (state == State.PREVIEW) {
            ocrDecode();
        }
    }

    /**
     * Request OCR when the on-screen shutter button is clicked.
     */
    void shutterButtonClick() {
        // Disable further clicks on this button until OCR request is finished
        fragment.setShutterButtonClickable(false);
        ocrDecode();
    }
}
