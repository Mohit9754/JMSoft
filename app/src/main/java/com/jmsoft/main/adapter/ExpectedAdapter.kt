package com.jmsoft.main.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Firebase
import com.jmsoft.R
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemCatalogBinding
import com.jmsoft.databinding.ItemExpectedBinding
import com.jmsoft.databinding.ItemProductImageBinding
import com.jmsoft.main.activity.DashboardActivity
import okio.Utf8

class ExpectedAdapter(
    private val context: Context,
    private val productList: ArrayList<ProductDataModel>,

) :
    RecyclerView.Adapter<ExpectedAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemExpectedBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productList[position], position)

    }

    inner class MyViewHolder(private val binding: ItemExpectedBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

       private lateinit var productDataModel:ProductDataModel
       private var position = 0

        fun bind(productDataModel: ProductDataModel,position: Int) {
            this.productDataModel = productDataModel
            this.position = position

            // Set product name
            setProductName()

            setRFIDCode()

        }

        // Set product name
        private fun setProductName(){
            binding.tvProductName.text = productDataModel.productName
        }

        private fun setRFIDCode() {
            binding.tvRFIDCode.text = productDataModel.productRFIDCode
        }

        override fun onClick(v: View?) {

        }

    }
}