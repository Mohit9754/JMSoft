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
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.utility.database.ProductDataModel
import com.jmsoft.utility.database.PurchasingDataModel
import com.jmsoft.utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentPurchasingBinding
import com.jmsoft.databinding.ItemImagesBinding
import com.jmsoft.databinding.ItemPurchasingBinding
import com.jmsoft.main.activity.DashboardActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AdapterPurchasing(
    private val context: Context,
    private val purchasingList: ArrayList<PurchasingDataModel>,
    private val fragmentPurchasingBinding: FragmentPurchasingBinding,
    private val lifecycleScope: LifecycleCoroutineScope
) :
    RecyclerView.Adapter<AdapterPurchasing.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemPurchasingBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = purchasingList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(purchasingList[position], position)
    }

    // Delete dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showPurchaseDeleteDialog(
        position: Int,
        purchaseUUID: String,
        productImageUri: String
    ) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivImage.setImageResource(R.drawable.img_delete_inventory)
        dialogBinding.tvMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_purchase_this_action_cannot_be_undone)

        dialogBinding.mcvYes.setOnClickListener {

            dialog.dismiss()

            Utils.deletePurchase(purchaseUUID)

            for (productImage in productImageUri.split(",")) {

                if (productImage != Constants.Default_Image) {
                    Utils.deleteImageFromInternalStorage(context,productImage)
                }

            }

            // Remove the item from the local list
            purchasingList.removeAt(position)

            // Notify the adapter about the removed item
            notifyItemRemoved(position)

            // Notify the adapter about the range of changed items
            notifyItemRangeChanged(position, purchasingList.size - position)

            if (purchasingList.isEmpty()) {

                fragmentPurchasingBinding.mcvPurchasingList?.visibility = View.GONE
                fragmentPurchasingBinding.llEmptyPurchasing?.visibility = View.VISIBLE

            }
        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    // set image
    fun setImage(productDataModel: ProductDataModel,imageView:ImageView) {

        val imageUri = productDataModel.productImageUri

        val imageBitmap = imageUri?.let { Utils.getImageFromInternalStorage(context, it) }

        imageView.setImageBitmap(imageBitmap)
    }

    inner class ImageViewHolder(val binding: ItemImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int, productDataModel: ProductDataModel, size: Int) {

            if (size <= 3) {
                setImage(productDataModel,binding.ivProduct)
            }
            else {

                if (position == 2) {
                    binding.tvMore.text = "+${size-2}"
                }

                else {
                    setImage(productDataModel,binding.ivProduct)
                }
            }
        }
    }

    inner class MyViewHolder(private val binding: ItemPurchasingBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var purchasingDataModel: PurchasingDataModel
        private var productDataModelList = ArrayList<ProductDataModel>()
        private var position: Int = 0

        // bind method
        fun bind(purchasingDataModel: PurchasingDataModel, position: Int) {

            this.purchasingDataModel = purchasingDataModel
            this.position = position

            if (purchasingDataModel.productWeights != null && purchasingDataModel.productRFIDs != null && purchasingDataModel.productUUIDs != null && purchasingDataModel.productImageUri != null && purchasingDataModel.productNames != null && purchasingDataModel.productPrices != null) {

                val productImageList = purchasingDataModel.productImageUri?.split(",")?: listOf()
                val productNameList = purchasingDataModel.productNames?.split(",")?: listOf()
                val productPriceList = purchasingDataModel.productPrices?.split(",")?: listOf()
                val productUUIDList = purchasingDataModel.productUUIDs?.split(",")?: listOf()
                val productRFIDList = purchasingDataModel.productRFIDs?.split(",")?: listOf()
                val productWeightList = purchasingDataModel.productWeights?.split(",")?: listOf()

                for (i in productImageList.indices) {

                    val productDataModel = ProductDataModel()

                    productDataModel.productUUID = productUUIDList[i]
                    productDataModel.productImageUri = productImageList[i]
                    productDataModel.productName = productNameList[i]
                    productDataModel.productPrice = productPriceList[i].toDouble()
                    productDataModel.productRFIDCode = productRFIDList[i]
                    productDataModel.productWeight = productWeightList[i].toDouble()

                    productDataModelList.add(productDataModel)

                }
            }

            checkPurchasingStatus()

            setProductImageRecyclerView()

            setProductRecyclerView()

            setOrderNumber()

            setSupplier()

            setDate()

            setTotalAmount()

            if (position + 1 == purchasingList.size)
                GetProgressBar.getInstance(context)?.dismiss()

            binding.mcvDelete.setOnClickListener(this)

            binding.mcvEdit.setOnClickListener(this)

            binding.ivDropDown.setOnClickListener(this)

            binding.mcvCheck.setOnClickListener(this)

        }

        private fun checkPurchasingStatus() {

            if (purchasingDataModel.purchaseStatus == Constants.confirm) {

                binding.mcvDelete.visibility = View.GONE
                binding.mcvCheck.visibility = View.GONE
            }
            else {
                binding.mcvDelete.visibility = View.VISIBLE
                binding.mcvCheck.visibility = View.VISIBLE
            }
        }

        private fun setProductImageRecyclerView() {

            binding.rvImage.layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

            binding.rvImage.adapter = object : RecyclerView.Adapter<ImageViewHolder>() {

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
                    val view = ItemImagesBinding.inflate(LayoutInflater.from(context), parent, false)
                    return ImageViewHolder(view)
                }

                override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
                    holder.bind(position,productDataModelList[position],productDataModelList.size)
                }

                override fun getItemCount(): Int {
                    return if (productDataModelList.size > 3) 3 else productDataModelList.size
                }
            }
        }

        private fun setTotalAmount() {
            binding.tvTotalAmount.text =
                purchasingDataModel.totalAmount?.let { Utils.getThousandSeparate(it) }
        }

        private fun setDate() {
            binding.tvDate.text = purchasingDataModel.date
        }

        @SuppressLint("SetTextI18n")
        private fun setSupplier() {

            val contactDataModel = purchasingDataModel.supplierUUID?.let {
                Utils.getContactByUUID(
                    it
                )
            }

            binding.tvSupplier.text = "${contactDataModel?.firstName} ${contactDataModel?.lastName}"
        }

        private fun setOrderNumber() {
            binding.tvOrderNo.text = purchasingDataModel.orderNo
        }

        private fun setProductRecyclerView() {
            binding.rvProduct.layoutManager = GridLayoutManager(context, 4) // Span Count is set to 4
            binding.rvProduct.adapter = AdapterProductPurchasing(context,productDataModelList)

        }

        private suspend fun updateProductDetails() {

            GetProgressBar.getInstance(context)?.show()

            val result = lifecycleScope.async(Dispatchers.IO) {

                for(productDataModel in productDataModelList) {
                    Utils.updateProductDetails(productDataModel)
                }

                purchasingDataModel.purchasingUUID?.let { Utils.updatePurchaseStatus(it) }

                return@async true
            }

            result.await()

            GetProgressBar.getInstance(context)?.dismiss()

            Utils.T(context, context.getString(R.string.product_updated_successfully))

            binding.mcvCheck.visibility = View.GONE
            binding.mcvDelete.visibility = View.GONE
        }

        // Handle All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(view: View?) {

            when (view) {

                binding.mcvDelete -> {

                    purchasingDataModel.purchasingUUID?.let {
                        purchasingDataModel.productImageUri?.let { it1 ->
                            showPurchaseDeleteDialog(
                                position,
                                it, it1
                            )
                        }
                    }

                }

                binding.mcvEdit -> {

                    GetProgressBar.getInstance(context)?.show()

                    val bundle = Bundle()

                    // Giving the product UUID
                    bundle.putString(Constants.purchasingUUID, purchasingDataModel.purchasingUUID)

                    (context as DashboardActivity).navController?.navigate(
                        R.id.addPurchasing,
                        bundle
                    )
                }

                binding.ivDropDown -> {

                    if (binding.rvProduct.visibility == View.GONE) {

                        Utils.expandView(binding.rvProduct)
                    }
                    else {

                        Utils.collapseView(binding.rvProduct)

                    }
                }

                binding.mcvCheck -> {

                    lifecycleScope.launch {
                        updateProductDetails()
                    }
                }

            }

        }

    }

}