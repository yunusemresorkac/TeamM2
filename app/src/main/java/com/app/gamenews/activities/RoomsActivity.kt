package com.app.gamenews.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.adapter.CommentAdapter
import com.app.gamenews.adapter.RoomAdapter
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.ActivityRoomsBinding
import com.app.gamenews.model.Room
import com.app.gamenews.viewmodel.RoomViewModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle

class RoomsActivity : AppCompatActivity(),RoomAdapter.RoomClick {

    private lateinit var binding : ActivityRoomsBinding
    private var firebaseUser : FirebaseUser? = null
    private lateinit var roomList : ArrayList<Room>
    private lateinit var roomAdapter : RoomAdapter
    private val roomViewModel by viewModel<RoomViewModel>()
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser

        initRecycler()
        roomViewModel.fetchInitialData()
        observeViewModel()

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isLoading && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    isLoading = true
                    roomViewModel.loadMoreData()
                }
            }
        })

        binding.createRoom.setOnClickListener {
            if (firebaseUser!=null){
                startActivity(Intent(this,CreateRoomActivity::class.java))
            }
        }

        binding.findByIdBtn.setOnClickListener {
            showFindRoomDialog()
        }


    }

    private fun initRecycler(){
        roomList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        roomAdapter = RoomAdapter(roomList,this,this)
        binding.recyclerView.adapter = roomAdapter

    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            roomViewModel.list.collect { rooms ->
                roomList.clear()
                roomList.addAll(rooms)
                roomAdapter.notifyDataSetChanged()
                isLoading = false
            }
        }
    }


    private fun showFindRoomDialog(){
        val dialog = Dialog(this@RoomsActivity,R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_find_room)

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)

        val findRoomEt: EditText = dialog.findViewById(R.id.findRoomEt)
        val findRoomBtn: Button = dialog.findViewById(R.id.findRoomBtn)

        findRoomBtn.setOnClickListener {
            if (findRoomEt.text.toString().trim().isNotEmpty()){
                roomViewModel.getRoomById(this,findRoomEt.text.toString().trim()){ room ->
                    if (room!=null){
                        dialog.dismiss()
                        joinToRoom(room)
                    }
                }

            }
        }
    }

    private fun joinToRoom(room: Room){
        if (room.currentUsers < room.maxUsers){
            roomViewModel.joinToRoom(room.roomId,firebaseUser!!.uid){
                startActivity(Intent(this,RoomDetailsActivity::class.java)
                    .putExtra("roomId",room.roomId))
            }

        }else{
            DummyMethods.showMotionToast(this,"Bu oda şu anda full!","Lütfen daha sonra tekrar dene",MotionToastStyle.WARNING)
        }
    }


    override fun clickRoom(room: Room) {
        joinToRoom(room)

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }


}