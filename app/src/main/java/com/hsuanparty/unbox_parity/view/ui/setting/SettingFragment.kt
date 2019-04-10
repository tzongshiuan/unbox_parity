package com.hsuanparty.unbox_parity.view.ui.setting

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.databinding.SettingFragmentBinding

import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.PreferencesHelper
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.view.ui.parity.ParityAdapter
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel
import com.squareup.picasso.Picasso
import javax.inject.Inject

class SettingFragment : Fragment(), Injectable{

    companion object {
        private val TAG = SettingFragment::class.java.simpleName
    }

    @Inject
    lateinit var factory: MyViewModelFactory

    @Inject
    lateinit var mPreferences: PreferencesHelper

    @Inject
    lateinit var searchViewModel: SearchViewModel

    private lateinit var viewModel: SettingViewModel

    private lateinit var mBinding: SettingFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogMessage.D(TAG, "onCreateView()")
        mBinding = SettingFragmentBinding.inflate(inflater, container, false)
        initUI()
        initSetting()

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(SettingViewModel::class.java)

        searchViewModel = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)
        searchViewModel.isSearchFinish.observe(this, Observer<Int> { status ->
            when (status) {
                SearchViewModel.SEARCH_START -> {
                    //updateRecentView()
                }

                SearchViewModel.SEARCH_FINISH -> {
                }

                else -> {}
            }
        })

        searchViewModel.isWaitingLiveData.observe(this, Observer<Boolean> { isWaiting ->
            if (isWaiting) {
                mBinding.waitingDialog.setVisibleWithAnimate(true)
            } else {
                mBinding.waitingDialog.setVisibleImmediately(false)
            }
        })
    }

    override fun onResume() {
        LogMessage.D(TAG, "onResume()")
        super.onResume()

        updateRecentView()
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
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.recentView.layoutManager = layoutManager
        val adapter = RecentKeywordAdapter()
        mBinding.recentView.adapter = adapter

        mBinding.logoutBtn.setOnClickListener {
            viewModel.logout(activity)
        }

        mBinding.manualBtn.setOnClickListener {
            // Show app instruction
        }
    }

    private fun initSetting() {
        // Show profile information on UI
        mBinding.userName.text = mPreferences.userName

        if (mPreferences.photo.isNotEmpty()) {
            Picasso.get()
                .load(mPreferences.photo)
                .resize(300, 300)
                .centerCrop()
                .into(mBinding.userImage)
        }
    }

    private fun updateRecentView() {
        LogMessage.D(TAG, "updateRecentView(), list.size: ${mPreferences.recentKeywordList.size}")
        // Update recent search keywords
        (mBinding.recentView.adapter as RecentKeywordAdapter).mRecentKeywordList = mPreferences.recentKeywordList
        (mBinding.recentView.adapter as RecentKeywordAdapter).searchViewModel = searchViewModel
        mBinding.recentView.adapter?.notifyDataSetChanged()
    }
}
