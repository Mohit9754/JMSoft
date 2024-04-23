package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentProductInventoryBinding
import com.jmsoft.databinding.ItemSelectedCollectionBinding
import com.jmsoft.main.model.SelectedCollectionModel

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class SelectedCollectionAdapter(

    private val context: Context,
    private var selectedCollectionDataList: ArrayList<SelectedCollectionModel>,
    private val fragmentProductInventoryBinding: FragmentProductInventoryBinding

) :
    RecyclerView.Adapter<SelectedCollectionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemSelectedCollectionBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = selectedCollectionDataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(selectedCollectionDataList[position],position)
    }

    inner class MyViewHolder(private val binding: ItemSelectedCollectionBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var selectedCollectionData:SelectedCollectionModel

        private var position = -1

        fun bind(selectedCollectionData: SelectedCollectionModel,position: Int) {

            this.selectedCollectionData = selectedCollectionData
            this.position = position

            setCollectionName()

            binding.ivCross.setOnClickListener(this)

        }

        private fun setCollectionName() {
            binding.tvCollectionName.text = selectedCollectionData.collectionDataModel.collectionName
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.ivCross) {

                selectedCollectionDataList.removeAt(position)
                selectedCollectionData.checkbox.isChecked = false

                if (selectedCollectionDataList.isEmpty()) {

                    fragmentProductInventoryBinding.tvCollection?.visibility = View.VISIBLE
                }

                notifyDataSetChanged()

            }
        }
    }

}