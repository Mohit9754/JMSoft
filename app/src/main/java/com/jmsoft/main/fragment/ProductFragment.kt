package com.jmsoft.main.fragment

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
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
    private fun setUpProductImageRecyclerView(productImages:String){

        val arrayOfImages = productImages.split(",").toTypedArray()

        val bitmapImages = ArrayList<Bitmap>()

        for (image in arrayOfImages){

            Utils.getImageFromInternalStorage(requireActivity(), image)
                ?.let { bitmapImages.add(it) }
        }

        binding.ivProduct?.setImageBitmap(bitmapImages[0])

        Utils.E("DAta ddis nto")


        val adapter = binding.ivProduct?.let { binding.llLeftBtn?.let { it1 ->
            binding.llRightBtn?.let { it2 ->
                ProductImageAdapter(requireActivity(), bitmapImages, it,
                    it1, it2
                )
            }
        } }

        Utils.E("DAta is nto")

        binding.rvProductImage?.layoutManager = LinearLayoutManager(requireActivity(),RecyclerView.VERTICAL,false)
        binding.rvProductImage?.adapter = adapter

    }

    // Set up collection Recycler view
    private fun setUpCollectionItemRecyclerView(productCategory:String?){

        val productList = productCategory?.let { Utils.getProductsThroughCategory(it) }

        val adapter = productList?.let { CollectionItemAdapter(requireActivity(), it) }

        binding.rvCollection?.layoutManager = LinearLayoutManager(requireActivity(),RecyclerView.HORIZONTAL,false)
        binding.rvCollection?.adapter = adapter

    }

    // Setting the May also like RecyclerView
    private fun setUpMayLikeRecyclerView() {

        val productList = Utils.getAllProducts()

        val catalogAdapter = CatalogAdapter(requireActivity(), productList)

        binding.rvCatalog?.layoutManager = GridLayoutManager(requireActivity(), 3) // Span Count is set to 3
        binding.rvCatalog?.adapter = catalogAdapter
    }

    private fun setUpProductDetails(productUUID:String){

        val productData = Utils.getProductThroughProductUUID(productUUID)

        // Setup Product Image Recycler View
        productData.productImage?.let { setUpProductImageRecyclerView(it) }

        binding.tvProductName?.text  = productData.productName
        binding.tvProductWeight?.text  = productData.productWeight
        binding.tvProductCarat?.text  = productData.productCarat
        binding.tvProductType?.text  = productData.productType

        binding.tvProductCategory?.text  = productData.categoryUUID?.let { Utils.getCategoryNameThroughCategoryUUID(it) }

        binding.tvProductDescription?.text = productData.productDescription
        binding.tvProductPrice?.text   = productData.productPrice.toString()

        // Set up collection Recycler view
        setUpCollectionItemRecyclerView(productData.productCategory)

    }

    // Set the Clicks , initialization And Setup
    private fun init(){

        // getting the product UUID
        val productUUID = arguments?.getString(Constants.productUUID)

        productUUID?.let { setUpProductDetails(it) }

        Utils.E(productUUID)

//         Setting the May also like RecyclerView
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