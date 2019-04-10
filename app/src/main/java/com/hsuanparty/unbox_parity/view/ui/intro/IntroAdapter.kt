package com.hsuanparty.unbox_parity.view.ui.intro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import com.hsuanparty.unbox_parity.model.IntroItem


class IntroAdapter(private val mContext: Context, private val onBoardItems: ArrayList<IntroItem>) : PagerAdapter() {
    companion object {
        private val TAG = IntroAdapter::class.java.simpleName
    }

    override fun getCount(): Int {
        return onBoardItems.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = LayoutInflater.from(mContext).inflate(com.hsuanparty.unbox_parity.R.layout.intro_page_item, container, false)

        val item = onBoardItems[position]

        val imageView = itemView.findViewById(com.hsuanparty.unbox_parity.R.id.pageImage) as ImageView
        imageView.setImageResource(item.imageId!!)

        container.addView(itemView)

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as RelativeLayout)
    }
}
