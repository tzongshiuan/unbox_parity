package com.hsuanparty.unbox_parity.utils.text_ocr;

import android.os.Handler;
import android.os.Looper;
import com.hsuanparty.unbox_parity.view.ui.scan.ScanFragment;

import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
public final class DecodeThread extends Thread {

  private final ScanFragment fragment;
  private Handler handler;
  private final CountDownLatch handlerInitLatch;

  public DecodeThread(ScanFragment activity) {
    this.fragment = activity;
    handlerInitLatch = new CountDownLatch(1);
  }

  public Handler getHandler() {
    try {
      handlerInitLatch.await();
    } catch (InterruptedException ie) {
      // continue?
    }
    return handler;
  }

  @Override
  public void run() {
    Looper.prepare();
    handler = new DecodeHandler(fragment);
    handlerInitLatch.countDown();
    Looper.loop();
  }
}
