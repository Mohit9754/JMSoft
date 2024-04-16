package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentProductInventoryBinding
import com.jmsoft.databinding.ItemCatalogBinding
import com.jmsoft.databinding.ItemMetalTypeBinding
import com.jmsoft.main.activity.DashboardActivity

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class MetalTypeListAdapter(
    private val context: Context,
    private var metalTypeList: ArrayList<String>,
    private var productInventoryBinding:FragmentProductInventoryBinding
) :
    RecyclerView.Adapter<MetalTypeListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemMetalTypeBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = metalTypeList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(metalTypeList[position])
    }

    inner class MyViewHolder(private val binding: ItemMetalTypeBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var metalType:String

        fun bind(metalType: String) {

            this.metalType = metalType
            setMetalType()

            binding.tvMetalType.setOnClickListener(this)

        }

        private fun setMetalType() {
            binding.tvMetalType.text = metalType
        }

        override fun onClick(v: View?) {

            if (v == binding.tvMetalType) {

                productInventoryBinding.tvMetalType?.text = metalType

                productInventoryBinding.tvMetalType?.let { Utils.rotateView(it,0f) }
            }
                productInventoryBinding.mcvMetalTypeList?.let { Utils.collapseView(it) }
            }

        }

    }

