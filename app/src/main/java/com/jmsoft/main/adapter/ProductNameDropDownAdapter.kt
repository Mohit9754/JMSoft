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
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.MetalTypeDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.Database.StockLocationDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentInventoryBinding
import com.jmsoft.databinding.ItemAddStockLocationBinding
import com.jmsoft.databinding.ItemMetalTypeDropdownBinding
import com.jmsoft.databinding.ItemStockLocationDropdownBinding
import com.jmsoft.main.fragment.InventoryFragment
import com.jmsoft.main.fragment.ProductInventoryFragment
import com.jmsoft.main.fragment.StockLocationFragment
import com.jmsoft.main.`interface`.SelectedCallback

class ProductNameDropDownAdapter(
    private val context: Context,
    private var stockLocationList: ArrayList<ProductDataModel>,
    private val selectedCallback: SelectedCallback
) :
    RecyclerView.Adapter<ProductNameDropDownAdapter.MyViewHolder>() {

    // Selected product name position
    var selectedProductNamePosition:Int = -1
    var dialogBinding:ItemAddStockLocationBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemStockLocationDropdownBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = stockLocationList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(stockLocationList[position],position)
    }


    inner class MyViewHolder(private val binding: ItemStockLocationDropdownBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var productDataModel:ProductDataModel

        private var position = -1

        // Bind method
        fun bind(productDataModel: ProductDataModel,position: Int) {

            this.productDataModel = productDataModel
            this.position = position

            // Set selected stock location
            setSelectedStockLocation()

            setProductName()

            binding.tvParent.visibility = View.GONE

            // Set click on stock location
            binding.llStockLocation.setOnClickListener(this)

        }

        private fun setProductName() {
            binding.tvName.text = productDataModel.productName
        }

        // Set selected metal type
        private fun setSelectedStockLocation() {

            if (selectedProductNamePosition == position) {

                binding.llStockLocation.setBackgroundColor(context.getColor(R.color.selected_drop_down_color))
                selectedCallback.selected(productDataModel)

            }
            else {

                binding.llStockLocation.setBackgroundColor(context.getColor(R.color.white))

            }
        }

        // Handle all the clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.llStockLocation) {
                selectedProductNamePosition = position
                notifyDataSetChanged()
            }

        }
    }

}

