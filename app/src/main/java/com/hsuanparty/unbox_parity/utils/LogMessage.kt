package com.hsuanparty.unbox_parity.utils

import android.util.Log

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 * Description: handle all logcat messages here
 */
class LogMessage {
    companion object {
        var isShowLog = true

        fun E(tag: String, message: String) {
            if (isShowLog) {
                Log.e(tag, message)
            }
        }

        fun E(tag: String, message: String, tr: Throwable) {
            if (isShowLog) {
                Log.e(tag, message, tr)
            }
        }

        fun W(tag: String, message: String) {
            if (isShowLog) {
                Log.w(tag, message)
            }
        }

        fun D(tag: String, message: String) {
            if (isShowLog) {
                Log.d(tag, message)
            }
        }

        fun I(tag: String, message: String) {
            if (isShowLog) {
                Log.i(tag, message)
            }
        }

        fun V(tag: String, message: String) {
            if (isShowLog) {
                Log.v(tag, message)
            }
        }

        fun WTF(tag: String, message: String) {
            if (isShowLog) {
                Log.wtf(tag, message)
            }
        }
    }
}