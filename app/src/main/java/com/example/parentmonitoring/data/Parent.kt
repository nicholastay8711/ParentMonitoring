package com.example.parentmonitoring.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@IgnoreExtraProperties
data class Parent(
    @get:Exclude var UserID:String="",
    @get:Exclude var email:String="",
    @get:Exclude var password:String="",
    @PropertyName("name") var name:String="",
    @PropertyName("phoneNo")var phoneNo:String="",
    @PropertyName("address")var address:String="",
    @PropertyName("registerDate")var registerDate:String=""

    )
@IgnoreExtraProperties
data class FeedbackRequest(
    @get:Exclude var requestID:String="",
    @PropertyName("content") var content:String="",
    @PropertyName("parentID")var parentID:String="",
    @PropertyName("parentEmail")var parentEmail:String="",
    @PropertyName("adminID")var adminID:String="",
    @PropertyName("status")var status:String="",
    @PropertyName("type")var type:String="",
    @PropertyName("date")var date:String="",


)

@IgnoreExtraProperties
data class Absence(
    @get:Exclude var absenceID:String="",
    @PropertyName("parentID")var parentID:String="",
    @PropertyName("childrenID")var childrenID:String="",
    @PropertyName("childrenName")var childrenName:String="",
    @PropertyName("date")var date:String="",
    @PropertyName("serviceCode")var serviceCode:String="",
    @PropertyName("status")var status:String=""


)


private var database: DatabaseReference= Firebase.database.reference

fun writeNewParent(parent:Parent) {
    database.child("Parent").child(parent.UserID).setValue(parent)
}
