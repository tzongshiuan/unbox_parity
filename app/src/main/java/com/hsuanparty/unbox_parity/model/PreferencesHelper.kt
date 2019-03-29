package com.hsuanparty.unbox_parity.model


interface PreferencesHelper {
    var developer: String

    var authStatus: Int
    var isFinishApp: Boolean

    fun readPreferences()
    fun savePreferences()
}