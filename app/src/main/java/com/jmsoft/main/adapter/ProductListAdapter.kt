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
import com.jmsoft.Utility.database.ProductDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.Utility.UtilityTools.ProductUUIDList
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.currency
import com.jmsoft.basic.UtilityTools.Constants.Companion.weightUnit
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentProductBinding
import com.jmsoft.databinding.ItemProductListBinding
import com.jmsoft.main.activity.DashboardActivity

class ProductListAdapter(
    private val context: Context,
    private var productDataList: ArrayList<ProductDataModel>,
    private val enableCheckBox: Boolean,
    private val fragmentProductBinding: FragmentProductBinding,
    private val selectedProductUUIDList: ArrayList<String>
) :
    RecyclerView.Adapter<ProductListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemProductListBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productDataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productDataList[position], position)
    }

    // Show Product Delete Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showProductDeleteDialog(position: Int, productUUID: String,barcodeImageUri:String) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivImage.setImageResource(R.drawable.img_delete_inventory)
        dialogBinding.tvMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_product_this_action_cannot_be_undone)

        dialogBinding.mcvYes.setOnClickListener {

            dialog.dismiss()

            GetProgressBar.getInstance(context)?.show()

            Utils.deleteImageFromInternalStorage(context,barcodeImageUri)

            val productImages = barcodeImageUri.split(",")

            for (product in productImages) {

                if (product != Constants.Default_Image) {

                    Utils.deleteImageFromInternalStorage(context,product)
                }
            }

            productDataList.removeAt(position)
            Utils.deleteProduct(productUUID)


            Utils.T(context, context.getString(R.string.deleted_successfully))

            notifyDataSetChanged()


            if (productDataList.isEmpty()) {

                GetProgressBar.getInstance(context)?.dismiss()

                fragmentProductBinding.mcvProductList?.visibility = View.GONE
                fragmentProductBinding.llEmptyProduct?.visibility = View.VISIBLE

            }
        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    inner class MyViewHolder(private val binding: ItemProductListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Product Data
        private lateinit var productData: ProductDataModel

        // Product position
        private var position: Int = -1

        // bind method
        fun bind(productData: ProductDataModel, position: Int) {

            this.productData = productData
            this.position = position

            // Checks state of the product list if it is for showing or adding in the collection
            checkState()

            // Set product image
            setProductImage()

            // Set product name
            setProductName()

            // Set product category
            setProductCategory()

            // Set product metal type
            setProductMetalType()

            // Set product origin
            setProductOrigin()

            // Set product weight
            setProductWeight()

            // Set product carat
            setProductCarat()

            // Set product cost
            setProductCost()

            // Set product price
            setProductPrice()

            // Set product stock location
            setStockLocationName()

            // Dismiss progress bar
            dismissProgressBar()

            // Set Click on Delete Icon
            binding.mcvDelete.setOnClickListener(this)

            // Set Click on Edit Icon
            binding.mcvEdit.setOnClickListener(this)


        }


        // Dismiss progress bar
        private fun dismissProgressBar() {

            if (position+1 == productDataList.size ) {

                // Dismiss progress bar
                GetProgressBar.getInstance(context)?.dismiss()

            }

        }

        // Set product image
        private fun setProductImage() {

            val imageArray = productData.productImageUri?.split(",")

            val bitmapImage =
                imageArray?.get(0)?.let { Utils.getImageFromInternalStorage(context, it) }

            binding.ivProduct.setImageBitmap(bitmapImage)
        }

        // Checks state of the product list if it is for showing or adding in the collection
        private fun checkState() {

            if (enableCheckBox) {

                binding.cbProduct.visibility = View.VISIBLE
                binding.mcvDelete.visibility = View.GONE

                if (Utils.SelectedProductUUIDList.getProductList().any { it == productData.productUUID  }) {
                    binding.cbProduct.isChecked = true
                }

                binding.cbProduct.setOnCheckedChangeListener { _, isChecked ->

                    if (isChecked) {

                        productData.productUUID?.let { Utils.SelectedProductUUIDList.addProductUUID(it) }

                    } else {
                        productData.productUUID?.let { Utils.SelectedProductUUIDList.removeProductUUID(it) }
                    }
                }
            }

            else {
                // Set Click on product
                binding.mcvProduct.setOnClickListener(this)
            }
        }

        // set product name
        private fun setProductName() {
            binding.tvProductName.text = productData.productName
        }
        private fun setStockLocationName() {

            val stockLocationDataModel = productData.stockLocationUUID?.let {
                Utils.getStockLocation(
                    it
                )
            }

            binding.tvStockLocation.text = if (stockLocationDataModel?.stockLocationName != null) stockLocationDataModel.stockLocationName else "_"
        }

        // Set product category
        private fun setProductCategory() {

            if (productData.categoryUUID?.isEmpty() == true) {

                binding.tvProductCategory.text = context.getString(R.string.na)

            } else {

                binding.tvProductCategory.text = productData.categoryUUID?.let {
                    Utils.getCategoryNameThroughCategoryUUID(
                        it
                    )
                }
            }
        }

        // Set product metal type
        private fun setProductMetalType() {

            if (productData.metalTypeUUID?.isEmpty() == true) {

                binding.tvMetalType.text = context.getString(R.string.na)

            } else {

                binding.tvMetalType.text = productData.metalTypeUUID?.let {
                    Utils.getMetalTypeNameThroughMetalTypeUUID(
                        it
                    )
                }
            }
        }

        // Set product origin
        private fun setProductOrigin() {
            binding.tvProductOrigin.text = productData.productOrigin
        }

        // Set product weight
        @SuppressLint("SetTextI18n")
        private fun setProductWeight() {
            binding.tvProductWeight.text = "${productData.productWeight.toString()} $weightUnit"
        }

        // Set product carat
        private fun setProductCarat() {
            binding.tvProductCarat.text = productData.productCarat.toString()
        }

        // Set product cost
        @SuppressLint("SetTextI18n")
        private fun setProductCost() {

            binding.tvProductCost.text = "${productData.productCost?.let {
                Utils.getThousandSeparate(
                    it
                )
            }} $currency"

        }

        // Set product price
        @SuppressLint("SetTextI18n")
        private fun setProductPrice() {

            binding.tvProductPrice.text = "${productData.productPrice?.let {
                Utils.getThousandSeparate(
                    it
                )
            }} $currency"
        }

        // Handle All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // When delete button Clicked
            when (v) {

                binding.mcvDelete -> {

                    // Show Category Delete Dialog
                    productData.productUUID?.let { productData.productImageUri?.let { it1 ->
                        productData.productBarcodeUri?.let {
                            showProductDeleteDialog(position, it,
                                it1
                            )
                        }
                    } }
                }

                // When edit button Clicked
                binding.mcvEdit -> {

                    GetProgressBar.getInstance(context)?.show()

                    ProductUUIDList.setStatus(false)

                    val bundle = Bundle()
                    // Giving the product UUID
                    bundle.putString(Constants.productUUID, productData.productUUID)

                    (context as DashboardActivity).navController?.navigate(
                        R.id.productInventory,
                        bundle
                    )

                }
                // Clicked on product
                binding.mcvProduct -> {

                    GetProgressBar.getInstance(context)?.show()

                    val bundle = Bundle()
                    // Giving the product UUID
                    bundle.putString(Constants.productUUID, productData.productUUID)

                    (context as DashboardActivity).navController?.navigate(R.id.productDetail, bundle)

                }
            }
        }
    }

}