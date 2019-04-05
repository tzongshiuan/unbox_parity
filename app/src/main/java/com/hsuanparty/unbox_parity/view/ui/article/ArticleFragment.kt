package com.hsuanparty.unbox_parity.view.ui.article

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.ArticleFragmentBinding

import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel
import javax.inject.Inject

class ArticleFragment : Fragment(), Injectable{

    companion object {
        private val TAG = ArticleFragment::class.java.simpleName

        const val DATE_RANGE_NONE  = 0
        const val DATE_RANGE_WEEK  = 1
        const val DATE_RANGE_MONTH = 2
        const val DATE_RANGE_YEAR  = 3

        @JvmStatic
        @BindingAdapter("convertNoneDateRange")
        fun convertNoneDateRange(view: RecyclerView, curDateRange: Int) {
            if (curDateRange == DATE_RANGE_NONE) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }

        @JvmStatic
        @BindingAdapter("convertWeekDateRange")
        fun convertWeekDateRange(view: RecyclerView, curDateRange: Int) {
            if (curDateRange == DATE_RANGE_WEEK) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }

        @JvmStatic
        @BindingAdapter("convertMonthDateRange")
        fun convertMonthDateRange(view: RecyclerView, curDateRange: Int) {
            if (curDateRange == DATE_RANGE_MONTH) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }

        @JvmStatic
        @BindingAdapter("convertYearDateRange")
        fun convertYearDateRange(view: RecyclerView, curDateRange: Int) {
            if (curDateRange == DATE_RANGE_YEAR) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }
    }

    @Inject
    lateinit var factory: MyViewModelFactory

    @Inject
    lateinit var searchViewModel: SearchViewModel

    private lateinit var viewModel: ArticleViewModel

    private lateinit var mBinding: ArticleFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogMessage.D(TAG, "onCreateView()")
        mBinding = ArticleFragmentBinding.inflate(inflater, container, false)
        initUI()

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        searchViewModel = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)
        searchViewModel.isSearchFinish.observe(this, Observer<Int> { status ->
            when (status) {
                SearchViewModel.SEARCH_START -> {
                    mBinding.noDataGroup.visibility = View.VISIBLE
                }

                SearchViewModel.SEARCH_FINISH -> {
                    mBinding.noDataGroup.visibility = View.GONE
                }

                else -> {}
            }
        })

        viewModel = ViewModelProviders.of(this).get(ArticleViewModel::class.java)
        viewModel.articleNoneResult.observe(this, Observer { list ->
//            (mBinding.recyclerView.adapter as YoutubeAdapter).mVideoList = result
//            (mBinding.recyclerView.adapter as YoutubeAdapter).selectIndex = -1
//            mBinding.recyclerView.adapter?.notifyDataSetChanged()
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
        mBinding.segmentView.setOnSelectionChangedListener { identifier, value ->
            LogMessage.D(TAG, "identifier: $identifier, value: $value")

            val array = resources.getStringArray(R.array.article_state_option)

            when (value) {
                array[0] -> {
                    mBinding.curDateRange = DATE_RANGE_NONE
                }

                array[1] -> {
                    mBinding.curDateRange = DATE_RANGE_WEEK
                }

                array[2] -> {
                    mBinding.curDateRange = DATE_RANGE_MONTH
                }

                array[3] -> {
                    mBinding.curDateRange = DATE_RANGE_YEAR
                }

                else -> {}
            }
        }
    }
}
