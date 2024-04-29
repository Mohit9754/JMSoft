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
 * Cart list Adapter
 *
 * Showing the catalog details
 *
 */

class CollectionDetailAdapter(
    private val context: Context,
    private val categoryDataList: ArrayList<CategoryDataModel>,
    private val collectionUUID: String,
    private val fragmentCollectionDetailBinding: FragmentCollectionDetailBinding
) :
    RecyclerView.Adapter<CollectionDetailAdapter.MyViewHolder>() {

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

    inner class MyViewHolder(private val binding: ItemCollectionDetailBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        //Product Data
        private lateinit var categoryData: CategoryDataModel

        //Position
        private var position: Int = 0

        // bind method
        fun bind(categoryData: CategoryDataModel, position: Int) {

            this.categoryData = categoryData
            this.position = position


            setCategoryName()

            setCollectionRecyclerView()
        }

        private fun setCategoryName(){
            binding.tvCollectionName.text = categoryData.categoryName
        }

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

        override fun onClick(v: View?) {

        }

    }
}