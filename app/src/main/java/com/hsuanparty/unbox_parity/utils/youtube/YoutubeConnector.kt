package com.hsuanparty.unbox_parity.utils.youtube

import android.content.Context
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchResult
import com.hsuanparty.unbox_parity.utils.LogMessage
import java.io.IOException
import java.util.ArrayList

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/1
 * Description:
 */
class YoutubeConnector(context: Context) {

    //Youtube object for executing api related queries through Youtube Data API
    private val youtube: YouTube

    //custom list of youtube which gets returned when searched for keyword
    //Returns a collection of search results that match the query parameters specified in the API request
    //By default, a search result set identifies matching video, channel, and playlist resources,
    //but you can also configure queries to only retrieve a specific type of resource
    private var query: YouTube.Search.List? = null

    init {

        //Youtube.Builder returns an instance of a new builder
        //Parameters:
        //transport - HTTP transport
        //jsonFactory - JSON factory
        //httpRequestInitializer - HTTP request initializer or null for none
        // This object is used to make YouTube Data API requests. The last
        // argument is required, but since we don't need anything
        // initialized when the HttpRequest is initialized, we override
        // the interface and provide a no-op function.
        youtube = YouTube.Builder(NetHttpTransport(), JacksonFactory(),
            HttpRequestInitializer { request ->
                //initialize method helps to add any extra details that may be required to process the query
                //setting package name and sha1 certificate to identify request by server
                request.headers.set("X-Android-Package", PACKAGE_NAME)
                request.headers.set("X-Android-Cert", SHA1)
            }).setApplicationName("SearchYoutube").build()

        try {

            // Define the API request for retrieving search results.
            query = youtube.search().list("id,snippet")

            //setting API key to query
            // Set your developer key from the {{ Google Cloud Console }} for
            // non-authenticated requests. See:
            // {{ https://cloud.google.com/console }}
            query?.key = KEY

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            query?.type = "video"

            query?.videoCaption = "any"

            //setting fields which should be returned
            //setting only those fields which are required
            //for maximum efficiency
            //here we are retreiving fiels:
            //-kind of video
            //-video ID
            //-title of video
            //-description of video
            //high quality thumbnail url of the video
            query?.fields = "items(id/kind,id/videoId,snippet/title,snippet/description,snippet/thumbnails/high/url)"

        } catch (e: IOException) {

            //printing stack trace if error occurs
            LogMessage.D(TAG, "Could not initialize: $e")
        }

    }

    fun search(keywords: String): List<VideoItem>? {

        //setting keyword to query
        query?.q = keywords

        //max results that should be returned
        query?.maxResults = MAX_RESULTS

        try {
            //executing prepared query and calling Youtube API
            val response = query?.execute()!!

            //retrieving list from response received
            //getItems method returns a list from the response which is originally in the form of JSON
            val results = response.items

            //list of type VideoItem for saving all data individually
            var items: List<VideoItem> = ArrayList()

            //check if result is found and call our setItemsList method
            if (results != null) {
                //iterator method returns a Iterator instance which can be used to iterate through all values in list
                items = setItemsList(results.iterator())
            }

            return items
        } catch (e: IOException) {
            //catch exception and print on console
            LogMessage.D(TAG, "Could not search: $e")
            return null
        }

    }

    companion object {
        private val TAG = YoutubeConnector::class.java.simpleName

        //Developer API key a developer can obtain after creating a new project in google developer console
        //Developer has to enable YouTube Data API v3 in the project
        //Add credentials and then provide the Application's package name and SHA fingerprint
        const val KEY = "AIzaSyA2GiVQoczSOT1DUOEJrxY1CD6lIxEtibo"

        //Package name of the app that will call the YouTube Data API
        const val PACKAGE_NAME = "com.hsuanparty.unbox_parity"

        //SHA1 fingerprint of APP can be found by double clicking on the app signing report on right tab called gradle
        val SHA1 = "SHA1_FINGERPRINT_STRING"

        //maximum results that should be downloaded via the YouTube data API at a time
        private const val MAX_RESULTS: Long = 30

        //method for filling our array list
        private fun setItemsList(iteratorSearchResults: Iterator<SearchResult>): List<VideoItem> {

            //temporary list to store the raw data from the returned results
            val tempSetItems = ArrayList<VideoItem>()

            //if no result then printing appropriate output
            if (!iteratorSearchResults.hasNext()) {
                LogMessage.D(TAG, " There aren't any results for your query.")
            }

            //iterating through all search results
            //hasNext() method returns true until it has no elements left to iterate
            while (iteratorSearchResults.hasNext()) {

                //next() method returns single instance of current video item
                //and returns next item everytime it is called
                //SearchResult is Youtube's custom result type which can be used to retrieve data of each video item
                val singleVideo = iteratorSearchResults.next()

                //getId() method returns the resource ID of one video in the result obtained
                val rId = singleVideo.id

                // Confirm that the result represents a video. Otherwise, the
                // item will not contain a video ID.
                //getKind() returns which type of resource it is which can be video, playlist or channel
                if (rId.kind == "youtube#video") {

                    //object of VideoItem class that can be added to array list
                    val item = VideoItem()

                    //getting High quality thumbnail object
                    //URL of thumbnail is in the heirarchy snippet/thumbnails/high/url
                    val thumbnail = singleVideo.snippet.thumbnails.high

                    //retrieving title,description,thumbnail url, id from the heirarchy of each resource
                    //Video ID - id/videoId
                    //Title - snippet/title
                    //Description - snippet/description
                    //Thumbnail - snippet/thumbnails/high/url
                    item.id = singleVideo.id.videoId
                    item.title = singleVideo.snippet.title
                    item.description = singleVideo.snippet.description
                    item. thumbnailURL = thumbnail.url

                    //adding one Video item to temporary array list
                    tempSetItems.add(item)

                    //for debug purpose printing one by one details of each Video that was found
                    LogMessage.D(TAG, " Video Id" + rId.videoId)
                    LogMessage.D(TAG, " Title: " + singleVideo.snippet.title)
                    LogMessage.D(TAG, " Thumbnail: " + thumbnail.url)
                    LogMessage.D(TAG, " Description: " + singleVideo.snippet.description)
                    LogMessage.D(TAG, "\n-------------------------------------------------------------\n")
                }
            }
            return tempSetItems
        }
    }
}