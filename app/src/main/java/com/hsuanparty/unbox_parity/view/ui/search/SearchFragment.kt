package com.hsuanparty.unbox_parity.view.ui.search

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.lifecycle.Observer

import com.hsuanparty.unbox_parity.databinding.SearchFragmentBinding
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel.Companion.SEARCH_FINISH_STATUS
import javax.inject.Inject

class SearchFragment : Fragment(), Injectable {

    companion object {
        private val TAG = SearchFragment::class.java.simpleName
    }

    @Inject
    lateinit var factory: MyViewModelFactory

    private lateinit var viewModel: SearchViewModel

    private lateinit var mBinding: SearchFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogMessage.D(TAG, "onCreateView()")
        mBinding = SearchFragmentBinding.inflate(inflater, container, false)
        initUI()

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)

        viewModel.isWaitingLiveData.observe(this, Observer<Boolean> { isWaiting ->
            if (isWaiting) {
                mBinding.waitingDialog.setVisibleWithAnimate(true)
            } else {
                mBinding.waitingDialog.setVisibleImmediately(false)
            }
        })

        viewModel.isSearchFinish.observe(this, Observer<Int> { status ->
            when (status) {
                SEARCH_FINISH_STATUS -> {
                }

                else -> {
                }
            }
        })
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initUI() {
        mBinding.searchEditText.setOnEditorActionListener(object: TextView.OnEditorActionListener {
            override fun onEditorAction(view: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard()
                    performSearch()
                    return true
                }
                return false
            }
        })

        mBinding.searchBtn.setOnClickListener {
            performSearch()
        }

        mBinding.speakBtn.setOnClickListener {
            // TODO voice record and then search
        }

        mBinding.segmentView.setOnSelectionChangedListener { identifier, value ->
            LogMessage.D(TAG, "identifier: $identifier, value: $value")
        }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    private fun performSearch() {
        viewModel.search(mBinding.searchEditText.text.toString())
    }
}
