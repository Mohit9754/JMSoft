package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.MetalTypeDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentMetalTypeBinding
import com.jmsoft.databinding.ItemInventoryBinding
import com.jmsoft.main.`interface`.EditInventoryCallback

/**
 * MetalType List Adapter
 *
 *
 */

class MetalTypeListAdapter(
    private val context: Context,
    private var metalTypeList: ArrayList<MetalTypeDataModel>,
    private val fragmentMetalTypeBinding: FragmentMetalTypeBinding,
    private val editInventoryCallback: EditInventoryCallback,
) :
    RecyclerView.Adapter<MetalTypeListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemInventoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = metalTypeList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(metalTypeList[position],position)
    }

    // Show Metal Type Delete Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showMetalTypeDeleteDialog(metalTypeUUID: String,position: Int) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivImage.setImageResource(R.drawable.img_delete_inventory)
        dialogBinding.tvMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_metal_type_this_action_cannot_be_undone)

        dialogBinding.mcvYes.setOnClickListener {

            dialog.dismiss()
            Utils.deleteMetalType(metalTypeUUID)

            Utils.T(context, context.getString(R.string.deleted_successfully))
            metalTypeList.removeAt(position)

            if (metalTypeList.isEmpty()){

                fragmentMetalTypeBinding.mcvMetalTypeList?.visibility = View.GONE
                fragmentMetalTypeBinding.llEmptyInventory?.visibility = View.VISIBLE
                fragmentMetalTypeBinding.tvEmptyMsg?.text = context.getString(R.string.metal_type_is_empty)

            }

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

        // Metal Type Data
        private lateinit var metalTypeData: MetalTypeDataModel

        // Metal Type position
        private var position:Int = -1

        // bind method
        fun bind(metalTypeData: MetalTypeDataModel,position: Int) {

            this.metalTypeData = metalTypeData
            this.position = position

            //Set Metal Type
            setMetalType()

            //Setting Click on Delete Icon
            binding.mcvDelete.setOnClickListener(this)

            //Setting Click on Edit Icon
            binding.mcvEdit.setOnClickListener(this)
        }

        //Set Metal Type
        private fun setMetalType() {
            binding.tvMetalType.text = metalTypeData.metalTypeName

        }

        //Handles All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // When delete button Clicked
            if (v == binding.mcvDelete){
                
                // Show Metal Type Delete Dialog
                metalTypeData.metalTypeUUID?.let { showMetalTypeDeleteDialog(it,position) }
            }

            // When edit button Clicked
            else if(v == binding.mcvEdit) {

                metalTypeData.metalTypeUUID?.let { editInventoryCallback.editInventory(it,position) }
            }

        }
    }

}