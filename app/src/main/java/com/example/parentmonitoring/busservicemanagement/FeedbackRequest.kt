package com.example.parentmonitoring.busservicemanagement

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.parentmonitoring.MainActivity
import com.example.parentmonitoring.R
import com.example.parentmonitoring.data.FeedbackRequest
import com.example.parentmonitoring.databinding.ActivityFeedbackRequestBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FeedbackRequest : AppCompatActivity() {

    private lateinit var binding :ActivityFeedbackRequestBinding
    private lateinit var preferences: SharedPreferences
    private var UserID : String = ""
    private lateinit var txtContent: EditText
    private var currentUser: FirebaseUser?=null
    private val formatter = SimpleDateFormat("ddMMyyyy")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feedback_request)
        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        val bundle : Bundle? = intent.extras

        UserID = bundle!!.getString("UserID").toString()
        currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")

        txtContent=binding.edtContent

        binding.btnBack.setOnClickListener{
            val intent = Intent(this@FeedbackRequest, BusServiceManagement::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener{
            val intent = Intent(this@FeedbackRequest, BusServiceManagement::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            startActivity(intent)
        }

        binding.btnSend.setOnClickListener{
            if(validationCheck()){
                var feedback=FeedbackRequest()
                feedback.requestID=getRandomString()
                feedback.content=txtContent.text.toString()
                feedback.parentEmail= currentUser!!.email.toString()
                feedback.adminID="N.A"
                feedback.status="sent"
                feedback.parentID=UserID

                val date=formatter.format(Date()).toString()
                feedback.date=date.substring(0,2)+"/"+
                        date.substring(2,4)+"/"+
                        date.substring(4,8)

                if(binding.radioBtnFeedback.isChecked){
                    feedback.type="Feedback"
                }else{
                    feedback.type="Request"
                }
                var database: DatabaseReference = Firebase.database.reference

                database.child("FeedbackRequest").child(feedback.requestID).setValue(feedback)

                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Thank you for your feedback/request.")
                    .setContentText("Your feedback/request has been sent. Our customer service will reply you an email within 3 working days.")
                    .setConfirmClickListener {
                        val intent = Intent(this@FeedbackRequest, BusServiceManagement::class.java)
                        intent.putExtra("UserID",UserID)
                        intent.putExtra("currentUser",currentUser)
                        startActivity(intent)
                    }
                    .show()
                txtContent.setText("")


            }
        }

    }
    private fun validationCheck():Boolean{
        txtContent.setError(null)

        var result=true
        //check content
        if(txtContent.text.isNullOrEmpty()){
            txtContent.setError("Please enter your feedback/request content.")
            result=false
        }


        return result
    }

    private fun getRandomString() : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..12)
            .map { allowedChars.random() }
            .joinToString("")
    }

}