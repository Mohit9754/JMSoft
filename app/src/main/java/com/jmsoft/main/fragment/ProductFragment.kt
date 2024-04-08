package com.jmsoft.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.databinding.FragmentProductBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CatalogAdapter
import com.jmsoft.main.adapter.CollectionItemAdapter
import com.jmsoft.main.adapter.ProductImageAdapter

class ProductFragment : Fragment(),View.OnClickListener {

    private lateinit var binding:FragmentProductBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentProductBinding.inflate(layoutInflater)

        // set the Clicks , initialization And Setup
        init()

        return binding.root
    }

    // Setup Product Image Recycler View
    private fun setUpProductImageRecyclerView(){

        val arr = ArrayList<Int>()
        arr.add(R.drawable.icon_scanner)
        arr.add(R.drawable.icon_tag_printer)
        arr.add(R.drawable.icon_ticket_printer)
        arr.add(R.drawable.img_ring)

        binding.ivProduct?.setImageResource(arr[0])

        val adapter = binding.ivProduct?.let { binding.llLeftBtn?.let { it1 ->
            binding.llRightBtn?.let { it2 ->
                ProductImageAdapter(requireActivity(), arr, it,
                    it1, it2
                )
            }
        } }

        binding.rvProductImage?.layoutManager = LinearLayoutManager(requireActivity(),RecyclerView.VERTICAL,false)
        binding.rvProductImage?.adapter = adapter

    }

    // Set up collection Recycler view
    private fun setUpCollectionItemRecyclerView(){

        val adapter = CollectionItemAdapter(requireActivity(), arrayListOf())

        binding.rvCollection?.layoutManager = LinearLayoutManager(requireActivity(),RecyclerView.HORIZONTAL,false)
        binding.rvCollection?.adapter = adapter

    }

    // Setting the May also like RecyclerView
    private fun setUpMayLikeRecyclerView() {

        val catalogAdapter = CatalogAdapter(requireActivity(), arrayListOf("","","","","","","","",""))

        binding.rvCatalog?.layoutManager = GridLayoutManager(requireActivity(), 3) // Span Count is set to 3
        binding.rvCatalog?.adapter = catalogAdapter
    }

    // Set the Clicks , initialization And Setup
    private fun init(){

        // Setup Product Image Recycler View
        setUpProductImageRecyclerView()

        // Set up collection Recycler view
        setUpCollectionItemRecyclerView()

        // Setting the May also like RecyclerView
        setUpMayLikeRecyclerView()

        //Set Click on Add to Card button
        binding.llAddToCard?.setOnClickListener(this)

    }

    // Handle all the clicks
    override fun onClick(v: View?) {

        //Set Click on Add to Card button
        if (v == binding.llAddToCard) {

        }

    }
}