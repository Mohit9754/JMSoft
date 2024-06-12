package com.jmsoft.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.InvoiceItemBinding
import com.jmsoft.main.model.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class PdfInvoiceAdapter(
    private val context: Context,
    private var textViewTotalAmount:TextView,
    private var cartDataList: ArrayList<CartDataModel>
    ) :
    RecyclerView.Adapter<PdfInvoiceAdapter.MyViewHolder>() {

   var totalAmount = 0.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = InvoiceItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = cartDataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(cartDataList[position],position)
    }

    inner class MyViewHolder(private val binding: InvoiceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var cardData: CartDataModel
        private lateinit var productData:ProductDataModel

        // bind method
        fun bind(cardData: CartDataModel,position: Int) {

            this.cardData = cardData
            this.productData = Utils.getProductThroughProductUUID(cardData.productUUID.toString())

            Utils.E("Card data ${cardData.productUUID}  ")

            binding.tvAmount.text = (productData.productPrice?.let {
                cardData.productQuantity?.times(
                    it
                )
            }).toString()

            binding.tvPrice.text = productData.productPrice.toString()
            binding.tvWeight.text = productData.productWeight.toString()
            binding.tvCarat.text = productData.productCarat.toString()
            binding.tvDescription.text = productData.productDescription.toString()
            binding.tvNP.text = cardData.productQuantity.toString()

            Utils.E("product name is ${productData.productName} ${cartDataList.size} ")

            totalAmount += binding.tvAmount.toString().toInt()

            if (position+1 == cartDataList.size) {
                textViewTotalAmount.text = Utils.getThousandSeparate(totalAmount)
            }


        }
    }

}