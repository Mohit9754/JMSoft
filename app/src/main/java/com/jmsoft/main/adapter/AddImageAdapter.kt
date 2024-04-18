package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemAddImageProductBinding
import com.jmsoft.main.fragment.ProductInventoryFragment

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class AddImageAdapter(

    private val context: Context,
    private var productImageList: ArrayList<Any>,
    private val productInventoryFragment: ProductInventoryFragment
) :
    RecyclerView.Adapter<AddImageAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemAddImageProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = if(productImageList.size < 5 ) productImageList.size + 1 else productImageList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

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

        private var position:Int = -1

        fun bind(productImage: Any?,position: Int) {

            this.position = position

            Utils.E("$position")

            if (productImage != null) {

                this.productImage = productImage


                setImage()

                binding.mcvCrossBtn.setOnClickListener(this)

            }

            binding.mcvAddImage.setOnClickListener(this)

            //Setting Click on CartStatus
        }

        private fun setImage(){

            if (productImage is Uri){
                binding.ivProductImage.setImageURI(productImage as Uri)
            }
            else if (productImage is Bitmap){
                binding.ivProductImage.setImageBitmap(productImage as Bitmap)
            }

            binding.mcvCrossBtn.visibility = View.VISIBLE
            binding.llAddImageSection.visibility = View.GONE
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            if (v == binding.mcvAddImage && productImage == null) {

                productInventoryFragment.showImageSelectionDialog(binding.ivProductImage)

            }
            else if (v == binding.mcvCrossBtn){

                productImageList.removeAt(position)

                productInventoryFragment.setProductImageRecyclerView()
            }

          }

    }

}