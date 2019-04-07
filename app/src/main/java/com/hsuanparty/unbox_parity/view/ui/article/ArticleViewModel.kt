package com.hsuanparty.unbox_parity.view.ui.article

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.ArticleItem
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.utils.LogMessage
import java.io.BufferedReader
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri


@Singleton
class ArticleViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = ArticleViewModel::class.java.simpleName

        private const val SEARCH_FILTER_WEEK   = "&tbs=qdr:W"
        private const val SEARCH_FILTER_MONTH = "&tbs=qdr:m"
        private const val SEARCH_FILTER_YEAR  = "&tbs=qdr:y"
        private const val SEARCH_FILTER_EXCLUDE = "&as_eq=\"video\"+\"購物\""

        private const val MAX_RESULT = 20
        private const val BASE_SEARCH_URL = "https://www.google.com/search?tbas=0&source=lnt&num=$MAX_RESULT$SEARCH_FILTER_EXCLUDE"

        private const val URL_PREFIX = "/url?q="
    }

    @Inject
    lateinit var mPreferences: MyPreferences

    val articleNoneResult: MutableLiveData<ArrayList<ArticleItem>> = MutableLiveData()
    val articleWeekResult: MutableLiveData<ArrayList<ArticleItem>> = MutableLiveData()
    val articleMonthResult: MutableLiveData<ArrayList<ArticleItem>> = MutableLiveData()
    val articleYearResult: MutableLiveData<ArrayList<ArticleItem>> = MutableLiveData()

    val showArticleContent: MutableLiveData<ArticleItem> = MutableLiveData()

    var activity: Activity? = null
    fun searchArticle(activity: Activity) {
        this.activity = activity
        searchWithDateRange(ArticleFragment.DATE_RANGE_NONE)
        searchWithDateRange(ArticleFragment.DATE_RANGE_WEEK)
        searchWithDateRange(ArticleFragment.DATE_RANGE_MONTH)
        searchWithDateRange(ArticleFragment.DATE_RANGE_YEAR)

//        object : Thread() {
//            override fun run() {
//
//                val path = BASE_SEARCH_URL + SEARCH_FILTER_YEAR + "&q=${mPreferences.lastSearchKeyword}"
//                LogMessage.D(TAG, "Artical search url: $path")
//
//                val agent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
//                val url = URL(path)
//                val connection = url.openConnection()
//
//                connection.setRequestProperty("User-Agent", agent)
//                val stream = connection.getInputStream()
//                val html = stream.bufferedReader().use(BufferedReader::readText)
//
//                LogMessage.D(TAG, html)
//
//                parseSearchArticleResult(html)
//            }
//        }.start()
    }

    private fun searchWithDateRange(dateRange: Int) {
        object : Thread() {
            override fun run() {

                var path = BASE_SEARCH_URL + "&q=${mPreferences.lastSearchKeyword}+開箱" + SEARCH_FILTER_EXCLUDE
                when (dateRange) {
                    ArticleFragment.DATE_RANGE_WEEK -> path += SEARCH_FILTER_WEEK
                    ArticleFragment.DATE_RANGE_MONTH -> path += SEARCH_FILTER_MONTH
                    ArticleFragment.DATE_RANGE_YEAR -> path += SEARCH_FILTER_YEAR
                    else -> {}
                }
                LogMessage.D(TAG, "Article search url: $path")

                val url = URL(path)
                //val url = URL("https://www.findprice.com.tw/g/dyson")
                val connection = url.openConnection()

                val agent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
                connection.setRequestProperty("User-Agent", agent)
                val stream = connection.getInputStream()
                val html = stream.bufferedReader().use(BufferedReader::readText)

                //LogMessage.D(TAG, html)

                parseSearchArticleResult(html, dateRange)
            }
        }.start()
    }

    private fun parseSearchArticleResult(html: String, dateRange: Int) {
        // These tokens are adequate for parsing the HTML from Google. First,
        // find a heading-3 element with an "r" class. Then find the next anchor
        // with the desired link. The last token indicates the end of the URL
        // for the link.
        val token1 = "<h3 class=\"r\">"
        val token2 = "<a href=\""
        val token3 = "&amp"

        val titleToken1 = "\">"
        val titleToken2 = "</a></h3>"

        val descToken1 = "<span class=\"st\">"
        val descToken2 = "</span>"

        val items: ArrayList<ArticleItem> = ArrayList()
        try {
            // Loop until all links are found and parsed. Find each link by
            // finding the beginning and ending index of the tokens defined
            // above.
            var index = 0
            while (-1 != html.indexOf(token1, index)) {
                var item = ArticleItem()
                index = html.indexOf(token1, index)

                // URL
                val result = html.indexOf(token2, index)
                val urlStart = result + token2.length
                val urlEnd = html.indexOf(token3, urlStart)
                val urlText = html.substring(urlStart + URL_PREFIX.length, urlEnd)

                val link = URL(urlText)
                item.url = link
                LogMessage.D(TAG, "Url: ${link.toURI()}")
                index = urlEnd + token3.length

                // Title
                val result2 = html.indexOf(titleToken1, index)
                val titleStart = result2 + titleToken1.length
                val titleEnd = html.indexOf(titleToken2, titleStart)
                val title = html.substring(titleStart, titleEnd)
                                .replace("<b>", "")
                                .replace("</b>", "")
                item.title = title
                LogMessage.D(TAG, "Title: $title")
                index = titleEnd + titleToken2.length

                // Description
                val result3 = html.indexOf(descToken1, index)
                val descStart = result3 + descToken1.length
                val descEnd = html.indexOf(descToken2, descStart)
                val description = html.substring(descStart, descEnd)
                                .replace("<b>", "")
                                .replace("</b>", "")
                                .replace("<br>", "")
                                .replace("&nbsp;", "")
                                .replace("&amp;", "&")
                                .replace("\n", "")
                item.description = description
                LogMessage.D(TAG, "Description: $description")
                index = descEnd + descToken2.length

                items.add(item)
            }

            when (dateRange) {
                ArticleFragment.DATE_RANGE_NONE -> articleNoneResult.postValue(items)
                ArticleFragment.DATE_RANGE_WEEK -> articleWeekResult.postValue(items)
                ArticleFragment.DATE_RANGE_MONTH -> articleMonthResult.postValue(items)
                ArticleFragment.DATE_RANGE_YEAR -> articleYearResult.postValue(items)
                else -> {}
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            //throw IOException("Failed to parse Google links.")
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            //throw IOException("Failed to parse Google links.")
        }
    }

    fun showUrlContent(articleItem: ArticleItem?) {
        LogMessage.D(TAG, "show url: ${articleItem?.url}, title: ${articleItem?.title}")

        // Open the browser to show url content
        //val uri = Uri.parse(articleItem?.url.toString())
        //val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        //activity?.startActivity(browserIntent)

        // Use embedded webview to get much better user experience
        showArticleContent.value = articleItem
    }
}
