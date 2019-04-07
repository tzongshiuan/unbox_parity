package com.hsuanparty.unbox_parity.utils.youtube

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchResult
import com.hsuanparty.unbox_parity.utils.LogMessage
import java.io.IOException
import java.util.*
import com.hsuanparty.unbox_parity.model.VideoItem
import java.io.BufferedReader
import java.net.URL

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/7
 * Description:
 */
class YoutubeConnectorV2(val keyWord: String) {

    companion object {
        private val TAG = YoutubeConnectorV2::class.java.simpleName

        //maximum results that should be downloaded via the YouTube data API at a time
        private const val MAX_RESULTS: Long = 30

        private const val HOT_MAX_RESULTS: Long = 10

        private const val BASE_SEARCH_URL = "https://www.youtube.com/results?"

        private const val SEARCH_FILTER_NONE  = "&sp=CAMSAhAB"
        private const val SEARCH_FILTER_DAY   = "&sp=CAMSBAgCEAE%253D"
        private const val SEARCH_FILTER_WEEK  = "&sp=CAMSBAgDEAE%253D"
        private const val SEARCH_FILTER_MONTH = "&sp=CAMSBAgEEAE%253D"
    }

    var mCredential: GoogleAccountCredential? = null

    fun search(keywords: String): List<VideoItem>? {
        return null
    }

    private fun getPreviousDay(): DateTime {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)

        return DateTime(cal.time)
    }

    private fun getPreviousWeek(): DateTime {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, -1)
        return DateTime(cal.time)
    }

    private fun getPreviousMonth(): DateTime {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        return DateTime(cal.time)
    }

    fun searchHotVideo(dateRange: Int): List<VideoItem>? {
        var path = "${BASE_SEARCH_URL}search_query==開箱"
        when (dateRange) {
            YoutubeConnector.NONE_HOT_VIDEO -> path += SEARCH_FILTER_NONE
            YoutubeConnector.DAILY_HOT_VIDEO -> path += SEARCH_FILTER_DAY
            YoutubeConnector.WEEKLY_HOT_VIDEO -> path += SEARCH_FILTER_WEEK
            YoutubeConnector.MONTHLY_HOT_VIDEO -> path += SEARCH_FILTER_MONTH
            else -> {}
        }
        LogMessage.D(TAG, "Youtube search url: $path")

        val url = URL(path)
        //val url = URL("https://www.findprice.com.tw/g/dyson")
        val connection = url.openConnection()

        val agent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
        connection.setRequestProperty("User-Agent", agent)
        val stream = connection.getInputStream()
        val html = stream.bufferedReader().use(BufferedReader::readText)

        //LogMessage.D(TAG, html)

        return parseSearchArticleResult(html, dateRange)
    }

    private fun parseSearchArticleResult(html: String, dateRange: Int): List<VideoItem>? {
        return null
    }
}