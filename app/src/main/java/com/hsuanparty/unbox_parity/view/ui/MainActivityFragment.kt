package com.hsuanparty.unbox_parity.view.ui

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.R

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 * Description: A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    companion object {
        private val TAG = MainActivityFragment::class.java.simpleName
    }

    private lateinit var mFragmentView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogMessage.D(TAG, "onCreateView()")

        mFragmentView = inflater.inflate(R.layout.fragment_main, container, false)
        initUI()

        return mFragmentView
    }

    override fun onResume() {
        LogMessage.D(TAG, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        LogMessage.D(TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        LogMessage.D(TAG, "onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        LogMessage.D(TAG, "onDestroy()")
        super.onDestroy()
    }

    private fun initUI() {
        LogMessage.D(TAG, "initUI()")
    }
}
