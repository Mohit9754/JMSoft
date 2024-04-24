package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentProductInventoryBinding
import com.jmsoft.databinding.ItemMetalTypeDropdownBinding
import com.jmsoft.main.`interface`.CategorySelectedCallback

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class CategoryDropdownAdapter(
    private val context: Context,
    private var categoryDataModelList: ArrayList<CategoryDataModel>,
    private val categorySelectedCallback: CategorySelectedCallback

) :
    RecyclerView.Adapter<CategoryDropdownAdapter.MyViewHolder>() {

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemMetalTypeDropdownBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = categoryDataModelList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(categoryDataModelList[position],position)
    }

    inner class MyViewHolder(private val binding: ItemMetalTypeDropdownBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var categoryData:CategoryDataModel

        private var position = -1

        fun bind(categoryData: CategoryDataModel,position: Int) {

            this.categoryData = categoryData
            this.position = position


            setCategoryName()

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

        private fun setCategoryName() {
            binding.tvMetalType.text = categoryData.categoryName
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.llMetalType) {

                selectedPosition = position

                categorySelectedCallback.categorySelected(categoryData)

                notifyDataSetChanged()
            }

        }

    }

}