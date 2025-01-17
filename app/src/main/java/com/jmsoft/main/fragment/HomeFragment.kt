package com.jmsoft.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.GetProgressBar
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

        // Set Click on Sales option
        binding.mcvPurchasingAndSales?.setOnClickListener(this)

        // Set Click on Inventory option
        binding.mcvInventory?.setOnClickListener(this)

        // Set Click on Audit option
        binding.mcvAudit?.setOnClickListener(this)


        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        // Navigate to Catalog Fragment
        when (v) {

            binding.mcvCatalog -> {

                // Show progress bar
                if (isAdded && !isRemoving && requireActivity().isFinishing.not() && requireActivity().isDestroyed.not()) {
                    GetProgressBar.getInstance(requireActivity())?.show()
                }


                (requireActivity() as DashboardActivity).navController?.navigate(R.id.catalog)
            }

            // Navigate to Setting Fragment
            binding.mcvSettings -> {
                (requireActivity() as DashboardActivity).navController?.navigate(R.id.setting)
            }
            binding.mcvPurchasingAndSales -> {

                GetProgressBar.getInstance(requireActivity())?.show()

                (requireActivity() as DashboardActivity).navController?.navigate(R.id.purchasingAndSales)

            }

            // Navigate to Inventory Fragment
            binding.mcvInventory -> {

                (requireActivity() as DashboardActivity).navController?.navigate(R.id.inventory)

            }
            binding.mcvAudit -> {

                GetProgressBar.getInstance(requireActivity())?.show()
                (requireActivity() as DashboardActivity).navController?.navigate(R.id.audit)

            }
        }
    }

}