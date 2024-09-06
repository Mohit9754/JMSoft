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
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.Database.PurchasingDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.Utility.UtilityTools.ProductUUIDList
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentPurchasingBinding
import com.jmsoft.databinding.ItemProductListBinding
import com.jmsoft.databinding.ItemPurchasingBinding
import com.jmsoft.main.activity.DashboardActivity

class AdapterPurchasing(
    private val context: Context,
    private val purchasingList: ArrayList<PurchasingDataModel>,
    private val fragmentPurchasingBinding: FragmentPurchasingBinding
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

    @SuppressLint("NotifyDataSetChanged")
    private fun showPurchaseDeleteDialog(position: Int,purchaseUUID: String) {

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

    inner class MyViewHolder(private val binding: ItemPurchasingBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var purchasingDataModel: PurchasingDataModel
        private var productDataModel: ProductDataModel? = null
        private var position: Int = 0

        // bind method
        fun bind(purchasingDataModel: PurchasingDataModel, position: Int) {

            this.purchasingDataModel = purchasingDataModel
            this.position = position

            val productDataModel =
                purchasingDataModel.productUUID?.let { Utils.getProductThroughProductUUID(it) }

            if (productDataModel != null) this.productDataModel = productDataModel

            setProductImage()

            setProductName()

            setOrderNumber()

            setSupplier()

            setDate()

            setTotalAmount()

            if (position+1 == purchasingList.size )
                GetProgressBar.getInstance(context)?.dismiss()

            binding.mcvDelete.setOnClickListener(this)

            binding.mcvEdit.setOnClickListener(this)

        }

        private fun setTotalAmount() {
            binding.tvTotalAmount.text = purchasingDataModel.totalAmount?.let { Utils.getThousandSeparate(it) }
        }

        private fun setDate() {
            binding.tvDate.text = purchasingDataModel.date
        }

        private fun setSupplier() {
            binding.tvSupplier.text = purchasingDataModel.supplier
        }

        private fun setOrderNumber() {
            binding.tvOrderNo.text = purchasingDataModel.orderNo
        }

        private fun setProductName() {

            binding.tvProductName.text = productDataModel?.productName
        }

        private fun setProductImage() {

            val imageArray = productDataModel?.productImageUri?.split(",")

            val bitmapImage =
                imageArray?.get(0)?.let { Utils.getImageFromInternalStorage(context, it) }

            binding.ivProduct.setImageBitmap(bitmapImage)
        }


        // Handle All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(view: View?) {

            when(view) {

                binding.mcvDelete -> {

                    purchasingDataModel.purchasingUUID?.let {
                        showPurchaseDeleteDialog(position,
                            it
                        )
                    }

                }

                binding.mcvEdit -> {

//                    GetProgressBar.getInstance(context)?.show()

                    val bundle = Bundle()
                    // Giving the product UUID
                    bundle.putString(Constants.purchasingUUID, purchasingDataModel.purchasingUUID)

                    (context as DashboardActivity).navController?.navigate(
                        R.id.addPurchasing,
                        bundle
                    )


                }

            }



        }

    }

}