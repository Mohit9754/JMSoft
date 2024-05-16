package com.jmsoft.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentCollectionDetailBinding
import com.jmsoft.databinding.ItemCollectionDetailBinding

/**
 * Collection Detail Adapter
 *
 *
 */

class CollectionDetailAdapter(
    private val context: Context,
    private val categoryDataList: ArrayList<CategoryDataModel>,
    private val collectionUUID: String,
    private val fragmentCollectionDetailBinding: FragmentCollectionDetailBinding
) :
    RecyclerView.Adapter<CollectionDetailAdapter.MyViewHolder>() {

    // Adapter reference
    var adapterReference = this

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCollectionDetailBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = categoryDataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // bind method
        holder.bind(categoryDataList[position], position)
    }

    inner class MyViewHolder(private val binding: ItemCollectionDetailBinding) : RecyclerView.ViewHolder(binding.root) {

        // Category Data
        private lateinit var categoryData: CategoryDataModel

        // Category Position
        private var position: Int = 0

        // bind method
        fun bind(categoryData: CategoryDataModel, position: Int) {

            this.categoryData = categoryData
            this.position = position

            // Set category name
            setCategoryName()

            // Set collection recyclerview
            setCollectionRecyclerView()
        }

        // Set category name
        private fun setCategoryName(){
            binding.tvCollectionName.text = categoryData.categoryName
        }

        // Set collection recyclerview
        private fun setCollectionRecyclerView(){

            val productDataList = categoryData.categoryUUID?.let {
                Utils.getAllProductsThroughCategoryAndCollection(
                    it,collectionUUID)
            }

            val adapter = productDataList?.let { categoryData.categoryName?.let { it1 ->
                ProductCategoryCollectionAdapter(context, it,
                    it1,categoryDataList,adapterReference,collectionUUID,position,fragmentCollectionDetailBinding)
            } }

            binding.rvCollection.layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.rvCollection.adapter = adapter

        }

    }
}