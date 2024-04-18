package com.jmsoft.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.category
import com.jmsoft.basic.UtilityTools.Constants.Companion.collection
import com.jmsoft.basic.UtilityTools.Constants.Companion.metalType
import com.jmsoft.basic.UtilityTools.Constants.Companion.product
import com.jmsoft.databinding.FragmentHomeBinding
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
    private fun init(){

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

    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        val bundle = Bundle()

        // When Back button clicked
        if (v == binding.mcvBackBtn) {
            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }

        // When metal type clicked
        else if(v == binding.mcvMetalType) {

            //Giving the fragment status
            bundle.putString(Constants.state, metalType)

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.metalType,bundle)
        }

        else if(v == binding.mcvCollection){

            //Giving the fragment status
            bundle.putString(Constants.state, collection)

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.metalType,bundle)

        }

        else if (v == binding.mcvCategory) {

            //Giving the fragment status
            bundle.putString(Constants.state, category)

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.metalType,bundle)

        }

        else if (v == binding.mcvProduct) {

            //Giving the fragment status
            bundle.putString(Constants.state, product)

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.metalType,bundle)

        }

    }

}