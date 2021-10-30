package com.example.parentmonitoring.busservicemanagement

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
import com.example.parentmonitoring.account.UpdateProfile
import com.example.parentmonitoring.databinding.ActivityBusServiceManagementBinding
import com.google.firebase.auth.FirebaseUser


class BusServiceManagement : AppCompatActivity() {

    private lateinit var binding : ActivityBusServiceManagementBinding
    private lateinit var preferences: SharedPreferences
    private var UserID : String = ""
    private var currentUser: FirebaseUser?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bus_service_management)
        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        val bundle : Bundle? = intent.extras

        UserID = bundle!!.getString("UserID").toString()
        currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")




        binding.imageButtonLogout.setOnClickListener{
            val editor : SharedPreferences.Editor = preferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this@BusServiceManagement, Login::class.java)
            finish()
            startActivity(intent)
        }

        binding.btnBBack.setOnClickListener{
            val intent = Intent(this@BusServiceManagement, MainActivity::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.cvChildrenInfo.setOnClickListener {
            val intent = Intent(this@BusServiceManagement, ChildrenInfo::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.cvRequest.setOnClickListener {
            val intent = Intent(this@BusServiceManagement, FeedbackRequest::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.cvAbsence.setOnClickListener {
            val intent = Intent(this@BusServiceManagement, ApplyAbsence::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.cvReport.setOnClickListener {
            val intent = Intent(this@BusServiceManagement, ReportLostCard::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }



    }
}