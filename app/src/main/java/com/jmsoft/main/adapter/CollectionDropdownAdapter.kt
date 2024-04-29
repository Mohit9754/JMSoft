package com.jmsoft.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemCollectionDropdownBinding
import com.jmsoft.main.`interface`.CollectionStatusCallback
import com.jmsoft.main.model.SelectedCollectionModel

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class CollectionDropdownAdapter(
    private val context: Context,
    private var collectionDataList: ArrayList<CollectionDataModel>,
    private val collectionStatusCallback: CollectionStatusCallback,
) :
    RecyclerView.Adapter<CollectionDropdownAdapter.MyViewHolder>() {

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

        private lateinit var collectionData:CollectionDataModel

        fun bind(metalType: CollectionDataModel) {

            this.collectionData = metalType

            setCollectionName()

            setSelectedCollection()


            binding.llCollectionItem.setOnClickListener(this)

        }

        private fun setSelectedCollection() {

            if (selectedCollectionUUID.any { it == collectionData.collectionUUID }) {

                binding.checkBoxCollection.isChecked = true
                collectionStatusCallback.collectionSelected(SelectedCollectionModel(binding.checkBoxCollection,collectionData))

            }
            else {

                binding.checkBoxCollection.isChecked = false
            }
        }

        private fun setCollectionName() {
            binding.checkBoxCollection.text = collectionData.collectionName
        }

        override fun onClick(v: View?) {

            if (v == binding.llCollectionItem) {

                if (binding.checkBoxCollection.isChecked){

                    binding.checkBoxCollection.isChecked = false

                    collectionStatusCallback.collectionUnSelected(SelectedCollectionModel(binding.checkBoxCollection,collectionData))
                    selectedCollectionUUID.remove(collectionData.collectionUUID)

                }
                else{

                    binding.checkBoxCollection.isChecked = true
                    collectionData.collectionUUID?.let { selectedCollectionUUID.add(it) }

                    collectionStatusCallback.collectionSelected(SelectedCollectionModel(binding.checkBoxCollection,collectionData))
                }
            }

        }

    }

}