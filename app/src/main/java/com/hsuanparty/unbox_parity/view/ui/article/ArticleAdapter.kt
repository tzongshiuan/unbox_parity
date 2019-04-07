package com.hsuanparty.unbox_parity.view.ui.article

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.ArticleItemBinding
import com.hsuanparty.unbox_parity.databinding.VideoItemBinding
import com.hsuanparty.unbox_parity.model.ArticleItem
import com.hsuanparty.unbox_parity.model.VideoItem
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.view.ui.video.VideoViewModel
import com.squareup.picasso.Picasso

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/6
 * Description:
 */
class ArticleAdapter: RecyclerView.Adapter<ArticleAdapter.MyViewHolder>() {

    companion object {
        private val TAG = ArticleAdapter::class.java.simpleName
    }

    var articleViewModel: ArticleViewModel? = null

    var mArticleList: List<ArticleItem> = ArrayList()

    var selectIndex = -1

    // Replace the contents of a view (invoked by the layout manager)
    //filling every item of view with respective text and image
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        LogMessage.D(TAG, "onBindViewHolder()")

        holder.bind(position)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        LogMessage.D(TAG, "onCreateViewHolder()")

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ArticleItemBinding.inflate(layoutInflater, parent, false)

        return MyViewHolder(binding)
    }

    // Return the size of your dataset (invoked by the layout manager)
    //here the dataset is mVideoList
    override fun getItemCount(): Int {
        return mArticleList.size
    }

    private fun initListener(binding: ArticleItemBinding, position: Int) {
        //setting on click listener for each video_item to launch clicked video in new activity
        binding.articleView.setOnClickListener {
            articleViewModel?.showUrlContent(binding.articleItem)
            selectIndex = position
            this.notifyDataSetChanged()
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    inner class MyViewHolder(private val binding: ArticleItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
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
}