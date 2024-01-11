package com.app.gamenews.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.app.gamenews.R
import com.app.gamenews.adapter.ChatAdapter
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.controller.StatusManager
import com.app.gamenews.databinding.ActivityChatBinding
import com.app.gamenews.model.Chat
import com.app.gamenews.notify.Data
import com.app.gamenews.notify.Sender
import com.app.gamenews.notify.Token
import com.app.gamenews.util.Constants
import com.app.gamenews.viewmodel.ChatViewModel
import com.app.gamenews.viewmodel.TimeViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class ChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChatBinding
    private  var firebaseUser: FirebaseUser? = null
    private val chatViewModel by viewModel<ChatViewModel>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList : ArrayList<Chat>
    private lateinit var receiverId : String
    private val userViewModel by viewModel<UserViewModel>()
    private var requestQueue: RequestQueue? = null
    private var isLoading = false

    private val timeViewModel by viewModel<TimeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        requestQueue = Volley.newRequestQueue(this)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        receiverId = intent.getStringExtra("receiverId").toString()
        firebaseUser?.let { StatusManager().setUserOnline(it,userViewModel,this) }

        initRecycler()
        chatViewModel.fetchInitialData(firebaseUser!!.uid,receiverId)
        observeViewModel()


        getReceiverData()

        binding.sendMsgBtn.setOnClickListener {
            sendMessage()
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isLoading && firstVisibleItemPosition == 0 && dy < 0) {
                    isLoading = true
                    chatViewModel.loadMoreData(firebaseUser!!.uid, receiverId)
                }
            }
        })


    }

    private fun sendMessage(){
        if (binding.messageEt.text.toString().trim().isNotEmpty()){
            val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            iso8601Format.timeZone = TimeZone.getTimeZone("UTC") // Zaman dilimini ayarlayalım (opsiyonel, Firestore genellikle UTC kullanır)

            val currentDate = iso8601Format.format(Date())
            val now = System.currentTimeMillis()
            val chat = firebaseUser?.let { Chat(it.uid,receiverId, currentDate,DummyMethods.generateRandomString(10),
                binding.messageEt.text.toString().trim()) }

            chat?.let { chatViewModel.sendMessage(it,this) }
            binding.messageEt.text.clear()

            if (binding.status.text.equals("•Çevrimdışı")){
                firebaseUser?.let {
                    userViewModel.getUsername(this, firebaseUser!!.uid){ myName ->
                        sendNotification(receiverId,binding.messageEt.text.toString().trim(),myName,
                            firebaseUser!!.uid)
                    }
                }
            }
        }
    }

    private fun getReceiverData(){
        userViewModel.getUserRealTime(this,receiverId){
            it?.let {user ->
                binding.username.text = user.username
                if (user.status == Constants.OFFLINE_STATUS){
                    binding.status.text = "•Çevrimdışı"
                    binding.status.setTextColor(resources.getColor(R.color.error))
                }else{
                    binding.status.text = "•Çevrim içi"
                    binding.status.setTextColor(resources.getColor(R.color.green))
                }
            }
        }


    }

    private fun initRecycler(){
        chatList = ArrayList()
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        chatAdapter = ChatAdapter(chatList,this)
        binding.recyclerView.adapter = chatAdapter
    }



    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            chatViewModel.list.collect { messages ->
                println("liste boyutu ${messages.size}")
                chatList.clear() // Önce mevcut listeyi temizle
                chatList.addAll(0, messages.reversed()) // Yeni mesajları listenin başına ters sıralı olarak ekle
                chatAdapter.notifyDataSetChanged()
                isLoading = false
                scrollToBottom()
            }
        }
    }


    private fun scrollToBottom() {
        binding.recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }

    private fun sendNotification(receiver: String, message: String, title: String, sender: String) {

        val db = FirebaseFirestore.getInstance()
        val tokensCollection = db.collection("Tokens")
        val query = tokensCollection.whereEqualTo(FieldPath.documentId(), receiver)

        query.get().addOnSuccessListener { querySnapshot ->
            for (documentSnapshot in querySnapshot) {
                val token = documentSnapshot.toObject(Token::class.java).token

                val data = Data(
                    sender,
                    message,
                    title,
                    receiver,
                    R.mipmap.ic_launcher_round
                )
                val senderData = Sender(token,data)

                try {
                    val senderJsonObj = JSONObject(Gson().toJson(senderData))
                    val jsonObjectRequest = object : JsonObjectRequest(
                        "https://fcm.googleapis.com/fcm/send",
                        senderJsonObj,
                        Response.Listener { response ->
                            Log.d("JSON_RESPONSE 0", "onResponseSuccess: $response")

                        },
                        Response.ErrorListener { error ->
                            Log.d("JSON_RESPONSE 1", "onResponseError: $error")

                        }) {
                        @Throws(AuthFailureError::class)
                        override fun getHeaders(): Map<String, String> {
                            val headers = HashMap<String, String>()

                            headers["Content-Type"] = "application/json"
                            headers["Authorization"] = "key=${Constants.FCM_KEY}"
                            return headers
                        }
                    }
                    requestQueue!!.add(jsonObjectRequest)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }.addOnFailureListener { error ->
            Log.d("QUERY_ERROR", "Failed to query tokens collection: $error")
        }
    }

    override fun onRestart() {
        super.onRestart()
        firebaseUser?.let { StatusManager().setUserOnline(it,userViewModel,this) }
    }

    override fun onPause() {
        super.onPause()
        firebaseUser?.let { StatusManager().setUserOffline(it,userViewModel,this) }

    }


    override fun onDestroy() {
        super.onDestroy()
        firebaseUser?.let { StatusManager().setUserOffline(it,userViewModel,this) }

    }

}