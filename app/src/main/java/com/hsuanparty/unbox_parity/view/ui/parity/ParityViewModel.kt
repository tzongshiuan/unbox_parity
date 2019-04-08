package com.hsuanparty.unbox_parity.view.ui.parity

import android.app.Activity
import android.text.Spanned
import android.text.SpannedString
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.MyPreferences
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

    @Inject
    lateinit var mPreferences: MyPreferences

    fun searchParity(activity: Activity) {
        object : Thread() {
            override fun run() {

                val path = BASE_SEARCH_URL + mPreferences.lastSearchKeyword.replace(" ", "%20")
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
