package com.hsuanparty.unbox_parity.view.ui

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.view.ui.article.ArticleFragment
import com.hsuanparty.unbox_parity.view.ui.parity.ParityFragment
import com.hsuanparty.unbox_parity.view.ui.search.SearchFragment
import com.hsuanparty.unbox_parity.view.ui.setting.SettingFragment
import com.hsuanparty.unbox_parity.view.ui.video.VideoFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_unbox_parity.*
import javax.inject.Inject

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

    private var curPageIndex = SEARCH_PAGE_INDEX

//    private lateinit var searchFrag: SearchFragment
//    private lateinit var videoFrag: VideoFragment
//    private lateinit var articleFrag: ArticleFragment
//    private lateinit var parityFrag: ParityFragment
//    private lateinit var settingFrag: SettingFragment
//
//    private lateinit var serchPage: View
//    private lateinit var videoPage: View
//    private lateinit var articlePage: View
//    private lateinit var parityPage: View
//    private lateinit var settingPage: View

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_search -> {
                setFragmentPage(SEARCH_PAGE_INDEX)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_video -> {
                setFragmentPage(VIDEO_PAGE_INDEX)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_article -> {
                setFragmentPage(ARTICLE_PAGE_INDEX)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_parity -> {
                setFragmentPage(PARITY_PAGE_INDEX)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                setFragmentPage(SETTING_PAGE_INDEX)
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

        // TODO show message to ask user whether to leave App
        mPreferences.isFinishApp = true
        this.finish()
    }

    private fun initUI() {
        setFragmentPage(SEARCH_PAGE_INDEX)
    }

    private fun setFragmentPage(pageIndex: Int) {
        curPageIndex = pageIndex

        searchPage.view?.visibility = View.GONE
        videoPage.view?.visibility = View.GONE
        articlePage.view?.visibility = View.GONE
        parityPage.view?.visibility = View.GONE
        settingPage.view?.visibility = View.GONE

        when (pageIndex) {
            SEARCH_PAGE_INDEX -> searchPage.view?.visibility = View.VISIBLE
            VIDEO_PAGE_INDEX -> videoPage.view?.visibility = View.VISIBLE
            ARTICLE_PAGE_INDEX -> articlePage.view?.visibility = View.VISIBLE
            PARITY_PAGE_INDEX -> parityPage.view?.visibility = View.VISIBLE
            SETTING_PAGE_INDEX -> settingPage.view?.visibility = View.VISIBLE

            else -> LogMessage.E(TAG, "Should not happen here...")
        }
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector
}
