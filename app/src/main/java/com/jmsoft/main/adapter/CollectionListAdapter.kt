package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.ItemInventoryBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.`interface`.EditInventoryCallback

/**
 * MetalType Adapter
 *
 * Showing the catalog details
 *
 */

class CollectionListAdapter(
    private val context: Context,
    private var collectionList: ArrayList<String>,
    private val editCollectionCallback: EditInventoryCallback
) :
    RecyclerView.Adapter<CollectionListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemInventoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = collectionList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(collectionList[position],position)
    }

    // Show Metal Type Delete Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showCollectionDeleteDialog(position: Int) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivImage.setImageResource(R.drawable.img_delete_inventory)
        dialogBinding.tvMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_collection_this_action_cannot_be_undone)
//            context.getString(R.string.are_you_sure_you_want_to_delete_this_metal_type_this_action_cannot_be_undone)

        dialogBinding.mcvYes.setOnClickListener {

            dialog.dismiss()

            collectionList.removeAt(position)
            notifyDataSetChanged()
        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    inner class MyViewHolder(private val binding: ItemInventoryBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Product Data
        private lateinit var collectionName: String

        private var position:Int = -1

        fun bind(collectionName: String,position: Int) {

            this.collectionName = collectionName
            this.position = position

            visibleCollectionImage()

            //Set Metal Type
            setCollectionName()

            //Setting Click on Delete Icon
            binding.mcvDelete.setOnClickListener(this)

            //Setting Click on Edit Icon
            binding.mcvEdit.setOnClickListener(this)

            binding.mcvInventory.setOnClickListener(this)
        }


        private fun visibleCollectionImage(){
             binding.mcvCollectionImage.visibility = View.VISIBLE
        }

        //Set Metal Type
        private fun setCollectionName(){
            binding.tvMetalType.text = collectionName

        }

        //Handles All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // When delete button Clicked
            if (v == binding.mcvDelete){

                // Show Metal Type Delete Dialog
                showCollectionDeleteDialog(position)
            }

            // When edit button Clicked
            else if(v == binding.mcvEdit) {
                editCollectionCallback.editInventory(position)
            }

            else if (v == binding.mcvInventory) {

                val bundle = Bundle()
                //Giving the product UUID
                bundle.putString(Constants.collection, "")
                (context as DashboardActivity).navController?.navigate(R.id.collectionDetail, bundle)

            }

        }
    }

}