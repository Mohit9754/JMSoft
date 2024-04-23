package com.jmsoft.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentCollectionDetailBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CollectionDetailAdapter

class CollectionDetailFragment : Fragment(),View.OnClickListener {

    lateinit var binding:FragmentCollectionDetailBinding

    var collectionUUID:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentCollectionDetailBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    private fun setCollectionDetailRecyclerView() {

        val categoryDataList = collectionUUID?.let { Utils.getAllCategoryOfParticularCollection(it) }

//        Utils.E("Size of list is :"+categoryDataList?.size.toString())

        if (categoryDataList?.isNotEmpty() == true) {

            binding.llEmptyInventory?.visibility = View.GONE

            Utils.E("${categoryDataList.size} ${categoryDataList[0].categoryName} ${collectionUUID}")

            val adapter =
                collectionUUID?.let {
                    CollectionDetailAdapter(requireActivity(), categoryDataList,
                        it,binding
                    )
                }

            binding.rvCollectionDetail?.layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            binding.rvCollectionDetail?.adapter = adapter

        } else {

            binding.llEmptyInventory?.visibility = View.VISIBLE

        }
    }

    private fun setCollectionNameAndImage(){

        collectionUUID = arguments?.getString(Constants.collectionUUID)

        if (collectionUUID != null) {

            val collectionData = Utils.getCollectionThroughUUID(collectionUUID!!)

            binding.ivCollectionImage?.setImageBitmap(collectionData.collectionImageUri?.let {
                Utils.getImageFromInternalStorage(requireActivity(),
                    it
                )
            })

            binding.tvCollectionName?.text  =  collectionData.collectionName

        }
    }
    private fun init() {

        setCollectionNameAndImage()

        setCollectionDetailRecyclerView()

        binding.mcvBackBtn?.setOnClickListener(this)

        binding.mcvAddProduct?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        if (v == binding.mcvBackBtn) {

            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }

        else if (v == binding.mcvAddProduct){

            //Giving the fragment status
            val bundle = Bundle()
            bundle.putString(Constants.collectionUUID, collectionUUID)

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.product,bundle)

        }

    }

}