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
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentMetalTypeBinding
import com.jmsoft.databinding.ItemInventoryBinding
import com.jmsoft.main.`interface`.EditInventoryCallback

/**
 * Category List Adapter
 *
 *
 */

class CategoryListAdapter(
    private val context: Context,
    private var categoryList: ArrayList<CategoryDataModel>,
    private val fragmentMetalTypeBinding: FragmentMetalTypeBinding,
    private val editInventoryCallback: EditInventoryCallback

) :
    RecyclerView.Adapter<CategoryListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemInventoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = categoryList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(categoryList[position],position)
    }

    // Show Category Delete Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showCategoryDeleteDialog(position: Int,categoryUUID: String) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivImage.setImageResource(R.drawable.img_delete_inventory)
        dialogBinding.tvMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_category_this_action_cannot_be_undone)
//            context.getString(R.string.are_you_sure_you_want_to_delete_this_metal_type_this_action_cannot_be_undone)

        dialogBinding.mcvYes.setOnClickListener {

            dialog.dismiss()

            Utils.deleteCategory(categoryUUID)
            categoryList.removeAt(position)

            Utils.T(context, context.getString(R.string.deleted_successfully))

            if (categoryList.isEmpty()){

                fragmentMetalTypeBinding.mcvMetalTypeList?.visibility = View.GONE
                fragmentMetalTypeBinding.llEmptyInventory?.visibility = View.VISIBLE
                fragmentMetalTypeBinding.tvEmptyMsg?.text =
                    context.getString(R.string.category_is_empty)

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

        // Category Data
        private lateinit var categoryData: CategoryDataModel

        // Category position
        private var position:Int = -1

        // Bind method
        fun bind(categoryData: CategoryDataModel,position: Int) {

            this.categoryData = categoryData
            this.position = position

            //Set Category Name
            setCategoryName()

            //Setting Click on Delete Icon
            binding.mcvDelete.setOnClickListener(this)

            //Setting Click on Edit Icon
            binding.mcvEdit.setOnClickListener(this)
        }


        //Set Category Name
        private fun setCategoryName() {
            binding.tvMetalType.text = categoryData.categoryName
        }

        // Handle All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // When delete button Clicked
            if (v == binding.mcvDelete) {

                // Show Category Delete Dialog
                categoryData.categoryUUID?.let { showCategoryDeleteDialog(position, it) }
            }

            // When edit button Clicked
            else if(v == binding.mcvEdit) {

                categoryData.categoryUUID?.let { editInventoryCallback.editInventory(it,position) }
            }

        }
    }

}