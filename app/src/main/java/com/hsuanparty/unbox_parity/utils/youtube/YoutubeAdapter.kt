package com.hsuanparty.unbox_parity.utils.youtube

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.VideoItemBinding
import com.hsuanparty.unbox_parity.model.VideoItem
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.view.ui.video.VideoViewModel
import com.squareup.picasso.Picasso

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/1
 * Description:
 */
class YoutubeAdapter: RecyclerView.Adapter<YoutubeAdapter.MyViewHolder>() {

    companion object {
        private val TAG = YoutubeAdapter::class.java.simpleName

        @JvmStatic
        @BindingAdapter("convertRecommendNumber")
        fun convertRecommendNumber(view: TextView, count: Int) {
            if (count < 10000) {
                view.text = String.format("%d", count)
            } else {
                val thousand = count/1000
                view.text = String.format("%d.%d萬", thousand/10, thousand%10)
            }
        }
    }

    var videoViewModel: VideoViewModel? = null

    var mVideoList: List<VideoItem> = ArrayList()

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
        val binding = VideoItemBinding.inflate(layoutInflater, parent, false)

        return MyViewHolder(binding)
    }

    // Return the size of your dataset (invoked by the layout manager)
    //here the dataset is mVideoList
    override fun getItemCount(): Int {
        return mVideoList.size
    }

    private fun initListener(binding: VideoItemBinding, position: Int) {
        //setting on click listener for each video_item to launch clicked video in new activity
        binding.videoView.setOnClickListener {
            videoViewModel?.playVideo(binding.videoItem)
            selectIndex = position
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    inner class MyViewHolder(private val binding: VideoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val item = mVideoList[position]
            LogMessage.D(TAG, "position: $position, item title: ${item.title}")

            initListener(binding, position)

            if (selectIndex == position) {
                binding.videoView.setBackgroundColor(ContextCompat.getColor(binding.videoView.context, R.color.orange))
            } else {
                binding.videoView.setBackgroundColor(ContextCompat.getColor(binding.videoView.context, R.color.white))
            }

            //with method gives access to the global default Picasso instance
            //load method starts an image request using the specified path may be a remote URL, file resource, etc.
            //resize method resizes the image to the specified size in pixels wrt width and height
            //centerCrop crops an image inside of the bounds specified by resize(int, int) rather than distorting the aspect ratio
            //into method asynchronously fulfills the request into the specified Target
            Picasso.get()
                .load(item.thumbnailURL)
                .resize(480, 270)
                .centerCrop()
                .into(binding.videoThumbnail)

            this.binding.videoItem = item
            this.binding.executePendingBindings()
        }
    }
}