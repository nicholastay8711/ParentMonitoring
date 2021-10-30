package com.example.parentmonitoring.busservicemanagement

import android.R.attr
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.parentmonitoring.data.Children
import com.example.parentmonitoring.databinding.ActivityReportLostCardBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

import android.content.Intent
import android.os.Handler
import com.example.parentmonitoring.data.Card
import com.example.parentmonitoring.data.FeedbackRequest
import com.google.firebase.database.*
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.CreateOrder
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.OrderIntent
import com.paypal.checkout.createorder.UserAction
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.*
import com.paypal.pyplcheckout.BuildConfig
import java.lang.Exception


class ReportLostCard : AppCompatActivity() {

    private lateinit var binding :ActivityReportLostCardBinding
    private lateinit var preferences: SharedPreferences
    private var UserID : String = ""

    private lateinit var spinnerName: Spinner
    private var currentUser: FirebaseUser?=null
    private lateinit var database: DatabaseReference
    private var nameList: MutableList<String> = ArrayList()
    private lateinit var childrenList: ArrayList<Children>
    private val formatter = SimpleDateFormat("ddMMyyyy")
    private var children=Children()
    private var childrenCard=Card()
    private lateinit var cardList :ArrayList<Card>






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.example.parentmonitoring.R.layout.activity_report_lost_card)
        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
        database = Firebase.database.reference

        val bundle : Bundle? = intent.extras

        UserID = bundle!!.getString("UserID").toString()
        currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")

        spinnerName=binding.spinnerName
        childrenList= arrayListOf()
        cardList= arrayListOf()

        getChildrenList()
        getCardList()




        binding.btnBack.setOnClickListener(){
            val intent = Intent(this@ReportLostCard, BusServiceManagement::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            finish()
            startActivity(intent)
        }



        binding.btnPayReport.setOnClickListener{
            var feedback=FeedbackRequest()
            feedback.requestID=getRandomString()

            if(binding.radioBtnDamage.isChecked){
                feedback.type="Damaged"
            }else{
                feedback.type="Lost"
            }



            for(s in childrenList){
                if(s.name==spinnerName.selectedItem.toString()&& s.parentID==UserID){
                    children=s
                    feedback.content="Request for card replacement\n"+
                            "Reason : "+feedback.type+"\n"+
                            "Children name : "+s.name+"\n"+
                            "Children ID : "+s.childrenID+"\n"+
                            "Card number : "+s.cardNo
                }
            }

            if(checkCardStatus()){
                feedback.parentEmail= currentUser!!.email.toString()
                feedback.adminID="N.A"
                feedback.status="sent"
                feedback.parentID=UserID

                val date=formatter.format(Date()).toString()
                feedback.date=date.substring(0,2)+"/"+
                        date.substring(2,4)+"/"+
                        date.substring(4,8)



                val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Are you sure to report lost/damaged card?")
                builder.setMessage("*By reporting lost/damaged card, the current card will be deactivated. A replacement card will be sent delivered to you within 5 working days.")
                builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    try{
                        //loading
                        val progressDialog=ProgressDialog(this)
                        progressDialog.setMessage("Transaction in Progress")
                        progressDialog.setCancelable(false)
                        progressDialog.show()



                        val config = CheckoutConfig(
                            application =application ,
                            clientId = "AX7t1gIHH70H3JYH9Tqva_XQftzHoNpvx-MKIYhI536h-N0A-X1b4b1ZYLcmYE5svmjCsEcICqH1CWuz",
                            environment = Environment.SANDBOX,
                            returnUrl = "com.example.parentmonitoring://paypalpay",
                            currencyCode = CurrencyCode.MYR,
                            userAction = UserAction.PAY_NOW,
                            settingsConfig = SettingsConfig(
                                loggingEnabled = true
                            )
                        )
                        PayPalCheckout.setConfig(config)
                        PayPalCheckout.start(
                            createOrder = CreateOrder { createOrderActions ->
                                val order = Order(
                                    intent = OrderIntent.CAPTURE,
                                    appContext = AppContext(
                                        userAction = UserAction.PAY_NOW
                                    ),
                                    purchaseUnitList = listOf(
                                        PurchaseUnit(
                                            amount = Amount(
                                                currencyCode = CurrencyCode.MYR,
                                                value = "5.00"
                                            )
                                        )
                                    )
                                )
                                createOrderActions.create(order)
                            },
                            onApprove = OnApprove { approval ->
                                approval.orderActions.capture { captureOrderResult ->



                                    var database: DatabaseReference = Firebase.database.reference

                                    database.child("FeedbackRequest").child(feedback.requestID).setValue(feedback)


                                    updateCard()
                                    progressDialog.dismiss()
                                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Transaction Approved")
                                        .setContentText("Report has been submitted. The current card will be deactivated. A replacement card will be sent delivered to you within 5 working days.")
                                        .setConfirmClickListener {
                                            val intent = Intent(this@ReportLostCard, BusServiceManagement::class.java)
                                            intent.putExtra("UserID",UserID)
                                            intent.putExtra("currentUser",currentUser)
                                            startActivity(intent)
                                        }
                                        .show()

                                }
                            },
                            onCancel = OnCancel {
                                progressDialog.dismiss()
                                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Transaction has been cancelled.")
                                    .setContentText("Please try again.")
                                    .show()
                            },
                            onError = OnError { errorInfo ->
                                progressDialog.dismiss()
                                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Transaction has been cancelled.")
                                    .setContentText("Please try again.")
                                    .show()
                            }
                        )
                    }catch (ex:Exception){
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Sorry")
                            .setContentText("PayPal server is currently unavailable, please try again later.")
                            .show()
                    }

                })
                builder.setNegativeButton(
                    "Cancel",
                    DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                builder.show()
            }else{
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("The card has already been deactivated.")
                    .show()
            }


        }


    }



    private fun getRandomString() : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..12)
            .map { allowedChars.random() }
            .joinToString("")
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
                    this@ReportLostCard,
                    android.R.layout.simple_spinner_item, nameList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerName.adapter = adapter


                if (childrenList.size<=0){
                    SweetAlertDialog(this@ReportLostCard, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("There is no children added or the children has no bus service subscribed")
                        .show()
                    binding.btnPayReport.isEnabled=false
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })

    }

    private fun getCardList(){
        database.child("Card").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snap in dataSnapshot.children) {
                    // TODO: handle the post
                    var card: Card = snap.getValue(Card::class.java)!!
                    card.cardID= snap.key!!
                    cardList.add(card)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })

    }

    private fun checkCardStatus():Boolean{
       var result=false
        for(c in cardList){
            if(c.childrenID==children.childrenID){
                childrenCard=c
            }
        }

        if(childrenCard.status=="Active"){
            result=true
        }
        return result
    }

    private fun updateCard(){
        database= FirebaseDatabase.getInstance().getReference("Card")
        val card= mapOf<String,String>(
            "childrenID" to childrenCard.childrenID,
            "status" to "Inactive"
        )

        database.child(childrenCard.cardID).updateChildren(card)
    }


}