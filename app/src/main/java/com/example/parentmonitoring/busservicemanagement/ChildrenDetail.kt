package com.example.parentmonitoring.busservicemanagement

import android.app.AlertDialog
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.parentmonitoring.R
import com.example.parentmonitoring.data.Children
import com.example.parentmonitoring.databinding.ActivityChildrenDetailBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChildrenDetail : AppCompatActivity() {

    private lateinit var binding : ActivityChildrenDetailBinding
    private lateinit var database: DatabaseReference
    private var children= Children()
    private lateinit var txtAddress: EditText
    private lateinit var txtAge: EditText
    private lateinit var txtName: EditText
    private lateinit var txtCardNo: EditText
    private lateinit var txtService: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private var UserID : String = ""
    private var childrenID : String = ""
    private var currentUser: FirebaseUser?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_children_detail)

        database = Firebase.database.reference
        txtName=binding.edtFullName
        txtAddress=binding.edtAddress
        txtAge=binding.edtAge
        txtCardNo=binding.edtCardNo
        txtService=binding.edtSubscribedService

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
            txtCardNo.setText(children.cardNo)
            txtService.setText(children.subscribed_service)
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        binding.btnEdit.setOnClickListener{
            val intent = Intent(this@ChildrenDetail, EditChildren::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            intent.putExtra("childrenID",childrenID)
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener{

            if(!children.subscribed_service.equals("No Subscription")){
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("The children is currently subscribe to a service. Please unsubscribe the service before deleting the information.")
                    .show()
            }else{
                val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Are you sure to delete "+children.name+ "'s details?")


                // Set up the buttons
                builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    database= FirebaseDatabase.getInstance().getReference()
                        .child("Children").child(childrenID);
                    database.removeValue().addOnCompleteListener{
                        if(it.isSuccessful){
                            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Successfully Deleted")
                                .setContentText("Children information has been deleted.")
                                .setConfirmClickListener {
                                    val intent = Intent(this@ChildrenDetail, ChildrenInfo::class.java)
                                    intent.putExtra("UserID",UserID)
                                    intent.putExtra("currentUser",currentUser)
                                    startActivity(intent)
                                }
                                .show()


                        }
                    }



                })
                builder.setNegativeButton(
                    "Cancel",
                    DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                builder.show()



            }
        }


        binding.btnBack.setOnClickListener{
            val intent = Intent(this@ChildrenDetail, ChildrenInfo::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            startActivity(intent)
        }

    }
}