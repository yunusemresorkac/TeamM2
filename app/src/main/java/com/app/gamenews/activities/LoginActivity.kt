package com.app.gamenews.activities

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.app.gamenews.R
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.ActivityLoginBinding
import com.app.gamenews.model.User
import com.app.gamenews.util.Constants
import com.app.gamenews.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.aviran.cookiebar2.CookieBar
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var firestore : FirebaseFirestore
    private val userViewModel by viewModel<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loginOrRegisterText()

        binding.btnLogin.setOnClickListener {
            login()
        }
        binding.btnRegister.setOnClickListener {
            register()
        }

        binding.tvForgotPassword.setOnClickListener {
            forgotPassword()
        }

    }

    private fun forgotPassword(){
        val dialog = Dialog(this@LoginActivity,R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_forgot_password)

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)

        val editText: EditText = dialog.findViewById(R.id.forgotPasswordEt)
        val button: Button = dialog.findViewById(R.id.sendPasswordLink)

        button.setOnClickListener {
            if (editText.text.toString().trim().isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(editText.text.toString().trim())
                    .addOnSuccessListener {
                        dialog.dismiss()
                        CookieBar.build(this@LoginActivity)
                            .setTitle("Gelen kutunuzu kontrol edebilirsiniz!")
                            .setCookiePosition(CookieBar.TOP)
                            .show()
                    }
                    .addOnFailureListener {
                        dialog.dismiss()
                    }
            }
        }

    }



    private fun loginOrRegisterText(){
        var isLoginActive = true
        binding.loginOrRegisterText.setOnClickListener {
            if (isLoginActive){
                binding.loginLay.visibility = View.VISIBLE
                binding.registerLay.visibility = View.GONE
                binding.loginOrRegisterText.text = "Hesap Oluştur"
                isLoginActive = false
            }else{
                binding.loginLay.visibility = View.GONE
                binding.registerLay.visibility = View.VISIBLE
                binding.loginOrRegisterText.text = "Zaten Hesabım Var"
                isLoginActive = true

            }
        }

    }

    private fun login() {
        if (binding.emailLogin.text.toString().isNotEmpty() && binding.passwordLogin.text.toString().isNotEmpty()) {
            val pd = ProgressDialog(this, R.style.CustomDialog)
            pd.setCancelable(false)
            pd.show()

            coroutineScope.launch {
                try {
                    val authResult = withContext(Dispatchers.IO) {
                        firebaseAuth.signInWithEmailAndPassword(
                            binding.emailLogin.text.toString().lowercase().trim(),
                            binding.passwordLogin.text.toString().trim()
                        ).await()
                    }

                    val user = authResult.user
                    user?.reload()
                    val myUser = withContext(Dispatchers.IO) {
                        user?.let {
                            firestore.collection("Users").document(user.uid)
                                .get().await().toObject(User::class.java)
                        }

                    }

                    if (myUser != null) {

                        pd.dismiss()
                        DummyMethods.showMotionToast(this@LoginActivity,"Giriş Yapıldı","",MotionToastStyle.SUCCESS)
                        val intent = Intent(this@LoginActivity,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    pd.dismiss()
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun register() {
        if (binding.emailRegister.text.toString().isNotEmpty() && binding.passwordRegister.text.toString().isNotEmpty() &&
            binding.usernameRegister.text.toString().isNotEmpty()) {
            val pd = ProgressDialog(this@LoginActivity, R.style.CustomDialog)
            pd.setCancelable(false)
            pd.show()

            coroutineScope.launch {
                try {
                    val authResult = firebaseAuth.createUserWithEmailAndPassword(
                        binding.emailRegister.text.toString().lowercase().trim(),
                        binding.passwordRegister.text.toString().trim()
                    ).await()

                    val userId = authResult.user?.uid ?: ""
                    val user = User(userId,System.currentTimeMillis(),binding.usernameRegister.text.toString().trim()
                    ,binding.emailRegister.text.toString().trim(),0.0,Constants.NORMAL_USER,Constants.OFFLINE_STATUS,""
                    ,0,0)

                    firestore.collection("Users").document(userId)
                        .set(user).addOnCompleteListener {

                            pd.dismiss()
                            DummyMethods.showMotionToast(this@LoginActivity,"Hesabınız Oluşturuldu","",MotionToastStyle.SUCCESS)
                            val intent = Intent(this@LoginActivity,MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        }

                } catch (e: Exception) {
                    pd.dismiss()
                    Toast.makeText(this@LoginActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




}