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
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.R.color
import com.jmsoft.Utility.Database.AddressDataModel
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentCartBinding
import com.jmsoft.databinding.ItemCardAddressBinding
import com.jmsoft.main.`interface`.AddressSelected

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
    private val addressSelected: AddressSelected
) :
    RecyclerView.Adapter<CartAddressAdapter.MyViewHolder>() {

    //Selected address binding data
    var selectedAddressBinding: ItemCardAddressBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCardAddressBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = addressList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(addressList[position], position)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteDialog(position: Int, addressUUID: String) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_user)
        dialog.findViewById<MaterialCardView>(R.id.mcvYes).setOnClickListener {

            dialog.dismiss()

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
        dialog.findViewById<MaterialCardView>(R.id.mcvNo).setOnClickListener {

            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.show()

    }

    inner class MyViewHolder(private val binding: ItemCardAddressBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Address Data
        private lateinit var addressData: AddressDataModel

        //position of Address Data
        private var position = -1

        fun bind(addressData: AddressDataModel, position: Int) {
            this.addressData = addressData
            this.position = position

            //Setting Address
            setAddress()

            //Setting Click on Delete Button
            binding.ivDelete.setOnClickListener(this)

            //Setting Click on Address Section
            binding.mcvAddress.setOnClickListener(this)

        }

        //Setting Address
        @SuppressLint("SetTextI18n")
        private fun setAddress() {
            binding.tvAddress.text = addressData.address
        }

        // Set Delete Icon Tint
        private fun setIconTint(imageView: ImageView, color: Int) {

            // Apply the ColorFilter to the ImageView
            imageView.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

        }

        //Select the address and change its background color and text
        private fun selectAddress() {

            binding.mcvAddress.setCardBackgroundColor(context.getColor(color.text_color))
            binding.tvAddress.setTextColor(context.getColor(color.white))

            setIconTint(binding.ivDelete, context.getColor(color.white))

            selectedAddressBinding = binding
        }


        //Handles All the Clicks
        override fun onClick(v: View?) {

            // Click on Delete Button
            if (v == binding.ivDelete) {

                //Showing Delete Dialog
                addressData.addressUUID?.let { showDeleteDialog(position, it) }
            }

            //Click on Address Section
            else if (v == binding.mcvAddress) {

                // making the previous selected address normal
                selectedAddressBinding?.mcvAddress?.setCardBackgroundColor(context.getColor(color.mcv_background_color))
                selectedAddressBinding?.tvAddress?.setTextColor(context.getColor(color.text_color))
                selectedAddressBinding?.let {
                    setIconTint(
                        it.ivDelete,
                        context.getColor(color.text_color)
                    )
                }

                //select address
                selectAddress()

                //Callback for Managing if any address is selected or not
                addressSelected.addressSelected()

            }


        }

    }
}