package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentAddPurchaseBinding
import com.jmsoft.databinding.ItemSelectedCollectionBinding

class SelectedProductAdapter(

    private val context: Context,
    private var selectedProductList: ArrayList<String>,
    private val fragmentAddPurchaseBinding: FragmentAddPurchaseBinding
) :
    RecyclerView.Adapter<SelectedProductAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemSelectedCollectionBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = selectedProductList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(selectedProductList[position],position)
    }

    inner class MyViewHolder(private val binding: ItemSelectedCollectionBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var productUUID:String

        // Selected collection position
        private var position = -1

        // Bind method
        fun bind(productUUID: String,position: Int) {

            this.productUUID = productUUID
            this.position = position

            setProductName()

            // Set click on cross button
            binding.ivCross.setOnClickListener(this)

        }

        private fun setProductName() {
            binding.tvCollectionName.text = Utils.getProductThroughProductUUID(productUUID).productName
        }

        // Handle all the clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // Clicked on cross button
            if (v == binding.ivCross) {

                selectedProductList.removeAt(position)

                if (selectedProductList.isEmpty())
//                    fragmentAddPurchaseBinding.tvProductName?.visibility = View.VISIBLE

                notifyDataSetChanged()

            }
        }
    }

}