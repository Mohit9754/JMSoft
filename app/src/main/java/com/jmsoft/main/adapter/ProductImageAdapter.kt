package com.jmsoft.main.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.databinding.ItemProductImageBinding

class ProductImageAdapter(
    private val context: Context,
    private val productImageList: ArrayList<Bitmap>,
    private val imageProduct: ImageView,
    private val llLeftBtn: LinearLayout,
    private val llRightBtn: LinearLayout
) :
    RecyclerView.Adapter<ProductImageAdapter.MyViewHolder>() {

    // visible image index
    var visibleImageIndex = 0

    // Selected Material Card View
    var selectedMaterialCardView: MaterialCardView? = null

    // All the product image material card view list
    var productImageMaterialCardViewList: ArrayList<MaterialCardView> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemProductImageBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productImageList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productImageList[position], position)
    }

    inner class MyViewHolder(private val binding: ItemProductImageBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var image: Bitmap
        private var position = -1
        fun bind(image: Bitmap, position: Int) {

            this.image = image
            this.position = position

            //Adding material card view in arraylist
            productImageMaterialCardViewList.add(binding.mcvProductImage)

            //Set the Image product
            setImage()

            // Selecting the first image
            if (position == 0) {
                selectedImage()
            }

            //Set Click on Product Image
            binding.mcvProductImage.setOnClickListener(this)

            //Set Click on Right button
            llRightBtn.setOnClickListener(this)

            //Set Click on left button
            llLeftBtn.setOnClickListener(this)

        }

        //Set the Image product
        private fun setImage() {

            binding.ivProduct.setImageBitmap(image)
        }

        // Select the Product image
        private fun selectedImage() {

            selectedMaterialCardView?.strokeColor = context.getColor(R.color.white)
            imageProduct.setImageBitmap(productImageList[visibleImageIndex])
            productImageMaterialCardViewList[visibleImageIndex].strokeColor =
                context.getColor(R.color.theme)
            selectedMaterialCardView = binding.mcvProductImage
        }

        //Handles All the Clicks
        override fun onClick(v: View?) {

            //Set Click on Product Image
            when (v) {

                binding.mcvProductImage -> {

                    selectedMaterialCardView = productImageMaterialCardViewList[visibleImageIndex]

                    //Change Visible image index
                    visibleImageIndex = position

                    // Select the Product image
                    selectedImage()
                }

                // Set Click on Right button
                llRightBtn -> {

                    selectedMaterialCardView = productImageMaterialCardViewList[visibleImageIndex]

                    //Increment Visible image index circularly
                    visibleImageIndex = (visibleImageIndex + 1) % productImageList.size

                    // Select the Product image
                    selectedImage()
                }

                // Set Click on left button
                llLeftBtn -> {

                    selectedMaterialCardView = productImageMaterialCardViewList[visibleImageIndex]

                    //decrement Visible image index circularly
                    visibleImageIndex =
                        (visibleImageIndex + productImageList.size - 1) % productImageList.size

                    // Select the Product image
                    selectedImage()

                }
            }

        }

    }
}