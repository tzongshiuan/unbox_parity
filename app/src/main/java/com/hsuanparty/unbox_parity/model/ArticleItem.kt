package com.hsuanparty.unbox_parity.model

import java.net.URL

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/1
 * Description:
 */
class ArticleItem {

    companion object {
        const val TYPE_ARTICLE = 0
        const val TYPE_BANNER = 1
    }

    var url: URL? = null

    var title: String? = null

    var description: String? = null

    var type = TYPE_ARTICLE
}