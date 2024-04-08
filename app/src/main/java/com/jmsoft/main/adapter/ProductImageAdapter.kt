package com.jmsoft.main.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemCatalogBinding
import com.jmsoft.databinding.ItemProductImageBinding
import com.jmsoft.main.activity.DashboardActivity
import okio.Utf8

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class ProductImageAdapter(
    private val context: Context,
    private val productImageList: ArrayList<Int>,
    private val imageProduct: ImageView,
    private val llLeftBtn:LinearLayout,
    private val llRightBtn:LinearLayout
) :
    RecyclerView.Adapter<ProductImageAdapter.MyViewHolder>() {

   // visible image index
   var visibleImageIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemProductImageBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productImageList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productImageList[position],position)
    }

    inner class MyViewHolder(private val binding: ItemProductImageBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private var image: Int = 0
        private var position = -1
        fun bind(image: Int,position: Int) {

            this.image = image
            this.position = position

            //Set the Image product
            setImage()

            //Set Click on Product Image
            binding.mcvProductImage.setOnClickListener(this)

            //Set Click on Right button
            llRightBtn.setOnClickListener(this)

            //Set Click on left button
            llLeftBtn.setOnClickListener(this)

        }

        //Set the Image product
        private fun setImage() {

            binding.ivProduct.setImageResource(image)
        }

        //Handles All the Clicks
        override fun onClick(v: View?) {

            //Set Click on Product Image
            if (v == binding.mcvProductImage){
                imageProduct.setImageResource(image)
                visibleImageIndex = position

            }

            // Set Click on Right button
            else if (v == llRightBtn) {

                visibleImageIndex = (visibleImageIndex + 1) % productImageList.size

                imageProduct.setImageResource(productImageList[visibleImageIndex])
            }

            // Set Click on left button
            else if (v == llLeftBtn){

                visibleImageIndex = (visibleImageIndex + productImageList.size - 1) % productImageList.size

                Utils.E(visibleImageIndex.toString())

                imageProduct.setImageResource(productImageList[visibleImageIndex])

            }

        }

    }
}