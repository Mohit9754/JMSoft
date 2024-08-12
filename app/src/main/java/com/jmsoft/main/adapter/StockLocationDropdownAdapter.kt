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

class StockLocationDropdownAdapter(
    private val context: Context,
    private var stockLocationList: ArrayList<StockLocationDataModel>,
    private var productInventoryFragment:ProductInventoryFragment?,
    private val isFromStockLocation:Boolean,
    private val selectedCallback: SelectedCallback
) :
    RecyclerView.Adapter<StockLocationDropdownAdapter.MyViewHolder>() {

    // Selected stock location position
    var selectedStockLocationPosition:Int = -1
    var dialogBinding:ItemAddStockLocationBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemStockLocationDropdownBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = stockLocationList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(stockLocationList[position],position)
    }

    // Show stock Location Delete Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showStockLocationDeleteDialog(position: Int,stockLocationUUID:String) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivImage.setImageResource(R.drawable.img_delete_inventory)
        dialogBinding.tvMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_stock_location_this_action_cannot_be_undone)

        dialogBinding.mcvYes.setOnClickListener {

            dialog.dismiss()

            Utils.deleteStockLocation(stockLocationUUID)
            stockLocationList.removeAt(position)

            Utils.T(context, context.getString(R.string.deleted_successfully))

            selectedStockLocationPosition = -1
            selectedCallback.unselect()

            notifyDataSetChanged()
        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    inner class MyViewHolder(private val binding: ItemStockLocationDropdownBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // stock location data
        private lateinit var stockLocationDataModel:StockLocationDataModel

        // stock location position
        private var position = -1

        // Bind method
        fun bind(stockLocationDataModel: StockLocationDataModel,position: Int) {

            this.stockLocationDataModel = stockLocationDataModel
            this.position = position

            // Set selected stock location
            setSelectedStockLocation()

            if (isFromStockLocation) {

                binding.tvName.text = stockLocationDataModel.stockLocationName

//                binding.llDelAndEditSection.visibility = View.GONE
            }
            else {

                binding.tvName.text = stockLocationDataModel.stockLocationName

                setParentName()

                // Set click on delete button
//                binding.mcvDelete.setOnClickListener(this)
//
//                // Set click on edit button
//                binding.mcvEdit.setOnClickListener(this)

            }

            // Set click on stock location
            binding.llStockLocation.setOnClickListener(this)

        }

        private fun setParentName() {

            val stockLocationDataModel = stockLocationDataModel.stockLocationParentUUID?.let {
                Utils.getStockLocation(
                    it
                )
            }

            binding.tvParent.text = if (stockLocationDataModel?.stockLocationName != null) stockLocationDataModel.stockLocationName else "_"
        }

        // Set selected metal type
        private fun setSelectedStockLocation() {

            if (selectedStockLocationPosition == position) {

                binding.llStockLocation.setBackgroundColor(context.getColor(R.color.selected_drop_down_color))
                selectedCallback.selected(stockLocationDataModel)

            }
            else {

                binding.llStockLocation.setBackgroundColor(context.getColor(R.color.white))

            }
        }

        // Handle all the clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.llStockLocation) {
                selectedStockLocationPosition = position
                notifyDataSetChanged()
            }

            // Clicked on delete button
//            else if (v == binding.mcvDelete) {
//
//                stockLocationDataModel.stockLocationUUID?.let { showStockLocationDeleteDialog(position, it) }
//            }
//
//            // Clicked on edit button
//            else if (v == binding.mcvEdit) {
//
////                inventoryFragment
//
//            }
        }
    }

}

