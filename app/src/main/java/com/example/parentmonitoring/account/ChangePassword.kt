package com.example.parentmonitoring.account

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.parentmonitoring.R
import com.example.parentmonitoring.busservicemanagement.AbsenceHistory
import com.example.parentmonitoring.data.Parent
import com.example.parentmonitoring.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChangePassword : AppCompatActivity() {



    private lateinit var preferences: SharedPreferences
    private var UserID : String = ""
    private lateinit var binding :ActivityChangePasswordBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var txtOldPassword: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtCfmPassword: EditText
    private var currentUser: FirebaseUser?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        database = Firebase.database.reference
        auth = Firebase.auth
        sharedPreferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        txtPassword=binding.etdPassword
        txtCfmPassword=binding.edtConfirmPassword
        txtOldPassword=binding.edtOldPassword

        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        val bundle : Bundle? = intent.extras
        UserID = bundle!!.getString("UserID").toString()
        currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")


        binding.btnBack.setOnClickListener{
            val intent = Intent(this@ChangePassword, AccountMain::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.btnConfirm.setOnClickListener{
            if(validationCheck()){
                val user = Firebase.auth.currentUser!!

                val credential = EmailAuthProvider
                    .getCredential(user!!.email.toString(), txtOldPassword.text.toString())
                user!!.reauthenticate(credential).addOnCompleteListener{
                    if(it.isSuccessful){
                        user!!.updatePassword(txtPassword.text.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                    editor.apply {
                                        putString("password", txtPassword.text.toString())
                                    }.apply()
                                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Success")
                                        .setContentText("Password has successfully changed.")
                                        .setConfirmClickListener {
                                            val intent = Intent(this@ChangePassword, AccountMain::class.java)
                                            intent.putExtra("UserID",UserID)
                                            intent.putExtra("currentUser",currentUser)
                                            finish()
                                            startActivity(intent)
                                        }
                                        .show()
                                    currentUser=user
                                }
                            }
                    }else{
                        txtOldPassword.setError("Wrong old password. Please try again")
                    }
                }
            }

        }

    }

    private fun validationCheck():Boolean{
        var result=true
        txtPassword.setError(null)
        txtCfmPassword.setError(null)
        txtOldPassword.setError(null)




        //check password
        if(txtPassword.text.isNullOrEmpty()||txtPassword.text.length<8){
            txtPassword.setError("Password must at least 8 characters.")
            result=false
        }

        if(!txtCfmPassword.text.toString().equals(txtPassword.text.toString())){
            txtCfmPassword.setError("Password does not match.")
            result=false
        }
        if(txtOldPassword.text.isNullOrEmpty()||txtOldPassword.text.length<8){
            txtOldPassword.setError("Password must at least 8 characters.")
            result=false
        }




        return result
    }
}