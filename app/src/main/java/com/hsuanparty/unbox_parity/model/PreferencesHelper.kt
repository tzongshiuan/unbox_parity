package com.hsuanparty.unbox_parity.model

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.hsuanparty.unbox_parity.utils.youtube.VideoItem


interface PreferencesHelper {
    var developer: String

    var authStatus: Int
    var isFinishApp: Boolean
    var lastSearchKeyword: String
    var curVideoItem: VideoItem?
    val dayHotVideoList: ArrayList<VideoItem>
    val weekHotVideoList: ArrayList<VideoItem>
    val monthHotVideoList: ArrayList<VideoItem>

    fun readPreferences()
    fun savePreferences()
}