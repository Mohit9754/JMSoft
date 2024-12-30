package com.jmsoft.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.Utility.database.CartDataModel
import com.jmsoft.Utility.database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.InvoiceItemBinding

class PdfInvoiceAdapter(
    private val context: Context,
    private var cartDataList: List<CartDataModel>,
    private var tvTotalAmount: TextView?,
    private var mcvTotalAmount:MaterialCardView?
    ) :
    RecyclerView.Adapter<PdfInvoiceAdapter.MyViewHolder>() {

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

            if (productData.productPrice != null && cardData.productQuantity != null) {

                val totalAmount = productData.productPrice!! * cardData.productQuantity!!
                binding.tvAmount.text = Utils.getThousandSeparate(totalAmount)

            }

            // set data
            binding.tvPrice.text = productData.productPrice?.let { Utils.getThousandSeparate(it) }
            binding.tvWeight.text = productData.productWeight?.let { Utils.getThousandSeparate(it) }
            binding.tvCarat.text = productData.productCarat.toString()
            binding.tvDescription.text = productData.productName.toString()
            binding.tvNP.text = cardData.productQuantity.toString()

            Utils.TotalAmount.addAmount(Utils.removeThousandSeparators(binding.tvAmount.text.toString()).toDouble())

            if (position+1 == cartDataList.size) {

                if (tvTotalAmount != null) {

                    tvTotalAmount?.text = Utils.getThousandSeparate(Utils.TotalAmount.getTotalAmount())
                    Utils.TotalAmount.resetTotalAmount()

                }
                else {
                    mcvTotalAmount?.visibility = View.GONE
                }
            }

        }
    }
}