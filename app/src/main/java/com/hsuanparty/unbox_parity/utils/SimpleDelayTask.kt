package com.hsuanparty.unbox_parity.utils

import android.app.Activity
import android.os.Handler

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/28
 * Description: Do something after a little time
 */
object SimpleDelayTask {
    fun after(delay: Long, process: () -> Unit) {
        Handler().postDelayed({
            process()
        }, delay)
    }

    fun afterOnMain(delay: Long, activity: Activity, process: () -> Unit) {
        Handler().postDelayed({
            activity.runOnUiThread {
                Runnable {
                    process()
                }
            }
        }, delay)
    }
}