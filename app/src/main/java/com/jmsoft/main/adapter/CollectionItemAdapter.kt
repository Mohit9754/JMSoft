package com.jmsoft.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.databinding.ItemCatalogBinding
import com.jmsoft.databinding.ItemCollectionBinding
import com.jmsoft.main.activity.DashboardActivity

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class CollectionItemAdapter(private val context: Context, private val catalogList: ArrayList<String>) :
    RecyclerView.Adapter<CollectionItemAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCollectionBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = 10

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.bind(catalogList[position])
    }

    inner class MyViewHolder(private val binding: ItemCollectionBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var data: String

        fun bind(data: String) {

            this.data = data

            //Setting Click on Catalog Item
//            binding.mcvCatalogItem?.setOnClickListener(this)
        }

        //Handles All the Clicks
        override fun onClick(v: View?) {

            if (v == binding.mcvCatalogItem){

                //Navigate to Product
                (context as DashboardActivity).navController?.navigate(R.id.product)
            }

        }

    }
}