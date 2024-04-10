package com.jmsoft.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemCatalogBinding
import com.jmsoft.databinding.ItemCollectionBinding
import com.jmsoft.main.activity.DashboardActivity

/**
 * Collection Adapter
 *
 * Showing the catalog details
 *
 */

class CollectionItemAdapter(
    private val context: Context,
    private val productList: ArrayList<ProductDataModel>
) :
    RecyclerView.Adapter<CollectionItemAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCollectionBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    inner class MyViewHolder(private val binding: ItemCollectionBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var productData: ProductDataModel

        fun bind(productData: ProductDataModel) {

            this.productData = productData

            //Set the Product image
            setProductName()

            //Set the Product price
            setProductPrice()

            //Set the Product image
            setProductImage()

            //Setting Click on Catalog Item
//            binding.mcvCatalogItem?.setOnClickListener(this)
        }

        //Set the Product image
        private fun setProductImage() {

            val arrayOfImages = productData.productImage?.split(",")?.toTypedArray()

            val bitmap = Utils.getImageFromInternalStorage(
                context,
                arrayOfImages?.get(0).toString()
            )
            binding.ivProductImage.setImageBitmap(bitmap)
        }

        //Set the Product price
        private fun setProductPrice() {
            binding.tvProductPrice.text = productData.productPrice.toString()
        }

        //Set the Product image
        private fun setProductName() {

            binding.tvProductName.text = productData.productName
        }

        //Handles All the Clicks
        override fun onClick(v: View?) {

            //Click on Catalog item
            if (v == binding.mcvCatalogItem) {

                //Navigate to Product
                (context as DashboardActivity).navController?.navigate(R.id.product)
            }

        }

    }
}