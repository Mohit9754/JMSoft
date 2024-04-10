package com.jmsoft.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentCatalogBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CatalogAdapter

/**
 * Catalog Fragment
 *
 * Showing the Catalog Details
 *
 */
class CatalogFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentCatalogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCatalogBinding.inflate(layoutInflater)

        //set the Clicks And initialization
        init()

        (requireActivity() as DashboardActivity).mcvSearch?.visibility = View.VISIBLE


        return binding.root
    }

    // Setting the RecyclerView
    private fun setRecyclerView() {

        val productList = Utils.getAllProducts()

        val catalogAdapter = CatalogAdapter(requireActivity(), productList)

        binding.rvCatalog?.layoutManager = GridLayoutManager(requireActivity(), 3) // Span Count is set to 3
        binding.rvCatalog?.adapter = catalogAdapter
    }

    private fun init() {

        //Sets the Recycler View
        setRecyclerView()
    }

    //Handles All the Clicks

    override fun onClick(v: View?) {

    }

}