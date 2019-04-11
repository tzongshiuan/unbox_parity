package com.hsuanparty.unbox_parity.view.ui.article

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import androidx.databinding.BindingAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.ArticleFragmentBinding

import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.ArticleItem
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.utils.MyWebViewClient
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

    private var curFilterStatus = DATE_RANGE_NONE

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

        viewModel = ViewModelProviders.of(this, factory).get(ArticleViewModel::class.java)
        viewModel.articleNoneResult.observe(this, Observer { list ->
            (mBinding.noneView.adapter as ArticleAdapter).mArticleList = list
            (mBinding.noneView.adapter as ArticleAdapter).selectIndex = -1
            mBinding.noneView.adapter?.notifyDataSetChanged()
        })

        viewModel.articleWeekResult.observe(this, Observer { list ->
            (mBinding.weekView.adapter as ArticleAdapter).mArticleList = list
            (mBinding.weekView.adapter as ArticleAdapter).selectIndex = -1
            mBinding.weekView.adapter?.notifyDataSetChanged()
        })

        viewModel.articleMonthResult.observe(this, Observer { list ->
            (mBinding.monthView.adapter as ArticleAdapter).mArticleList = list
            (mBinding.monthView.adapter as ArticleAdapter).selectIndex = -1
            mBinding.monthView.adapter?.notifyDataSetChanged()
        })

        viewModel.articleYearResult.observe(this, Observer { list ->
            (mBinding.yearView.adapter as ArticleAdapter).mArticleList = list
            (mBinding.yearView.adapter as ArticleAdapter).selectIndex = -1
            mBinding.yearView.adapter?.notifyDataSetChanged()
        })

        viewModel.showArticleContent.observe(this, Observer { articleItem ->
            showUrlContent(articleItem)
        })

        initAdapters()
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

        viewModel.removeObservers(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initUI() {
        mBinding.webView.settings.javaScriptEnabled = true
        mBinding.webView.webViewClient = MyWebViewClient()

        mBinding.segmentView.setOnSelectionChangedListener { identifier, value ->
            LogMessage.D(TAG, "identifier: $identifier, value: $value")

            val array = resources.getStringArray(R.array.article_state_option)

            when (value) {
                array[0] -> {
                    curFilterStatus = DATE_RANGE_NONE
                    mBinding.curDateRange = DATE_RANGE_NONE
                }

                array[1] -> {
                    curFilterStatus = DATE_RANGE_WEEK
                    mBinding.curDateRange = DATE_RANGE_WEEK
                }

                array[2] -> {
                    curFilterStatus = DATE_RANGE_MONTH
                    mBinding.curDateRange = DATE_RANGE_MONTH
                }

                array[3] -> {
                    curFilterStatus = DATE_RANGE_YEAR
                    mBinding.curDateRange = DATE_RANGE_YEAR
                }

                else -> {}
            }
        }

        mBinding.previousBtn.setOnClickListener {
            if (mBinding.webView.canGoBack()) {
                mBinding.webView.goBack()
            }
        }

        mBinding.closeBtn.setOnClickListener {
            changeToArticleUI()
        }

        mBinding.nextBtn.setOnClickListener {
            if (mBinding.webView.canGoForward()) {
                mBinding.webView.goForward()
            }
        }

        mBinding.browserBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(mBinding.webView.url.toString())
            activity?.startActivity(intent)
        }
    }

    private fun initAdapters() {
        // None filter
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.noneView.layoutManager = layoutManager
        val adapter = ArticleAdapter()
        adapter.articleViewModel = viewModel
        mBinding.noneView.adapter = adapter

        // Week filter
        val layoutManager2 = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.weekView.layoutManager = layoutManager2
        val adapter2 = ArticleAdapter()
        adapter2.articleViewModel = viewModel
        mBinding.weekView.adapter = adapter2

        // Month filter
        val layoutManager3 = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.monthView.layoutManager = layoutManager3
        val adapter3 = ArticleAdapter()
        adapter3.articleViewModel = viewModel
        mBinding.monthView.adapter = adapter3

        // Year filter
        val layoutManager4 = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.yearView.layoutManager = layoutManager4
        val adapter4 = ArticleAdapter()
        adapter4.articleViewModel = viewModel
        mBinding.yearView.adapter = adapter4
    }

    private fun showUrlContent(articleItem: ArticleItem) {
        mBinding.webView.loadUrl(articleItem.url?.toString())
        changeToWebUI()
    }

    private fun changeToArticleUI() {
        mBinding.articleGroup.visibility = View.VISIBLE
        mBinding.webGroup.visibility = View.GONE

        when (curFilterStatus) {
            DATE_RANGE_NONE -> mBinding.noneView.visibility = View.VISIBLE
            DATE_RANGE_WEEK -> mBinding.weekView.visibility = View.VISIBLE
            DATE_RANGE_MONTH -> mBinding.monthView.visibility = View.VISIBLE
            DATE_RANGE_YEAR -> mBinding.yearView.visibility = View.VISIBLE
            else -> {}
        }
    }

    private fun changeToWebUI() {
        mBinding.articleGroup.visibility = View.GONE
        mBinding.webGroup.visibility = View.VISIBLE

        when (curFilterStatus) {
            DATE_RANGE_NONE -> mBinding.noneView.visibility = View.GONE
            DATE_RANGE_WEEK -> mBinding.weekView.visibility = View.GONE
            DATE_RANGE_MONTH -> mBinding.monthView.visibility = View.GONE
            DATE_RANGE_YEAR -> mBinding.yearView.visibility = View.GONE
            else -> {}
        }
    }
}
