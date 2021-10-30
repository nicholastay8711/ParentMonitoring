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
import com.example.parentmonitoring.R
import com.example.parentmonitoring.data.Children
import com.example.parentmonitoring.data.writeNewChildren
import com.example.parentmonitoring.databinding.ActivityAddChildrenBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddChildren : AppCompatActivity() {

    private lateinit var binding : ActivityAddChildrenBinding
    private lateinit var database: DatabaseReference
    private var children= Children()
    private lateinit var txtAddress:EditText
    private lateinit var txtAge:EditText
    private lateinit var txtName: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private var UserID : String = ""
    private var currentUser:FirebaseUser?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_children)

        database = Firebase.database.reference
        txtName=binding.edtFullName
        txtAddress=binding.edtAddress
        txtAge=binding.edtAge

        sharedPreferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
        val bundle : Bundle? = intent.extras
        UserID = bundle!!.getString("UserID").toString()
        currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")

        binding.btnBack.setOnClickListener{
            val intent = Intent(this@AddChildren, ChildrenInfo::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            startActivity(intent)
        }

        binding.btnReset.setOnClickListener{
            reset()
        }

        binding.btnConfirm.setOnClickListener{
            if(validationCheck()){
                children.address=txtAddress.text.toString()
                children.age=txtAge.text.toString()
                children.name=txtName.text.toString()
                children.parentID=UserID
                children.cardNo="Not Assigned"
                children.subscribed_service="No Subscription"
                children.childrenID=getRandomString()
                writeNewChildren(children)

                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Successfully Added")
                    .setContentText("Children Information Added")
                    .setConfirmClickListener {
                        val intent = Intent(this@AddChildren, ChildrenInfo::class.java)
                        intent.putExtra("UserID",UserID)
                        intent.putExtra("currentUser",currentUser)
                        startActivity(intent)
                    }
                    .show()


            }
        }


    }

    private fun reset(){
        txtName.setText("")
        txtAge.setText("")
        txtAddress.setText("")
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

    private fun getRandomString() : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..12)
            .map { allowedChars.random() }
            .joinToString("")
    }
}