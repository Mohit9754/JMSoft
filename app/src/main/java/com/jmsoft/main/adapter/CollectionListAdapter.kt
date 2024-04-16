package com.jmsoft.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemCollectionListBinding
import com.jmsoft.main.`interface`.CollectionStatusCallback

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class CollectionListAdapter(
    private val context: Context,
    private var collectionList: ArrayList<String>,
    private val collectionStatusCallback: CollectionStatusCallback

) :
    RecyclerView.Adapter<CollectionListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCollectionListBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = collectionList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(collectionList[position])
    }

    inner class MyViewHolder(private val binding: ItemCollectionListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var collection:String

        fun bind(metalType: String) {

            this.collection = metalType
            setMetalType()

            binding.llCollectionItem.setOnClickListener(this)

        }

        private fun setMetalType() {
            binding.checkBoxCollection.text = collection
        }

        override fun onClick(v: View?) {

            if (v == binding.llCollectionItem) {

                Utils.E("$collection")

                if (binding.checkBoxCollection.isChecked == true){

                    binding.checkBoxCollection.isChecked = false
                    Utils.E("$collection")
                    collectionStatusCallback.collectionUnSelected(collection)

                }
                else{
                    binding.checkBoxCollection.isChecked = true
                    Utils.E("$collection")

                    collectionStatusCallback.collectionSelected(collection)
                }
            }

        }

    }

}