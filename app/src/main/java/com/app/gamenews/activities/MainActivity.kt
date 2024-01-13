package com.app.gamenews.activities

import android.content.Intent
import android.net.http.UrlRequest.Status
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.app.gamenews.R
import com.app.gamenews.controller.AdsController
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.controller.NavFragment
import com.app.gamenews.controller.StatusManager
import com.app.gamenews.databinding.ActivityMainBinding
import com.app.gamenews.fragments.EpicGamesFragment
import com.app.gamenews.fragments.NewsFragment
import com.app.gamenews.fragments.PostFragment
import com.app.gamenews.fragments.SteamFragment
import com.app.gamenews.model.Post
import com.app.gamenews.util.Constants
import com.app.gamenews.viewmodel.ChatViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceId
import com.yeslab.fastprefs.FastPrefs
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var firebaseUser : FirebaseUser? = null
    private var firebaseAuth : FirebaseAuth? = null
    private val userViewModel by viewModel<UserViewModel>()
    private lateinit var adsController : AdsController
    private val chatViewModel by viewModel<ChatViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                SteamFragment()
            ).commit()
        }

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings


        adsController = AdsController(this)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseAuth = FirebaseAuth.getInstance()

        adsController.loadRewarded()



        binding.steam.setOnClickListener {
            NavFragment.openNewFragment(SteamFragment(),this, R.id.fragment_container)
        }
        binding.epic.setOnClickListener {
            NavFragment.openNewFragment(EpicGamesFragment(),this, R.id.fragment_container)
        }

        binding.news.setOnClickListener {
            NavFragment.openNewFragment(NewsFragment(),this, R.id.fragment_container)
        }

        binding.menuBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)

        }

        binding.unreadsLay.setOnClickListener {
            handleMessagesCardClick()
        }


        if (firebaseUser!=null){
            userViewModel.updateToken(FirebaseInstanceId.getInstance().token!!, firebaseUser!!.uid)
            setupUnreadMessagesCounter()

        }
        setMenu()



    }

    private fun setMenu() {
        val header: View = binding.navigationDrawerMenu.getHeaderView(0)

        // CardViews
        val goSettingsCard = header.findViewById<CardView>(R.id.goSettingsCard)
        val goProfileCard = header.findViewById<CardView>(R.id.profileCard)
        val roomsCard = header.findViewById<CardView>(R.id.roomsCard)
        val shopCard = header.findViewById<CardView>(R.id.shopCard)
        val postsCard = header.findViewById<CardView>(R.id.postsCard)
        val searchCard = header.findViewById<CardView>(R.id.searchCard)
        val signOutCard = header.findViewById<CardView>(R.id.signOutCard)
        val earnCoinCard = header.findViewById<CardView>(R.id.earnCoinCard)

        // TextViews
        val profileText = header.findViewById<TextView>(R.id.profileText)
        val coinText = header.findViewById<TextView>(R.id.coinText)

        // Layouts
        val coinLay = header.findViewById<RelativeLayout>(R.id.coinLay)

        // Check if user is authenticated
        if (firebaseUser != null) {
            setupAuthenticatedUserUI(postsCard,profileText,signOutCard,coinLay,coinText)
        } else {
            setupUnauthenticatedUserUI(postsCard,coinLay,profileText,signOutCard)
        }

        // Click listeners
        searchCard.setOnClickListener {
            handleSearchCardClick()
        }

        postsCard.setOnClickListener {
            handlePostsCardClick()
        }

        earnCoinCard.setOnClickListener {
            handleEarnCoinCardClick()
        }

        roomsCard.setOnClickListener {
            if (firebaseUser!=null){
                startActivity(Intent(this,RoomsActivity::class.java))
            }else{
                DummyMethods.showMotionToast(this,"Odaları görmek için giriş yapmalısın","",MotionToastStyle.INFO)
            }
        }


        signOutCard.setOnClickListener {
            handleSignOutCardClick()
        }

        goSettingsCard.setOnClickListener {
            handleGoSettingsCardClick()
        }

        goProfileCard.setOnClickListener {
            handleGoProfileCardClick()
        }
    }

    private fun setupAuthenticatedUserUI(postsCard :CardView, profileText : TextView, signOutCard : CardView, coinLay : RelativeLayout, coinText : TextView) {
        postsCard.visibility = View.VISIBLE
        profileText.text = "Profil"
        signOutCard.visibility = View.VISIBLE
        coinLay.visibility = View.VISIBLE

        userViewModel.getUserCoin(this, firebaseUser!!.uid) { coin ->
            coinText.text = "M2 Coin: ${DummyMethods.formatDoubleNumber(coin)}"
        }
    }

    private fun setupUnreadMessagesCounter() {
        chatViewModel.getUnreadMessages(firebaseUser!!.uid) { size ->
            size?.let {
                if (size < 1) {
                    binding.unreadsImage.setImageResource(R.drawable.message_square_lines_alt_svgrepo_com)
                    binding.unReadMessages.visibility = View.GONE
                } else {
                    binding.unreadsImage.setImageResource(R.drawable.unread_mail_svgrepo_com)
                    binding.unReadMessages.visibility = View.VISIBLE
                    binding.unReadMessages.text = "$size"
                }
            }
        }
    }

    private fun setupUnauthenticatedUserUI(postsCard: CardView, coinLay: RelativeLayout, profileText: TextView, signOutCard: CardView) {
        postsCard.visibility = View.GONE
        coinLay.visibility = View.GONE
        profileText.text = "Kayıt Ol"
        signOutCard.visibility = View.GONE
    }

    private fun handleSearchCardClick() {
        if (firebaseUser != null) {
            startActivity(Intent(this, SearchActivity::class.java))
        } else {
            DummyMethods.showMotionToast(
                this,
                "Kullanıcı Aramak İçin Oturum Açmalısınız.",
                "",
                MotionToastStyle.INFO
            )
        }
    }

    private fun handlePostsCardClick() {
        if (firebaseUser != null) {
            binding.drawerLayout.closeDrawers()
            NavFragment.openNewFragment(PostFragment(), this, R.id.fragment_container)
        }
    }

    private fun handleEarnCoinCardClick() {
        if (firebaseUser != null) {
            adsController.showRewarded {
                userViewModel.updateCoin(this, firebaseUser!!.uid, Constants.BONUS_REVENUE)
                DummyMethods.showMotionToast(this, "${Constants.BONUS_REVENUE} Coin Eklendi", "", MotionToastStyle.SUCCESS)
                adsController.loadRewarded()
            }
        } else {
            DummyMethods.showMotionToast(this, "Coin Kazanabilmek İçin Hesap Oluşturmalısın.", "", MotionToastStyle.INFO)
        }
    }

    private fun handleMessagesCardClick() {
        if (firebaseUser != null) {
            startActivity(Intent(this, MessagesActivity::class.java))
        } else {
            DummyMethods.showMotionToast(this, "Mesajlaşabilmek için hesap oluşturmalısın.", "", MotionToastStyle.INFO)
        }
    }

    private fun handleSignOutCardClick() {
        firebaseAuth?.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun handleGoSettingsCardClick() {
        startActivity(Intent(this, OptionsActivity::class.java))
    }

    private fun handleGoProfileCardClick() {
        if (firebaseUser != null) {
            startActivity(
                Intent(this, ProfileActivity::class.java)
                    .putExtra("userId", firebaseUser?.uid)
            )
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }




    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }




}