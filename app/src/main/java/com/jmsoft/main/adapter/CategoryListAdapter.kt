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

class CategoryListAdapter(
    private val context: Context,
    private var categoryList: ArrayList<String>,
    private var productInventoryBinding:FragmentProductInventoryBinding
) :
    RecyclerView.Adapter<CategoryListAdapter.MyViewHolder>() {


    var selectedItemBinding:ItemMetalTypeBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemMetalTypeBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = categoryList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(categoryList[position])
    }

    inner class MyViewHolder(private val binding: ItemMetalTypeBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var category:String

        fun bind(category: String) {

            this.category = category

            Utils.E("REcrated $category")

            setCategory()

            binding.tvMetalType.setOnClickListener(this)

        }

//        private fun checkSelectedCategory(){
//
//            val selectedCategory = productInventoryBinding.tvCategory?.text.toString()
//
//            if (selectedCategory == category){
//
//                Utils.E("$selectedCategory $category true")
//
//                binding.tvMetalType.setBackgroundColor(context.getColor(R.color.selected_drop_down_color))
//                selectedItemBinding = binding
//            }
//            else {
//
//                Utils.E("$selectedCategory $category false")
//            }
//        }

        private fun setCategory() {
            binding.tvMetalType.text = category
        }

        override fun onClick(v: View?) {

            if (v == binding.tvMetalType) {

                selectedItemBinding?.tvMetalType?.setBackgroundColor(context.getColor(R.color.white))

                binding.tvMetalType.setBackgroundColor(context.getColor(R.color.selected_drop_down_color))
                selectedItemBinding = binding

                productInventoryBinding.tvCategory?.text = category
                productInventoryBinding.ivCategory?.let { Utils.rotateView(it,0f) }
                productInventoryBinding.mcvCategoryList?.let { Utils.collapseView(it) }
            }

        }

    }

}