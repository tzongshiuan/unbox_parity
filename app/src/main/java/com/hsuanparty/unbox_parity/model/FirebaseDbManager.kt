package com.hsuanparty.unbox_parity.model

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.utils.LogMessage
import javax.inject.Singleton
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener



/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/28
 * Description:
 */
@Singleton
class FirebaseDbManager(private val mAuth: FirebaseAuth): Injectable {
    companion object {
        private val TAG = FirebaseDbManager::class.java.simpleName

        private const val ACTION_NONE = 0
        private const val ACTION_CHECK_USER_LIKE = 1
        private const val ACTION_GIVE_LIKE = 2
        private const val ACTION_DISTRACT_LIKE = 3
//        private const val
    }

    val isUserLikeLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private var fireDB = FirebaseDatabase.getInstance()

    private var dbVideoRef = fireDB.getReference("VideoData")
    var videoList: MutableList<VideoData> = mutableListOf()
    private var videoIndex = 0
    private var userVideoIndex = 0

    private var action = ACTION_NONE
    private var curVideoItem: VideoItem? = null

    private fun setVideoValueEvent() {
        dbVideoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    videoList.clear()

                    for (item in dataSnapshot.children) {
                        val video = item.getValue(VideoData::class.java)
                        videoList.add(video!!)
                    }
                    //show()
                    LogMessage.D(TAG, "video list size = ${videoList.size}")

                    when (action) {
                        ACTION_CHECK_USER_LIKE -> {
                            if (!isVideoDataExist(curVideoItem?.id!!)) {
                                addNewVideoData()
                            } else {
                                isUserLikeLiveData.value = isUserLike()
                            }
                        }

                        else -> {}
                    }
                }
            }
        })
    }

//    fun setVideoChildValueEvent(videoData: VideoData) {
//        // Need to set child event listeners for all child
//        val listener = object: ChildEventListener {
//            override fun onCancelled(p0: DatabaseError) {}
//
//            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
//
//            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
//
//            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//                LogMessage.D(TAG, "onChildAdded()")
////                val user = p0.getValue(User::class.java)
//            }
//
//            override fun onChildRemoved(p0: DataSnapshot) {
//                LogMessage.D(TAG, "onChildRemoved()")
////                val video = p0.getValue(VideoData::class.java)
////                videoList.remove(videoList.first { x -> x.videoId == video!!.videoId })
//            }
//        }
//
//        dbVideoRef.child(item?.id!!).child("likeUsers").addChildEventListener(listener)
//
//        dbVideoRef.child(item?.id!!).child("likeUsers").removeEventListener(listener)
//    }

    fun checkUserLike(videoItem: VideoItem) {
        action = ACTION_CHECK_USER_LIKE
        curVideoItem = videoItem

        setVideoValueEvent()
    }

    private fun isVideoDataExist(videoId: String): Boolean {
        if (videoList.size == 0) {
            return false
        }

        for (item in videoList) {
            if (item.videoId == videoId) {
                videoIndex = videoList.indexOf(item)
                LogMessage.D(TAG, "Find video data")
                return true
            }
        }

        LogMessage.D(TAG, "Not find video data")
        return false
    }

    fun isUserLike(): Boolean {
        val users = videoList[videoIndex].likeUsers
        for (user in users) {
            if (user.uid == mAuth.uid) {
                userVideoIndex = users.indexOf(user)
                return true
            }
        }

        return false
    }

    private fun addNewVideoData() {
        val likeUsers: ArrayList<User> = ArrayList()

        val uid = curVideoItem?.id
        val video = VideoData(uid!!, 0, curVideoItem, likeUsers)
        dbVideoRef.child(uid).setValue(video)
        dbVideoRef.push()
    }

    fun giveVideoLike(item: VideoItem?) {
//        val user = User(mAuth.uid!!, System.currentTimeMillis())
//        val uid = item?.id
//
//        videoList[videoIndex].likeUsers.add(user)
//        dbVideoRef.child(uid!!).child("likeUsers").setValue(videoList[videoIndex].likeUsers)
//
//        videoList[videoIndex].count++
//        dbVideoRef.child(uid).child("count").setValue(videoList[videoIndex].count)
//
//        dbVideoRef.push()
    }

    fun retractVideoLike(item: VideoItem?) {
        val uid = item?.id

        videoList[videoIndex].likeUsers.remove(videoList[videoIndex].likeUsers[userVideoIndex])
        dbVideoRef.child(uid!!).child("likeUsers").child(userVideoIndex.toString()).removeValue()

//        val uid = item?.id
//        dbVideoRef.child(uid!!).child("likeUsers").setValue(videoList[videoIndex].likeUsers)

        videoList[videoIndex].count--
        // TODO
//        dbVideoRef.child(uid).child("count").setValue(videoList[videoIndex].count)

//        dbVideoRef.push()
    }

    fun clearAllVideo() {
        for (video in videoList) {
            dbVideoRef.child(video.videoId).removeValue()
        }
    }
}