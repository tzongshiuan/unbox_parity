package com.hsuanparty.unbox_parity.model


interface PreferencesHelper {
    var developer: String

    var authStatus: Int
    var isFinishApp: Boolean
    val lastSearchKeyword: String

    fun readPreferences()
    fun savePreferences()
}