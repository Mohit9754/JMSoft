package com.jmsoft.main.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.utility.database.ProductDataModel
import com.jmsoft.utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.databinding.ItemExpectedBinding
import com.jmsoft.main.activity.DashboardActivity

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

            if (position == productList.size-1)
                GetProgressBar.getInstance(context)?.dismiss()

            binding.mcvProduct.setOnClickListener(this)

        }

        // Set product name
        private fun setProductName(){
            binding.tvProductName.text = productDataModel.productName
        }

        private fun setRFIDCode() {
            binding.tvRFIDCode.text = productDataModel.productRFIDCode
        }

        override fun onClick(v: View?) {

            if (v == binding.mcvProduct) {

                GetProgressBar.getInstance(context)?.show()

                val bundle = Bundle()
                //Giving the product UUID
                bundle.putString(Constants.productUUID, productDataModel.productUUID)
                (context as DashboardActivity).navController?.navigate(R.id.productDetail, bundle)

            }

        }

    }
}