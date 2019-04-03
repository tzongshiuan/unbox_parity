package com.hsuanparty.unbox_parity.model

import com.hsuanparty.unbox_parity.utils.youtube.VideoItem


interface PreferencesHelper {
    var developer: String

    var authStatus: Int
    var isFinishApp: Boolean
    var lastSearchKeyword: String
    var curVideoItem: VideoItem?
    val weekHotVideoList: ArrayList<VideoItem>
    val monthHotVideoList: ArrayList<VideoItem>
    val yearHotVideoList: ArrayList<VideoItem>

    fun readPreferences()
    fun savePreferences()
}