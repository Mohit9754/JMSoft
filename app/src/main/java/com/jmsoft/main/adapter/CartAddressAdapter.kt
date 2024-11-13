package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.R.color
import com.jmsoft.Utility.database.AddressDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentCartBinding
import com.jmsoft.databinding.ItemCardAddressBinding
import com.jmsoft.main.`interface`.AddressSelectionStatus

/**
 * Cart Address Adapter
 *
 *
 *
 */

class CartAddressAdapter(
    private val context: Context,
    private val addressList: ArrayList<AddressDataModel>,
    private val fragmentCardBinding: FragmentCartBinding,
    private var selectedAddressData: AddressDataModel?,
    private val addressSelectionStatus: AddressSelectionStatus
) :
    RecyclerView.Adapter<CartAddressAdapter.MyViewHolder>() {

    // Selected address binding data
    var selectedAddressBinding: ItemCardAddressBinding? = null

    // Selected address position
    var selectedAddressPosition = -1

    // Deleted address position
    var deleteAddressPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCardAddressBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = addressList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(addressList[position], position)
    }

    // Delete Address Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteDialog(position: Int, addressUUID: String) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivImage.setImageResource(R.drawable.img_delete_address)
        dialogBinding.tvMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_address_this_action_cannot_be_undone)

        dialogBinding.mcvYes.setOnClickListener {

            dialog.dismiss()

            if (selectedAddressPosition == position) {
                unSelectAddress()
            }
            else {
                if (position < selectedAddressPosition) {

                    selectedAddressBinding?.mcvAddress?.setCardBackgroundColor(
                        context.getColor(
                            color.mcv_background_color
                        )
                    )
                    selectedAddressBinding?.tvFullName?.setTextColor(context.getColor(color.text_color))


                    selectedAddressBinding?.let {
                        setIconTint(
                            it.ivDelete,
                            context.getColor(color.text_color)
                        )
                    }
                    deleteAddressPosition = position
                }

            }

            //Deleting the address
            Utils.deleteAddress(addressUUID)

            addressList.removeAt(position)

            // if list is empty then show the no address image
            if (addressList.size == 0) {

                fragmentCardBinding.ivNoAddress?.visibility = View.VISIBLE

            } else {

                fragmentCardBinding.ivNoAddress?.visibility = View.GONE
            }
            notifyDataSetChanged()
        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    // making the previous selected address normal
    fun unSelectAddress() {

        selectedAddressBinding?.mcvAddress?.setCardBackgroundColor(
            context.getColor(
                color.mcv_background_color
            )
        )
        selectedAddressBinding?.tvFullName?.setTextColor(context.getColor(color.text_color))


        selectedAddressBinding?.let {
            setIconTint(
                it.ivDelete,
                context.getColor(color.text_color)
            )
        }

        selectedAddressBinding = null
        selectedAddressPosition = -1

        addressSelectionStatus.addressUnselected()

    }

    // Set Delete Icon Tint
    private fun setIconTint(imageView: ImageView, color: Int) {

        // Apply the ColorFilter to the ImageView
        imageView.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

    }

    inner class MyViewHolder(private val binding: ItemCardAddressBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Address Data
        private lateinit var addressData: AddressDataModel

        // position of Address Data
        private var position = -1

        fun bind(addressData: AddressDataModel, position: Int) {
            this.addressData = addressData
            this.position = position

            // After the notify Data set Change it select the previous selected address
            selectPreviousSelectedAddress()

            // Select new added address
            selectNewAddedAddress()

            // Setting full name
            setFullName()

            // Setting Click on Delete Button
            binding.ivDelete.setOnClickListener(this)

            // Setting Click on Address Section
            binding.mcvAddress.setOnClickListener(this)

            if (position+1 == addressList.size) {
                GetProgressBar.getInstance(context)?.dismiss()
            }
        }

        // Select new added address
        private fun selectNewAddedAddress() {

            if (selectedAddressData != null) {

                if (addressData.addressUUID == selectedAddressData!!.addressUUID) {
                    selectAddress()
                    addressSelectionStatus.addressSelected(addressData)
                    selectedAddressData = null

                }
            }
        }

        // After the notify Data set Change it select the previous selected address
        private fun selectPreviousSelectedAddress() {

            if (selectedAddressBinding != null && selectedAddressPosition != -1 && deleteAddressPosition != -1) {

                if (deleteAddressPosition < selectedAddressPosition) {

                    if (selectedAddressPosition - 1 == position) {
                        selectAddress()
                        deleteAddressPosition = -1
                    }
                }
            }
        }

        // Setting Address
        @SuppressLint("SetTextI18n")
        private fun setFullName() {
            binding.tvFullName.text = "${addressData.firstName} ${addressData.lastName}"
        }


        // Select the address and change its background color and text
        private fun selectAddress() {

            binding.mcvAddress.setCardBackgroundColor(context.getColor(color.text_color))
            binding.tvFullName.setTextColor(context.getColor(color.white))
            setIconTint(binding.ivDelete, context.getColor(color.white))

            selectedAddressBinding = binding
            selectedAddressPosition = position
        }


        // Handles All the Clicks
        override fun onClick(v: View?) {

            // Click on Delete Button
            if (v == binding.ivDelete) {

                //Showing Delete Dialog
                addressData.addressUUID?.let { showDeleteDialog(position, it) }
            }

            // Click on Address Section
            else if (v == binding.mcvAddress) {

                if (selectedAddressBinding == binding) {

                    // making the previous selected address normal
                    unSelectAddress()

                    selectedAddressBinding = null

                } else {

                    // making the previous selected address normal
                    unSelectAddress()

                    //select address
                    selectAddress()

                    //Callback for Managing if any address is selected or not
                    addressSelectionStatus.addressSelected(addressData)
                }

            }
        }
    }
}