package com.jmsoft.main.fragment

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.All
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentProductBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CatalogAdapter
import com.jmsoft.main.adapter.ProductListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductFragment : Fragment(), View.OnClickListener {

    private var productListAdapter: ProductListAdapter? = null

    private lateinit var binding: FragmentProductBinding

    private var collectionUUID: String? = null

    private var productDataList = ArrayList<ProductDataModel>()

    private var categoryFilterList = ArrayList<ProductDataModel>()

    private var selectedProductUUIDList = ArrayList<String>()

    private var isRunFilter = false

    private val searchFilterList  = ArrayList<ProductDataModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentProductBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    // Set the spinner
    private suspend fun setSpinner() {

        val result =  lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.getAllCategory() }

        val categoryDataList = result.await()

        val listSpinner = mutableListOf<String?>()
        listSpinner.add(All)
        categoryDataList.map { it.categoryName }.let { listSpinner.addAll(it) }

        withContext(Dispatchers.Main){

            val spinnerAdapter = ArrayAdapter(requireActivity(), R.layout.item_spinner, listSpinner)
            spinnerAdapter.setDropDownViewResource(R.layout.item_custom_spinner_list)
            binding.spinner?.adapter = spinnerAdapter

            binding.spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position == 0) {

                        if (isRunFilter) {

                            binding.mcvProductList?.visibility = View.VISIBLE
                            binding.llEmptyProduct?.visibility = View.GONE

                            productListAdapter?.filterProductDataList(productDataList)
                            categoryFilterList = productDataList

                            binding.etSearch?.text?.clear()

                        } else {
                            isRunFilter = true
                        }

                    } else {

                        categoryFilterList =
                            productDataList.filter { it.categoryUUID == categoryDataList[position - 1].categoryUUID } as ArrayList<ProductDataModel>

                        setCategoryFilterList()

                        isRunFilter = true
                        binding.etSearch?.text?.clear()

                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle case when nothing is selected (optional)
                }
            }
        }

    }

    // Set category filter list
    private fun setCategoryFilterList() {

        if (categoryFilterList.isNotEmpty()) {

            binding.mcvProductList?.visibility = View.VISIBLE
            binding.llEmptyProduct?.visibility = View.GONE

            productListAdapter?.filterProductDataList(categoryFilterList)

        } else {

            binding.mcvProductList?.visibility = View.GONE
            binding.llEmptyProduct?.visibility = View.VISIBLE
        }
    }

    // Checks fragment state
    private fun checkState() {

        collectionUUID = arguments?.getString(Constants.collectionUUID)

        if (collectionUUID != null) {
            binding.mcvAdd?.visibility = View.VISIBLE
            binding.tvTitle?.text = getString(R.string.select_products_to_add)
        } else {
            binding.tvTitle?.text = getString(R.string.product)
        }

    }

    // make the isRunFilter false
    override fun onResume() {
        super.onResume()
        isRunFilter = false

    }

    // Set search
    private fun setSearch() {

        binding.etSearch?.addTextChangedListener( object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                searchFilterList.clear()

                if (binding.etSearch?.text?.isNotEmpty() == true) {

                    for (product in categoryFilterList) {

                        if (product.productName?.contains(
                                binding.etSearch?.text.toString().trim(),
                                true
                            ) == true
                            ||
                            product.productDescription?.contains(
                                binding.etSearch?.text.toString().trim(),
                                true
                            ) == true
                            ||
                            product.productOrigin?.contains(
                                binding.etSearch?.text.toString().trim(),
                                true
                            ) == true
                            ||
                            product.productRFIDCode?.contains(
                                binding.etSearch?.text.toString().trim(),
                                true
                            ) == true
                            ||
                            product.productBarcodeData?.contains(
                                binding.etSearch?.text.toString().trim(),
                                true
                            ) == true

                        ) {
                            searchFilterList.add(product)
                        }
                    }

                    if (searchFilterList.isNotEmpty()) {

                        binding.mcvProductList?.visibility = View.VISIBLE
                        binding.llEmptyProduct?.visibility = View.GONE

                        productListAdapter?.filterProductDataList(searchFilterList)

                    }
                    else {

                        binding.mcvProductList?.visibility = View.GONE
                        binding.llEmptyProduct?.visibility = View.VISIBLE
                    }
                }

                else {
                    setCategoryFilterList()
                }
            }
            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun init() {

        // Checks fragment state
        checkState()

        // Set Product Recycler View
        lifecycleScope.launch(Dispatchers.IO) {
            setProductRecyclerView()
        }

        // Set the spinner
        lifecycleScope.launch(Dispatchers.Default) {
            setSpinner()
        }

        // Set search
//        lifecycleScope.launch(Dispatchers.Default) {
            setSearch()
//        }

        // Set focus change listener on edittext search
        binding.etSearch?.let {
            binding.mcvSearch?.let { it1 ->
                Utils.setFocusChangeListener(requireActivity(),
                    it, it1
                )
            }
        }

        // Set click on back button
        binding.mcvBackBtn?.setOnClickListener(this)

        // Set click on add product button
        binding.mcvAddProduct?.setOnClickListener(this)

        // Set click on add button
        binding.mcvAdd?.setOnClickListener(this)

    }

    // Set Product Recycler View
    private suspend fun setProductRecyclerView() {

        val job = if (collectionUUID != null) {

            lifecycleScope.launch(Dispatchers.IO) {
                productDataList = Utils.getAllProductsAcceptCollection(
                    collectionUUID!!
                )
            }
        }
        else {

            lifecycleScope.launch(Dispatchers.IO) {
                productDataList = Utils.getAllProducts()
            }
        }

        job.join()

        categoryFilterList = productDataList

        withContext(Dispatchers.Main) {

            if (productDataList.isNotEmpty()) {

                binding.mcvProductList?.visibility = View.VISIBLE
                binding.llEmptyProduct?.visibility = View.GONE

                productListAdapter = ProductListAdapter(
                    requireActivity(),
                    productDataList,
                    collectionUUID,
                    binding,
                    selectedProductUUIDList
                )

                binding.rvProduct?.layoutManager =
                    LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
                binding.rvProduct?.adapter = productListAdapter

            }

            else {
                binding.mcvProductList?.visibility = View.GONE
                binding.mcvFilter?.visibility = View.GONE
                binding.llEmptyProduct?.visibility = View.VISIBLE

                // Dismiss progress bar
                GetProgressBar.getInstance(requireActivity())?.dismiss()
            }
            
        }
    }

    // Handle all the clicks
    override fun onClick(v: View?) {

        // Clicked on back button
        if (v == binding.mcvBackBtn) {
            (requireActivity() as DashboardActivity).navController?.popBackStack()

        }

        // Clicked on add product button
        else if (v == binding.mcvAddProduct) {
            (requireActivity() as DashboardActivity).navController?.navigate(R.id.productInventory)
        }

        // Clicked on add button
        else if (v == binding.mcvAdd) {

            GetProgressBar.getInstance(requireActivity())?.show()

            for (selectedUUID in selectedProductUUIDList) {

                for (productData in productDataList) {

                    if (selectedUUID == productData.productUUID) {

                        val collectionUUIDData = productData.collectionUUID

                        val listOfCollection = collectionUUIDData?.split(",")?.toMutableList()

                        if (collectionUUID != null) {

                            listOfCollection?.add(collectionUUID!!)

                            val productDataModel = ProductDataModel()
                            productDataModel.productUUID = selectedUUID
                            productDataModel.collectionUUID =
                                listOfCollection?.joinToString()?.replace(" ", "")

                            Utils.updateCollectionInProduct(productDataModel)

                            Utils.T(
                                requireActivity(),
                                context?.getString(R.string.added_successfully)
                            )
                        }
                        break
                    }
                }
            }

            (requireActivity() as DashboardActivity).navController?.popBackStack()

        }

    }

}