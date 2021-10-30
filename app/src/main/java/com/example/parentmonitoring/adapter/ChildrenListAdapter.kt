package com.example.parentmonitoring.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parentmonitoring.R
import com.example.parentmonitoring.data.Children

class ChildrenListAdapter(private val ChildrenList : ArrayList<Children>) : RecyclerView.Adapter<ChildrenListAdapter.MyViewHolder>(){
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
    ): ChildrenListAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.children_list,parent,false)


        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: ChildrenListAdapter.MyViewHolder, position: Int) {

        val childrenList : Children = ChildrenList[position]
        holder.txtName.text = childrenList.name

    }

    override fun getItemCount(): Int {
        return ChildrenList.size
    }

    public class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){

        val txtName = itemView.findViewById<TextView>(R.id.txtName)


        init{
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }

    }
}