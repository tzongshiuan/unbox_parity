package com.hsuanparty.unbox_parity.model

import java.net.URL

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/1
 * Description:
 */
class ParityItem {

    companion object {
        const val TYPE_PARITY = 0
        const val TYPE_BANNER = 1
    }

    var url: URL? = null

    var title: String? = null

    var price = 0

    var thumbnailURL: URL? = null

    var type = TYPE_PARITY
}