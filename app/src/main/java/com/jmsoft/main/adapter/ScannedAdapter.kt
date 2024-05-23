package com.jmsoft.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.databinding.ItemExpectedBinding

class ScannedAdapter(
    private val context: Context,
    private val scannedProductList: ArrayList<ProductDataModel>,
    ) :
    RecyclerView.Adapter<ScannedAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemExpectedBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = scannedProductList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(scannedProductList[position], position)
    }

    inner class MyViewHolder(private val binding: ItemExpectedBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var productData:ProductDataModel
        private var position:Int = -1

        fun bind(productDataModel: ProductDataModel,position: Int) {

            this.productData = productDataModel
            this.position = position

            setProductName()

            setRFIDCode()

        }

        private fun setProductName() {
            binding.tvProductName.text = productData.productName
        }

        private fun setRFIDCode() {
            binding.tvRFIDCode.text = productData.productRFIDCode
        }


        override fun onClick(v: View?) {

        }

    }
}