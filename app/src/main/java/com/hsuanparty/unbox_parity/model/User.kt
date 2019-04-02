package com.hsuanparty.unbox_parity.model

import com.google.gson.annotations.SerializedName

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/28
 * Description:
 */
class User {

    @SerializedName("uid")
    var uid: String? = null

    @SerializedName("dateTime")
    var dateTime: Long = 0

    constructor()

    constructor(uid: String, dateTime: Long) {
        this.uid = uid
        this.dateTime = dateTime
    }
}