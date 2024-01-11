package com.app.gamenews.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.adapter.CommentAdapter
import com.app.gamenews.adapter.GamesAdapter
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.ActivityCommentBinding
import com.app.gamenews.model.Comment
import com.app.gamenews.model.Game
import com.app.gamenews.viewmodel.CommentViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle
import java.util.HashMap

class CommentActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCommentBinding
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList : ArrayList<Comment>
    private val commentViewModel by viewModel<CommentViewModel>()
    private lateinit var postId : String
    private var firebaseUser : FirebaseUser? = null
    private lateinit var collectionName : String
    private val userViewModel by viewModel<UserViewModel>()
    private var isLoading = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }


        firebaseUser = FirebaseAuth.getInstance().currentUser
        postId = intent.getStringExtra("postId").toString()
        collectionName = intent.getStringExtra("collectionName").toString()

        initRecycler()

        commentViewModel.fetchInitialData(postId)
        observeViewModel()

        isAuthed()

        binding.sendCommentBtn.setOnClickListener {
            sendComment()
        }


        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isLoading && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    isLoading = true
                    commentViewModel.loadMoreData(postId)
                }
            }
        })

    }

    private fun isAuthed(){
        if (firebaseUser!=null){
            binding.commentEt.isEnabled = true
        }else{
            binding.commentEt.isEnabled = false
            binding.commentEt.hint = "Yorum Yapabilmek İçin Oturum Açmalısınız"
            binding.commentEt.textSize = 13f

        }
    }

    private fun sendComment(){


        if (binding.commentEt.text.toString().trim().isNotEmpty() && firebaseUser!=null){
            val comment = Comment(postId,DummyMethods.generateRandomString(10),System.currentTimeMillis()
                ,binding.commentEt.text.toString().trim(),firebaseUser!!.uid)
            commentViewModel.addComment(comment,{
                binding.commentEt.text.clear()
                increaseLikeCount() }
                , {commentFailed() })
        }


    }

    private fun commentAddedSuccessfully(){
        DummyMethods.showMotionToast(this,"Yorumun Alındı","",MotionToastStyle.SUCCESS)
        finish()
    }


    private fun increaseLikeCount(){
        val map: HashMap<String, Any> = HashMap()
        map["countOfComments"] = FieldValue.increment(1)

        FirebaseFirestore.getInstance().collection(collectionName).document(postId)
            .update(map).addOnSuccessListener {
                commentAddedSuccessfully()
            }
    }

    private fun commentFailed(){
        DummyMethods.showMotionToast(this,"Bir hata oldu","Lütfen Yeniden Dene",MotionToastStyle.ERROR)
    }

    private fun initRecycler(){
        commentList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        commentAdapter = CommentAdapter(commentList,this,userViewModel)
        binding.recyclerView.adapter = commentAdapter

    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            commentViewModel.list.collect { comments ->
                commentList.clear()
                commentList.addAll(comments)
                commentAdapter.notifyDataSetChanged()
                isLoading = false
            }
        }
    }
}