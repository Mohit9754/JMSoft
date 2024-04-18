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
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentProductInventoryBinding
import com.jmsoft.databinding.ItemMetalTypeDropdownBinding
import com.jmsoft.main.fragment.ProductInventoryFragment

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class MetalTypeDropdownAdapter(
    private val context: Context,
    private var metalTypeList: ArrayList<String>,
    private var productInventoryBinding: FragmentProductInventoryBinding,
    private val productInventoryFragment: ProductInventoryFragment

) :
    RecyclerView.Adapter<MetalTypeDropdownAdapter.MyViewHolder>() {

    var selectedPosition:Int? = -1

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
    private fun showMetalTypeDeleteDialog(position: Int) {

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

            metalTypeList.removeAt(position)
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

        private lateinit var metalType:String

        private var position = -1

        fun bind(metalType: String,position: Int) {

            this.metalType = metalType
            this.position = position

            setMetalType()

            visibleViews()

            Utils.E(" Rebind $metalType")

            setSelected()

            binding.llMetalType.setOnClickListener(this)

            binding.mcvDelete.setOnClickListener(this)

            binding.mcvEdit.setOnClickListener(this)

        }

        private fun visibleViews(){
            binding.llDelAndEditSection.visibility = View.VISIBLE
            binding.viewLine.visibility = View.VISIBLE
        }

        private fun setSelected(){

            if (selectedPosition == position) {

                binding.llMetalType.setBackgroundColor(context.getColor(R.color.selected_drop_down_color))

            }
            else {

                binding.llMetalType.setBackgroundColor(context.getColor(R.color.white))

            }
        }

        private fun setMetalType() {
            binding.tvMetalType.text = metalType
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.llMetalType) {

                selectedPosition = position


                productInventoryBinding.tvMetalType?.text = metalType
                productInventoryBinding.tvMetalTypeError?.visibility = View.GONE

                productInventoryBinding.ivMetalType?.let { Utils.rotateView(it,0f) }

                productInventoryBinding.mcvMetalTypeList?.let { Utils.collapseView(it) }

                notifyDataSetChanged()

            }

            else if (v == binding.mcvDelete){

                showMetalTypeDeleteDialog(position)
            }

            else if (v == binding.mcvEdit) {
                productInventoryFragment.showAddOrEditMetalTypeDialog(position)
            }


        }

    }

}

