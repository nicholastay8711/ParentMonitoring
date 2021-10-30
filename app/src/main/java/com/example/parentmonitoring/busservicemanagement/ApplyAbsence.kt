package com.example.parentmonitoring.busservicemanagement


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.example.parentmonitoring.databinding.ActivityApplyAbsenceBinding
import com.google.firebase.auth.FirebaseUser
import java.util.*
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import android.widget.ArrayAdapter

import android.R
import android.app.AlertDialog
import android.content.*
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.parentmonitoring.data.Absence
import com.example.parentmonitoring.data.Children
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ApplyAbsence : AppCompatActivity(), DatePickerDialog.OnDateSetListener {


    private lateinit var binding :ActivityApplyAbsenceBinding
    private lateinit var preferences: SharedPreferences
    private var UserID : String = ""
    private lateinit var tvdate: TextView
    private lateinit var spinnerName: Spinner
    private var currentUser: FirebaseUser?=null
    private lateinit var database: DatabaseReference
    private var nameList: MutableList<String> =ArrayList()
    private lateinit var childrenList: ArrayList<Children>



    private var calendar =Calendar.getInstance()
    private var min_date_c =Calendar.getInstance()
    private var max_date_c=Calendar.getInstance()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.example.parentmonitoring.R.layout.activity_apply_absence)
        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
        database = Firebase.database.reference

        val bundle : Bundle? = intent.extras

        UserID = bundle!!.getString("UserID").toString()
        currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")

        tvdate=binding.tvDate
        spinnerName=binding.spinnerName
        childrenList= arrayListOf()

        getChildrenList()


//        val adapter = ArrayAdapter(
//            this,
//            android.R.layout.simple_spinner_item, nameList
//        )
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerName.adapter = adapter

        binding.btnBack.setOnClickListener{
            val intent = Intent(this@ApplyAbsence, BusServiceManagement::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.imageBtnHistory.setOnClickListener{
            val intent = Intent(this@ApplyAbsence, AbsenceHistory::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }

        binding.btnApply.setOnClickListener {

            if(tvdate.text.toString()==""){
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Please select a date.")
                    .show()
            }else{
                val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Are you sure to apply absence?")
                builder.setMessage("By applying absence, driver will not pick your children on the selected date. Are you sure you want to apply?")


                // Set up the buttons
                builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    for(s in childrenList){
                        if(s.name==spinnerName.selectedItem.toString()&& s.parentID==UserID){
                            if(s.subscribed_service=="No Subscription"){
                                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error")
                                    .setContentText("The children does not have bus service subscribed.")
                                    .show()
                            }else{
                                var absence=Absence()
                                absence.absenceID=getRandomString()
                                absence.parentID=UserID
                                absence.childrenID=s.childrenID
                                absence.date=tvdate.text.toString()
                                absence.serviceCode=s.subscribed_service
                                absence.childrenName=s.name
                                absence.status="Applied"
                                var database: DatabaseReference = Firebase.database.reference
                                database.child("Absence").child(absence.absenceID).setValue(absence)
                                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Success")
                                    .setContentText("Absence has been applied.")
                                    .show()
                            }
                        }
                    }
                })
                builder.setNegativeButton(
                    "Cancel",
                    DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                builder.show()
            }



        }


        binding.imageBtnDate.setOnClickListener{
            val dpd = DatePickerDialog.newInstance(
                this@ApplyAbsence,
                calendar.get(Calendar.YEAR),  // Initial year selection
                calendar.get(Calendar.MONTH),  // Initial month selection
                calendar.get(Calendar.DAY_OF_MONTH) // Inital day selection
            )
            min_date_c =Calendar.getInstance()
            max_date_c=Calendar.getInstance()

            dpd.setMinDate(min_date_c)

            max_date_c.set(Calendar.MONTH,Calendar.getInstance().time.month+1)
            dpd.setMaxDate(max_date_c)
            //Disable all SUNDAYS and SATURDAYS between Min and Max Dates

                var loopdate: Calendar = calendar
                while (min_date_c.before(max_date_c)) {
                    val dayOfWeek = loopdate[Calendar.DAY_OF_WEEK]
                    if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
                        val disabledDays = arrayOfNulls<Calendar>(1)
                        disabledDays[0] = loopdate
                        dpd.setDisabledDays(disabledDays)
                    }
                    min_date_c.add(Calendar.DATE, 1)
                    loopdate = min_date_c
                }

            dpd.show(getSupportFragmentManager(), "Datepickerdialog");

        }
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val date = dayOfMonth.toString() + "/" + (monthOfYear + 1).toString() + "/" + year
        tvdate.text=date
    }

    private fun getChildrenList(){
        database.child("Children").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snap in dataSnapshot.children) {
                    // TODO: handle the post
                    var children: Children = snap.getValue(Children::class.java)!!
                    children.childrenID= snap.key!!
                    if(children.parentID==UserID&&children.subscribed_service!="No Subscription"){
                        nameList.add(children.name)
                        childrenList.add(children)
                    }
                }
                val adapter = ArrayAdapter(
                    this@ApplyAbsence,
                    android.R.layout.simple_spinner_item, nameList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerName.adapter = adapter

                if (childrenList.size<=0){
                    SweetAlertDialog(this@ApplyAbsence, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("There is no children added or the children has no bus service subscribed")
                        .show()
                    binding.imageBtnDate.isEnabled=false
                    binding.btnApply.isEnabled=false
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })

    }

    private fun getRandomString() : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..12)
            .map { allowedChars.random() }
            .joinToString("")
    }


}