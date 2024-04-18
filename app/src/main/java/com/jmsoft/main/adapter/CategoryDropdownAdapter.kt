package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentProductInventoryBinding
import com.jmsoft.databinding.ItemMetalTypeDropdownBinding

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class CategoryDropdownAdapter(
    private val context: Context,
    private var categoryList: ArrayList<String>,
    private var productInventoryBinding:FragmentProductInventoryBinding
) :
    RecyclerView.Adapter<CategoryDropdownAdapter.MyViewHolder>() {

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemMetalTypeDropdownBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = categoryList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(categoryList[position],position)
    }

    inner class MyViewHolder(private val binding: ItemMetalTypeDropdownBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var category:String

        private var position = -1

        fun bind(category: String,position: Int) {

            this.category = category
            this.position = position

            Utils.E("REcrated $category")

            setCategory()

            setSelected()



            binding.llMetalType.setOnClickListener(this)

        }

        private fun setSelected(){

            if (selectedPosition == position) {

                binding.llMetalType.setBackgroundColor(context.getColor(R.color.selected_drop_down_color))
            }
            else {

                binding.llMetalType.setBackgroundColor(context.getColor(R.color.white))
            }
        }

        private fun setCategory() {
            binding.tvMetalType.text = category
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.llMetalType) {

                selectedPosition = position

                productInventoryBinding.tvCategory?.text = category
                productInventoryBinding.tvCategoryError?.visibility = View.GONE
                productInventoryBinding.ivCategory?.let { Utils.rotateView(it,0f) }
                productInventoryBinding.mcvCategoryList?.let { Utils.collapseView(it) }

                notifyDataSetChanged()
            }

        }

    }

}