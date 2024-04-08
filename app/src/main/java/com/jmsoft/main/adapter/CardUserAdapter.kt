package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.R.color
import com.jmsoft.basic.Database.UserDataModel
import com.jmsoft.basic.UtilityTools.Constants.Companion.Default_Country_Code
import com.jmsoft.basic.UtilityTools.Constants.Companion.Default_Country_Region
import com.jmsoft.basic.UtilityTools.Constants.Companion.defaultCoordinates
import com.jmsoft.basic.UtilityTools.Constants.Companion.userUUID
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemCardUserBinding
import com.jmsoft.databinding.ItemUserManagementBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.fragment.UserManagementFragment


/**
 * User list Adapter
 *
 *
 *
 */

class CardUserAdapter(
    private val context: Context,
    private val userList: ArrayList<UserDataModel>,
    private val ivNoUser: ImageView
) :
    RecyclerView.Adapter<CardUserAdapter.MyViewHolder>() {

    var selectedUserBinding:ItemCardUserBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCardUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = userList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(userList[position],position)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteDialog(position: Int){

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_user)
        dialog.findViewById<MaterialCardView>(R.id.mcvYes).setOnClickListener {

            dialog.dismiss()
            // Deleting the user from the user Table
//            Utils.deleteUserThroughUserUUID(userUUID)
            userList.removeAt(position)
            if (userList.size == 0) {
                ivNoUser.visibility = View.VISIBLE
            }else{
                ivNoUser.visibility = View.GONE
            }
            notifyDataSetChanged()
        }
        dialog.findViewById<MaterialCardView>(R.id.mcvNo).setOnClickListener {

            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.show()

    }

    inner class MyViewHolder(private val binding: ItemCardUserBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var userData: UserDataModel
        private var position = -1

        fun bind(userDataModel: UserDataModel,position: Int) {
            this.userData = userDataModel
            this.position = position

            //Setting Full Name of the User
            setFullName()

            //Setting Click on Delete Button
            binding.ivDelete.setOnClickListener(this)

            //Setting Click on User Section
            binding.mcvUser.setOnClickListener(this)

        }

        //Setting Full Name of the User
        @SuppressLint("SetTextI18n")
        private fun setFullName(){
            binding.tvUserName.text = "${userData.firstName} ${userData.lastName}"
        }

        private fun setIconTint(imageView: ImageView,color: Int){

            // Apply the ColorFilter to the ImageView
            imageView.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

        }


        //Handles All the Clicks
        override fun onClick(v: View?) {

            // Click on Delete Button
            if (v == binding.ivDelete) {

                //Showing Delete Dialog
                showDeleteDialog(position)
            }

            else if (v == binding.mcvUser) {

                selectedUserBinding?.mcvUser?.setCardBackgroundColor(context.getColor(color.mcv_background_color))
                selectedUserBinding?.tvUserName?.setTextColor(context.getColor(color.text_color))

                selectedUserBinding?.let { setIconTint(it.ivDelete,context.getColor(color.text_color)) }

                binding.mcvUser.setCardBackgroundColor(context.getColor(color.text_color))
                binding.tvUserName.setTextColor(context.getColor(color.white))

                setIconTint(binding.ivDelete,context.getColor(color.white))

                selectedUserBinding = binding

            }

        }

    }
}