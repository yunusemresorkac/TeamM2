package com.app.gamenews.repo

import android.content.Context
import com.app.gamenews.model.User
import com.app.gamenews.notify.Token
import com.app.gamenews.util.Constants
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepo {

    fun getUserRealTime(context: Context,userId: String, callback: (User?) -> Unit) {
        val docRef = FirebaseFirestore.getInstance().collection("Users").document(userId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                callback(null)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    callback(user)
                }
            } else {
                callback(null)
            }
        }

    }

    fun getUser(context : Context, userId: String, callback: (User?) -> Unit) {
        val docRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        callback(user)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }


    fun getUsername(context : Context, userId: String, callback: (String) -> Unit) {
        val docRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        callback(user.username)
                    }
                } else {
                    callback("")
                }
            }
            .addOnFailureListener {
                callback("")
            }
    }

    fun getUserImage(context : Context, userId: String, callback: (String) -> Unit) {
        val docRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        callback(user.profileImage)
                    }
                } else {
                    callback("")
                }
            }
            .addOnFailureListener {
                callback("")
            }
    }

    fun getUserFollowers(context : Context, userId: String, callback: (Int) -> Unit) {
        val docRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        callback(user.followers)
                    }
                } else {
                    callback(0)
                }
            }
            .addOnFailureListener {
                callback(0)
            }
    }


    fun getUserFollowing(context : Context, userId: String, callback: (Int) -> Unit) {
        val docRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        callback(user.following)
                    }
                } else {
                    callback(0)
                }
            }
            .addOnFailureListener {
                callback(0)
            }
    }


    fun getUserCoin(context : Context, userId: String, callback: (Double) -> Unit) {
        val docRef = FirebaseFirestore.getInstance().collection("Users").document(userId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                callback(0.0)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    callback(user.coin)
                }
            } else {
                callback(0.0)
            }
        }
    }


    fun updateCoin(context: Context, userId: String, amount : Double){
        CoroutineScope(Dispatchers.IO).launch {
            val map : HashMap<String,Any> = HashMap()
            map["coin"] = FieldValue.increment(amount)

            FirebaseFirestore.getInstance().collection("Users").document(userId)
                .update(map).addOnSuccessListener {

                }
        }
    }


    fun updateStatusOffline(context: Context, userId: String){
        CoroutineScope(Dispatchers.IO).launch {
            val map : HashMap<String,Any> = HashMap()
            map["status"] = Constants.OFFLINE_STATUS

            FirebaseFirestore.getInstance().collection("Users").document(userId)
                .update(map).addOnSuccessListener {

                }
        }
    }

    fun updateStatusOnline(context: Context, userId: String){
        CoroutineScope(Dispatchers.IO).launch {
            val map : HashMap<String,Any> = HashMap()
            map["status"] = Constants.ONLINE_STATUS

            FirebaseFirestore.getInstance().collection("Users").document(userId)
                .update(map).addOnSuccessListener {

                }
        }
    }

    fun updateToken(token: String, userId: String) {
        val ref = FirebaseFirestore.getInstance().collection("Tokens")
        val mToken = Token(token)
        ref.document(userId).set(mToken)
    }


}