package com.hsuanparty.unbox_parity.model


interface PreferencesHelper {
    var developer : String

    fun readPreferences()
    fun savePreferences()
}