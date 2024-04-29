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
import com.jmsoft.databinding.ItemMetalTypeDropdownBinding
import com.jmsoft.main.fragment.ProductInventoryFragment
import com.jmsoft.main.`interface`.SelectedCallback

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class CategoryDropdownAdapter(
    private val context: Context,
    private var categoryDataModelList: ArrayList<CategoryDataModel>,
    private val productInventoryFragment: ProductInventoryFragment,
    private val selectedCallback: SelectedCallback

) :
    RecyclerView.Adapter<CategoryDropdownAdapter.MyViewHolder>() {

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemMetalTypeDropdownBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = categoryDataModelList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(categoryDataModelList[position],position)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showCategoryDeleteDialog(position: Int, categoryUUID:String) {

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

            Utils.deleteCategory(categoryUUID)
            categoryDataModelList.removeAt(position)

            Utils.T(context, context.getString(R.string.deleted_successfully))

            selectedPosition = -1
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

        private lateinit var categoryData:CategoryDataModel

        private var position = -1

        fun bind(categoryData: CategoryDataModel,position: Int) {

            this.categoryData = categoryData
            this.position = position

            setCategoryName()

            setSelected()

            binding.llMetalType.setOnClickListener(this)

            binding.mcvDelete.setOnClickListener(this)

            binding.mcvEdit.setOnClickListener(this)

        }

        private fun setSelected() {

            if (selectedPosition == position) {
                binding.llMetalType.setBackgroundColor(context.getColor(R.color.selected_drop_down_color))
            }
            else {
                binding.llMetalType.setBackgroundColor(context.getColor(R.color.white))
            }
        }

        private fun setCategoryName() {
            binding.tvMetalType.text = categoryData.categoryName
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.llMetalType) {

                selectedPosition = position

                selectedCallback.selected(categoryData)

                notifyDataSetChanged()
            }
            else if (v == binding.mcvDelete){
                categoryData.categoryUUID?.let { showCategoryDeleteDialog(position, it) }
            }

            else if (v == binding.mcvEdit) {

                productInventoryFragment.showAddOrEditCategoryDialog(position,categoryData.categoryUUID)
            }

        }

    }

}