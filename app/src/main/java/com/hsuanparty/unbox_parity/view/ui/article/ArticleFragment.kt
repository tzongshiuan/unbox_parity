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
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.ArticleFragmentBinding

import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.ArticleItem
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.utils.MyWebViewClient
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeAdapter
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel
import javax.inject.Inject

class ArticleFragment : Fragment(), Injectable{

    companion object {
        private val TAG = ArticleFragment::class.java.simpleName

        const val DATE_RANGE_NONE  = 0
        const val DATE_RANGE_WEEK  = 1
        const val DATE_RANGE_MONTH = 2
        const val DATE_RANGE_YEAR  = 3

//        @JvmStatic
//        @BindingAdapter("convertNoneDateRange")
//        fun convertNoneDateRange(view: RecyclerView, curDateRange: Int) {
//            if (curDateRange == DATE_RANGE_NONE) {
//                view.visibility = View.VISIBLE
//            } else {
//                view.visibility = View.GONE
//            }
//        }
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
                    mBinding.segmentView.visibility = View.VISIBLE
                }

                else -> {}
            }
        })

        viewModel = ViewModelProviders.of(this, factory).get(ArticleViewModel::class.java)
        viewModel.articleNoneResult.observe(this, Observer { list ->
            onGetSearchResult(list)
        })

        viewModel.articleWeekResult.observe(this, Observer { list ->
            onGetSearchResult(list)
        })

        viewModel.articleMonthResult.observe(this, Observer { list ->
            onGetSearchResult(list)
        })

        viewModel.articleYearResult.observe(this, Observer { list ->
            onGetSearchResult(list)
        })

        viewModel.showArticleContent.observe(this, Observer { articleItem ->
            showUrlContent(articleItem)
        })

        initAdapters()
    }

    private fun onGetSearchResult(list: ArrayList<ArticleItem>) {
        (mBinding.recyclerView.adapter as ArticleAdapter).mArticleList = list
        (mBinding.recyclerView.adapter as ArticleAdapter).selectIndex = -1
        mBinding.recyclerView.adapter?.notifyDataSetChanged()
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

            // Clear data
            val adapter = mBinding.recyclerView.adapter as ArticleAdapter
            (adapter.mArticleList as ArrayList).clear()
            adapter.notifyDataSetChanged()

            when (value) {
                array[0] -> {
                    curFilterStatus = DATE_RANGE_NONE
                    viewModel.curFilterStatus = DATE_RANGE_NONE
                    mBinding.curDateRange = DATE_RANGE_NONE

                    viewModel.searchArticle(activity!!)
                }

                array[1] -> {
                    curFilterStatus = DATE_RANGE_WEEK
                    viewModel.curFilterStatus = DATE_RANGE_WEEK
                    mBinding.curDateRange = DATE_RANGE_WEEK

                    viewModel.searchArticle(activity!!)
                }

                array[2] -> {
                    curFilterStatus = DATE_RANGE_MONTH
                    viewModel.curFilterStatus = DATE_RANGE_MONTH
                    mBinding.curDateRange = DATE_RANGE_MONTH

                    viewModel.searchArticle(activity!!)
                }

                array[3] -> {
                    curFilterStatus = DATE_RANGE_YEAR
                    viewModel.curFilterStatus = DATE_RANGE_YEAR
                    mBinding.curDateRange = DATE_RANGE_YEAR

                    viewModel.searchArticle(activity!!)
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
        mBinding.recyclerView.layoutManager = layoutManager
        val adapter = ArticleAdapter()
        adapter.articleViewModel = viewModel
        mBinding.recyclerView.adapter = adapter
    }

    private fun showUrlContent(articleItem: ArticleItem) {
        mBinding.webView.loadUrl(articleItem.url?.toString())
        changeToWebUI()
    }

    private fun changeToArticleUI() {
        mBinding.segmentView.visibility = View.VISIBLE
        mBinding.webGroup.visibility = View.GONE
        mBinding.recyclerView.visibility = View.VISIBLE
    }

    private fun changeToWebUI() {
        mBinding.segmentView.visibility = View.GONE
        mBinding.webGroup.visibility = View.VISIBLE
        mBinding.recyclerView.visibility = View.GONE
    }
}
