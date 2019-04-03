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
import androidx.databinding.BindingAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.R

import com.hsuanparty.unbox_parity.databinding.SearchFragmentBinding
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeAdapter
import com.hsuanparty.unbox_parity.view.ui.video.VideoViewModel
import javax.inject.Inject

class SearchFragment : Fragment(), Injectable {

    companion object {
        private val TAG = SearchFragment::class.java.simpleName

        private const val WEEK_HOT_VIDEO = 0
        private const val MONTH_HOT_VIDEO = 1
        private const val YEAR_HOT_VIDEO = 2

        @JvmStatic
        @BindingAdapter("convertWeekRankView")
        fun convertWeekRankView(view: RecyclerView, curHotStatus: Int) {
            if (curHotStatus == WEEK_HOT_VIDEO) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }

        @JvmStatic
        @BindingAdapter("convertMonthRankView")
        fun convertMonthRankView(view: RecyclerView, curHotStatus: Int) {
            if (curHotStatus == MONTH_HOT_VIDEO) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }

        @JvmStatic
        @BindingAdapter("convertYearRankView")
        fun convertYearRankView(view: RecyclerView, curHotStatus: Int) {
            if (curHotStatus == YEAR_HOT_VIDEO) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }
    }

    @Inject
    lateinit var factory: MyViewModelFactory

    @Inject
    lateinit var videoViewModel: VideoViewModel

    @Inject
    lateinit var mPreferences: MyPreferences

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
        mBinding.curHotStatus = WEEK_HOT_VIDEO
        initUI()

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        videoViewModel = ViewModelProviders.of(this, factory).get(VideoViewModel::class.java)
        videoViewModel.curVideoItem.observe(this, Observer {
            when (mBinding.curHotStatus) {
                WEEK_HOT_VIDEO -> mBinding.weekRankView.adapter?.notifyDataSetChanged()

                MONTH_HOT_VIDEO -> mBinding.monthRankView.adapter?.notifyDataSetChanged()

                YEAR_HOT_VIDEO -> mBinding.yearRankView.adapter?.notifyDataSetChanged()

                else -> {}
            }
        })

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
                SearchViewModel.SEARCH_FINISH -> {
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
        LogMessage.D(TAG, "onActivityResult()")
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initUI() {
        // Week Rank
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.weekRankView.layoutManager = layoutManager
        val adapter = YoutubeAdapter()
        adapter.mVideoList = mPreferences.weekHotVideoList
        adapter.selectIndex = -1
        adapter.videoViewModel = videoViewModel
        mBinding.weekRankView.adapter = adapter
        mBinding.weekRankView.adapter?.notifyDataSetChanged()

        // Month Rank
        val layoutManager2 = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.monthRankView.layoutManager = layoutManager2
        val adapter2 = YoutubeAdapter()
        adapter.mVideoList = mPreferences.monthHotVideoList
        adapter.selectIndex = -1
        adapter.videoViewModel = videoViewModel
        mBinding.monthRankView.adapter = adapter2
        mBinding.monthRankView.adapter?.notifyDataSetChanged()

        // Year Rank
        val layoutManager3 = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.yearRankView.layoutManager = layoutManager3
        val adapter3 = YoutubeAdapter()
        adapter.mVideoList = mPreferences.yearHotVideoList
        adapter.selectIndex = -1
        adapter.videoViewModel = videoViewModel
        mBinding.yearRankView.adapter = adapter3
        mBinding.yearRankView.adapter?.notifyDataSetChanged()

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

            val array = resources.getStringArray(R.array.search_three_state_option)

            when (value) {
                array[0] -> {
                    mBinding.curHotStatus = WEEK_HOT_VIDEO
                    mBinding.weekRankView.adapter?.notifyDataSetChanged()
                }

                array[1] -> mBinding.curHotStatus = MONTH_HOT_VIDEO

                array[2] -> {
                    mBinding.curHotStatus = YEAR_HOT_VIDEO
                    mBinding.yearRankView.adapter?.notifyDataSetChanged()
                }

                else -> {}
            }
        }

        // TODO for test convenience
        mBinding.searchEditText.setText("dyson")
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    private fun performSearch() {
        viewModel.search(mBinding.searchEditText.text.toString().trimStart().trimEnd())
    }
}
