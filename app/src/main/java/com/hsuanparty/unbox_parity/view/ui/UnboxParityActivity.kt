package com.hsuanparty.unbox_parity.view.ui

import android.app.SearchManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.utils.Constants
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.utils.SimpleDelayTask
import com.hsuanparty.unbox_parity.view.ui.article.ArticleViewModel
import com.hsuanparty.unbox_parity.view.ui.parity.ParityViewModel
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel.Companion.SEARCH_FINISH
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

        private const val SEARCH_PAGE_INDEX   = 0
        private const val VIDEO_PAGE_INDEX    = 1
        private const val ARTICLE_PAGE_INDEX  = 2
        private const val PARITY_PAGE_INDEX   = 3
        private const val SETTING_PAGE_INDEX  = 4
        private const val GOOGLE_SEARCH_INDEX = 5
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

    @Inject
    lateinit var articleViewModel: ArticleViewModel

    @Inject
    lateinit var parityViewModel: ParityViewModel

    private var curPageIndex = SEARCH_PAGE_INDEX

    private var isFullScreen = false

    private var isSearchStart = false

    private var isBackPress = false

    private var isEverSearch = false

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        searchPage.view?.visibility = View.GONE
        videoPage.view?.visibility = View.GONE
        articlePage.view?.visibility = View.GONE
        parityPage.view?.visibility = View.GONE
        settingPage.view?.visibility = View.GONE

        when (item.itemId) {
            com.hsuanparty.unbox_parity.R.id.navigation_search -> {
                curPageIndex = SEARCH_PAGE_INDEX
                searchPage.view?.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            com.hsuanparty.unbox_parity.R.id.navigation_video -> {
                curPageIndex = VIDEO_PAGE_INDEX
                videoPage.view?.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            com.hsuanparty.unbox_parity.R.id.navigation_article -> {
                curPageIndex = ARTICLE_PAGE_INDEX
                articlePage.view?.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            com.hsuanparty.unbox_parity.R.id.navigation_parity -> {
                curPageIndex = PARITY_PAGE_INDEX
                parityPage.view?.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            com.hsuanparty.unbox_parity.R.id.navigation_setting -> {
                curPageIndex = SETTING_PAGE_INDEX
                settingPage.view?.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(com.hsuanparty.unbox_parity.R.layout.activity_unbox_parity)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        initViewModel()
        initUI()
    }

    override fun onResume() {
        LogMessage.D(TAG, "onResume()")
        super.onResume()

        mPreferences.readPreferences()

        if (isEverSearch) {
            videoViewModel.searchVideo(this)
            articleViewModel.searchArticle(this)
            parityViewModel.searchParity(this)
        }
    }

    override fun onPause() {
        LogMessage.D(TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        LogMessage.D(TAG, "onStop()")
        super.onStop()

        mPreferences.savePreferences()
    }

    override fun onDestroy() {
        LogMessage.D(TAG, "onDestroy()")
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogMessage.D(TAG, "onActivityResult(), requestCode: $requestCode, resultCode: $resultCode")
    }

    override fun onBackPressed() {
        LogMessage.D(TAG, "onBackPressed()")

        if (isFullScreen) {
            videoViewModel.performExitFullScreen()
            return
        }

        // Show message to ask user whether to leave App
        if (!isBackPress) {
            isBackPress = true
            Toast.makeText(this, getString(com.hsuanparty.unbox_parity.R.string.msg_ask_user_leave_app), Toast.LENGTH_LONG).show()
            SimpleDelayTask.after(2000) {
                isBackPress = false
            }
            return
        }

        mPreferences.isFinishApp = true
        this.finish()
    }

    private fun initViewModel() {
        searchViewModel = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)
        videoViewModel = ViewModelProviders.of(this, factory).get(VideoViewModel::class.java)
        articleViewModel = ViewModelProviders.of(this, factory).get(ArticleViewModel::class.java)
        parityViewModel = ViewModelProviders.of(this, factory).get(ParityViewModel::class.java)

        searchViewModel.isSearchFinish.observe(this, Observer<Int> { status ->
            when (status) {
                SearchViewModel.SEARCH_START -> {
                    if (!isEverSearch) {
                        isEverSearch = true
                    }

                    isSearchStart = true
                    videoViewModel.searchVideo(this)

                    // Search article
                    articleViewModel.searchArticle(this)

                    // Search parity
                    parityViewModel.searchParity(this)
                }

                SearchViewModel.SEARCH_FINISH -> {
                    if (!isSearchStart) {
                        return@Observer
                    }
                    isSearchStart = false

                    setFragmentPage(VIDEO_PAGE_INDEX)
                }

                SearchViewModel.SWITCH_TO_VIDEO_PAGE -> {
                    if (curPageIndex != VIDEO_PAGE_INDEX) {
                        setFragmentPage(VIDEO_PAGE_INDEX)
                    }
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
                    hideToolBr()
                }

                VideoViewModel.EXIT_FULL_SCREEN -> {
                    LogMessage.D(TAG, "Player Exit FullScreen")
                    navigation.visibility = View.VISIBLE
                    isFullScreen = false
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    showSystemUI()
                }

                else -> {}
            }
        })

        videoViewModel.searchVideoFinished.observe(this, Observer { isFinish ->
            if (isFinish) {
                SimpleDelayTask.after(Constants.SEARCH_DELAY_TIME) {
                    searchViewModel.isWaitingLiveData.value = false
                    searchViewModel.isSearchFinish.value = SEARCH_FINISH
                }
            }
        })
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun hideToolBr() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        val uiOptions = window.decorView.systemUiVisibility
        var newUiOptions = uiOptions
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        val isImmersiveModeEnabled = uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY == uiOptions
        if (isImmersiveModeEnabled) {
            LogMessage.D(TAG, "Turning immersive mode mode off. ")
        } else {
            LogMessage.D(TAG, "Turning immersive mode mode on.")
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
//        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        }

        // Status bar hiding: Backwards compatible to Jellybean
//        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_FULLSCREEN
//        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
//        if (Build.VERSION.SDK_INT >= 18) {
//            newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//        }

        window.decorView.systemUiVisibility = newUiOptions
        //END_INCLUDE (set_ui_flags)
    }

    private fun initUI() {
        setFragmentPage(SEARCH_PAGE_INDEX)
        introPage.view?.visibility = View.GONE
    }

    private fun setFragmentPage(pageIndex: Int) {
        LogMessage.D(TAG, "setFragmentPage: $pageIndex")

        when (pageIndex) {
            SEARCH_PAGE_INDEX -> {
                navigation.selectedItemId = com.hsuanparty.unbox_parity.R.id.navigation_search
            }
            VIDEO_PAGE_INDEX -> {
                navigation.selectedItemId = com.hsuanparty.unbox_parity.R.id.navigation_video
            }
            ARTICLE_PAGE_INDEX -> {
                navigation.selectedItemId = com.hsuanparty.unbox_parity.R.id.navigation_article
            }
            PARITY_PAGE_INDEX -> {
                navigation.selectedItemId = com.hsuanparty.unbox_parity.R.id.navigation_parity
            }
            SETTING_PAGE_INDEX -> {
                navigation.selectedItemId = com.hsuanparty.unbox_parity.R.id.navigation_setting
            }

            else -> LogMessage.E(TAG, "Should not happen here...")
        }
    }

    fun showIntroduction() {
        LogMessage.D(TAG, "showIntroduction()")
        introPage.view?.visibility = View.VISIBLE
        navigation.visibility = View.GONE
    }

    fun closeIntroduction() {
        LogMessage.D(TAG, "closeIntroduction()")
        introPage.view?.visibility = View.GONE
        navigation.visibility = View.VISIBLE
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector
}
