package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.database.OrderDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemOrderBinding
import com.jmsoft.main.activity.PdfViewActivity
import java.io.File

class ConfirmOrderAdapter(
    private val context: Context,
    private var orderList: ArrayList<OrderDataModel> ) :

    RecyclerView.Adapter<ConfirmOrderAdapter.MyViewHolder>() {

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

            setOrderId()

            setProductQuantity()

            setAddress()

            setTotalPrice()

            binding.mcvOpen.setOnClickListener(this)
            binding.mcvPrint.setOnClickListener(this)

            if (position+1 == orderList.size) {
                GetProgressBar.getInstance(context)?.dismiss()
            }

        }

        private fun setOrderId(){
            binding.tvOrderId.text = orderDataModel.orderNo
        }

        @SuppressLint("SetTextI18n")
        private fun setAddress() {

            val addressDataModel = orderDataModel.addressUUID?.let { Utils.getAddressThroughAddressUUID(it) }

            binding.tvAddress.text = "${addressDataModel?.address} , ${addressDataModel?.zipCode}"
        }

        private fun setProductQuantity() {

            var qty = 0

            val quantityList = orderDataModel.productQuantityUri?.split(",")

            if (quantityList != null) {

                for (quantity in quantityList) {

                    qty += quantity.toInt()
                }
            }

            binding.tvProductQuantity.text = qty.toString()
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

            if (v == binding.mcvPrint) {

                val pdfFile = File(context.getExternalFilesDir(null), "${Constants.path}${orderDataModel.pdfName}.pdf")

                if (pdfFile.exists()) {

                    Utils.printPdf(context,pdfFile)
                }
                else {
                    Utils.T(context, context.getString(R.string.file_not_found))
                }

            }

            else if (v == binding.mcvOpen) {

                val intent = Intent(context, PdfViewActivity::class.java)
                intent.putExtra(Constants.pdfName,orderDataModel.pdfName)
                context.startActivity(intent)

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