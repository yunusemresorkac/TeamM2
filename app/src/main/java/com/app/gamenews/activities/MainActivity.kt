package com.app.gamenews.activities

import android.content.Intent
import android.net.http.UrlRequest.Status
import android.os.Bundle
import android.view.View
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

        setMenu()



    }



    private fun setMenu(){
        val header: View = binding.navigationDrawerMenu.getHeaderView(0)
        val goSettingsCard = header.findViewById<CardView>(R.id.goSettingsCard)
        val goProfileCard = header.findViewById<CardView>(R.id.profileCard)
        val messagesCard = header.findViewById<CardView>(R.id.messagesCard)
        val roomsCard = header.findViewById<CardView>(R.id.roomsCard)
        val shopCard = header.findViewById<CardView>(R.id.shopCard)
        val postsCard = header.findViewById<CardView>(R.id.postsCard)
        val searchCard = header.findViewById<CardView>(R.id.searchCard)

        val signOutCard = header.findViewById<CardView>(R.id.signOutCard)
        val earnCoinCard = header.findViewById<CardView>(R.id.earnCoinCard)

        val profileText = header.findViewById<TextView>(R.id.profileText)
        val coinText = header.findViewById<TextView>(R.id.coinText)
        val coinLay = header.findViewById<RelativeLayout>(R.id.coinLay)

        searchCard.setOnClickListener {
            if (firebaseUser!=null){
                startActivity(Intent(this,SearchActivity::class.java))
            }else{
                DummyMethods.showMotionToast(this,"Kullanıcı Aramak İçin Oturum Açmalısınız.","",
                    MotionToastStyle.INFO)
            }

        }

        if (firebaseUser !=null){
            postsCard.visibility = View.VISIBLE
            profileText.text = "Profil"
            signOutCard.visibility = View.VISIBLE
            coinLay.visibility = View.VISIBLE
            userViewModel.getUserCoin(this, firebaseUser!!.uid){ coin ->
                coinText.text = "M2 Coin: ${DummyMethods.formatDoubleNumber(coin)}"
            }
        }else{
            postsCard.visibility = View.GONE
            coinLay.visibility = View.GONE
            profileText.text = "Kayıt Ol"
            signOutCard.visibility = View.GONE
        }

        postsCard.setOnClickListener {
            if (firebaseUser!=null){
                binding.drawerLayout.closeDrawers()
                NavFragment.openNewFragment(PostFragment(),this, R.id.fragment_container)

            }
        }

        earnCoinCard.setOnClickListener {
            if (firebaseUser!=null){
                adsController.showRewarded {
                    userViewModel.updateCoin(this,firebaseUser!!.uid,Constants.BONUS_REVENUE)
                    DummyMethods.showMotionToast(this,"${Constants.BONUS_REVENUE} Coin Eklendi","",
                        MotionToastStyle.SUCCESS)
                    adsController.loadRewarded()
                }

            }else{
                DummyMethods.showMotionToast(this,"Coin Kazanabilmek İçin Hesap Oluşturmalısın.","",
                    MotionToastStyle.INFO)
            }
        }


        messagesCard.setOnClickListener {
            if (firebaseUser!=null){
                startActivity(Intent(this,MessagesActivity::class.java))
            }else{
                DummyMethods.showMotionToast(this,"Mesajlaşabilmek için hesap oluşturmalısın.","",
                    MotionToastStyle.INFO)

            }
        }

        signOutCard.setOnClickListener {
            firebaseAuth?.signOut()
            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        goSettingsCard.setOnClickListener {
            startActivity(Intent(this,OptionsActivity::class.java))
        }

        goProfileCard.setOnClickListener {
            if (firebaseUser!=null){
                startActivity(Intent(this,ProfileActivity::class.java)
                    .putExtra("userId", firebaseUser?.uid)
                )

            }else{
                startActivity(Intent(this,LoginActivity::class.java))

            }
        }

    }

    private fun updateStatusBarColor() {
        val isDarkMode = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)

        if (isDarkMode) {
            // Karanlık moddaysa
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        } else {
            // Normal moddaysa
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
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