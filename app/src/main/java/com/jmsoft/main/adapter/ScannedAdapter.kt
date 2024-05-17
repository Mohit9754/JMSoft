package com.jmsoft.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.databinding.ItemExpectedBinding

class ScannedAdapter(
    private val context: Context,
    private val productList: ArrayList<String>,

    ) :
    RecyclerView.Adapter<ScannedAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemExpectedBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = 10

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.bind(productList[position], position)

    }

    inner class MyViewHolder(private val binding: ItemExpectedBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {



        override fun onClick(v: View?) {

        }

    }
}