package com.example.parentmonitoring

import android.app.AlertDialog
import android.content.*
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.parentmonitoring.data.Parent
import com.example.parentmonitoring.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var ParentList: MutableList<Parent>
    private lateinit var database: DatabaseReference
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var auth: FirebaseAuth
    private var parent: Parent = Parent()
    private var UserID=""

    // Remember Me Feature
    private lateinit var sharedPreferences: SharedPreferences
    var isRemembered = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = Firebase.auth

        sharedPreferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
        isRemembered = sharedPreferences.getBoolean("CHECKBOX", false)

        if (isRemembered) {
            var email = sharedPreferences!!.getString("email","").toString()
            var password = sharedPreferences!!.getString("password","").toString()
            auth.signInWithEmailAndPassword(email, password)
            val currentUser=auth.currentUser
            val intent = Intent(this@Login, MainActivity::class.java)
            intent.putExtra("UserID", sharedPreferences.getString("UserID", "").toString())
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }


        database = Firebase.database.reference
        ParentList = mutableListOf()
//        getParentList()

        txtEmail = binding.tfEmail
        txtPassword = binding.tfPassword

        binding.tvForgotPassword.setOnClickListener {

            val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Reset Password")

            // Set up the input
            val input = EditText(this)
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setHint("Enter your email to reset password.")
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("Reset", DialogInterface.OnClickListener { dialog, which ->
                // Here you get get input text from the Edittext
                var emailAddress = input.text.toString()
                Firebase.auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email sent.")
                        }
                    }
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Success")
                    .setContentText("Password reset email has been sent to your email. Please check your inbox and reset your password.")
                    .show()
            })
            builder.setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

            builder.show()


        }

        binding.btnSignUp.setOnClickListener {

            val intent = Intent(this@Login, SignUp::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            var checked: Boolean = binding.chkRememberMe.isChecked

            if (emptyFieldCheck()) {

                auth.signInWithEmailAndPassword(
                    txtEmail.text.toString(),
                    txtPassword.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            UserID=user!!.uid
                            var isParent=false

                            lifecycleScope.launch {
                                isParent =isParent()
                            }.invokeOnCompletion {
                                if(isParent){
                                    if (user!!.isEmailVerified()) {
                                        if (checked) {
                                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                            editor.apply {
                                                putString("UserID", user!!.uid)
                                                putBoolean("CHECKBOX", checked)
                                                putString("email", user!!.email)
                                                putString("password",txtPassword.text.toString())
                                            }.apply()
                                        }

                                        val intent = Intent(this@Login, MainActivity::class.java)
                                        intent.putExtra("UserID", user!!.uid)
                                        intent.putExtra("currentUser",user)
                                        finish()
                                        startActivity(intent)
                                    } else {

                                        user!!.sendEmailVerification()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Log.d(TAG, "Email sent.")
                                                }
                                            }
                                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Email is not verified")
                                            .setContentText("Verification email has been send, please check your email inbox and verified your email.")
                                            .show()
                                    }
                                }else{
                                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Error")
                                        .setContentText("Invalid email or password, please try again.111")
                                        .show()
                                }

                            }


                        } else {
                            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error")
                                .setContentText("Invalid email or password, please try again.")
                                .show()

                        }
                    }



            }

        }

    }

    private fun emptyFieldCheck(): Boolean {
        var result = true

        txtEmail.setError(null)
        txtPassword.setError(null)

        if (txtEmail.text.toString().isNullOrEmpty()) {
            txtEmail.setError("Email is required.")
            result = false
        }

        if (txtPassword.text.toString().isNullOrEmpty()) {
            txtPassword.setError("Password is required.")
            result = false
        }
        return result

    }

    private suspend fun isParent():Boolean{
        return database.child("Parent").child(UserID).get().await().exists()
    }


}