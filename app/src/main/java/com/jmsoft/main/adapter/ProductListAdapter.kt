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
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.edit
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.ItemProductListBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.`interface`.EditInventoryCallback

/**
 * MetalType Adapter
 *
 * Showing the catalog details
 *
 */

class ProductListAdapter(
    private val context: Context,
    private var productList: ArrayList<String>

) :
    RecyclerView.Adapter<ProductListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemProductListBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productList[position],position)
    }



    // Show Category Delete Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showProductDeleteDialog(position: Int) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivImage.setImageResource(R.drawable.img_delete_inventory)
        dialogBinding.tvMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_product_this_action_cannot_be_undone)

        dialogBinding.mcvYes.setOnClickListener {

            dialog.dismiss()
            productList.removeAt(position)
            notifyDataSetChanged()

        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    inner class MyViewHolder(private val binding: ItemProductListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Product Data
        private lateinit var productData: String

        private var position:Int = -1

        fun bind(productData: String,position: Int) {

            this.productData = productData
            this.position = position

            //Set Category Name
//            setCategoryName()

            //Setting Click on Delete Icon
            binding.mcvDelete.setOnClickListener(this)

            //Setting Click on Edit Icon
            binding.mcvEdit.setOnClickListener(this)
        }

        //Set Category Name
        private fun setCategoryName() {
//            binding.tvMetalType.text = categoryName
        }

        //Handles All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // When delete button Clicked
            if (v == binding.mcvDelete){

                // Show Category Delete Dialog
                showProductDeleteDialog(position)
            }

            // When edit button Clicked
            else if(v == binding.mcvEdit) {

                val bundle = Bundle()
                //Giving the product UUID
                bundle.putString(Constants.state, edit)


                (context as DashboardActivity).navController?.navigate(R.id.productInventory, bundle)

            }

        }
    }

}