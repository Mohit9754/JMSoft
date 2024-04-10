package com.jmsoft.main.adapter

import android.annotation.SuppressLint
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

        // Product Data
        private lateinit var productData: ProductDataModel

        fun bind(productData: ProductDataModel) {

            this.productData = productData

            //Set the Product name
            setProductName()

            //Set the Product weight
            setProductWeight()

            //Set the Product type
            setProductType()

            //Set the Product category
            setProductCategory()

            //Set the Product carat
            setProductCarat()

            //Set the Product price
            setProductPrice()

            //Set the Product image
            setProductImage()

            //Setting Click on Catalog Item
            binding.mcvCatalogItem.setOnClickListener(this)
        }

        //Set the Product image
        private fun setProductImage(){

            val arrayOfImages = productData.productImage?.split(",")?.toTypedArray()

            val bitmap = Utils.getImageFromInternalStorage(context,
                arrayOfImages?.get(0).toString()
            )

            binding.tvProductImage?.setImageBitmap(bitmap)
        }

        //Set the Product price
        private fun setProductPrice(){
            binding.tvProductPrice.text = productData.productPrice.toString()
        }

        //Set the Product carat
        private fun setProductCarat() {
            binding.tvProductCarat.text = productData.productCarat
        }

        //Set the Product category
        private fun setProductCategory() {

            binding.tvProductCategory.text =
                productData.categoryUUID?.let { Utils.getCategoryNameThroughCategoryUUID(it) }
        }

        //Set the Product type
        private fun setProductType() {
            binding.tvProductType.text = productData.productMetalType
        }

        //Set the Product weight
        @SuppressLint("SetTextI18n")
        private fun setProductWeight() {
            binding.tvProductWeight.text = "${productData.productWeight} ${productData.productUnitOfMeasurement} "
        }

        //Set the Product name
        private fun setProductName() {

            binding.tvProductName.text = productData.productName
        }

        //Handles All the Clicks
        override fun onClick(v: View?) {

            // Click on Catalog item
            if (v == binding.mcvCatalogItem) {

                val bundle = Bundle()
                //Giving the product UUID
                bundle.putString(Constants.productUUID, productData.productUUId)
               (context as DashboardActivity).navController?.navigate(R.id.product,bundle)

            }

        }

    }
}