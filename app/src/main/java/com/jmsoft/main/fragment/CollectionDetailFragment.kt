package com.jmsoft.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentCollectionDetailBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CollectionDetailAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CollectionDetailFragment : Fragment(),View.OnClickListener {

    private lateinit var binding:FragmentCollectionDetailBinding

    private var collectionUUID:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentCollectionDetailBinding.inflate(layoutInflater)

        lifecycleScope.launch(Dispatchers.Main) {
            init()
        }

        return binding.root
    }

    // Set Collection Detail recycler view
    private fun setCollectionDetailRecyclerView() {

        val categoryDataList = collectionUUID?.let { Utils.getAllCategoryOfParticularCollection(it) }

        if (categoryDataList?.isNotEmpty() == true) {

            binding.llEmptyInventory?.visibility = View.GONE

//            Utils.E("${categoryDataList.size} ${categoryDataList[0].categoryName} ${collectionUUID}")

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

    // Set name and image of the collection
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

    private suspend fun init() {

        // Set name and image of the collection
        setCollectionNameAndImage()

        // Set Collection Detail recycler view
        val job = lifecycleScope.launch (Dispatchers.Main){
            setCollectionDetailRecyclerView()
        }

        // Set click on back button
        binding.mcvBackBtn?.setOnClickListener(this)

        // Set click on add product button
        binding.mcvAddProduct?.setOnClickListener(this)

        job.join()
        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    // Handle all the clicks
    override fun onClick(v: View?) {

        // Clicked on back button
        if (v == binding.mcvBackBtn) {

            GetProgressBar.getInstance(requireActivity())?.show()

            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }

        // Clicked on Add product button
        else if (v == binding.mcvAddProduct){

            GetProgressBar.getInstance(requireActivity())?.show()

            //Giving the fragment status
            val bundle = Bundle()
            bundle.putString(Constants.collectionUUID, collectionUUID)

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.product,bundle)

        }

    }

}