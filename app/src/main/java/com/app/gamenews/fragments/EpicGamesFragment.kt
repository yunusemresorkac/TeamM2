package com.app.gamenews.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.ProgressDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.activities.CommentActivity
import com.app.gamenews.adapter.GamesAdapter
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.controller.LikeManager
import com.app.gamenews.databinding.FragmentEpicGamesBinding
import com.app.gamenews.model.Game
import com.app.gamenews.util.Constants
import com.app.gamenews.viewmodel.CommentViewModel
import com.app.gamenews.viewmodel.EpicViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle
import java.io.OutputStream

class EpicGamesFragment : Fragment(), GamesAdapter.GamesClick {

    private lateinit var binding : FragmentEpicGamesBinding
    private lateinit var gamesAdapter: GamesAdapter
    private lateinit var gameList : ArrayList<Game>
    private val epicViewModel by viewModel<EpicViewModel>()

    private var scrolling = false
    private var isLoading = false

    private var firebaseUser: FirebaseUser? = null
    private lateinit var pd : ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEpicGamesBinding.inflate(inflater,container,false)
        return binding.root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuBar = activity?.findViewById<LinearLayout>(R.id.menuLay)


        firebaseUser = FirebaseAuth.getInstance().currentUser

        initRecycler()

        epicViewModel.fetchInitialData()
        observeViewModel()

        // Load more data when needed
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isLoading && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    isLoading = true
                    epicViewModel.loadMoreData()
                }
            }
        })

    }

//    private fun showMenuWithFadeIn(menuBar: LinearLayout) {
//        val fadeIn = ObjectAnimator.ofFloat(menuBar, "alpha", 0f, 1f)
//        fadeIn.duration = 500 // Animasyon süresi, istediğin gibi ayarlayabilirsin
//
//        fadeIn.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationStart(animation: Animator) {
//                super.onAnimationStart(animation)
//                menuBar.visibility = View.VISIBLE
//            }
//        })
//
//        fadeIn.start()
//    }
//
//    private fun hideMenuWithFadeOut(menuBar: LinearLayout) {
//        val fadeOut = ObjectAnimator.ofFloat(menuBar, "alpha", 1f, 0f)
//        fadeOut.duration = 500 // Animasyon süresi, istediğin gibi ayarlayabilirsin
//
//        fadeOut.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: Animator) {
//                super.onAnimationEnd(animation)
//                menuBar.visibility = View.GONE
//            }
//        })
//
//        fadeOut.start()
//    }
//
//    private fun showOrHideMenuWithAnimation(menuBar: LinearLayout) {
//        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                scrolling = newState == RecyclerView.SCROLL_STATE_DRAGGING
//            }
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                if (dy > 0 && menuBar.visibility == View.VISIBLE && !scrolling) {
//                    hideMenuWithFadeOut(menuBar)
//                } else if (dy < 0 && menuBar.visibility != View.VISIBLE && !scrolling) {
//                    showMenuWithFadeIn(menuBar)
//                }
//            }
//        })
//    }


    private fun initRecycler(){
        gameList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
        gamesAdapter = GamesAdapter(gameList, requireContext(), Constants.EPIC_NAME, this)
        binding.recyclerView.adapter = gamesAdapter



    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            epicViewModel.gameList.collect { games ->
                gameList.clear()
                gameList.addAll(games)
                gamesAdapter.notifyDataSetChanged()
                isLoading = false
            }
        }
    }

    override fun clickCommentBtn(game: Game) {
        val intent = Intent(context, CommentActivity::class.java)
        intent.putExtra("postId",game.time)
        intent.putExtra("collectionName",Constants.EPIC_NAME)
        startActivity(intent)
    }

    override fun clickLikeBtn(game: Game,likeManager: LikeManager) {
        if (firebaseUser != null){
            likeManager.toggleLike(game.time, Constants.EPIC_NAME)

        }else{
            DummyMethods.showMotionToast(requireContext(),"Beğenmek için hesap oluşturun","",
                MotionToastStyle.INFO)
        }
    }

    override fun clickShareBtn(game: Game) {
        sharePost(game)
    }

    override fun clickDetailsBtn(game: Game) {
        if (!game.link.equals("")){
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(game.link)))
        }
    }

    private fun sharePost(news: Game){
        if (DummyMethods.getWritePermission(requireContext())){
            Glide.with(requireContext())
                .asBitmap()
                .load(news.imageUrls[0])
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val icon: Bitmap = resource
                        val share = Intent(Intent.ACTION_SEND)
                        share.type = "image/jpeg"
                        val textToAdd = news.title
                        share.putExtra(Intent.EXTRA_TEXT, textToAdd)
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.TITLE, "title")
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        val uri: Uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!


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