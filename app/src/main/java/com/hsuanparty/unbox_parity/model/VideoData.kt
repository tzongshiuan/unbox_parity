package com.hsuanparty.unbox_parity.model

import com.google.gson.annotations.SerializedName
import com.hsuanparty.unbox_parity.utils.youtube.VideoItem

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/02
 * Description:
 */
class VideoData {

    @SerializedName("videoId")
    var videoId = ""

    @SerializedName("count")
    var count: Int = 0

    @SerializedName("item")
    var item: VideoItem? = null

    @SerializedName("likeUsers")
    var likeUsers: ArrayList<User> = ArrayList()

    constructor()

    constructor(videoId: String, count: Int, item: VideoItem?, likeUsers: ArrayList<User>) {
        this.videoId = videoId
        this.count = count
        this.item = item
        this.likeUsers = likeUsers
    }
}