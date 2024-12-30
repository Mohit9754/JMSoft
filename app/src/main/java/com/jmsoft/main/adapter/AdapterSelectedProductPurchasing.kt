package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.database.ProductDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentAddPurchaseBinding
import com.jmsoft.databinding.ItemProductListPurchaseBinding

class AdapterSelectedProductPurchasing(
    private val context: Context,
    private val productList: ArrayList<ProductDataModel>,
    private val fragmentAddPurchaseBinding: FragmentAddPurchaseBinding,
    private val isAddStatus: Boolean,
    private val selectedProductUUIDList: ArrayList<String>,
    private val purchaseStatus: String?,
    private val etRFIDCode: (EditText) -> Unit
) :
    RecyclerView.Adapter<AdapterSelectedProductPurchasing.MyViewHolder>() {

    // Price of each product in the cart
    private var cartPrice = ArrayList<Double>()

    private var totalAmount: Double = 0.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            ItemProductListPurchaseBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productList[position], position)
    }

    // delete dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showProductDeleteDialog(
        position: Int,
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
            context.getString(R.string.are_you_sure_you_want_to_delete_this_product_this_action_cannot_be_undone)

        dialogBinding.mcvYes.setOnClickListener {

            dialog.dismiss()

            //Utils.deletePurchase(purchaseUUID)

            for (productImage in productImageUri.split(",")) {

                if (productImage != Constants.Default_Image) {
                    Utils.deleteImageFromInternalStorage(context, productImage)
                }

            }

            removeItem(position)

        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    // Remove item
    @SuppressLint("NotifyDataSetChanged")
    private fun removeItem(position: Int) {

        // Remove the item from the local list
        productList.removeAt(position)

        cartPrice.clear()

        notifyDataSetChanged()

        selectedProductUUIDList.removeAt(position)

        if (productList.isEmpty()) {
            fragmentAddPurchaseBinding.nsvProduct?.visibility = View.GONE
            fragmentAddPurchaseBinding.rlEmpty?.visibility = View.VISIBLE
        }
    }

    // Set Total Price of the cart
    @SuppressLint("SetTextI18n")
    private fun setTotalPrice() {

        var totalPrice = 0.0

        for (price in cartPrice) {
            totalPrice += price
        }

        totalAmount = totalPrice

        fragmentAddPurchaseBinding.tvTotalPrice?.text =
            "${Utils.getThousandSeparate(Utils.roundToTwoDecimalPlaces(totalPrice))} ${Constants.currency}"
    }

    inner class MyViewHolder(private val binding: ItemProductListPurchaseBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var productDataModel: ProductDataModel
        private var position: Int = 0

        // bind method
        fun bind(productDataModel: ProductDataModel, position: Int) {

            this.productDataModel = productDataModel
            this.position = position

            checkPurchaseStatus()

            setProductImage()

            setProductName()

            setRFIDCode()

            setProductWeight()

            setProductPrice()

            if (position + 1 == productList.size) {
                GetProgressBar.getInstance(context)?.dismiss()
                setTotalPrice()
            }

            binding.mcvDelete.setOnClickListener(this)

            binding.mcvEdit.setOnClickListener(this)

            binding.mcvRemove.setOnClickListener(this)

            binding.mcvSave.setOnClickListener(this)

            //binding.mcvCheck.setOnClickListener(this)

            binding.mcvRFID.setOnClickListener(this)

        }

        private fun checkPurchaseStatus() {

            if (purchaseStatus != null && purchaseStatus == Constants.confirm) {
                binding.llEditAndDelete.visibility = View.GONE
                binding.mcvReadOnly.visibility = View.VISIBLE
            }
        }

        private fun setProductName() {
            binding.etProductName.setText(productDataModel.productName)
        }

        private fun setProductWeight() {
            binding.etProductWeight.setText(productDataModel.productWeight.toString())
        }

        private fun setProductPrice() {

            binding.etProductPrice.setText(productDataModel.productPrice?.let {
                Utils.roundToTwoDecimalPlaces(
                    it
                )
            }?.let { Utils.getThousandSeparate(it) })

            // Calculating price of each product for storing in cartPrice array
            productDataModel.productPrice?.let { cartPrice.add(it) }
        }

        private fun setRFIDCode() {
            binding.etRfIdCode.setText(productDataModel.productRFIDCode)
        }

        private fun setProductImage() {

            val imageArray = productDataModel.productImageUri?.split(",")

            val imageBitmap =
                imageArray?.get(0)?.let { Utils.getImageFromInternalStorage(context, it) }

            binding.ivProduct.setImageBitmap(imageBitmap)
        }

        private fun changeEditText(isEnable:Boolean,color:Int) {

            binding.etProductName.isEnabled = isEnable
            binding.etRfIdCode.isEnabled = isEnable
            binding.etProductWeight.isEnabled = isEnable
            binding.etProductPrice.isEnabled = isEnable

            binding.mcvProductName.strokeColor = color
            binding.mcvRFIDCode.strokeColor = color
            binding.mcvWeight.strokeColor = color
            binding.mcvPrice.strokeColor = color
        }

        private fun validate() {

            if (binding.etProductName.text.toString().trim().isNotEmpty()) {

                if (binding.etProductWeight.text.toString().trim().isNotEmpty()){

                    if (binding.etProductPrice.text.toString().trim().isNotEmpty()) {

                        productList[position].productName = binding.etProductName.text.toString().trim()
                        productList[position].productRFIDCode = binding.etRfIdCode.text.toString().trim()
                        productList[position].productWeight = binding.etProductWeight.text.toString().toDouble()
                        productList[position].productPrice = Utils.removeThousandSeparators(binding.etProductPrice.text.toString()).toDouble()

                        cartPrice[position] = Utils.removeThousandSeparators(binding.etProductPrice.text.toString()).toDouble()

                        binding.etProductPrice.setText(productList[position].productPrice?.let {
                            Utils.roundToTwoDecimalPlaces(
                                it
                            )
                        }?.let { Utils.getThousandSeparate(it) })

                        setTotalPrice()

                        changeEditText(false,context.getColor(R.color.white))

                        binding.llEditAndDelete.visibility = View.VISIBLE
                        binding.llSaveAndRemove.visibility = View.GONE

                    }
                    else {
                        Utils.T(context, context.getString(R.string.please_enter_the_product_price))
                    }
                }
                else {
                    Utils.T(context, context.getString(R.string.please_enter_the_product_weight))
                }
            }
            else {
                Utils.T(context, context.getString(R.string.please_enter_the_product_name))
            }

        }

        // Handle All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(view: View?) {

            when (view) {

                binding.mcvDelete -> {

                    if (isAddStatus) {
                        removeItem(position)
                    }
                    else {

                        productDataModel.productImageUri?.let {
                            showProductDeleteDialog(position,
                                it
                            )
                        }
                    }

                }

                binding.mcvEdit -> {

                    changeEditText(true,context.getColor(R.color.text_hint))

                    Utils.setFocusChangeLis(context, binding.etProductName, binding.mcvProductName)
                    Utils.setFocusChangeLis(context, binding.etRfIdCode, binding.mcvRFIDCode)
                    Utils.setFocusChangeLis(context, binding.etProductWeight, binding.mcvWeight)
                    Utils.setFocusChangeLis(context, binding.etProductPrice, binding.mcvPrice)

                    binding.llEditAndDelete.visibility = View.GONE
                    binding.llSaveAndRemove.visibility = View.VISIBLE

                }

                binding.mcvSave -> {
                    validate()
                }

                binding.mcvRemove -> {

                    binding.etProductName.setText(productList[position].productName)
                    binding.etRfIdCode.setText(productList[position].productRFIDCode)
                    binding.etProductWeight.setText(productList[position].productWeight.toString())

                    binding.etProductPrice.setText(productList[position].productPrice?.let {
                        Utils.roundToTwoDecimalPlaces(
                            it
                        )
                    }?.let { Utils.getThousandSeparate(it) })

                    changeEditText(false,context.getColor(R.color.white))

                    binding.llEditAndDelete.visibility = View.VISIBLE
                    binding.llSaveAndRemove.visibility = View.GONE

                }

                binding.mcvRFID -> {
                    etRFIDCode(binding.etRfIdCode)
                }

            }

        }

    }

}