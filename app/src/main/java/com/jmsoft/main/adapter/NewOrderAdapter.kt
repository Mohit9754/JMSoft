package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.OrderDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemOrderBinding
import com.jmsoft.main.activity.DashboardActivity

class NewOrderAdapter(
    private val context: Context,
    private var orderList: ArrayList<OrderDataModel> ) :

    RecyclerView.Adapter<NewOrderAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemOrderBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = orderList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(orderList[position],position)
    }

    inner class MyViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var orderDataModel:OrderDataModel

        // bind method
        @SuppressLint("SetTextI18n")
        fun bind(orderDataModel:OrderDataModel,position: Int) {

            this.orderDataModel = orderDataModel

            binding.mcvPrint.visibility = View.GONE
            binding.mcvOpen.visibility = View.GONE
            binding.llAddress.visibility = View.GONE

            setOrderId()

            setProductQuantity()

            setTotalPrice()

            binding.mcvOrder.setOnClickListener(this)

            if (position+1 == orderList.size) {
                GetProgressBar.getInstance(context)?.dismiss()
            }

        }

        private fun setOrderId() {
            binding.tvOrderId.text = orderDataModel.orderNo
        }

        private fun setProductQuantity() {

            if (orderDataModel.productQuantityUri?.isNotEmpty() == true) {

                val quantityList = orderDataModel.productQuantityUri?.split(",")

                var qty = 0

                if (quantityList != null) {

                    for (quantity in quantityList) {

                        qty+=quantity.toInt()
                    }

                    binding.tvProductQuantity.text = qty.toString()

                }

            }
            else {

                val quantityList = orderDataModel.productUUIDUri?.split(",")
                binding.tvProductQuantity.text = quantityList?.size.toString()

            }
        }

        @SuppressLint("SetTextI18n")
        private fun setTotalPrice() {

            binding.tvTotalAmount.text = "${orderDataModel.totalAmount?.let {
                Utils.getThousandSeparate(
                    it
                )
            }} ${Constants.currency}"
        }

        // Handle all the clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.mcvOrder) {

//                GetProgressBar.getInstance(context)?.show()

                val bundle = Bundle()

                //Giving the order UUID
                bundle.putString(Constants.orderUUID, orderDataModel.orderUUID)

                (context as DashboardActivity).navController?.navigate(R.id.cart, bundle)

            }

        }
    }


    // Filter function for filtering the order list
    @SuppressLint("NotifyDataSetChanged")
    fun filterProductDataList(orderList: ArrayList<OrderDataModel>) {

        this.orderList = orderList
        notifyDataSetChanged()

    }

}