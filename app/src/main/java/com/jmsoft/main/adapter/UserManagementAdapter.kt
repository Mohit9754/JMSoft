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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.database.UserDataModel
import com.jmsoft.basic.UtilityTools.Constants.Companion.Default_Country_Code
import com.jmsoft.basic.UtilityTools.Constants.Companion.userUUID
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemUserManagementBinding
import com.jmsoft.main.activity.DashboardActivity

class UserManagementAdapter(
    private val context: Context,
    private val userList: ArrayList<UserDataModel>,
    private val ivNoUser: ImageView
) :
    RecyclerView.Adapter<UserManagementAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemUserManagementBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = userList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(userList[position],position)
    }

    // Show delete user dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteUserDialog(userUUID:String,position: Int){

        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_user)
        dialog.findViewById<MaterialCardView>(R.id.mcvYes).setOnClickListener {

            dialog.dismiss()
            // Deleting the user from the user Table
            Utils.deleteUserThroughUserUUID(userUUID)
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

    inner class MyViewHolder(private val binding: ItemUserManagementBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var userDataModel: UserDataModel
        private var position = -1

        fun bind(userDataModel: UserDataModel, position: Int) {
            this.userDataModel = userDataModel
            this.position = position

            //Setting Full Name of the User
            setFullName()

            //Setting Email of the User
            setEmail()

            //Setting Phone Number of the User
            setPhoneNumber()

            //Setting Click on Delete Button
            binding.ivDelete.setOnClickListener(this)

            //Setting Click on EditProfile Button
            binding.ivEditProfile.setOnClickListener(this)

        }

        //Setting Full Name of the User
        @SuppressLint("SetTextI18n")
        private fun setFullName(){
            binding.tvUserName.text = "${userDataModel.firstName} ${userDataModel.lastName}"
        }

        //Setting Email of the User
        private fun setEmail(){
            binding.tvEmail.text = userDataModel.email
        }

        //Setting Phone Number of the User
        @SuppressLint("SetTextI18n")
        private fun setPhoneNumber(){
            binding.tvPhoneNumber.text = "$Default_Country_Code ${userDataModel.phoneNumber}"
        }

        //Handles All the Clicks
        override fun onClick(v: View?) {

            // Click on Delete Button
            if (v == binding.ivDelete) {

                // Show delete user dialog
                userDataModel.userUUID?.let { showDeleteUserDialog(it,position) }
            }

            //Click on Edit Profile Button
            else if(v == binding.ivEditProfile){

                val bundle = Bundle()

                //Giving the userUUID
                bundle.putString(userUUID, userDataModel.userUUID)

                //Navigate to Edit Profile
                (context as DashboardActivity).navController?.navigate(R.id.editProfile,bundle)
            }

        }

    }
}