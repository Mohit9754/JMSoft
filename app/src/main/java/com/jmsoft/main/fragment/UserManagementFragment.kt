package com.jmsoft.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentUserManagementBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.UserManagementAdapter

class UserManagementFragment : Fragment(),View.OnClickListener {

    lateinit var binding:FragmentUserManagementBinding
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentUserManagementBinding.inflate(layoutInflater)

        // set the Clicks , initialization And Setup
        init()

        return binding.root
    }

    // set the Recycler View of User List
    private fun setRecyclerView(){

        // getting All User Details Accept Admin

        val userList = Utils.getAllUserDetails()

        if (userList.size != 0) {
            binding.ivNoUser?.visibility = View.GONE

            val userManagementAdapter =
                binding.ivNoUser?.let { UserManagementAdapter(requireActivity(), userList, it) }

            binding.rvUserList?.layoutManager = GridLayoutManager(requireActivity(), 2) // Span Count is set to 3
            binding.rvUserList?.adapter = userManagementAdapter

        }
        // if no user
        else{
            binding.ivNoUser?.visibility = View.VISIBLE
        }
    }

    // set the Clicks , initialization And Setup
    private fun init(){

        // set the Recycler View of User List
        setRecyclerView()

        // set Click on add user btn
        binding.mcvAddUser?.setOnClickListener(this)

        // set Click on Back Btn
        binding.mcvBackBtn?.setOnClickListener(this)

    }

    // Handles All the Clicks
    override fun onClick(v: View?) {

        // Click on Add user button
        if (v == binding.mcvAddUser) {

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.editProfile)
        }

        // Click on Back button
        else if(v == binding.mcvBackBtn) {

            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }

    }
}