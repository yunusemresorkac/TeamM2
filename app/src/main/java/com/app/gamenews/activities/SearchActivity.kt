package com.app.gamenews.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.GridLayoutManager
import com.app.gamenews.R
import com.app.gamenews.adapter.UserAdapter
import com.app.gamenews.databinding.ActivitySearchBinding
import com.app.gamenews.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity(),UserAdapter.UserClick {

    private lateinit var binding : ActivitySearchBinding
    private lateinit var userAdapter: UserAdapter
    private var firebaseUser : FirebaseUser? = null
    private var userList : ArrayList<User>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser

        userList = ArrayList()
        binding.recyclerView.layoutManager = GridLayoutManager(this,3)

        binding.searchEt.addTextChangedListener(textWatcher)


    }
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val username = s.toString().trim()
            if (username.length>1){
                searchUsers(username)

            }


        }

        override fun afterTextChanged(s: Editable?) {
            userList!!.clear()

        }
    }

    private fun searchUsers(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            val usersRef = db.collection("Users")

            val querySnapshot = usersRef.whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .await()

            val tempList = ArrayList<User>() // Geçici liste oluştur

            for (document in querySnapshot.documents) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    tempList.add(user) // Geçici listeye kullanıcıları ekle
                }
            }

            withContext(Dispatchers.Main) {
                userList = tempList // userList'i geçici listeden al

                if (userList!!.isNotEmpty()) {
                    userAdapter = UserAdapter(userList!!, this@SearchActivity, this@SearchActivity)
                    binding.recyclerView.adapter = userAdapter
                    userAdapter.notifyDataSetChanged()
                } else {
                    userList!!.clear() // Eğer veri bulunamazsa userList'i temizle
                    userAdapter = UserAdapter(userList!!, this@SearchActivity, this@SearchActivity)
                    binding.recyclerView.adapter = userAdapter
                    userAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun goToProfile(user: User){
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("userId", user.userId)
        startActivity(intent)
    }

    override fun clickUser(user: User) {
        goToProfile(user)
    }



}