package com.hsuanparty.unbox_parity.model


interface PreferencesHelper {
    var developer: String
    var recentKeywordList: ArrayList<RecentKeywordItem>

    var authStatus: Int
    var isFinishApp: Boolean
    var isLogout: Boolean
    var lastSearchKeyword: String
    var curVideoItem: VideoItem?
    val hotVideoList: ArrayList<VideoItem>
    var userName: String
    var photo: String

    fun readPreferences()
    fun savePreferences()
}