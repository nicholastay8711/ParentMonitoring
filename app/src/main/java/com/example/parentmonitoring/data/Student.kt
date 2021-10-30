package com.example.parentmonitoring.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


@IgnoreExtraProperties
data class Children(
    @get:Exclude var childrenID:String="",
    @PropertyName("parentID") var parentID:String="",
    @PropertyName("name") var name:String="",
    @PropertyName("age")var age:String="",
    @PropertyName("address")var address:String="",
    @PropertyName("cardNo")var cardNo:String="",
    @PropertyName("subscribed_service")var subscribed_service:String=""
)


data class Card(
    @get:Exclude var cardID:String="",
    @PropertyName("childrenID") var childrenID:String="",
    @PropertyName("status") var status:String="",
)
private var database: DatabaseReference = Firebase.database.reference

fun writeNewChildren(children:Children) {
    database.child("Children").child(children.childrenID).setValue(children)
}