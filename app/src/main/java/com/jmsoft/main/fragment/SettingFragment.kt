package com.jmsoft.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentSettingBinding
import com.jmsoft.main.activity.LoginActivity

/**
 * Setting Fragment
 *
 * Showing the Vendors and Customers option
 * Showing the Device Management option
 * Shows profile picture ,name And also edit it
 * Removing the Session when logOut button clicked
 *
 */
class SettingFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentSettingBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentSettingBinding.inflate(layoutInflater)

        //set the Clicks And initalize
        init()

        return binding.root
    }

    private fun init() {

        //Set Click on Device Management Option
        binding.mcvDeviceManagement?.setOnClickListener(this)

        //Set Click on LogOut Option
        binding.mcvLogOut?.setOnClickListener(this)

    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        //Navigate to Device Management Fragment
        if (v == binding.mcvDeviceManagement) {

            val navController = findNavController()
            navController.navigate(R.id.deviceManagement)
        }
        // When LogOut button Clicked
        else if (v == binding.mcvLogOut) {

            // Remove the session
            Utils.LOGOUT()
            //intent to login activity with Clear back stack
            Utils.I_clear(requireActivity(), LoginActivity::class.java, null)
        }

    }
}