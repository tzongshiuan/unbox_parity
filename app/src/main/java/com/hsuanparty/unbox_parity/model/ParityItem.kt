package com.hsuanparty.unbox_parity.model

import java.net.URL

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/1
 * Description:
 */
class ParityItem {

    //stores id of a video
    //getter and setter methods for id
    var url: URL? = null

    //stores title of the video
    //getter and setter methods for video Title
    var title: String? = null

    //stores the description of video
    //getter and setter methods for video description
    var price = 0

    //stores the url of thumbnail of video
    //getter and setter methods for thumbnail url
    var thumbnailURL: URL? = null
}