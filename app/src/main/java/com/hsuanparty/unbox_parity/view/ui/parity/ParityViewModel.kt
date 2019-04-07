package com.hsuanparty.unbox_parity.view.ui.parity

import android.app.Activity
import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.utils.LogMessage
import java.io.BufferedReader
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParityViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = ParityViewModel::class.java.simpleName

        private const val BASE_SEARCH_URL = "https://www.findprice.com.tw/g/"
    }

    fun searchParity(activity: Activity) {
        object : Thread() {
            override fun run() {

                var path = BASE_SEARCH_URL
//                when (dateRange) {
//                    ArticleFragment.DATE_RANGE_WEEK -> path += SEARCH_FILTER_WEEK
//                    ArticleFragment.DATE_RANGE_MONTH -> path += SEARCH_FILTER_MONTH
//                    ArticleFragment.DATE_RANGE_YEAR -> path += SEARCH_FILTER_YEAR
//                    else -> {}
//                }
                LogMessage.D(TAG, "Parity search url: $path")

                val url = URL(path)
                //val url = URL("https://www.findprice.com.tw/g/dyson")
                val connection = url.openConnection()

                //val agent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
                //connection.setRequestProperty("User-Agent", agent)
                val stream = connection.getInputStream()
                val html = stream.bufferedReader().use(BufferedReader::readText)

                //LogMessage.D(TAG, html)

                //parseSearchArticleResult(html)
            }
        }.start()
    }
}
