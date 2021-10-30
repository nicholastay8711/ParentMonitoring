package com.example.parentmonitoring.busservicemanagement

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parentmonitoring.R
import com.example.parentmonitoring.adapter.ChildrenListAdapter
import com.example.parentmonitoring.data.Children
import com.example.parentmonitoring.databinding.ActivityChildrenInfoBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChildrenInfo : AppCompatActivity() {

    private lateinit var binding : ActivityChildrenInfoBinding
    private lateinit var database: DatabaseReference
    private lateinit var childrenList: ArrayList<Children>
    private lateinit var myAdapter: ChildrenListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private var UserID : String = ""
    private var currentUser:FirebaseUser?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_children_info)

        database = Firebase.database.reference

        sharedPreferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
        val bundle : Bundle? = intent.extras
        UserID = bundle!!.getString("UserID").toString()
        currentUser=bundle!!.getParcelable<FirebaseUser>("currentUser")


        binding.imgBtnBack.setOnClickListener{
            val intent = Intent(this@ChildrenInfo, BusServiceManagement::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            startActivity(intent)
        }

        binding.imageBtnAdd.setOnClickListener{
            val intent = Intent(this@ChildrenInfo, AddChildren::class.java)
            intent.putExtra("UserID",UserID)
            intent.putExtra("currentUser",currentUser)
            startActivity(intent)
        }


        childrenList= arrayListOf()
        getChildrenList()
        recyclerView = findViewById(R.id.rvChildren)
        recyclerView.layoutManager = LinearLayoutManager(this@ChildrenInfo)
        recyclerView.setHasFixedSize(false)

        myAdapter = ChildrenListAdapter(childrenList)
        recyclerView.adapter = myAdapter






        myAdapter.setOnItemClickListener(object : ChildrenListAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                val intent = Intent(this@ChildrenInfo, ChildrenDetail::class.java)
                intent.putExtra("UserID",UserID)
                intent.putExtra("currentUser",currentUser)
                intent.putExtra("childrenID",childrenList[position].childrenID)
                startActivity(intent)
            }

        })




    }

        private fun getChildrenList(){
            database.child("Children").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snap in dataSnapshot.children) {
                        // TODO: handle the post
                        var children:Children= snap.getValue(Children::class.java)!!
                        children.childrenID= snap.key!!
                        if(children.parentID==UserID){
                            childrenList.add(children)
                        }
                    }
                    myAdapter.notifyDataSetChanged()
                    checkRecords()

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
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



}