package com.example.parentmonitoring.busmonitoring

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.parentmonitoring.Login
import com.example.parentmonitoring.MainActivity
import com.example.parentmonitoring.R
import com.example.parentmonitoring.databinding.ActivityBusMonitoringBinding
import com.google.firebase.auth.FirebaseUser


class BusMonitoring : AppCompatActivity() {

    private lateinit var binding :ActivityBusMonitoringBinding
    private lateinit var preferences: SharedPreferences
    private var UserID : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bus_monitoring)

        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        val bundle : Bundle? = intent.extras
        UserID = bundle!!.getString("UserID").toString()
        val currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")



        binding.imageButtonLogout.setOnClickListener{
            val editor : SharedPreferences.Editor = preferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this@BusMonitoring, Login::class.java)
            finish()
            startActivity(intent)
        }

        binding.btnMBack.setOnClickListener{
            val intent = Intent(this@BusMonitoring, MainActivity::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }
    }
}