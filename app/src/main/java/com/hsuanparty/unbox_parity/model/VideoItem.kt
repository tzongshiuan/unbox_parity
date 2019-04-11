package com.hsuanparty.unbox_parity.model

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/1
 * Description:
 */
class VideoItem {

    //stores id of a video
    //getter and setter methods for id
    var id: String? = null

    //stores title of the video
    //getter and setter methods for video Title
    var title: String? = null

    //stores the description of video
    //getter and setter methods for video description
    var description: String? = null

    //stores the url of thumbnail of video
    //getter and setter methods for thumbnail url
    var thumbnailURL: String? = null

    var uploadText: String? = null

    var viewCount = 0

    var likeCount = 0
}