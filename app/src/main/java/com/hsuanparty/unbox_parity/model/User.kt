package com.hsuanparty.unbox_parity.model

import com.google.gson.annotations.SerializedName

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/28
 * Description:
 */
class User {

    @SerializedName("uid")
    var uid = "6666"

    @SerializedName("name")
    var name: String = "Hsuan"

    @SerializedName("age")
    var age: String = "30"

    @SerializedName("tel")
    var tel: String = "123123"

    constructor()

    constructor(uid: String, name: String, age: String, tel: String) {
        this.uid = uid
        this.name = name
        this.age = age
        this.tel = tel
    }
}