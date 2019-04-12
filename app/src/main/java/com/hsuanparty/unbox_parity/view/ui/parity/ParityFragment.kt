package com.hsuanparty.unbox_parity.view.ui.parity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.ParityFragmentBinding
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel
import javax.inject.Inject

class ParityFragment : Fragment(), Injectable{

    companion object {
        private val TAG = ParityFragment::class.java.simpleName

        @JvmStatic
        @BindingAdapter("convertRelativeOrder")
        fun convertRelativeOrder(view: Button, curOrderStatus: Int) {
            if (curOrderStatus == ParityViewModel.ORDER_RELATIVE) {
                view.setBackgroundResource(R.drawable.btn_parity_order_pressed)
                view.setTextColor(ContextCompat.getColor(view.context, R.color.white))
            } else {
                view.setBackgroundResource(R.drawable.btn_parity_order_selector)
                view.setTextColor(ContextCompat.getColor(view.context, R.color.color_parity_order_btn_selector))
            }
        }

        @JvmStatic
        @BindingAdapter("convertLow2HighOrder")
        fun convertLow2HighOrder(view: Button, curOrderStatus: Int) {
            if (curOrderStatus == ParityViewModel.ORDER_LOW_TO_HIGH) {
                view.setBackgroundResource(R.drawable.btn_parity_order_pressed)
                view.setTextColor(ContextCompat.getColor(view.context, R.color.white))
            } else {
                view.setBackgroundResource(R.drawable.btn_parity_order_selector)
                view.setTextColor(ContextCompat.getColor(view.context, R.color.color_parity_order_btn_selector))
            }
        }

        @JvmStatic
        @BindingAdapter("convertHighToLowOrder")
        fun convertHighToLowOrder(view: Button, curOrderStatus: Int) {
            if (curOrderStatus == ParityViewModel.ORDER_HIGH_TO_LOW) {
                view.setBackgroundResource(R.drawable.btn_parity_order_pressed)
                view.setTextColor(ContextCompat.getColor(view.context, R.color.white))
            } else {
                view.setBackgroundResource(R.drawable.btn_parity_order_selector)
                view.setTextColor(ContextCompat.getColor(view.context, R.color.color_parity_order_btn_selector))
            }
        }

        @JvmStatic
        @BindingAdapter("convertPageNum")
        fun convertPageNum(view: TextView, pageNum: Int) {
            val text = String.format("第%d頁", pageNum)
            view.text = text
        }
    }

    @Inject
    lateinit var factory: MyViewModelFactory

    @Inject
    lateinit var searchViewModel: SearchViewModel

    private lateinit var viewModel: ParityViewModel

    private lateinit var mBinding: ParityFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogMessage.D(TAG, "onCreateView()")
        mBinding = ParityFragmentBinding.inflate(inflater, container, false)
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

        viewModel = ViewModelProviders.of(this, factory).get(ParityViewModel::class.java)
        viewModel.parityResult.observe(this, Observer { list ->
            if (list.size == 0) {
                // Not found any result
                mBinding.noResultGroup.visibility = View.VISIBLE
                mBinding.parityGroup.visibility = View.GONE
            } else {
                // Show result on the UI
                mBinding.noResultGroup.visibility = View.GONE
                mBinding.parityGroup.visibility = View.VISIBLE
                (mBinding.parityView.adapter as ParityAdapter).mParityList = list
                (mBinding.parityView.adapter as ParityAdapter).selectIndex = -1
                (mBinding.parityView.adapter as ParityAdapter).parityViewModel = viewModel
                mBinding.parityView.adapter?.notifyDataSetChanged()

                mBinding.curPageNum = viewModel.curPageNum
            }
        })

        viewModel.pageState.observe(this, Observer { state ->
            when (state) {
                ParityViewModel.PAGE_STATE_FIRST -> {
                    mBinding.previousBtn.isEnabled = false
                    mBinding.nextBtn.isEnabled = true
                }
                ParityViewModel.PAGE_STATE_MIDDLE -> {
                    mBinding.previousBtn.isEnabled = true
                    mBinding.nextBtn.isEnabled = true
                }
                ParityViewModel.PAGE_STATE_END -> {
                    mBinding.previousBtn.isEnabled = true
                    mBinding.nextBtn.isEnabled = false
                }
            }
        })

        // init parity order status
        mBinding.curOrderStatus = viewModel.curOrderStatus
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
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.parityView.layoutManager = layoutManager
        val adapter = ParityAdapter()
        mBinding.parityView.adapter = adapter

        mBinding.relativeBtn.setOnClickListener {
            mBinding.curOrderStatus = ParityViewModel.ORDER_RELATIVE
            viewModel.curOrderStatus = ParityViewModel.ORDER_RELATIVE
            pageRestore()
            viewModel.searchParity()
        }

        mBinding.low2highBtn.setOnClickListener {
            mBinding.curOrderStatus = ParityViewModel.ORDER_LOW_TO_HIGH
            viewModel.curOrderStatus = ParityViewModel.ORDER_LOW_TO_HIGH
            pageRestore()
            viewModel.searchParity()
        }

        mBinding.high2lowBtn.setOnClickListener {
            mBinding.curOrderStatus = ParityViewModel.ORDER_HIGH_TO_LOW
            viewModel.curOrderStatus = ParityViewModel.ORDER_HIGH_TO_LOW
            pageRestore()
            viewModel.searchParity()
        }

        mBinding.previousBtn.setOnClickListener {
            pagePrevious()
            viewModel.searchParity()
        }

        mBinding.nextBtn.setOnClickListener {
            pageNext()
            viewModel.searchParity()
        }
    }

    private fun pageRestore() {
        viewModel.curPageNum = 1
        mBinding.curPageNum = viewModel.curPageNum
    }

    private fun pageNext() {
        viewModel.curPageNum += 1
        mBinding.curPageNum = viewModel.curPageNum
    }

    private fun pagePrevious() {
        viewModel.curPageNum -= 1
        mBinding.curPageNum = viewModel.curPageNum
    }
}
