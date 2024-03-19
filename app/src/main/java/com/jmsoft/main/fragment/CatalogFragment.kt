package com.jmsoft.main.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.databinding.FragmentCatalogBinding
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

        //set the Clicks And initalize
        init()

        return binding.root
    }

    // Setting the RecyclerView
    private fun setRecyclerView() {

        val catalogAdapter = CatalogAdapter(requireActivity(), arrayListOf(""))

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