package com.hsuanparty.unbox_parity.view.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.view.ui.article.ArticleFragment
import com.hsuanparty.unbox_parity.view.ui.parity.ParityFragment
import com.hsuanparty.unbox_parity.view.ui.search.SearchFragment
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel.Companion.SEARCH_FINISH
import com.hsuanparty.unbox_parity.view.ui.setting.SettingFragment
import com.hsuanparty.unbox_parity.view.ui.video.VideoFragment
import com.hsuanparty.unbox_parity.view.ui.video.VideoViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_unbox_parity.*
import javax.inject.Inject

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/29
 */
class UnboxParityActivity : AppCompatActivity(), HasSupportFragmentInjector, Injectable {

    companion object {
        private val TAG = UnboxParityActivity::class.java.simpleName

        private const val SEARCH_PAGE_INDEX  = 0
        private const val VIDEO_PAGE_INDEX   = 1
        private const val ARTICLE_PAGE_INDEX = 2
        private const val PARITY_PAGE_INDEX  = 3
        private const val SETTING_PAGE_INDEX = 4
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var mPreferences: MyPreferences

    @Inject
    lateinit var factory: MyViewModelFactory

    @Inject
    lateinit var searchViewModel: SearchViewModel

    @Inject
    lateinit var videoViewModel: VideoViewModel

    private var curPageIndex = SEARCH_PAGE_INDEX

    private var isFullScreen = false

    private var isSearchStart = false

    // Need to be search first to enter video/article/parity pages
    private var isEverSearch = false

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (!isEverSearch &&
            item.itemId != R.id.navigation_search &&
            item.itemId != R.id.navigation_setting) {
            Toast.makeText(this, getString(R.string.txt_need_search_first), Toast.LENGTH_SHORT).show()
            return@OnNavigationItemSelectedListener false
        }


        searchPage.view?.visibility = View.GONE
        videoPage.view?.visibility = View.GONE
        articlePage.view?.visibility = View.GONE
        parityPage.view?.visibility = View.GONE
        settingPage.view?.visibility = View.GONE

        when (item.itemId) {
            R.id.navigation_search -> {
                searchPage.view?.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_video -> {
                videoPage.view?.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_article -> {
                articlePage.view?.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_parity -> {
                parityPage.view?.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                settingPage.view?.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unbox_parity)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        initViewModel()
        initUI()
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

    override fun onBackPressed() {
        LogMessage.D(TAG, "onBackPressed()")

        if (isFullScreen) {
            videoViewModel.performExitFullScreen()
            return
        }

        // TODO show message to ask user whether to leave App
        mPreferences.isFinishApp = true
        this.finish()
    }

    private fun initViewModel() {
        searchViewModel = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)
        videoViewModel = ViewModelProviders.of(this, factory).get(VideoViewModel::class.java)

        searchViewModel.isSearchFinish.observe(this, Observer<Int> { status ->
            when (status) {
                SearchViewModel.SEARCH_START -> {
                    isSearchStart = true
                    videoViewModel.searchVideo(this)
                }

                SearchViewModel.SEARCH_FINISH -> {
                    if (!isSearchStart) {
                        return@Observer
                    }
                    isSearchStart = false

                    if (!isEverSearch) {
                        isEverSearch = true
                    }

                    setFragmentPage(VIDEO_PAGE_INDEX)
                }

                else -> {}
            }
        })

        videoViewModel.screenStatusLiveData.observe(this, Observer { status ->
            when (status) {
                VideoViewModel.ENTER_FULL_SCREEN -> {
                    LogMessage.D(TAG, "Player Enter FullScreen")
                    navigation.visibility = View.GONE
                    isFullScreen = true
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }

                VideoViewModel.EXIT_FULL_SCREEN -> {
                    LogMessage.D(TAG, "Player Exit FullScreen")
                    navigation.visibility = View.VISIBLE
                    isFullScreen = false
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

                else -> {}
            }
        })

        videoViewModel.searchVideoFinished.observe(this, Observer { isFinish ->
            if (isFinish) {
                searchViewModel.isWaitingLiveData.value = false
                searchViewModel.isSearchFinish.value = SEARCH_FINISH
            }
        })
    }

    private fun initUI() {
        setFragmentPage(SEARCH_PAGE_INDEX)
    }

    private fun setFragmentPage(pageIndex: Int) {
        LogMessage.D(TAG, "setFragmentPage: $pageIndex")

        curPageIndex = pageIndex

        when (pageIndex) {
            SEARCH_PAGE_INDEX -> {
                navigation.selectedItemId = R.id.navigation_search
            }
            VIDEO_PAGE_INDEX -> {
                navigation.selectedItemId = R.id.navigation_video
            }
            ARTICLE_PAGE_INDEX -> {
                navigation.selectedItemId = R.id.navigation_article
            }
            PARITY_PAGE_INDEX -> {
                navigation.selectedItemId = R.id.navigation_parity
            }
            SETTING_PAGE_INDEX -> {
                navigation.selectedItemId = R.id.navigation_setting
            }

            else -> LogMessage.E(TAG, "Should not happen here...")
        }
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector
}
