package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.databinding.FragmentProductInventoryBinding
import com.jmsoft.databinding.ItemSelectedCollectionBinding
import com.jmsoft.main.model.SelectedCollectionModel

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

        // Selected collection data
        private lateinit var selectedCollectionData:SelectedCollectionModel

        // Selected collection position
        private var position = -1

        // Bind method
        fun bind(selectedCollectionData: SelectedCollectionModel,position: Int) {

            this.selectedCollectionData = selectedCollectionData
            this.position = position

            // Set collection name
            setCollectionName()

            // Set click on cross button
            binding.ivCross.setOnClickListener(this)

        }

        // Set collection name
        private fun setCollectionName() {
            binding.tvCollectionName.text = selectedCollectionData.collectionDataModel.collectionName
        }

        // Handle all the clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // Clicked on cross button
            if (v == binding.ivCross) {

                selectedCollectionDataList.removeAt(position)
                selectedCollectionData.checkbox.isChecked = false

                if (selectedCollectionDataList.isEmpty()) {

                    fragmentProductInventoryBinding.tvCollection.visibility = View.VISIBLE
                }

                notifyDataSetChanged()

            }
        }
    }

}