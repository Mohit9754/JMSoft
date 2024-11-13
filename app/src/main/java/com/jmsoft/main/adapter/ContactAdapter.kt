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
import com.jmsoft.Utility.database.ContactDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
import com.jmsoft.databinding.FragmentContactBinding
import com.jmsoft.databinding.ItemCardAddressBinding
import com.jmsoft.main.`interface`.ContactSelectionStatus

class ContactAdapter(
    private val context: Context,
    private val contactList: ArrayList<ContactDataModel>,
    private val fragmentContactBinding: FragmentContactBinding,
    private var selectedContactData: ContactDataModel?,
    private val contactSelectionStatus: ContactSelectionStatus
) :
    RecyclerView.Adapter<ContactAdapter.MyViewHolder>() {

    // Selected contact binding data
    var selectedContactBinding: ItemCardAddressBinding? = null

    // Selected contact position
    var selectedContactPosition = -1

    // Deleted contact position
    var deleteContactPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCardAddressBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = contactList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(contactList[position], position)
    }

    // Delete Address Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteDialog(position: Int, contactUUID: String) {

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivImage.setImageResource(R.drawable.img_delete_address)
        dialogBinding.tvMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_contact_this_action_cannot_be_undone)

        dialogBinding.mcvYes.setOnClickListener {

            dialog.dismiss()

            if (selectedContactPosition == position) {
                unSelectContact()
            }
            else {
                if (position < selectedContactPosition) {

                    selectedContactBinding?.mcvAddress?.setCardBackgroundColor(
                        context.getColor(
                            color.mcv_background_color
                        )
                    )
                    selectedContactBinding?.tvFullName?.setTextColor(context.getColor(color.text_color))

                    selectedContactBinding?.let {
                        setIconTint(
                            it.ivDelete,
                            context.getColor(color.text_color)
                        )
                    }
                    deleteContactPosition = position
                }
            }

            // Deleting the contact
            Utils.deleteContact(contactUUID)

            contactList.removeAt(position)

            // if list is empty then show the no address image
            if (contactList.size == 0) {

                fragmentContactBinding.ivNoContact?.visibility = View.VISIBLE

            } else {

                fragmentContactBinding.ivNoContact?.visibility = View.GONE
            }
            notifyDataSetChanged()
        }

        dialogBinding.mcvNo.setOnClickListener {

            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    // making the previous selected contact normal
    fun unSelectContact() {

        selectedContactBinding?.mcvAddress?.setCardBackgroundColor(
            context.getColor(
                color.mcv_background_color
            )
        )
        selectedContactBinding?.tvFullName?.setTextColor(context.getColor(color.text_color))


        selectedContactBinding?.let {
            setIconTint(
                it.ivDelete,
                context.getColor(color.text_color)
            )
        }

        selectedContactBinding = null
        selectedContactPosition = -1

        contactSelectionStatus.contactUnselected()

    }

    // Set Delete Icon Tint
    private fun setIconTint(imageView: ImageView, color: Int) {

        // Apply the ColorFilter to the ImageView
        imageView.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

    }

    inner class MyViewHolder(private val binding: ItemCardAddressBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Contact Data
        private lateinit var contactData: ContactDataModel

        // position of Contact Data
        private var position = -1

        fun bind(contactData: ContactDataModel, position: Int) {
            this.contactData = contactData
            this.position = position

            // After the notify Data set Change it select the previous selected contact
            selectPreviousSelectedContact()

            // Select new added contact
            selectNewAddedContact()

            // Setting full name
            setFullName()

            // Setting Click on Delete Button
            binding.ivDelete.setOnClickListener(this)

            // Setting Click on Contact Section
            binding.mcvAddress.setOnClickListener(this)

            if (position+1 == contactList.size) {
                GetProgressBar.getInstance(context)?.dismiss()
            }
        }

        // Select new added contact
        private fun selectNewAddedContact() {

            if (selectedContactData != null) {

                if (contactData.contactUUID == selectedContactData!!.contactUUID) {

                    selectContact()
                    contactSelectionStatus.contactSelected(contactData)
                    selectedContactData = null

                }
            }
        }

        // After the notify Data set Change it select the previous selected contact
        private fun selectPreviousSelectedContact() {

            if (selectedContactBinding != null && selectedContactPosition != -1 && deleteContactPosition != -1) {

                if (deleteContactPosition < selectedContactPosition) {

                    if (selectedContactPosition - 1 == position) {
                        selectContact()
                        deleteContactPosition = -1
                    }
                }
            }
        }

        // Setting Address
        @SuppressLint("SetTextI18n")
        private fun setFullName() {
            binding.tvFullName.text = "${contactData.firstName} ${contactData.lastName}"
        }

        // Select the contact and change its background color and text
        private fun selectContact() {

            binding.mcvAddress.setCardBackgroundColor(context.getColor(color.text_color))
            binding.tvFullName.setTextColor(context.getColor(color.white))
            setIconTint(binding.ivDelete, context.getColor(color.white))

            selectedContactBinding = binding
            selectedContactPosition = position
        }


        // Handles All the Clicks
        override fun onClick(v: View?) {

            // Click on Delete Button
            if (v == binding.ivDelete) {

                // Showing Delete Dialog
                contactData.contactUUID?.let { showDeleteDialog(position, it) }
            }

            // Click on Contact Section
            else if (v == binding.mcvAddress) {

                if (selectedContactBinding == binding) {

                    // making the previous selected contact normal
                    unSelectContact()

                    selectedContactBinding = null

                } else {

                    // making the previous selected contact normal
                    unSelectContact()

                    // select contact
                    selectContact()

                    // Callback for Managing if any contact is selected or not
                    contactSelectionStatus.contactSelected(contactData)
                }
            }
        }
    }
}