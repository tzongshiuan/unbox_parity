package com.hsuanparty.unbox_parity.view.ui.parity

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.ParityItemBinding
import com.hsuanparty.unbox_parity.databinding.VideoItemBinding
import com.hsuanparty.unbox_parity.model.ParityItem
import com.hsuanparty.unbox_parity.model.VideoItem
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.view.ui.video.VideoViewModel
import com.squareup.picasso.Picasso

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/8
 * Description:
 */
class ParityAdapter: RecyclerView.Adapter<ParityAdapter.MyViewHolder>() {

    companion object {
        private val TAG = ParityAdapter::class.java.simpleName

        @JvmStatic
        @BindingAdapter("convertParityPrice")
        fun convertParityPrice(view: TextView, price: Int) {
            view.text = String.format("$%d", price)
        }
    }

    var parityViewModel: ParityViewModel? = null

    var mParityList: List<ParityItem> = ArrayList()

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
        val binding = ParityItemBinding.inflate(layoutInflater, parent, false)

        return MyViewHolder(binding)
    }

    // Return the size of your dataset (invoked by the layout manager)
    //here the dataset is mVideoList
    override fun getItemCount(): Int {
        return mParityList.size
    }

    private fun initListener(binding: ParityItemBinding, position: Int) {
        //setting on click listener for each video_item to launch clicked video in new activity
        binding.goPlatformBtn.setOnClickListener {
            parityViewModel?.showPlatform(binding.parityItem)
            //selectIndex = position
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    inner class MyViewHolder(private val binding: ParityItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val item = mParityList[position]
            LogMessage.D(TAG, "position: $position, item title: ${item.title}")

            initListener(binding, position)

            if (selectIndex == position) {
                binding.parityView.setBackgroundColor(ContextCompat.getColor(binding.parityView.context, R.color.orange))
            } else {
                binding.parityView.setBackgroundColor(ContextCompat.getColor(binding.parityView.context, R.color.white))
            }

            //with method gives access to the global default Picasso instance
            //load method starts an image request using the specified path may be a remote URL, file resource, etc.
            //resize method resizes the image to the specified size in pixels wrt width and height
            //centerCrop crops an image inside of the bounds specified by resize(int, int) rather than distorting the aspect ratio
            //into method asynchronously fulfills the request into the specified Target
            Picasso.get()
                .load(item.thumbnailURL.toString())
                .resize(480, 480)
                .centerCrop()
                .into(binding.parityThumbnail)

            this.binding.parityItem = item
            this.binding.executePendingBindings()
        }
    }
}