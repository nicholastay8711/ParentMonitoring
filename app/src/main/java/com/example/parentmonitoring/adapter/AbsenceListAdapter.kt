package com.example.parentmonitoring.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parentmonitoring.R
import com.example.parentmonitoring.data.Absence
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AbsenceListAdapter(private val AbsenceList : ArrayList<Absence>) : RecyclerView.Adapter<AbsenceListAdapter.MyViewHolder>(){
    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AbsenceListAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.absence_list,parent,false)


        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: AbsenceListAdapter.MyViewHolder, position: Int) {
        val absenceList : Absence = AbsenceList[position]
        holder.txtName.text = absenceList.childrenName
        holder.txtDate.text = absenceList.date
        var date= SimpleDateFormat("dd/MM/yyyy").parse(absenceList.date)

        if(date.before(Calendar.getInstance().time)||absenceList.status=="Cancelled"){
            holder.btnCancel.visibility=View.GONE
        }else{
            holder.btnCancel.visibility=View.VISIBLE
        }

    }

    override fun getItemCount(): Int {
        return AbsenceList.size
    }

    public class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){

        val txtName = itemView.findViewById<TextView>(R.id.txtName)
        val txtDate = itemView.findViewById<TextView>(R.id.txtDate)
        val btnCancel=itemView.findViewById<Button>(R.id.btnCancel)


        init{
//            itemView.setOnClickListener{
//                listener.onItemClick(adapterPosition)
//            }
            btnCancel.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }

    }
}