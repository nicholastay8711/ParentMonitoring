package com.example.parentmonitoring

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.parentmonitoring.account.AccountMain
import com.example.parentmonitoring.busmonitoring.BusMonitoring
import com.example.parentmonitoring.busservicemanagement.BusServiceManagement
import com.example.parentmonitoring.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var preferences: SharedPreferences
    private var UserID : String = ""
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        val bundle : Bundle? = intent.extras
        UserID = bundle!!.getString("UserID").toString()
        val currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")




        binding.imageButtonLogout.setOnClickListener{
            val editor : SharedPreferences.Editor = preferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this@MainActivity, Login::class.java)
            finish()
            startActivity(intent)
        }

        binding.cvService.setOnClickListener {
            val intent = Intent(this@MainActivity, BusServiceManagement::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.cvMonitoring.setOnClickListener {
            val intent = Intent(this@MainActivity, BusMonitoring::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.cvProfile.setOnClickListener {
            val intent = Intent(this@MainActivity, AccountMain::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

    }
}