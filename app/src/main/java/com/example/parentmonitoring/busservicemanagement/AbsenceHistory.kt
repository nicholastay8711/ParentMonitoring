package com.example.parentmonitoring.busservicemanagement

import android.app.AlertDialog
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.parentmonitoring.R
import com.example.parentmonitoring.adapter.AbsenceListAdapter
import com.example.parentmonitoring.adapter.ChildrenListAdapter
import com.example.parentmonitoring.data.Absence
import com.example.parentmonitoring.data.Children
import com.example.parentmonitoring.databinding.ActivityAbsenceHistoryBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AbsenceHistory : AppCompatActivity() {

    private lateinit var binding :ActivityAbsenceHistoryBinding
    private lateinit var preferences: SharedPreferences
    private var UserID : String = ""
    private var currentUser: FirebaseUser?=null
    private lateinit var myAdapter: AbsenceListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var absenceList: ArrayList<Absence>
    private var absence=Absence()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.example.parentmonitoring.R.layout.activity_absence_history)
        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
        database = Firebase.database.reference

        val bundle : Bundle? = intent.extras

        UserID = bundle!!.getString("UserID").toString()
        currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")

        binding.imgBtnBack.setOnClickListener{
            val intent = Intent(this@AbsenceHistory, ApplyAbsence::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        absenceList= arrayListOf()
        getAbsenceList()
        recyclerView = findViewById(R.id.rvAbsence)
        recyclerView.layoutManager = LinearLayoutManager(this@AbsenceHistory)
        recyclerView.setHasFixedSize(false)

        myAdapter = AbsenceListAdapter(absenceList)
        recyclerView.adapter = myAdapter

        myAdapter.setOnItemClickListener(object : AbsenceListAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this@AbsenceHistory)
                builder.setTitle("Are you sure to cancel the absence application? ")


                // Set up the buttons
                builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    absence=absenceList[position]
                    updateAbsence()
                })
                builder.setNegativeButton(
                    "Cancel",
                    DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                builder.show()
            }

        })

    }

    private fun getAbsenceList(){
        database.child("Absence").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snap in dataSnapshot.children) {
                    // TODO: handle the post
                    var absence: Absence = snap.getValue(Absence::class.java)!!
                    absence.absenceID= snap.key!!
                    if(absence.parentID==UserID){
                        absenceList.add(absence)
                    }
                }
                myAdapter.notifyDataSetChanged()
                checkRecords()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })

    }
    private fun checkRecords(){
        if(myAdapter.itemCount.toString().toInt() > 0){
            binding.imgEmptySN.visibility = View.GONE
            binding.tvEmptySN.visibility = View.GONE
        }else{
            binding.imgEmptySN.visibility = View.VISIBLE
            binding.tvEmptySN.visibility = View.VISIBLE
        }
    }


    private fun updateAbsence(){
        database= FirebaseDatabase.getInstance().getReference("Absence")
        val abs= mapOf<String,String>(
            "parentID" to absence.parentID,
            "childrenID" to absence.childrenID,
            "childrenName" to absence.childrenName,
            "date" to absence.date,
            "serviceCode" to absence.serviceCode,
            "status" to "Cancelled"

        )

        database.child(absence.absenceID).updateChildren(abs).addOnSuccessListener {
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Successfully Cancelled")
                .setConfirmClickListener {
                    val intent = Intent(this@AbsenceHistory, AbsenceHistory::class.java)
                    intent.putExtra("UserID",UserID)
                    intent.putExtra("currentUser",currentUser)
                    finish()
                    startActivity(intent)
                }
                .setContentText("The absence has been successfully cancelled.")
                .show()
        }
    }
}


