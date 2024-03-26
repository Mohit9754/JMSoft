package com.jmsoft.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.jmsoft.R
import com.jmsoft.databinding.FragmentHomeBinding
import com.jmsoft.main.activity.DashboardActivity

/**
 * Home Fragment
 *
 * Showing the List Of Options
 *
 */
class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentHomeBinding.inflate(layoutInflater)

        //set the Clicks And initalize
        init()

        return binding.root
    }

    private fun init() {

        // Set Click on Catalog option
        binding.mcvCatalog?.setOnClickListener(this)
    }

    //Handles All the Clicks

    override fun onClick(v: View?) {

        // Navigate to Catalog Fragment
        if (v == binding.mcvCatalog) {

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.catalog)

        }

    }

}