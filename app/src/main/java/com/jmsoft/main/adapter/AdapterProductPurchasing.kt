package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.Utility.database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants.Companion.currency
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemPurchasingProductBinding

class AdapterProductPurchasing(
    private val context: Context,
    private var productList: ArrayList<ProductDataModel>,

) :
    RecyclerView.Adapter<AdapterProductPurchasing.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemPurchasingProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    inner class MyViewHolder(private val binding: ItemPurchasingProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // bind method
        @SuppressLint("SetTextI18n")
        fun bind(productDataModel: ProductDataModel) {

            val imageUri = productDataModel.productImageUri?.split(",")?.get(0)
            val bitmapImage = imageUri?.let { Utils.getImageFromInternalStorage(context, it) }

            binding.ivProduct.setImageBitmap(bitmapImage)

            binding.tvProductName.text = productDataModel.productName
            binding.tvPrice.text = "${productDataModel.productPrice?.let { Utils.getThousandSeparate(it) }} $currency"

        }

    }

}