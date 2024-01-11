package com.app.gamenews.controller


import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.app.gamenews.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class FollowManager {


    val db = FirebaseFirestore.getInstance()
    val usersCollection = db.collection("Users")

    // Kullanıcının takip durumunu dinlemek için ListenerRegistration
    var followListener: ListenerRegistration? = null

    // Kullanıcının takip durumunu güncelleyen ve buton metnini değiştiren fonksiyon
    fun updateFollowButton(userId: String, targetUserId: String, followButton: Button) {
        val userFollowersCollection = usersCollection.document(userId).collection("Followings")

        followListener?.remove() // Eski listener'ı kaldır

        followListener = userFollowersCollection.document(targetUserId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                // Kullanıcı takip ediliyorsa
                followButton.text = "Takip Ediliyor"
            } else {
                // Kullanıcı takip etmiyorsa
                followButton.text = "Takip Et"
            }
        }
    }


    // Kullanıcıyı takip etme fonksiyonu
    fun followUser(userId: String, targetUserId: String,context : Context) {
        val userFollowersCollection = usersCollection.document(targetUserId).collection("Followers")

        userFollowersCollection.document(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Kullanıcı zaten takip ediliyorsa, takipten çık
                userFollowersCollection.document(userId).delete().addOnSuccessListener {
                    FirebaseFirestore.getInstance().collection("Users")
                        .document(targetUserId).update("followers",FieldValue.increment(-1))
                }

            } else {
                // Kullanıcıyı takip et
                userFollowersCollection.document(userId).set(mapOf("followed" to true))
                FirebaseFirestore.getInstance().collection("Users")
                    .document(targetUserId).update("followers",FieldValue.increment(1))
            }
        }

        val followingCollection = usersCollection.document(userId).collection("Followings")

        followingCollection.document(targetUserId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Kullanıcı zaten takip ediliyorsa, takipten çık
                followingCollection.document(targetUserId).delete()
                    .addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("Users")
                            .document(userId).update("following",FieldValue.increment(-1))
                    }

            } else {
                // Kullanıcıyı takip et
                followingCollection.document(targetUserId).set(mapOf("followed" to true)).addOnSuccessListener {
                    FirebaseFirestore.getInstance().collection("Users")
                        .document(userId).update("following",FieldValue.increment(1))
                }
            }
        }
    }

}