package com.hsuanparty.unbox_parity.model


interface PreferencesHelper {
    var developer: String

    var authStatus: Int
    var isFinishApp: Boolean
    var isLogout: Boolean
    var lastSearchKeyword: String
    var curVideoItem: VideoItem?
    val dayHotVideoList: ArrayList<VideoItem>
    val weekHotVideoList: ArrayList<VideoItem>
    val monthHotVideoList: ArrayList<VideoItem>

    fun readPreferences()
    fun savePreferences()
}