package com.hsuanparty.unbox_parity.view.ui.intro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.hsuanparty.unbox_parity.databinding.IntroFragmentBinding
import com.hsuanparty.unbox_parity.model.IntroItem
import com.hsuanparty.unbox_parity.utils.LogMessage
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.view.ui.UnboxParityActivity


class IntroFragment : Fragment() {
    companion object {
        private val TAG = IntroFragment::class.java.simpleName
    }

    private lateinit var mBinding: IntroFragmentBinding

    private lateinit var adapter: IntroAdapter

    private val pagerItemList: ArrayList<IntroItem> = ArrayList()

    private var dotsCount = 0
    private var previousPos = 0
    private val dots: ArrayList<ImageView> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogMessage.D(TAG, "onCreateView()")
        mBinding = IntroFragmentBinding.inflate(inflater, container, false)
        initUI()

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onActivityCreated()")
        super.onActivityCreated(savedInstanceState)
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
        loadData()

        adapter = IntroAdapter(context!!, pagerItemList)
        mBinding.introPager.adapter = adapter
        mBinding.introPager.currentItem = 0
        mBinding.introPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                LogMessage.D(TAG, "onPageScrolled(), position: $position")

                for (dot in dots) {
                    dot.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.pager_indicator_none))
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.pager_indicator))

                val pos = position + 1
                if (pos == dotsCount && previousPos == (dotsCount - 2)) {
                    showAnimation()
                } else if (pos == (dotsCount-1) && previousPos == (dotsCount - 1)) {
                    hideAnimation()
                }
                previousPos = position
            }
            override fun onPageSelected(position: Int) {

            }
        })

        setUiPageViewController()

        // Get start button
        mBinding.startBtn.setOnClickListener {
            mBinding.introPager.currentItem = 0
            mBinding.startBtn.visibility = View.GONE
            (activity as UnboxParityActivity).closeIntroduction()
        }
    }

    private fun loadData() {
        val imageId = intArrayOf(
            com.hsuanparty.unbox_parity.R.mipmap.intro_page_1, com.hsuanparty.unbox_parity.R.mipmap.intro_page_2
                                        , com.hsuanparty.unbox_parity.R.mipmap.intro_page_3, com.hsuanparty.unbox_parity.R.mipmap.intro_page_4)

        for (id in imageId) {
            val item = IntroItem()
            item.imageId = id
            pagerItemList.add(item)
        }
    }

    private fun setUiPageViewController() {
        dotsCount = adapter.count

        dots.clear()
        for (i in 0..(dotsCount-1)) {
            val imageView = ImageView(context)
            imageView.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.pager_indicator_none))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(6, 0, 6, 0);

            mBinding.pagerCountDots.addView(imageView, params)
            dots.add(imageView)
        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.pager_indicator))
    }

    private fun showAnimation() {
        val show = AnimationUtils.loadAnimation(context!!, R.anim.slide_up_anim)

        mBinding.startBtn.startAnimation(show)
        show.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                mBinding.startBtn.clearAnimation()
            }

            override fun onAnimationStart(animation: Animation?) {
                mBinding.startBtn.visibility = View.VISIBLE
            }
        })
    }

    private fun hideAnimation() {
        val hide = AnimationUtils.loadAnimation(context!!, R.anim.slide_down_anim)

        mBinding.startBtn.startAnimation(hide)
        hide.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                mBinding.startBtn.clearAnimation()
                mBinding.startBtn.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
    }
}
