package com.jmsoft.main.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentProductInventoryBinding
import com.jmsoft.main.adapter.CategoryListAdapter
import com.jmsoft.main.adapter.CollectionListAdapter
import com.jmsoft.main.adapter.MetalTypeListAdapter
import com.jmsoft.main.adapter.SelectedCollectionAdapter
import com.jmsoft.main.`interface`.CollectionStatusCallback

class ProductInventoryFragment : Fragment(),View.OnClickListener {

    private lateinit var binding:FragmentProductInventoryBinding

    private var selectedCollectionList  = ArrayList<String>()

    private var selectedCollectionAdapter:SelectedCollectionAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentProductInventoryBinding.inflate(layoutInflater)

        //set the Clicks And initialization
        init()

        return binding.root
    }

    private fun setMetalTypeRecyclerView(){

        val metalTypeAdapter = MetalTypeListAdapter(requireActivity(), arrayListOf("Silver","Platinum","Gold","Diamond"),binding)

        binding.rvMetalType?.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvMetalType?.adapter = metalTypeAdapter

    }

    private fun setCategoryRecyclerView(){

        val categoryAdapter = CategoryListAdapter(requireActivity(), arrayListOf("Ring","Earrings","necklace","Bangle"),binding)

        binding.rvCategory?.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvCategory?.adapter = categoryAdapter

    }

    private fun setCollectionRecyclerView() {

        val collectionAdapter = CollectionListAdapter(requireActivity(), arrayListOf("Wedding","engagement","anniversary","Bangle"),

            object : CollectionStatusCallback {

                @SuppressLint("NotifyDataSetChanged")
                override fun collectionSelected(collectionName: String) {

                    binding.tvCollectionHint?.visibility = View.GONE

                    selectedCollectionList.add(collectionName)
                    selectedCollectionAdapter?.notifyDataSetChanged()

                    Utils.E("$collectionName")
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun collectionUnSelected(collectionName: String) {

                    selectedCollectionList.remove(collectionName)

                    if (selectedCollectionList.size == 0) {
                        binding.tvCollectionHint?.visibility = View.VISIBLE
                    }

                    selectedCollectionAdapter?.notifyDataSetChanged()

                }
              }
            )

        binding.rvCollectionList?.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)

        binding.rvCollectionList?.adapter = collectionAdapter

    }


    private fun setSelectedCollectionRecyclerView() {

        selectedCollectionAdapter = SelectedCollectionAdapter(requireActivity(), selectedCollectionList,binding)

        binding.rvCollectionSelectedList?.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)

        binding.rvCollectionSelectedList?.adapter = selectedCollectionAdapter

    }

    //set the Clicks And initialization
    private fun init(){

        setMetalTypeRecyclerView()

        setCategoryRecyclerView()

        setCollectionRecyclerView()

        setSelectedCollectionRecyclerView()

        binding.llMetalType?.setOnClickListener(this)

        binding.llCategory?.setOnClickListener(this)

        binding.llCollection?.setOnClickListener(this)

    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        if (v ==  binding.llMetalType){

            if (binding.mcvMetalTypeList?.visibility == View.VISIBLE) {

                binding.ivMetalType?.let { Utils.rotateView(it,180f) }

                binding.mcvMetalTypeList?.let { Utils.collapseView(it) }

            }
            else {

                binding.ivMetalType?.let { Utils.rotateView(it,0f) }
                binding.mcvMetalTypeList?.let { Utils.expandView(it) }
            }

        }
        else if (v == binding.llCategory){

            if (binding.mcvCategoryList?.visibility == View.VISIBLE) {

                binding.ivCategory?.let { Utils.rotateView(it,180f) }
                binding.mcvCategoryList?.let { Utils.collapseView(it) }

            }
            else {

                binding.ivCategory?.let { Utils.rotateView(it,0f) }
                binding.mcvCategoryList?.let { Utils.expandView(it) }
            }
        }

        else if (v == binding.llCollection) {

            if (binding.mcvCollectionList?.visibility == View.VISIBLE) {

                binding.ivCollection?.let { Utils.rotateView(it,180f) }
                binding.mcvCollectionList?.let { Utils.collapseView(it) }

            }
            else {

                binding.ivCollection?.let { Utils.rotateView(it,0f) }
                binding.mcvCollectionList?.let { Utils.expandView(it) }
            }
        }


    }

}