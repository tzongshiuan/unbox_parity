package com.hsuanparty.unbox_parity.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.youtube.VideoItem
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
    }

    private var fireDB = FirebaseDatabase.getInstance()

    private var dbVideoRef = fireDB.getReference("VideoData")
    var videoList: MutableList<VideoData> = mutableListOf()
    private var videoIndex = 0
    private var userVideoIndex = 0

    fun setVideoValueEvent() {
        dbVideoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (item in dataSnapshot.children) {
                        val video = item.getValue(VideoData::class.java)
                        videoList.add(video!!)
                    }
                    //show()
                    LogMessage.D(TAG, "video list size = ${videoList.size}")
                }
            }
        })
    }

    fun setVideoChildValueEvent(item: VideoItem?) {
        // Need to set child event listeners for all child
        val listener = object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                LogMessage.D(TAG, "onChildAdded()")
//                val user = p0.getValue(User::class.java)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                LogMessage.D(TAG, "onChildRemoved()")
//                val video = p0.getValue(VideoData::class.java)
//                videoList.remove(videoList.first { x -> x.videoId == video!!.videoId })
            }
        }
        dbVideoRef.child(item?.id!!).child("likeUsers").addChildEventListener(listener)

        dbVideoRef.child(item?.id!!).child("likeUsers").removeEventListener(listener)
    }

    fun removeVideoChildValueEvent()

    fun isVideoDataExist(videoId: String): Boolean {
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

    fun addNewVideoData(item: VideoItem?) {
        val likeUsers: ArrayList<User> = ArrayList()

        val uid = item?.id
        val video = VideoData(uid!!, 0, item, likeUsers)
        dbVideoRef.child(uid).setValue(video)
        dbVideoRef.push()
    }

    fun giveVideoLike(item: VideoItem?) {
        val user = User(mAuth.uid!!, System.currentTimeMillis())
        val uid = item?.id

        videoList[videoIndex].likeUsers.add(user)
        dbVideoRef.child(uid!!).child("likeUsers").setValue(videoList[videoIndex].likeUsers)

        videoList[videoIndex].count++
        dbVideoRef.child(uid).child("count").setValue(videoList[videoIndex].count)

        dbVideoRef.push()
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