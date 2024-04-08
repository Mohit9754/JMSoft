package com.jmsoft.main.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.jmsoft.R
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemCatalogBinding
import com.jmsoft.main.activity.DashboardActivity

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class CatalogAdapter(
    private val context: Context,
    private val productList: ArrayList<ProductDataModel>
) :
    RecyclerView.Adapter<CatalogAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCatalogBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    inner class MyViewHolder(private val binding: ItemCatalogBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var productData: ProductDataModel

        fun bind(productData: ProductDataModel) {

            this.productData = productData

            Utils.E("${productData.productName}")

            setProductName()

            setProductWeight()

            setProductType()

            setProductCategory()

            setProductCarat()

            setProductPrice()

            setProductImage()

            //Setting Click on Catalog Item
            binding.mcvCatalogItem.setOnClickListener(this)
        }

        private fun setProductImage(){

            val arrayOfImages = productData.productImage?.split(",")?.toTypedArray()

            val bitmap = Utils.getImageFromInternalStorage(context,
                arrayOfImages?.get(0).toString()
            )

            binding.tvProductImage?.setImageBitmap(bitmap)
        }

        private fun setProductPrice(){
            binding.tvProductPrice.text = productData.productPrice.toString()
        }

        private fun setProductCarat() {
            binding.tvProductCarat.text = productData.productCarat
        }

        private fun setProductCategory() {

            binding.tvProductCategory.text =
                productData.categoryUUID?.let { Utils.getCategoryNameThroughCategoryUUID(it) }
        }

        private fun setProductType() {
            binding.tvProductType.text = productData.productType
        }

        private fun setProductWeight() {
            binding.tvProductWeight.text = productData.productWeight
        }

        private fun setProductName() {

            binding.tvProductName.text = productData.productName
        }

        //Handles All the Clicks
        override fun onClick(v: View?) {

            if (v == binding.mcvCatalogItem) {

                val bundle = Bundle()

                //Giving the product UUID
                bundle.putString(Constants.productUUID, productData.productUUId)
               (context as DashboardActivity).navController?.navigate(R.id.product,bundle)

            }

        }

    }
}