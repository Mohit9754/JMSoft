package com.jmsoft.main.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Firebase
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemCatalogBinding
import com.jmsoft.databinding.ItemExpectedBinding
import com.jmsoft.databinding.ItemProductImageBinding
import com.jmsoft.main.activity.DashboardActivity
import okio.Utf8

class ExpectedAdapter(
    private val context: Context,
    private val list: ArrayList<String>,

) :
    RecyclerView.Adapter<ExpectedAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemExpectedBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = 10

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.bind(list[position], position)

    }

    inner class MyViewHolder(private val binding: ItemExpectedBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        override fun onClick(v: View?) {

        }


    }
}