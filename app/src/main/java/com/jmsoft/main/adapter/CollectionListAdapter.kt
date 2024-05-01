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
import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentMetalTypeBinding
import com.jmsoft.databinding.ItemInventoryBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.`interface`.EditInventoryCallback
import java.util.UUID

/**
 * Collection List Adapter
 *
 *
 */

class CollectionListAdapter(
    private val context: Context,
    private var collectionDataList: ArrayList<CollectionDataModel>,
    private val fragmentMetalTypeBinding: FragmentMetalTypeBinding,
    private val editCollectionCallback: EditInventoryCallback
) :
    RecyclerView.Adapter<CollectionListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemInventoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = collectionDataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(collectionDataList[position],position)
    }

    // Show Collection Delete Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showCollectionDeleteDialog(position: Int,collectionUUID: String,collectionImageUri:String) {

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

            Utils.deleteCollectionUUIDFromProductTable(collectionUUID)

            Utils.deleteImageFromInternalStorage(context,collectionImageUri)

            // Delete the collection first
            Utils.deleteCollection(collectionUUID)

            // Remove the item from the local list
            collectionDataList.removeAt(position)

            // Notify the adapter about the removed item
            notifyItemRemoved(position)

            // Notify the adapter about the range of changed items
            notifyItemRangeChanged(position, collectionDataList.size - position)

            if (collectionDataList.isEmpty()) {

                fragmentMetalTypeBinding.mcvMetalTypeList?.visibility = View.GONE
                fragmentMetalTypeBinding.llEmptyInventory?.visibility = View.VISIBLE
                fragmentMetalTypeBinding.tvEmptyMsg?.text = context.getString(R.string.collection_is_empty)

            }

        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    inner class MyViewHolder(private val binding: ItemInventoryBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Collection Data
        private lateinit var collectionData: CollectionDataModel

        // Collection position
        private var position:Int = -1

        // Bind method
        @SuppressLint("NotifyDataSetChanged")
        fun bind(collectionData: CollectionDataModel, position: Int) {

            this.collectionData = collectionData
            this.position = position

            // Set collection image
            setCollectionImage()

            // Set collection name
            setCollectionName()

            // Set click on Delete Icon
            binding.mcvDelete.setOnClickListener(this)

            // Set click on Edit Icon
            binding.mcvEdit.setOnClickListener(this)

            // Set click on collection
            binding.mcvInventory.setOnClickListener(this)
        }

        // Set collection image
        private fun setCollectionImage(){

             binding.mcvCollectionImage.visibility = View.VISIBLE

             binding.ivProduct.setImageBitmap(collectionData.collectionImageUri?.let {
                 Utils.getImageFromInternalStorage(context,
                     it
                 )
             })
            
        }

        // Set Metal Type
        private fun setCollectionName(){
            binding.tvMetalType.text = collectionData.collectionName

        }

        // Handles All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // When delete button Clicked
            if (v == binding.mcvDelete) {

                // Show Metal Type Delete Dialog
                collectionData.collectionUUID?.let { collectionData.collectionImageUri?.let { it1 ->
                    showCollectionDeleteDialog(position, it,
                        it1
                    )
                } }
            }

            // When edit button Clicked
            else if(v == binding.mcvEdit) {
                collectionData.collectionUUID?.let { editCollectionCallback.editInventory(it,position) }
            }

            // Clicked on collection
            else if (v == binding.mcvInventory) {

                val bundle = Bundle()

                //Giving the collection UUID
                bundle.putString(Constants.collectionUUID,collectionData.collectionUUID)

                (context as DashboardActivity).navController?.navigate(R.id.collectionDetail, bundle)

            }

        }
    }

}