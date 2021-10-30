package com.example.parentmonitoring.account

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.parentmonitoring.databinding.ActivityUpdateProfileBinding
import com.example.parentmonitoring.R
import com.example.parentmonitoring.data.Parent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class UpdateProfile : AppCompatActivity() {


    private lateinit var preferences: SharedPreferences
    private var UserID : String = ""
    private lateinit var binding :ActivityUpdateProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var txtName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPhoneNo: EditText
    private lateinit var txtAddress: EditText
    private var currentUser:FirebaseUser?=null
    private lateinit var parent: Parent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_profile)

        database = Firebase.database.reference
        auth = Firebase.auth
        sharedPreferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        txtName=binding.edtFullName
        txtEmail=binding.editEmail
        txtPhoneNo=binding.edtPhoneNumber
        txtAddress=binding.edtAddress


        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        val bundle : Bundle? = intent.extras
        UserID = bundle!!.getString("UserID").toString()
        currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")




        binding.btnBack.setOnClickListener{
            val intent = Intent(this@UpdateProfile, AccountMain::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }
        parent= Parent()

        database.child("Parent").child(UserID).get().addOnSuccessListener {
            parent= it.getValue(Parent::class.java)!!
            txtName.setText(parent.name)
            txtAddress.setText(parent.address)
            txtPhoneNo.setText(parent.phoneNo)
            txtEmail.setText(currentUser!!.email)
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        binding.btnUpdate.setOnClickListener{

            if(validationCheck()){
                updateParent(parent.registerDate)

                if(!txtEmail.text.toString().equals(currentUser!!.email)){
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.apply {
                        putString("email", txtEmail.text.toString())
                    }.apply()

                    currentUser!!.updateEmail(txtEmail.text.toString()).addOnCompleteListener {
                        if(it.isSuccessful)
                        Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                    }
                }


            }

        }




    }

    private fun updateParent(registerDate:String){
        database=FirebaseDatabase.getInstance().getReference("Parent")
        val user= mapOf<String,String>(
            "name" to txtName.text.toString(),
            "phoneNo" to txtPhoneNo.text.toString(),
            "address" to txtAddress.text.toString(),
            "registerDate" to registerDate
        )

        database.child(UserID).updateChildren(user).addOnSuccessListener {
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Successfully Update")
                .setContentText("Personal Details has been updated.")
                .show()
        }
    }

    private fun validationCheck():Boolean{
        txtName.setError(null)
        txtEmail.setError(null)
        txtPhoneNo.setError(null)
        txtAddress.setError(null)
        var result=true
        //check name
        if(txtName.text.isNullOrEmpty()){
            txtName.setError("Please enter your name.")
            result=false
        }
        //check email
        if(txtEmail.text.isNullOrEmpty()){
            txtEmail.setError("Please enter your email.")
            result=false
        }else if(!checkEmail(txtEmail.text.toString())){
            txtEmail.setError("Invalid email format.")
            result=false
        }



        //check phone no
        if(txtPhoneNo.text.isNullOrEmpty()){
            txtPhoneNo.setError("Please enter your phone number.")
            result=false
        }else if(txtPhoneNo.text.toString().length<10){
            txtPhoneNo.setError("Invalid phone number.")
            result=false
        }


        //check ic
        if(txtAddress.text.isNullOrEmpty()){
            txtAddress.setError("Please enter your home address.")
            result=false
        }

        return result
    }

    private fun checkEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}