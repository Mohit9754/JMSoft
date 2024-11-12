package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.databinding.ItemAddImageProductBinding
import com.jmsoft.main.fragment.ProductInventoryFragment

class AddImageAdapter(
    private val context: Context,
    private var productImageList: ArrayList<Any>,
    private val productInventoryFragment: ProductInventoryFragment,
    private val selectedProductImageBitmap:ArrayList<Bitmap>
) :
    RecyclerView.Adapter<AddImageAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemAddImageProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    // if product image list is less than 5 it returns (productImageListSize + 1)  for showing Add Image Button
    override fun getItemCount() = if(productImageList.size < 5 ) productImageList.size + 1 else productImageList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        // At the last time it will pass null
        if (position == productImageList.size){
            holder.bind(null,position)
        }

        else{
            holder.bind(productImageList[position],position)
        }
    }

    inner class MyViewHolder(private val binding: ItemAddImageProductBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Product Data
        private  var productImage: Any? = null

        // Product position
        private var position:Int = -1

        // bind method
        fun bind(productImage: Any?,position: Int) {

            this.position = position

            if (productImage != null) {

                this.productImage = productImage

                // At the fire time
                if (position == 0) {
                    // Clear the selected product image list
                    selectedProductImageBitmap.clear()
                }

                // Set product image
                setImage()

                // Set click on cross button
                binding.mcvCrossBtn.setOnClickListener(this)

            }

            // Set click on Add Image button
            binding.mcvAddImage.setOnClickListener(this)

        }

        // Set product image
        private fun setImage(){

            if (productImage is Uri){
                binding.ivProductImage.setImageURI(productImage as Uri)
            }
            else if (productImage is Bitmap){
                binding.ivProductImage.setImageBitmap(productImage as Bitmap)
            }

            binding.mcvCrossBtn.visibility = View.VISIBLE
            binding.llAddImageSection.visibility = View.GONE

            // Add in the selected product image list
            selectedProductImageBitmap.add(binding.ivProductImage.drawable.toBitmap())
        }

        // Handle all the clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // Add image button clicked
            if (v == binding.mcvAddImage && productImage == null) {

                productInventoryFragment.showImageSelectionDialog(binding.ivProductImage)

            }
            // Cross button clicked
            else if (v == binding.mcvCrossBtn){

                productImageList.removeAt(position)
                selectedProductImageBitmap.removeAt(position)

                productInventoryFragment.setProductImageRecyclerView()
            }

          }

    }

}