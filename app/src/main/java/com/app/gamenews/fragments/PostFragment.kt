package com.app.gamenews.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.activities.CommentActivity
import com.app.gamenews.adapter.PostAdapter
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.controller.LikeManager
import com.app.gamenews.databinding.FragmentPostBinding
import com.app.gamenews.model.Game
import com.app.gamenews.model.Post
import com.app.gamenews.util.Constants
import com.app.gamenews.viewmodel.PostViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle
import java.io.OutputStream

class PostFragment : Fragment(), PostAdapter.PostClick {

    private lateinit var binding: FragmentPostBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: ArrayList<Post>
    private var scrolling = false
    private var isLoading = false
    private var firebaseUser: FirebaseUser? = null
    private val postViewModel by viewModel<PostViewModel>()
    private val userViewModel by viewModel<UserViewModel>()
    private var followers: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuBar = activity?.findViewById<LinearLayout>(R.id.menuLay)

        menuBar?.let { showOrHideMenuWithAnimation(it) }
        firebaseUser = FirebaseAuth.getInstance().currentUser

        initRecycler()

        if (firebaseUser != null) {
            postViewModel.getFollowings(
                firebaseUser!!.uid,
                onComplete = { followingIds ->
                    if (followingIds.isNotEmpty()) {
                        followers = followingIds
                        // Do something with the followerIds list
                        println("Follower IDs: $followingIds")
                        postViewModel.fetchInitialData(followingIds)
                        observeViewModel()
                    }

                },
                onError = { exception ->
                    // Handle the exception
                    println("Error fetching followers: $exception")
                }
            )
            binding.infoText.visibility = View.GONE

        } else {
            binding.infoText.visibility = View.VISIBLE
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
                    if (followers!!.isNotEmpty()) {
                        postViewModel.loadMoreData(followers!!)

                    }
                }
            }
        })


    }

    private fun initRecycler() {
        postList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
        postAdapter = PostAdapter(postList, requireContext(), userViewModel, this)
        binding.recyclerView.adapter = postAdapter

        binding.recyclerView.onFlingListener = null

        val dividerItemDecoration = DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)


    }


    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            postViewModel.list.collect { posts ->
                println("postlar $posts")
                postList.clear()
                postList.addAll(posts)
                postAdapter.notifyDataSetChanged()
                isLoading = false
            }
        }
    }


    private fun showMenuWithFadeIn(menuBar: LinearLayout) {
        val fadeIn = ObjectAnimator.ofFloat(menuBar, "alpha", 0f, 1f)
        fadeIn.duration = 500 // Animasyon süresi, istediğin gibi ayarlayabilirsin

        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                menuBar.visibility = View.VISIBLE
            }
        })

        fadeIn.start()
    }

    private fun hideMenuWithFadeOut(menuBar: LinearLayout) {
        val fadeOut = ObjectAnimator.ofFloat(menuBar, "alpha", 1f, 0f)
        fadeOut.duration = 500 // Animasyon süresi, istediğin gibi ayarlayabilirsin

        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                menuBar.visibility = View.GONE
            }
        })

        fadeOut.start()
    }

    private fun showOrHideMenuWithAnimation(menuBar: LinearLayout) {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scrolling = newState == RecyclerView.SCROLL_STATE_DRAGGING
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0 && menuBar.visibility == View.VISIBLE && !scrolling) {
                    hideMenuWithFadeOut(menuBar)
                } else if (dy < 0 && menuBar.visibility != View.VISIBLE && !scrolling) {
                    showMenuWithFadeIn(menuBar)
                }
            }
        })
    }

    override fun clickCommentBtn(post: Post) {
        val intent = Intent(context, CommentActivity::class.java)
        intent.putExtra("postId", post.postId)
        intent.putExtra("collectionName", "Posts")
        startActivity(intent)
    }

    override fun clickLikeBtn(post: Post, likeManager: LikeManager) {
        if (firebaseUser != null) {
            likeManager.toggleLike(post.postId, "Posts")

        } else {
            DummyMethods.showMotionToast(
                requireContext(), "Beğenmek için hesap oluşturun", "",
                MotionToastStyle.INFO
            )
        }
    }

    override fun clickShareBtn(post: Post) {
        sharePost(post)
    }

    override fun clickPostActions(post: Post) {
        showActionsDialog("Bildir", "İptal", "Bu gönderiyi bildirmek ister misin",
            "Bir ihlal yapıldığı kesinleşirse bu gönderi silinecektir", {

            }, {

            })

    }


    private fun showActionsDialog(
        positiveBtnText: String,
        negativeBtnText: String,
        title: String,
        message: String,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {

        val mDialog = MaterialDialog.Builder(requireContext() as Activity)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(positiveBtnText) { dialogInterface, which ->
                onPositiveClick()
                dialogInterface?.dismiss()
            }
            .setNegativeButton(negativeBtnText) { dialogInterface, which ->
                onNegativeClick()
                dialogInterface.dismiss()
            }
            .build()

        mDialog.show()


    }


    private fun sharePost(post: Post) {
        if (DummyMethods.getWritePermission(requireContext())) {
            Glide.with(requireContext())
                .asBitmap()
                .load(post.imageUrls[0])
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val icon: Bitmap = resource
                        val share = Intent(Intent.ACTION_SEND)
                        share.type = "image/jpeg"
                        val textToAdd = post.title
                        share.putExtra(Intent.EXTRA_TEXT, textToAdd)
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.TITLE, "title")
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        val uri: Uri = requireContext().contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values
                        )!!


                        val outstream: OutputStream
                        try {
                            outstream = requireActivity().contentResolver.openOutputStream(uri)!!
                            icon.compress(Bitmap.CompressFormat.JPEG, 100, outstream)
                            outstream.close()
                        } catch (e: Exception) {
                            System.err.println(e.toString())
                        }

                        share.putExtra(Intent.EXTRA_STREAM, uri)
                        startActivity(Intent.createChooser(share, "Share Image"))
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }


    }


}