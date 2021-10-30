package com.example.parentmonitoring.busservicemanagement

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.parentmonitoring.R
import com.example.parentmonitoring.data.Children
import com.example.parentmonitoring.databinding.ActivityEditChildrenBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditChildren : AppCompatActivity() {

    private lateinit var binding :ActivityEditChildrenBinding
    private lateinit var database: DatabaseReference
    private var children= Children()
    private lateinit var txtAddress: EditText
    private lateinit var txtAge: EditText
    private lateinit var txtName: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private var UserID : String = ""
    private var childrenID : String = ""
    private var currentUser: FirebaseUser?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_children)
        database = Firebase.database.reference
        txtName=binding.edtFullName
        txtAddress=binding.edtAddress
        txtAge=binding.edtAge

        sharedPreferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
        val bundle : Bundle? = intent.extras
        UserID = bundle!!.getString("UserID").toString()
        currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")
        childrenID = bundle!!.getString("childrenID").toString()

        database.child("Children").child(childrenID).get().addOnSuccessListener {
            children= it.getValue(Children::class.java)!!
            txtName.setText(children.name)
            txtAddress.setText(children.address)
            txtAge.setText(children.age)
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        binding.btnBack.setOnClickListener{
            val intent = Intent(this@EditChildren, ChildrenDetail::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            intent.putExtra("childrenID",childrenID)
            startActivity(intent)
        }

        binding.btnUpdate.setOnClickListener{
            if(validationCheck()){

                children.address=txtAddress.text.toString()
                children.age=txtAge.text.toString()
                children.name=txtName.text.toString()

                updateChildren()


                lifecycleScope.launch{
                    delay(1500)
                    val intent = Intent(this@EditChildren, ChildrenDetail::class.java)
                    intent.putExtra("UserID",UserID)
                    intent.putExtra("currentUser",currentUser)
                    intent.putExtra("childrenID",childrenID)
                    startActivity(intent)
                }


            }
        }

    }


    private fun validationCheck():Boolean{
        txtName.setError(null)
        txtAge.setError(null)
        txtAddress.setError(null)
        var result=true
        //check name
        if(txtName.text.isNullOrEmpty()){
            txtName.setError("Please enter your name.")
            result=false
        }


        //check address
        if(txtAddress.text.isNullOrEmpty()){
            txtAddress.setError("Please enter your children's address.")
            result=false
        }

        if(txtAge.text.isNullOrEmpty()){
            txtAge.setError("Please enter your children's age.")
            result=false
        }else if(txtAge.text.toString().toInt()<5 || txtAge.text.toString().toInt()>17){
            txtAge.setError("Only children age between 5 to 17 are allowed for this service.")
            result=false
        }

        return result
    }


    private fun updateChildren(){
        database= FirebaseDatabase.getInstance().getReference("Children")
        val user= mapOf<String,String>(
            "parentID" to children.parentID,
            "name" to txtName.text.toString(),
            "age" to txtAge.text.toString(),
            "address" to txtAddress.text.toString(),
            "cardNo" to children.cardNo,
            "subscribed_service" to children.subscribed_service

        )

        database.child(childrenID).updateChildren(user).addOnSuccessListener {
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Successfully Update")
                .setContentText("Children information has been updated.")
                .show()
        }
    }
}