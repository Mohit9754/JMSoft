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
import com.jmsoft.databinding.ItemMetalTypeDropdownBinding
import com.jmsoft.main.fragment.ProductInventoryFragment
import com.jmsoft.main.`interface`.SelectedCallback

/**
 * MetalType Dropdown Adapter
 *
 *
 */
class MetalTypeDropdownAdapter(
    private val context: Context,
    private var metalTypeList: ArrayList<MetalTypeDataModel>,
    private val productInventoryFragment: ProductInventoryFragment,
    private val selectedCallback: SelectedCallback

) :
    RecyclerView.Adapter<MetalTypeDropdownAdapter.MyViewHolder>() {

    // Selected metal type position
    var selectedMetalTypePosition:Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemMetalTypeDropdownBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = metalTypeList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(metalTypeList[position],position)
    }

    // Show Metal Type Delete Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showMetalTypeDeleteDialog(position: Int,metalTypeUUID:String) {

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
            metalTypeList.removeAt(position)

            Utils.T(context, context.getString(R.string.deleted_successfully))

            selectedMetalTypePosition = -1
            selectedCallback.unselect()

            notifyDataSetChanged()
        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    inner class MyViewHolder(private val binding: ItemMetalTypeDropdownBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Metal type data
        private lateinit var metalTypeData:MetalTypeDataModel

        // metal type position
        private var position = -1

        // Bind method
        fun bind(metalTypeData: MetalTypeDataModel,position: Int) {

            this.metalTypeData = metalTypeData
            this.position = position

            // Set metal type name
            setMetalTypeName()

            // Set selected metal type
            setSelectedMetalType()

            // Set click on metal type
            binding.llMetalType.setOnClickListener(this)

            // Set click on delete button
            binding.mcvDelete.setOnClickListener(this)

            // Set click on edit button
            binding.mcvEdit.setOnClickListener(this)

        }

        // Set selected metal type
        private fun setSelectedMetalType(){

            if (selectedMetalTypePosition == position) {

                binding.llMetalType.setBackgroundColor(context.getColor(R.color.selected_drop_down_color))
                selectedCallback.selected(metalTypeData)

            }
            else {

                binding.llMetalType.setBackgroundColor(context.getColor(R.color.white))

            }
        }

        // Set metal type name
        private fun setMetalTypeName() {
            binding.tvMetalType.text = metalTypeData.metalTypeName
        }

        // Handle all the clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // Clicked on metal type
            if (v == binding.llMetalType) {
                selectedMetalTypePosition = position
                notifyDataSetChanged()
            }

            // Clicked on delete button
            else if (v == binding.mcvDelete) {

                metalTypeData.metalTypeUUID?.let { showMetalTypeDeleteDialog(position, it) }
            }

            // Clicked on edit button
            else if (v == binding.mcvEdit) {

                productInventoryFragment.showAddOrEditMetalTypeDialog(position,metalTypeData.metalTypeUUID)
            }

        }

    }

}

