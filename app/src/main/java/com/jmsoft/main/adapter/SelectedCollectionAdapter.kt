package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentProductInventoryBinding
import com.jmsoft.databinding.ItemSelectedCollectionBinding

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class SelectedCollectionAdapter(

    private val context: Context,
    private var selectedCollectionList: ArrayList<String>,
    private val fragmentProductInventoryBinding: FragmentProductInventoryBinding

) :
    RecyclerView.Adapter<SelectedCollectionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemSelectedCollectionBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = selectedCollectionList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(selectedCollectionList[position],position)
    }

    inner class MyViewHolder(private val binding: ItemSelectedCollectionBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var collection:String

        private var position = -1

        fun bind(collection: String,position: Int) {

            this.collection = collection
            this.position = position

            setCollectionName()

            binding.ivCross.setOnClickListener(this)

        }

        private fun setCollectionName() {
            binding.tvCollectionName.text = collection
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.ivCross) {

                selectedCollectionList.removeAt(position)

                if (selectedCollectionList.size == 0) {

                    fragmentProductInventoryBinding.tvCollection?.visibility = View.VISIBLE
                }

                notifyDataSetChanged()

            }
        }

    }

}