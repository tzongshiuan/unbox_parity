package com.hsuanparty.unbox_parity.view.ui.search

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
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
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeConnector
import com.hsuanparty.unbox_parity.view.ui.video.VideoViewModel
import javax.inject.Inject
import android.speech.RecognizerIntent
import android.app.Activity.RESULT_OK
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeConnectorV2


class SearchFragment : Fragment(), Injectable {

    companion object {
        private val TAG = SearchFragment::class.java.simpleName

        private const val REQUEST_VOICE = 123

//        @JvmStatic
//        @BindingAdapter("convertWeekRankView")
//        fun convertWeekRankView(view: RecyclerView, curHotStatus: Int) {
//            if (curHotStatus == YoutubeConnector.DAILY_HOT_VIDEO) {
//                view.visibility = View.VISIBLE
//            } else {
//                view.visibility = View.GONE
//            }
//        }
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
        mBinding.curHotStatus = YoutubeConnector.DAILY_HOT_VIDEO
        initUI()

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onActivityCreated()")
        super.onActivityCreated(savedInstanceState)

        videoViewModel = ViewModelProviders.of(this, factory).get(VideoViewModel::class.java)
        videoViewModel.curVideoItem.observe(this, Observer {
            mBinding.rankView.adapter?.notifyDataSetChanged()
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

        val yc = YoutubeConnectorV2("")
        object : Thread() {
            override fun run() {
                mPreferences.hotVideoList.addAll(
                    yc.searchHotVideo(mBinding.curHotStatus!!, "開箱") as ArrayList
                )
                refreshView()
            }
        }.start()
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

        viewModel.removeObservers(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        LogMessage.D(TAG, "onActivityResult()")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_VOICE && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition engine thought it heard
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches?.isEmpty()!!) {
                val query = matches[0]
                mBinding.searchEditText.setText(query)
                performSearch()
            }
        }
    }

    private fun initUI() {
        // Week Rank
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.rankView.layoutManager = layoutManager
        val adapter = YoutubeAdapter()
        adapter.videoViewModel = videoViewModel
        mBinding.rankView.adapter = adapter

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
            // Voice record and then search
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.msg_voice_search))
            startActivityForResult(intent, REQUEST_VOICE)
        }

        mBinding.segmentView.setOnSelectionChangedListener { identifier, value ->
            LogMessage.D(TAG, "identifier: $identifier, value: $value")

            val yc = YoutubeConnectorV2("")
            val array = resources.getStringArray(com.hsuanparty.unbox_parity.R.array.search_three_state_option)

            mPreferences.hotVideoList.clear()

            // Clear data
            val adapter = mBinding.rankView.adapter as YoutubeAdapter
            (adapter.mVideoList as ArrayList).clear()
            adapter.notifyDataSetChanged()

            when (value) {
                array[0] -> {
                    mBinding.curHotStatus = YoutubeConnector.DAILY_HOT_VIDEO

                    object : Thread() {
                        override fun run() {
                            mPreferences.hotVideoList.addAll(
                                yc.searchHotVideo(YoutubeConnector.DAILY_HOT_VIDEO, "開箱") as ArrayList
                            )
                            refreshView()
                        }
                    }.start()
                }

                array[1] -> {
                    mBinding.curHotStatus = YoutubeConnector.WEEKLY_HOT_VIDEO

                    object : Thread() {
                        override fun run() {
                            mPreferences.hotVideoList.addAll(
                                yc.searchHotVideo(YoutubeConnector.WEEKLY_HOT_VIDEO, "開箱") as ArrayList
                            )
                            refreshView()
                        }
                    }.start()
                }

                array[2] -> {
                    mBinding.curHotStatus = YoutubeConnector.MONTHLY_HOT_VIDEO

                    object : Thread() {
                        override fun run() {
                            mPreferences.hotVideoList.addAll(
                                yc.searchHotVideo(YoutubeConnector.MONTHLY_HOT_VIDEO, "開箱") as ArrayList
                            )
                            refreshView()
                        }
                    }.start()
                }

                else -> {}
            }
        }

        // TODO for test convenience
        //mBinding.searchEditText.setText("dyson")
    }

    private fun refreshView() {
        activity?.runOnUiThread {
            val adapter = mBinding.rankView.adapter as YoutubeAdapter
            adapter.selectIndex = -1
            adapter.mVideoList = mPreferences.hotVideoList
            adapter.notifyDataSetChanged()
        }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    private fun performSearch() {
        //this.activity?.runOnUiThread {
            viewModel.search(mBinding.searchEditText.text.toString().trimStart().trimEnd())
        //}
    }
}
