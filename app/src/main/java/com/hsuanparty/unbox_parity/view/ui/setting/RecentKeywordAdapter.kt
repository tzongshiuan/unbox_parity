package com.hsuanparty.unbox_parity.view.ui.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.databinding.RecentKeywordItemBinding
import com.hsuanparty.unbox_parity.model.RecentKeywordItem
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/9
 * Description:
 */
class RecentKeywordAdapter: RecyclerView.Adapter<RecentKeywordAdapter.MyViewHolder>() {

    companion object {
        private val TAG = RecentKeywordAdapter::class.java.simpleName

        @JvmStatic
        @BindingAdapter("convertRecentDateTime")
        fun convertRecentDateTime(view: TextView, dateTime: Long) {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            val date = sdf.format(Date(dateTime))
            view.text = date
        }
    }

    var searchViewModel: SearchViewModel? = null

    var mRecentKeywordList: List<RecentKeywordItem> = ArrayList()

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
        val binding = com.hsuanparty.unbox_parity.databinding.RecentKeywordItemBinding.inflate(layoutInflater, parent, false)

        return MyViewHolder(binding)
    }

    // Return the size of your dataset (invoked by the layout manager)
    //here the dataset is mVideoList
    override fun getItemCount(): Int {
        return mRecentKeywordList.size
    }

    private fun initListener(binding: RecentKeywordItemBinding, position: Int) {
        //setting on click listener for each video_item to launch clicked video in new activity
        binding.keywordBtn.setOnClickListener {
            searchViewModel?.doSearch(binding.recentKeywordItem?.keyword!!)
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    inner class MyViewHolder(private val binding: RecentKeywordItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val item = mRecentKeywordList[position]
            LogMessage.D(TAG, "position: $position, keyword: ${item.keyword}")

            initListener(binding, position)

            this.binding.recentKeywordItem = item
            this.binding.executePendingBindings()
        }
    }
}