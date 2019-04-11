package com.hsuanparty.unbox_parity.view.ui.parity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.model.ParityItem
import com.hsuanparty.unbox_parity.utils.Constants
import com.hsuanparty.unbox_parity.utils.LogMessage
import java.io.BufferedReader
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParityViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = ParityViewModel::class.java.simpleName

        private const val BASE_SEARCH_URL = "https://ezprice.com.tw/s/"

        // parity order
        const val ORDER_RELATIVE = 0
        const val ORDER_LOW_TO_HIGH = 1
        const val ORDER_HIGH_TO_LOW = 2
    }

    @Inject
    lateinit var mPreferences: MyPreferences

    val parityResult: MutableLiveData<ArrayList<ParityItem>> = MutableLiveData()

    var mActivity: Activity? = null

    var curOrderStatus = ORDER_RELATIVE

    fun removeObservers(fragment: Fragment) {
        parityResult.removeObservers(fragment)
    }

    fun searchParity() {
        if (mActivity != null) {
            searchParity(mActivity!!)
        }
    }

    fun searchParity(activity: Activity) {
        mActivity = activity
        object : Thread() {
            override fun run() {

                var path = BASE_SEARCH_URL + mPreferences.lastSearchKeyword.replace(" ", "%20")
                when (curOrderStatus) {
                    ORDER_RELATIVE -> path += "/?"
                    ORDER_LOW_TO_HIGH -> path += "/?st=1"
                    ORDER_HIGH_TO_LOW -> path += "/?st=2"
                    else -> {}
                }
                LogMessage.D(TAG, "Parity search url: $path")

                val url = URL(path)
                //val url = URL("https://www.findprice.com.tw/g/dyson")
                val connection = url.openConnection()

                val agent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
                connection.setRequestProperty("User-Agent", agent)
                val stream = connection.getInputStream()
                val html = stream.bufferedReader().use(BufferedReader::readText)

                //LogMessage.D(TAG, html)

                parseSearchArticleResult(html)
            }
        }.start()
    }

    private fun parseSearchArticleResult(html: String) {
        val items: ArrayList<ParityItem> = ArrayList()

        if (Constants.IS_SHOW_ADMOB) {
            val adItem = ParityItem()
            adItem.type = ParityItem.TYPE_BANNER
            items.add(adItem)
        }

        try {
            // First, retrieve useful data
            val bodyToken1 = "比價結果的商店"
            val bodyToken2 = "您可能有興趣的商品"
            val body = html.substring(html.indexOf(bodyToken1), html.indexOf(bodyToken2))

            val token1 = "class=\"search-rst clearfix\">"
            val urlToken1 = "<a href=\""
            val urlToken2 = "\""

            val titleToken1 = "title=\""
            val titleToken2 = "\""

            val priceToken1 = "prp="
            val priceToken2 = "&amp"

            val thumbToken1 = "<img src=\""
            val thumbToken2 = "http"
            val thumbToken3 = "\""

            var index = 0
            var result = 0
            while (-1 != body.indexOf(token1, index)) {
                val item = ParityItem()
                index = body.indexOf(token1, index)

                // URL
                result = body.indexOf(urlToken1, index)
                val urlStart = result + urlToken1.length
                val urlEnd = body.indexOf(urlToken2, urlStart)
                val urlText = body.substring(urlStart, urlEnd)

                val link = URL(urlText)
                item.url = link
                LogMessage.D(TAG, "Url: ${link.toURI()}")
                index = urlEnd + urlToken2.length

                // Title
                result = body.indexOf(titleToken1, index)
                val titleStart = result + titleToken1.length
                val titleEnd = body.indexOf(titleToken2, titleStart)
                val title = HtmlCompat.fromHtml(body.substring(titleStart, titleEnd), HtmlCompat.FROM_HTML_MODE_LEGACY)

                item.title = title.toString()
                LogMessage.D(TAG, "Title: $title")
                index = titleEnd + titleToken2.length

                // Price
                result = body.indexOf(priceToken1, index)
                val priceStart = result + priceToken1.length
                val priceEnd = body.indexOf(priceToken2, priceStart)
                val price = body.substring(priceStart, priceEnd).toInt()

                item.price = price
                LogMessage.D(TAG, "Price: $price")
                index = priceEnd + priceToken2.length

                // Thumbnail
                result = body.indexOf(thumbToken1, index)
                val thumbStart = body.indexOf(thumbToken2, result)
                val thumbEnd = body.indexOf(thumbToken3, thumbStart)
                var thumbnailText = body.substring(thumbStart, thumbEnd)

                if (thumbnailText.contains("&amp")) {
                    thumbnailText = thumbnailText.substringBefore("&amp")
                }

                item.thumbnailURL = URL(thumbnailText)
                LogMessage.D(TAG, "Thumbnail URL: ${item.thumbnailURL?.toURI()}")
                index = thumbEnd + thumbToken3.length

                items.add(item)
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            //throw IOException("Failed to parse Google links.")
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            //throw IOException("Failed to parse Google links.")
        } finally {
            parityResult.postValue(items)
        }
    }

    fun showPlatform(parityItem: ParityItem?) {
        LogMessage.D(TAG, "Uri: ${parityItem?.url.toString()}")

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(parityItem?.url.toString())
        mActivity?.startActivity(intent)
    }
}
