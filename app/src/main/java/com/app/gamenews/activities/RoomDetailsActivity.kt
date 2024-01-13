package com.app.gamenews.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.adapter.ChatAdapter
import com.app.gamenews.adapter.RoomChatAdapter
import com.app.gamenews.adapter.RoomUserAdapter
import com.app.gamenews.adapter.UserAdapter
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.controller.StatusManager
import com.app.gamenews.databinding.ActivityRoomDetailsBinding
import com.app.gamenews.model.Chat
import com.app.gamenews.model.RoomChat
import com.app.gamenews.model.User
import com.app.gamenews.viewmodel.JoinersViewModel
import com.app.gamenews.viewmodel.RoomChatViewModel
import com.app.gamenews.viewmodel.RoomViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.hdodenhof.circleimageview.CircleImageView
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class RoomDetailsActivity : AppCompatActivity() , RoomUserAdapter.UserClick{

    private lateinit var binding : ActivityRoomDetailsBinding
    private lateinit var roomId : String
    private val roomViewModel by viewModel<RoomViewModel>()
    private lateinit var userAdapter: RoomUserAdapter
    private lateinit var userList : ArrayList<User>
    private val joinersViewModel by viewModel<JoinersViewModel>()
    private var firebaseUser : FirebaseUser? = null
    private lateinit var chatAdapter: RoomChatAdapter
    private lateinit var chatList : ArrayList<RoomChat>
    private val roomChatViewModel by viewModel<RoomChatViewModel>()
    private var isLoading = false
    private val userViewModel by viewModel<UserViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            manageExit()
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser

        roomId = intent.getStringExtra("roomId").toString()

        initRecycler()
        initRecyclerChat()
        roomChatViewModel.fetchInitialData(roomId)
        observeChatViewModel()
        getRoomById()

        getJoiners()

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isLoading && firstVisibleItemPosition == 0 && dy < 0) {
                    isLoading = true
                    roomChatViewModel.loadMoreData(roomId)
                }
            }
        })

        binding.sendMsgBtn.setOnClickListener {
            sendMessage()
        }

        binding.moreBtn.setOnClickListener {
            showMoreDialog()
        }

        binding.shareBtn.setOnClickListener{
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("roomId",roomId)
            clipboard.setPrimaryClip(clip)
            DummyMethods.showMotionToast(this,"Oda ID'si Kopyalandı","",MotionToastStyle.SUCCESS)
        }


    }

    private fun showMoreDialog(){
        val dialog = Dialog(this@RoomDetailsActivity,R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_room_more)

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)

        val dcCard: CardView = dialog.findViewById(R.id.dcCard)
        val dcInfoText: TextView = dialog.findViewById(R.id.dcInfoText)
        val adminText: TextView = dialog.findViewById(R.id.adminName)
        val titleMore: TextView = dialog.findViewById(R.id.titleMore)
        val adminImage: CircleImageView = dialog.findViewById(R.id.adminImage)
        val gameImage: ImageView = dialog.findViewById(R.id.gameImage)

        roomViewModel.getRoomById(this,roomId){
            it?.let {  room ->
                if (room.roomImage.isNotEmpty()){
                    Glide.with(this).load(room.roomImage).into(gameImage)
                }else{
                    gameImage.setImageResource(R.drawable.logo_transparent)
                }
                if (room.discordUrl.equals("")){
                    dcInfoText.text = "Discord Mevcut Değil"
                    dcCard.isEnabled = false
                }else{
                    dcCard.isEnabled = true
                    dcInfoText.text = "Discord Adresine Git"
                    dcCard.setOnClickListener{
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(room.discordUrl)))
                    }
                }
                titleMore.text = room.title
                userViewModel.getUsername(this,room.adminId){ username ->
                    adminText.text = "Admin -> $username"
                }
                userViewModel.getUserImage(this,room.adminId){ image ->
                    if (image.isNotEmpty()){
                        Glide.with(this).load(image).into(adminImage)
                    }
                }
            }
        }


    }





    private fun sendMessage(){
        if (binding.messageEt.text.toString().trim().isNotEmpty()){
            val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            iso8601Format.timeZone = TimeZone.getTimeZone("UTC")

            val currentDate = iso8601Format.format(Date())
            val chat = firebaseUser?.let { RoomChat(it.uid,currentDate,roomId, DummyMethods.generateRandomString(10),
                binding.messageEt.text.toString().trim()) }

            chat?.let { roomChatViewModel.sendMessage(it) { binding.messageEt.text.clear() } }

        }
    }

    private fun getJoiners(){
        joinersViewModel.getJoinerIds(
            roomId,
            onComplete = { ids ->
                if (ids.isNotEmpty()) {
                    // Do something with the followerIds list
                    joinersViewModel.fetchInitialData(ids)
                    observeViewModel()
                }

            },
            onError = { exception ->
                // Handle the exception
                println("Error fetching followers: $exception")
            }
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            joinersViewModel.list.collect { users ->
                userList.clear()
                userList.addAll(users)
                userList.sort()
                userAdapter.notifyDataSetChanged()
                println("kullanıcılar ${users.size}")
            }
        }
    }

    private fun observeChatViewModel() {
        lifecycleScope.launchWhenStarted {
            roomChatViewModel.list.collect { messages ->
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


    private fun initRecycler(){
        userList = ArrayList()
        binding.recyclerView.layoutManager = GridLayoutManager(this,1,RecyclerView.HORIZONTAL,false)
        binding.recyclerView.setHasFixedSize(true)
        userAdapter = RoomUserAdapter(userList, this,this)
        binding.recyclerView.adapter = userAdapter
    }

    private fun initRecyclerChat(){
        chatList = ArrayList()
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerViewChat.layoutManager = layoutManager
        binding.recyclerViewChat.setHasFixedSize(true)
        chatAdapter = RoomChatAdapter(chatList,this,userViewModel,roomChatViewModel)
        binding.recyclerViewChat.adapter = chatAdapter
    }
    private fun getRoomById(){
        roomViewModel.getRoomById(this,roomId){
            it?.let {  room ->
                binding.toolbar.title = room.game

            }
        }
    }

    override fun clickUser(user: User) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("userId", user.userId)
        startActivity(intent)
    }

//    override fun onRestart() {
//        super.onRestart()
//        roomViewModel.joinToRoom(roomId,firebaseUser!!.uid) { println("") }
//
//    }

//    override fun onPause() {
//        super.onPause()
//        roomViewModel.deleteUserFromRoom(roomId,firebaseUser!!.uid) { println("") }
//
//    }




    override fun onDestroy() {
        super.onDestroy()
        roomViewModel.deleteUserFromRoom(roomId,firebaseUser!!.uid) { println("") }

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        manageExit()
    }


    private fun manageExit(){
        joinersViewModel.getJoinerIds(
            roomId,
            onComplete = { ids ->
                if (ids.size == 1){
                    showSureDialog("Çık","İptal","Odadan çıkmak istediğine emin misin?","",{
                        roomViewModel.deleteRoom(roomId)
                        startActivity(Intent(this,RoomsActivity::class.java))
                        finish()
                    },{

                    })
                }else{
                    showSureDialog("Çık","İptal","Odadan çıkmak istediğine emin misin?","",{
                        startActivity(Intent(this,RoomsActivity::class.java))
                        finish()
                    },{

                    })
                }


            },
            onError = { exception ->
                // Handle the exception
                println("Error fetching followers: $exception")
            }
        )


    }

    private fun showSureDialog( positiveBtnText : String, negativeBtnText : String, title : String,message : String, onPositiveClick: () -> Unit, onNegativeClick : () -> Unit){

        val mDialog = MaterialDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(positiveBtnText) { dialogInterface, which ->
                dialogInterface?.dismiss()
                onPositiveClick()
            }
            .setNegativeButton(negativeBtnText) { dialogInterface, which ->
                dialogInterface.dismiss()
                onNegativeClick()
            }
            .build()

        if (!this.isFinishing){
            mDialog.show()
        }



    }


}