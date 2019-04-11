package com.hsuanparty.unbox_parity.view.ui.article

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.ArticleItemBinding
import com.hsuanparty.unbox_parity.databinding.BannerItemBinding
import com.hsuanparty.unbox_parity.databinding.VideoItemBinding
import com.hsuanparty.unbox_parity.model.ArticleItem
import com.hsuanparty.unbox_parity.model.VideoItem
import com.hsuanparty.unbox_parity.utils.Constants
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeAdapter
import com.hsuanparty.unbox_parity.view.ui.video.VideoViewModel
import com.squareup.picasso.Picasso

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/6
 * Description:
 */
class ArticleAdapter: RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    companion object {
        private val TAG = ArticleAdapter::class.java.simpleName
    }

    var articleViewModel: ArticleViewModel? = null

    var mArticleList: List<ArticleItem> = ArrayList()

    var selectIndex = -1

    // Replace the contents of a view (invoked by the layout manager)
    //filling every item of view with respective text and image
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        LogMessage.D(TAG, "onBindViewHolder()")

        holder.bind(position)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        LogMessage.D(TAG, "onCreateViewHolder()")

        return if (viewType == VideoItem.TYPE_VIDEO) {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ArticleItemBinding.inflate(layoutInflater, parent, false)
            MyViewHolder(binding)
        } else {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = BannerItemBinding.inflate(layoutInflater, parent, false)
            AdViewHolder(binding)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    //here the dataset is mVideoList
    override fun getItemCount(): Int {
        return mArticleList.size
    }

    override fun getItemViewType(position: Int): Int {
        return mArticleList[position].type
    }

    private fun initListener(binding: ArticleItemBinding, position: Int) {
        //setting on click listener for each video_item to launch clicked video in new activity
        binding.articleView.setOnClickListener {
            articleViewModel?.showUrlContent(binding.articleItem)
            selectIndex = position
            this.notifyDataSetChanged()
        }
    }

    abstract inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun bind(position: Int)
    }

    inner class MyViewHolder(private val binding: ArticleItemBinding) : ViewHolder(binding.root) {
        override fun bind(position: Int) {
            val item = mArticleList[position]
            //LogMessage.D(TAG, "position: $position, item title: ${item.title}")

            initListener(binding, position)

            if (selectIndex == position) {
                binding.articleView.setBackgroundColor(ContextCompat.getColor(binding.articleView.context, R.color.orange))
            } else {
                binding.articleView.setBackgroundColor(ContextCompat.getColor(binding.articleView.context, R.color.white))
            }

            this.binding.articleItem = item
            this.binding.executePendingBindings()
        }
    }

    inner class AdViewHolder(private val binding: BannerItemBinding) : ViewHolder(binding.root) {
        override fun bind(position: Int) {

            val adView = AdView(binding.root.context)
            adView.adSize = AdSize.BANNER
            if (Constants.IS_DEBUG_MODE) {
                adView.adUnitId = binding.root.context.getString(R.string.test_banner_id)
            } else {
                adView.adUnitId = binding.root.context.getString(R.string.article_banner_id)
            }

            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            params.addRule(RelativeLayout.CENTER_HORIZONTAL,  RelativeLayout.TRUE)
            adView.layoutParams = params

            binding.bannerView.removeAllViews()
            binding.bannerView.addView(adView)

            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)

            binding.executePendingBindings()
        }
    }
}