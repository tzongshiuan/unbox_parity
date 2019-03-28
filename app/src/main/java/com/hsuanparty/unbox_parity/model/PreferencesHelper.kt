package com.hsuanparty.unbox_parity.model


interface PreferencesHelper {
    var developer: String

    var authStatus: Int

    fun readPreferences()
    fun savePreferences()
}