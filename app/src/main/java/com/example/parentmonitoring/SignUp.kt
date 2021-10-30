package com.example.parentmonitoring

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import com.example.parentmonitoring.data.Parent
import com.example.parentmonitoring.data.writeNewParent
import com.example.parentmonitoring.databinding.ActivitySignUpBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.parentmonitoring.busservicemanagement.AbsenceHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class SignUp : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var parent:Parent= Parent()

    private lateinit var txtName:EditText
    private lateinit var txtEmail:EditText
    private lateinit var txtPhoneNo:EditText
    private lateinit var txtPassword:EditText
    private lateinit var txtCfmPassword:EditText
    private lateinit var txtAddress:EditText

    private val formatter = SimpleDateFormat("ddMMyyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Firebase.database.reference
        auth = Firebase.auth
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        txtName=binding.edtFullName
        txtEmail=binding.editEmail
        txtPhoneNo=binding.edtPhoneNumber
        txtPassword=binding.etdPassword
        txtCfmPassword=binding.edtConfirmPassword
        txtAddress=binding.edtAddress




        binding.btnBack.setOnClickListener{
            val intent = Intent(this@SignUp, Login::class.java)
            startActivity(intent)
        }

        binding.btnReset.setOnClickListener{
            reset()

        }

        binding.btnConfirm.setOnClickListener{

            if(validationCheck()){
                parent.name=txtName.text.toString()
                parent.email=txtEmail.text.toString()
                parent.phoneNo=txtPhoneNo.text.toString()
                parent.password=txtPassword.text.toString()
                parent.address=txtAddress.text.toString()

                val date=formatter.format(Date()).toString()
                parent.registerDate=date.substring(0,2)+"/"+
                        date.substring(2,4)+"/"+
                        date.substring(4,8)

                auth.createUserWithEmailAndPassword(parent.email, parent.password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")

                            val user = auth.currentUser

                            user!!.sendEmailVerification()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "Email sent.")
                                    }
                                }


                            var userID= user?.uid
                            parent.UserID=userID
                            writeNewParent(parent)
                            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Successfully Signed Up")
                                .setContentText("Verification email has been sent, please verify your email. We will redirect you to login page in 5 seconds")
                                .setConfirmClickListener {
                                    val intent = Intent(this@SignUp, Login::class.java)
                                    startActivity(intent)
                                }
                                .show()
                            reset()

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            txtEmail.setError("Email is already registered.")

                        }
                    }




            }else{
                Toast.makeText(applicationContext,"Some information does not match the requirement. Please try again.", Toast.LENGTH_LONG).show()
            }

        }


    }

    private fun validationCheck():Boolean{
        txtName.setError(null)
        txtEmail.setError(null)
        txtPhoneNo.setError(null)
        txtPassword.setError(null)
        txtCfmPassword.setError(null)
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
//        else if(checkDuplicateEmail(txtEmail.text.toString())){
//            txtEmail.setError("Email is already registered.")
//            result=false
//        }


        //check phone no
        if(txtPhoneNo.text.isNullOrEmpty()){
            txtPhoneNo.setError("Please enter your phone number.")
            result=false
        }else if(txtPhoneNo.text.toString().length<10){
            txtPhoneNo.setError("Invalid phone number.")
            result=false
        }

        //check password
        if(txtPassword.text.isNullOrEmpty()||txtPassword.text.length<8){
            txtPassword.setError("Password must at least 8 characters.")
            result=false
        }

        if(!txtCfmPassword.text.toString().equals(txtPassword.text.toString())){
            txtCfmPassword.setError("Password does not match.")
            result=false
        }

        //check address
        if(txtAddress.text.isNullOrEmpty()){
            txtAddress.setError("Please enter your home address.")
            result=false
        }

        return result
    }

    private fun checkEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

//    private fun checkDuplicateEmail(email: String):Boolean{
//        var result=false
//        for(p in ParentList){
//            if(p.email.equals(email))
//                result=true
//        }
//        return result
//    }



//        var parent:Parent
//
//            database.child("Parent").child(ICNumber).get().addOnSuccessListener {
//                if(it.getValue(Parent::class.java)!=null){
//                    parent= it.getValue(Parent::class.java)!!
//                    if(!parent.name.isNullOrEmpty()){
//                        txtICNumber.setError("IC number has already registered an account.")
//                    }
//                }
//            }




    private fun reset(){
        txtName.setText("")
        txtEmail.setText("")
        txtPhoneNo.setText("")
        txtPassword.setText("")
        txtCfmPassword.setText("")
        txtAddress.setText("")
    }

//    private fun getParentList(){
//        database.child("Parent").addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (snap in dataSnapshot.children) {
//                    // TODO: handle the post
//                    var parent:Parent= snap.getValue(Parent::class.java)!!
//                    ParentList.add(parent)
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//                // ...
//            }
//        })
//
//
//    }





}