package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.Utility.database.CollectionDataModel
import com.jmsoft.databinding.ItemCollectionDropdownBinding
import com.jmsoft.main.`interface`.CollectionStatusCallback
import com.jmsoft.main.model.SelectedCollectionModel

/**
 * Collection Dropdown Adapter
 *
 *
 */

class CollectionDropdownAdapter(
    private val context: Context,
    private var collectionDataList: ArrayList<CollectionDataModel>,
    private val collectionStatusCallback: CollectionStatusCallback,
) :
    RecyclerView.Adapter<CollectionDropdownAdapter.MyViewHolder>() {

   // Selected collection uuid list
   var selectedCollectionUUID = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCollectionDropdownBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = collectionDataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(collectionDataList[position])
    }

    inner class MyViewHolder(private val binding: ItemCollectionDropdownBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Collection data
        private lateinit var collectionData:CollectionDataModel

        // Bind method
        fun bind(metalType: CollectionDataModel) {

            this.collectionData = metalType

            // Set collection name
            setCollectionName()

            // Set selected collection
            setSelectedCollection()

            // Set click on collection
            binding.llCollectionItem.setOnClickListener(this)

        }

        // Set selected collection
        private fun setSelectedCollection() {

            if (selectedCollectionUUID.any { it == collectionData.collectionUUID }) {

                binding.checkBoxCollection.isChecked = true
                collectionStatusCallback.collectionSelected(SelectedCollectionModel(binding.checkBoxCollection,collectionData),false)

            }
            else {

                binding.checkBoxCollection.isChecked = false
            }
        }

        // Set collection name
        private fun setCollectionName() {
            binding.checkBoxCollection.text = collectionData.collectionName
        }

        // Handle all the click
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // Clicked on collection
            if (v == binding.llCollectionItem) {

                if (binding.checkBoxCollection.isChecked){

                    binding.checkBoxCollection.isChecked = false

                    collectionStatusCallback.collectionUnSelected(SelectedCollectionModel(binding.checkBoxCollection,collectionData))
                    selectedCollectionUUID.remove(collectionData.collectionUUID)

                }
                else {

                    binding.checkBoxCollection.isChecked = true
                    selectedCollectionUUID.clear()
                    collectionData.collectionUUID?.let { selectedCollectionUUID.add(it) }
                    collectionStatusCallback.collectionSelected(SelectedCollectionModel(binding.checkBoxCollection,collectionData),true)
                    notifyDataSetChanged()
                }
            }

        }

    }

}