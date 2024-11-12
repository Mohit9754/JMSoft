package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.utility.database.StockLocationDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemAddStockLocationBinding
import com.jmsoft.databinding.ItemStockLocationDropdownBinding
import com.jmsoft.main.`interface`.SelectedCallback

class StockLocationDropdownAdapter(
    private val context: Context,
    private var stockLocationList: ArrayList<StockLocationDataModel>,
    private val isFromStockLocation: Boolean,
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


            }
            else {

                binding.tvName.text = stockLocationDataModel.stockLocationName

                setParentName()

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

        }
    }

}

