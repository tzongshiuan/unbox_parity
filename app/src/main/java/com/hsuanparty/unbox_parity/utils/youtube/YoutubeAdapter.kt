package com.hsuanparty.unbox_parity.utils.youtube

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.databinding.VideoItemBinding
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.squareup.picasso.Picasso

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/1
 * Description:
 */
class YoutubeAdapter: RecyclerView.Adapter<YoutubeAdapter.MyViewHolder>() {

    companion object {
        private val TAG = YoutubeAdapter::class.java.simpleName
    }

    var mVideoList: List<VideoItem> = ArrayList()

    // Replace the contents of a view (invoked by the layout manager)
    //filling every item of view with respective text and image
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        LogMessage.D(TAG, "onBindViewHolder()")

        holder.bind(mVideoList[position])
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        LogMessage.D(TAG, "onCreateViewHolder()")

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = VideoItemBinding.inflate(layoutInflater, parent, false)

        initListener(binding)

        return MyViewHolder(binding)
    }

    // Return the size of your dataset (invoked by the layout manager)
    //here the dataset is mVideoList
    override fun getItemCount(): Int {
        return mVideoList.size
    }

    private fun initListener(binding: VideoItemBinding) {
        //setting on click listener for each video_item to launch clicked video in new activity
        binding.videoView.setOnClickListener {
//            //creating a intent for PlayerActivity class from this Activity
//            //arguments needed are package context and the new Activity class
//            val intent = Intent(mContext, PlayerActivity::class.java)
//
//            //putExtra method helps to add extra/extended data to the intent
//            //which can then be used by the new activity to get initial data from older activity
//            //arguments is a name used to identify the data and other is the data itself
//            intent.putExtra("VIDEO_ID", singleVideo.getId())
//            intent.putExtra("VIDEO_TITLE", singleVideo.getTitle())
//            intent.putExtra("VIDEO_DESC", singleVideo.getDescription())
//
//            //Flags define hot the activity should behave when launched
//            //FLAG_ACTIVITY_NEW_TASK flag if set, the activity will become the start of a new task on this history stack.
//            //adding flag as it is required for YoutubePlayerView Activity
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//
//            //launching the activity by startActivity method
//            //use mContext as this class is not the original context
//            mContext.startActivity(intent)
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    inner class MyViewHolder(private val binding: VideoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VideoItem?) {
            LogMessage.D(TAG, "item title: ${item?.title}")

            //with method gives access to the global default Picasso instance
            //load method starts an image request using the specified path may be a remote URL, file resource, etc.
            //resize method resizes the image to the specified size in pixels wrt width and height
            //centerCrop crops an image inside of the bounds specified by resize(int, int) rather than distorting the aspect ratio
            //into method asynchronously fulfills the request into the specified Target
            Picasso.get()
                .load(item?.thumbnailURL)
                .resize(480, 270)
                .centerCrop()
                .into(binding.videoThumbnail)

            this.binding.videoItem = item!!
            this.binding.executePendingBindings()
        }
    }
}