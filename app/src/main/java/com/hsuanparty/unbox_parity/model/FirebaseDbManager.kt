package com.hsuanparty.unbox_parity.model

import com.google.firebase.database.*
import com.hsuanparty.unbox_parity.utils.LogMessage
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/28
 * Description:
 */
@Singleton
class FirebaseDbManager {
    companion object {
        private val TAG = FirebaseDbManager::class.java.simpleName
    }

    private var fireDB = FirebaseDatabase.getInstance()
    private var dbRef = fireDB.getReference("Users")
    var list: MutableList<User> = mutableListOf()

    fun setSingleValueEvent() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (item in dataSnapshot.children) {
                        val user = item.getValue(User::class.java)
                        list.add(user!!)
                    }
                    //show()
                    LogMessage.D(TAG, "list size = ${list.size}")
                }
            }
        })
    }

    fun setChildEvent() {
        dbRef.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val user = p0.getValue(User::class.java)
                list.add(user!!)
                //show()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)
                list.remove(list.first { x -> x.uid == user!!.uid })
                //show()
            }

        })
    }

    fun add() {
        val uid: String = UUID.randomUUID().toString()
        val user = User(uid, "Ben", "15", "01234")
        dbRef.child(uid).setValue(user)
        dbRef.push()
    }

    fun clearAll() {
        for (user in list) {
            dbRef.child(user.uid).removeValue()
        }
    }
}