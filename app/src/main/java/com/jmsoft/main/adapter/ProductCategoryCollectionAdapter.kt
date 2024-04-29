package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.weightUnit
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentCollectionDetailBinding
import com.jmsoft.databinding.ItemCollectionBinding
import com.jmsoft.main.activity.DashboardActivity
import java.util.UUID

/**
 * Collection Adapter
 *
 * Showing the catalog details
 *
 */

class ProductCategoryCollectionAdapter(
    private val context: Context,
    private val productList: ArrayList<ProductDataModel>,
    private val productCategory: String,
    private val categoryDataList: ArrayList<CategoryDataModel>,
    private val collectionDetailAdapter: CollectionDetailAdapter,
    private val collectionUUID: String,
    private val collectionDetailAdapterPosition: Int,
    private val fragmentCollectionDetailBinding: FragmentCollectionDetailBinding
) :
    RecyclerView.Adapter<ProductCategoryCollectionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCollectionBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productList[position],position)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showCollectionDeleteDialog(
        position: Int,
        productUUID: String,
        productCollectionUUID: String
    ) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivImage.setImageResource(R.drawable.img_delete_inventory)

        dialogBinding.tvTitle.text = context.getString(R.string.remove)

        dialogBinding.tvMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_remove_this_product_from_the_collection)

        dialogBinding.mcvYes.setOnClickListener {

            val collectionUUIDList:MutableList<String> = productCollectionUUID.split(",").toMutableList()
            collectionUUIDList.remove(collectionUUID)

            val productDataModel = ProductDataModel()
            productDataModel.productUUID = productUUID
            productDataModel.collectionUUID = collectionUUIDList.joinToString().replace(" ","")

            Utils.updateCollectionInProduct(productDataModel)

            productList.removeAt(position)

            notifyItemRemoved(position)

            notifyItemRangeChanged(position,productList.size - position)

            if (productList.isEmpty()) {

                categoryDataList.removeAt(collectionDetailAdapterPosition)
                collectionDetailAdapter.notifyItemRemoved(collectionDetailAdapterPosition)

                collectionDetailAdapter.notifyItemRangeChanged(position,categoryDataList.size - collectionDetailAdapterPosition)

                if (categoryDataList.isEmpty()) {
                    fragmentCollectionDetailBinding.llEmptyInventory?.visibility  = View.VISIBLE
                }

            }

            Utils.T(context,context.getString(R.string.removed_successfully))

            dialog.dismiss()

        }

        dialogBinding.mcvNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    inner class MyViewHolder(private val binding: ItemCollectionBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Product Data
        private lateinit var productData: ProductDataModel

        private var position:Int = -1

        fun bind(productData: ProductDataModel,position: Int) {

            this.productData = productData
            this.position = position

            //Set the Product image
            setProductName()

            setProductWeight()

            setProductCategory()

            setProductType()

            setProductCarat()

            //Set the Product price
            setProductPrice()

            //Set the Product image
            setProductImage()

            binding.mcvDelete.visibility = View.VISIBLE
            binding.rlCollectionDetail.visibility = View.VISIBLE

            binding.mcvCartStatus.visibility = View.GONE

            binding.mcvDelete.setOnClickListener(this)

            //Setting Click on Collection Item
            binding.mcvCollectionItem.setOnClickListener(this)

        }

        private fun setProductCarat(){
            binding.tvProductCarat.text = productData.productCarat.toString()
        }

        private fun setProductCategory() {
            binding.tvProductCategory.text = productCategory
        }

        private fun setProductType(){
            binding.tvProductType.text = productData.metalTypeUUID?.let {
                Utils.getMetalTypeNameThroughMetalTypeUUID(
                    it
                )
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setProductWeight() {
            binding.tvProductWeight.text = "${productData.productWeight} $weightUnit"
        }

        //Set the Product image
        private fun setProductImage() {

            val arrayOfImages = productData.productImageUri?.split(",")?.toTypedArray()

            val bitmap = Utils.getImageFromInternalStorage(
                context,
                arrayOfImages?.get(0).toString()
            )
            binding.ivProductImage.setImageBitmap(bitmap)
        }

        //Set the Product price
        private fun setProductPrice() {

            binding.tvProductPrice.text = productData.productCost?.let {
                Utils.roundToTwoDecimalPlaces(
                    it
                )
            }?.let { Utils.getThousandSeparate(it) }
        }


        //Set the Product image
        private fun setProductName() {

            binding.tvProductName.text = productData.productName
        }

        //Handles All the Clicks
        override fun onClick(v: View?) {

            //Click on Collection item
            if (v == binding.mcvCollectionItem) {

                val bundle = Bundle()
                //Giving the product UUID
                bundle.putString(Constants.productUUID, productData.productUUID)

                //Navigate to Product
                (context as DashboardActivity).navController?.navigate(R.id.productDetail,bundle)

            }

            else if (v == binding.mcvDelete){

                productData.productUUID?.let { productData.collectionUUID?.let { it1 ->
                    showCollectionDeleteDialog(position, it,
                        it1
                    )
                } }
            }

        }

    }
}