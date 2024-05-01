package com.jmsoft.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.verification
import com.jmsoft.databinding.FragmentHomeBinding
import com.jmsoft.main.activity.DashboardActivity

class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentHomeBinding.inflate(layoutInflater)

        // Hide the Search option
        (requireActivity() as DashboardActivity).binding?.mcvSearch?.visibility = View.INVISIBLE

        // Set current sate to verification
        (requireActivity() as DashboardActivity).currentState = verification

        //set the Clicks And initialization
        init()

        return binding.root
    }

    //set the Clicks And initialization
    private fun init() {

        // Set Click on Catalog option
        binding.mcvCatalog?.setOnClickListener(this)

        // Set Click on Settings option
        binding.mcvSettings?.setOnClickListener(this)

        // Set Click on Purchasing option
        binding.mcvPurchasing?.setOnClickListener(this)

        // Set Click on Inventory option
        binding.mcvInventory?.setOnClickListener(this)

    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        // Navigate to Catalog Fragment
        if (v == binding.mcvCatalog) {

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.catalog)
        }

        // Navigate to Setting Fragment
        else if (v == binding.mcvSettings) {
            (requireActivity() as DashboardActivity).navController?.navigate(R.id.setting)
        }

        // Navigate to Cart Fragment
        else if (v == binding.mcvPurchasing) {

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.cart)

        }

        // Navigate to Inventory Fragment
        else if (v == binding.mcvInventory) {

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.inventory)

        }

    }

}