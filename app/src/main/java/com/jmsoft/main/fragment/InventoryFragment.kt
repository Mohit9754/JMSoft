package com.jmsoft.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jmsoft.R
import com.jmsoft.utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.category
import com.jmsoft.basic.UtilityTools.Constants.Companion.collection
import com.jmsoft.basic.UtilityTools.Constants.Companion.metalType
import com.jmsoft.databinding.FragmentInventoryBinding
import com.jmsoft.main.activity.DashboardActivity

class InventoryFragment : Fragment() ,View.OnClickListener {

    private lateinit var binding: FragmentInventoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentInventoryBinding.inflate(layoutInflater)

        //set the Clicks And initialization
        init()

        return binding.root
    }

    //set the Clicks And initialization
    private fun init() {

        // Set Click on Back Button
        binding.mcvBackBtn?.setOnClickListener(this)

        // Set Click on Inventory option
        binding.mcvMetalType?.setOnClickListener(this)

        // Set Click on Collection option
        binding.mcvCollection?.setOnClickListener(this)

        // Set Click on Category option
        binding.mcvCategory?.setOnClickListener(this)

        // Set Click on Product option
        binding.mcvProduct?.setOnClickListener(this)

        binding.mcvStockLocation?.setOnClickListener(this)

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        val bundle = Bundle()

        // When Back button clicked
        when (v) {

            binding.mcvBackBtn -> {
                (requireActivity() as DashboardActivity).navController?.popBackStack()
            }

            // When metal type clicked
            binding.mcvMetalType -> {

                GetProgressBar.getInstance(requireActivity())?.show()

                //Giving the fragment status
                bundle.putString(Constants.state, metalType)

                (requireActivity() as DashboardActivity).navController?.navigate(R.id.metalType,bundle)
            }

            // Clicked on collection
            binding.mcvCollection -> {

                GetProgressBar.getInstance(requireActivity())?.show()

                //Giving the fragment status
                bundle.putString(Constants.state, collection)

                (requireActivity() as DashboardActivity).navController?.navigate(R.id.metalType,bundle)

            }

            // Clicked on Category
            binding.mcvCategory -> {

                GetProgressBar.getInstance(requireActivity())?.show()

                //Giving the fragment status
                bundle.putString(Constants.state, category)

                (requireActivity() as DashboardActivity).navController?.navigate(R.id.metalType,bundle)

            }

            // Clicked on product
            binding.mcvProduct -> {

                // Show progress bar
                GetProgressBar.getInstance(requireActivity())?.show()

                (requireActivity() as DashboardActivity).navController?.navigate(R.id.product)

            }
            binding.mcvStockLocation -> {

                // Show progress bar
                GetProgressBar.getInstance(requireActivity())?.show()

                (requireActivity() as DashboardActivity).navController?.navigate(R.id.stockLocation)

            }
        }

    }

}