package com.hsuanparty.unbox_parity.utils.youtube

import androidx.core.text.HtmlCompat
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.DateTime
import com.hsuanparty.unbox_parity.utils.LogMessage
import java.util.*
import com.hsuanparty.unbox_parity.model.VideoItem
import com.hsuanparty.unbox_parity.utils.Constants
import java.io.BufferedReader
import java.net.MalformedURLException
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

        private const val HOT_MAX_RESULTS: Long = 30

        private const val BASE_SEARCH_URL = "https://www.youtube.com/results?"

        private const val SEARCH_FILTER_NONE  = "&sp=CAASAhAB"
        private const val SEARCH_FILTER_NONE_COUNT = "&sp=CAMSAhAB"
        private const val SEARCH_FILTER_NONE_DATE = "&sp=CAI%253D"

        private const val SEARCH_FILTER_DAY   = "&sp=CAMSBAgCEAE%253D"
        private const val SEARCH_FILTER_WEEK  = "&sp=CAMSBAgDEAE%253D"
        private const val SEARCH_FILTER_MONTH = "&sp=CAMSBAgEEAE%253D"
    }

    var mCredential: GoogleAccountCredential? = null

    fun search(type: Int): List<VideoItem>? {
        val keyWord = this.keyWord.replace(" ", "+")

        return when (type) {
            YoutubeConnector.NONE_HOT_VIDEO -> searchHotVideo(YoutubeConnector.NONE_HOT_VIDEO, "$keyWord+開箱")
            YoutubeConnector.NONE_HOT_VIDEO_COUNT -> searchHotVideo(YoutubeConnector.NONE_HOT_VIDEO_COUNT, "$keyWord+開箱")
            YoutubeConnector.NONE_HOT_VIDEO_UPLOAD -> searchHotVideo(YoutubeConnector.NONE_HOT_VIDEO_UPLOAD, "$keyWord+開箱")
            else -> null
        }
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

    fun searchHotVideo(dateRange: Int, keyWord: String): List<VideoItem>? {
        var path = "${BASE_SEARCH_URL}search_query=$keyWord"
        when (dateRange) {
            YoutubeConnector.NONE_HOT_VIDEO -> path += SEARCH_FILTER_NONE
            YoutubeConnector.NONE_HOT_VIDEO_COUNT -> path += SEARCH_FILTER_NONE_COUNT
            YoutubeConnector.NONE_HOT_VIDEO_UPLOAD -> path += SEARCH_FILTER_NONE_DATE
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
        val indexToken = "<div class=\"yt-thumb video-thumb\">"

        val imgToken1 = "<div class=\"yt-thumb video-thumb\"><span class=\"yt-thumb-simple\">"
        val imgToken2 = "</div>"
        val imgToken3 = "src=\""
        val imgToken4 = "data-thumb=\""

        val idToken1 = "<a href=\"/watch?v="
        val idToken2 = "\""

        val titleToken1 = "title=\""
        val titleToken2 = "\""

        val metaToken1 = "<ul class=\"yt-lockup-meta-info\">"
        val uploadToken1 = "<li>"
        val uploadToken2 = "</li>"
        val countToken1 = "<li>觀看次數："
        val countToken2 = "次</li>"

        val descToken1 = "<div class=\"yt-lockup-description"
        val descToken2 = "dir=\"ltr\">"
        val descToken3 = "</div>"

        val items: ArrayList<VideoItem> = ArrayList()

        if (Constants.IS_SHOW_ADMOB) {
            val adItem = VideoItem()
            adItem.type = VideoItem.TYPE_BANNER
            items.add(adItem)
        }

        try {
            // Loop until all links are found and parsed. Find each link by
            // finding the beginning and ending index of the tokens defined
            // above.
            var index = 0
            while (-1 != html.indexOf(indexToken, index)) {
                val item = VideoItem()
                index = html.indexOf(indexToken, index)

                LogMessage.D(TAG, "-----------------------------------------------------------")

                // Thumbnail
                var result = html.indexOf(imgToken1, index)
                val imgStart = result + imgToken1.length
                val imgEnd = html.indexOf(imgToken2, imgStart)
                val thumbnailAttr = html.substring(imgStart, imgEnd)

                var thumbnail: String
                var thumbStart: Int
                var thumbEnd: Int
                if (thumbnailAttr.contains(imgToken4)) {
                    // Gif type
                    thumbStart = thumbnailAttr.indexOf(imgToken4, 0)
                    thumbStart += imgToken4.length
                    thumbEnd = thumbnailAttr.indexOf("\"", thumbStart)
                    thumbnail = thumbnailAttr.substring(thumbStart, thumbEnd)
                } else {
                    // Normal image type
                    thumbStart = thumbnailAttr.indexOf(imgToken3, 0)
                    thumbStart += imgToken3.length
                    thumbEnd = thumbnailAttr.indexOf("\"", thumbStart)
                    thumbnail = thumbnailAttr.substring(thumbStart, thumbEnd)
                }

                item.thumbnailURL = thumbnail
                LogMessage.D(TAG, "Thumbnail URL: $thumbnail")
                index = imgEnd + imgToken2.length

                // Video ID
                result = html.indexOf(idToken1, index)
                val idStart = result + idToken1.length
                val idEnd = html.indexOf(idToken2, idStart)
                val videoId = html.substring(idStart, idEnd)

                item.id = videoId
                LogMessage.D(TAG, "videoId: $videoId")
                index = idEnd + idToken2.length

                // Video title
                result = html.indexOf(titleToken1, index)
                val titleStart = result + titleToken1.length
                val titleEnd = html.indexOf(titleToken2, titleStart)
                val title = HtmlCompat.fromHtml(html.substring(titleStart, titleEnd), HtmlCompat.FROM_HTML_MODE_LEGACY)

                item.title = title.toString()
                LogMessage.D(TAG, "Title: $title")
                index = titleEnd + titleToken2.length

                // Upload time
                index = html.indexOf(metaToken1, index)
                result = html.indexOf(uploadToken1, index)
                val uploadStart = result + uploadToken1.length
                val uploadEnd = html.indexOf(uploadToken2, result)
                val uploadText = html.substring(uploadStart, uploadEnd)

                item.uploadText = uploadText
                LogMessage.D(TAG, "Upload time: $uploadText")
                index = uploadEnd + uploadToken2.length

                // View count
                result = html.indexOf(countToken1, index)
                val countStart = result + countToken1.length
                val countEnd = html.indexOf(countToken2, countStart)
                val count = html.substring(countStart, countEnd).replace(",", "")

                item.viewCount = count.toInt()
                LogMessage.D(TAG, "View count: $count")
                index = countEnd + countToken2.length

                // Description
                result = html.indexOf(descToken1, index)
                result = html.indexOf(descToken2, result)
                val descStart = result + descToken2.length
                val descEnd = html.indexOf(descToken3, descStart)
                val description = HtmlCompat.fromHtml(html.substring(descStart, descEnd), HtmlCompat.FROM_HTML_MODE_LEGACY)

                item.description = description.toString()
                LogMessage.D(TAG, "Description: $description")
                index = descEnd + descToken3.length

                items.add(item)

                if (items.size >= HOT_MAX_RESULTS) {
                    break
                }
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            //throw IOException("Failed to parse Google links.")
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            //throw IOException("Failed to parse Google links.")
        }

        return items
    }

//    private fun beautifyDescription(desc: String): String {
//        val refToken1 = "<a href=\""
//        val refToken2 = "</a>"
//        val titleToken1 = "title=\""
//        val titleToken2 = "\""
//        var result = desc.replace("<wbr/>", "")
//                            .replace("<wbr />", "")
//                            .replace("<br/>", "")
//                            .replace("<br />", "")
//                            .replace("<b>", "")
//                            .replace("</b>", "")
//
//        while (result.contains(refToken1)) {
//            val start = result.indexOf(refToken1, 0)
//            val end = result.indexOf(refToken2, start)
//            val link = result.substring(start, end + refToken2.length)
//
//            val titleStart = result.indexOf(titleToken1, start)
//            val titleEnd = result.indexOf(titleToken2, titleStart + titleToken1.length)
//            val title = result.substring(titleStart + titleToken1.length, titleEnd)
//
//            result = result.replace(link, title)
//        }
//
//        return result
//    }
}