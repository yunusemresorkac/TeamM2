package com.app.gamenews.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gamenews.R
import com.app.gamenews.adapter.ChatAdapter
import com.app.gamenews.adapter.MessagesAdapter
import com.app.gamenews.databinding.ActivityMessagesBinding
import com.app.gamenews.model.Chat
import com.app.gamenews.viewmodel.ChatViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMessagesBinding
    private var firebaseUser: FirebaseUser? = null
    private val chatViewModel by viewModel<ChatViewModel>()
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var messagesList : ArrayList<Chat>
    private val userViewModel by viewModel<UserViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser

        initRecycler()
        getMessages()


    }

    private fun initRecycler(){
        messagesList = ArrayList()
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        messagesAdapter = MessagesAdapter(messagesList,this, userViewModel)
        binding.recyclerView.adapter = messagesAdapter
    }



    private fun getMessages(){
        lifecycleScope.launch {
            chatViewModel.getAllLastMessages(firebaseUser!!.uid).collect { messages ->
                messagesList.clear()
                if (messages.isNotEmpty()) {
                    messagesList.addAll(messages)
                }

                messagesAdapter.notifyDataSetChanged()
                if (messagesList.size > 0){
                    binding.chatsLay.visibility = View.VISIBLE
                } else {
                    binding.chatsLay.visibility = View.GONE
                }

            }
        }
    }


}