package com.example.parentmonitoring.account

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.parentmonitoring.Login
import com.example.parentmonitoring.MainActivity
import com.example.parentmonitoring.R
import com.example.parentmonitoring.databinding.ActivityAccountMainBinding
import com.google.firebase.auth.FirebaseUser

class AccountMain : AppCompatActivity() {

    private lateinit var preferences: SharedPreferences
    private var UserID : String = ""
    private lateinit var binding :ActivityAccountMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account_main)


        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        val bundle : Bundle? = intent.extras
        UserID = bundle!!.getString("UserID").toString()
        val currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")

        binding.imageButtonLogout.setOnClickListener{
            val editor : SharedPreferences.Editor = preferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this@AccountMain, Login::class.java)
            finish()
            startActivity(intent)
        }

        binding.btnMBack.setOnClickListener{
            val intent = Intent(this@AccountMain, MainActivity::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.cvUpdateProfile.setOnClickListener {
            val intent = Intent(this@AccountMain, UpdateProfile::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.cvChangePassword.setOnClickListener {
            val intent = Intent(this@AccountMain, ChangePassword::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }
    }
}