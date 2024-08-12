package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.StockLocationDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentStockLocationBinding
import com.jmsoft.databinding.ItemAddStockLocationBinding
import com.jmsoft.databinding.ItemStockLocationBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.fragment.ProductInventoryFragment
import com.jmsoft.main.fragment.StockLocationFragment

class StockLocationAdapter(
    private val context: Context,
    private var stockLocationList: ArrayList<StockLocationDataModel>,
    private val stockLocationFragmentBinding: FragmentStockLocationBinding,
    private var stockLocationFragment:StockLocationFragment
    ) :
    RecyclerView.Adapter<StockLocationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemStockLocationBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = stockLocationList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(stockLocationList[position], position)
    }

    // Show Stock Location Delete Dialog
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("NotifyDataSetChanged")
    private fun showStockLocationDeleteDialog(stockLocationUUID: String,position: Int) {

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

            Utils.T(context, context.getString(R.string.deleted_successfully))

            stockLocationList.removeAt(position)

            if (stockLocationList.isEmpty()) {

                stockLocationFragmentBinding.mcvStockLocationList?.visibility = View.GONE
                stockLocationFragmentBinding.llEmptyStockLocation?.visibility = View.VISIBLE
            }

            notifyItemRemoved(position)
            notifyItemRangeChanged(position, stockLocationList.size - position)

            stockLocationFragment.removeItemFromDropdownList(stockLocationUUID)

        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    inner class MyViewHolder(private val binding: ItemStockLocationBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Product position
        private var position: Int = 0
        private lateinit var stockLocationDataModel: StockLocationDataModel

        // bind method
        fun bind(stockLocationDataModel: StockLocationDataModel, position: Int) {

            this.position = position
            this.stockLocationDataModel = stockLocationDataModel

            setStockLocationName()

            setStockLocationParent()

            binding.mcvDelete.setOnClickListener(this)

            binding.mcvEdit.setOnClickListener(this)

            binding.mcvAddProduct.setOnClickListener(this)

            if (position == stockLocationList.size-1){
                GetProgressBar.getInstance(context)?.dismiss()
            }

        }

        private fun setStockLocationParent() {

            val stockLocationDataModel = stockLocationDataModel.stockLocationParentUUID?.let {
                Utils.getStockLocation(
                    it
                )
            }

            binding.tvParent.text = if (stockLocationDataModel?.stockLocationName != null) stockLocationDataModel.stockLocationName else "_"
        }

        private fun setStockLocationName() {
            binding.tvName.text = stockLocationDataModel.stockLocationName
        }

        // Handle all the clicks
        @RequiresApi(Build.VERSION_CODES.N)
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.mcvDelete) {

                stockLocationDataModel.stockLocationUUID?.let { showStockLocationDeleteDialog(it,position) }
            }

            else if (v == binding.mcvEdit) {

                stockLocationFragment.showAddStockLocationDialog(position,stockLocationDataModel)

            }

            else if (v == binding.mcvAddProduct) {

                GetProgressBar.getInstance(context)?.show()

                val bundle = Bundle()

                //Giving the product UUID
                bundle.putString(Constants.stockLocationUUID, stockLocationDataModel.stockLocationUUID)

                (context as DashboardActivity).navController?.navigate(R.id.productInventory, bundle)

            }
        }
    }
}