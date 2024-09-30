package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.ContactDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.databinding.ItemAddStockLocationBinding
import com.jmsoft.databinding.ItemStockLocationDropdownBinding
import com.jmsoft.main.`interface`.SelectedCallback

class SupplierDropDownAdapter(
    private val context: Context,
    private var supplierList: ArrayList<ContactDataModel>,
    private val selectedCallback: SelectedCallback
) :
    RecyclerView.Adapter<SupplierDropDownAdapter.MyViewHolder>() {

    var selectedSupplierPosition:Int = -1
    var dialogBinding:ItemAddStockLocationBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemStockLocationDropdownBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = supplierList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(supplierList[position],position)
    }

    inner class MyViewHolder(private val binding: ItemStockLocationDropdownBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var contactDataModel:ContactDataModel

        private var position = -1

        // Bind method
        fun bind(contactDataModel: ContactDataModel,position: Int) {

            this.contactDataModel = contactDataModel
            this.position = position

            // Set selected stock location
            setSelectedStockLocation()

            setSupplierName()

            binding.tvParent.visibility = View.GONE

            // Set click on stock location
            binding.llStockLocation.setOnClickListener(this)

        }

        @SuppressLint("SetTextI18n")
        private fun setSupplierName() {
            binding.tvName.text = "${contactDataModel.firstName} ${contactDataModel.lastName}"
        }

        // Set selected metal type
        private fun setSelectedStockLocation() {

            if (selectedSupplierPosition == position) {

                binding.llStockLocation.setBackgroundColor(context.getColor(R.color.selected_drop_down_color))
                selectedCallback.selected(contactDataModel)

            }
            else {

                binding.llStockLocation.setBackgroundColor(context.getColor(R.color.white))

            }
        }

        // Handle all the clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.llStockLocation) {
                selectedSupplierPosition = position
                notifyDataSetChanged()
            }

        }
    }

}

