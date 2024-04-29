package com.jmsoft.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.All
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentProductBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.ProductListAdapter

class ProductFragment : Fragment(), View.OnClickListener {

    private var productListAdapter: ProductListAdapter? = null

    private lateinit var binding: FragmentProductBinding

    private var collectionUUID: String? = null

    private var productDataList = ArrayList<ProductDataModel>()

    private var selectedProductUUIDList = ArrayList<String>()

    private var isRunFilter = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentProductBinding.inflate(layoutInflater)

        val progressBarDialog = Utils.pdfProgressDialog(requireActivity())

        init()

        progressBarDialog.dismiss()

        return binding.root
    }

    private fun setSpinner() {

        val categoryDataList = Utils.getAllCategory()

        val listSpinner = mutableListOf<String?>()
        listSpinner.add(All)
        categoryDataList.map { it.categoryName }.let { listSpinner.addAll(it) }

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
                    } else {

                        isRunFilter = true
                    }
                } else {

                    val filterDataList =
                        productDataList.filter { it.categoryUUID == categoryDataList[position - 1].categoryUUID } as ArrayList<ProductDataModel>

                    if (filterDataList.isNotEmpty()) {

                        binding.mcvProductList?.visibility = View.VISIBLE
                        binding.llEmptyProduct?.visibility = View.GONE

                        productListAdapter?.filterProductDataList(filterDataList)
                    } else {

                        binding.mcvProductList?.visibility = View.GONE
                        binding.llEmptyProduct?.visibility = View.VISIBLE

                    }
                }

                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when nothing is selected (optional)
            }
        }
    }

    private fun checkState() {

        collectionUUID = arguments?.getString(Constants.collectionUUID)

        if (collectionUUID != null) {
            binding.mcvAdd?.visibility = View.VISIBLE
            binding.tvTitle?.text = getString(R.string.select_products_to_add)
        } else {
            binding.tvTitle?.text = getString(R.string.product)
        }
    }

    private fun init() {

        checkState()

        setSpinner()

        setProductRecyclerView()

        binding.mcvBackBtn?.setOnClickListener(this)

        binding.mcvAddProduct?.setOnClickListener(this)

        binding.mcvAdd?.setOnClickListener(this)

    }

    // Set Product Recycler View
    private fun setProductRecyclerView() {

        productDataList = if (collectionUUID != null) Utils.getAllProductsAcceptCollection(
            collectionUUID!!
        ) else Utils.getAllProducts()

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

        } else {

            binding.mcvProductList?.visibility = View.GONE
            binding.mcvFilter?.visibility = View.GONE
            binding.llEmptyProduct?.visibility = View.VISIBLE

        }
    }

    override fun onClick(v: View?) {

        if (v == binding.mcvBackBtn) {
            (requireActivity() as DashboardActivity).navController?.popBackStack()

        } else if (v == binding.mcvAddProduct) {
            (requireActivity() as DashboardActivity).navController?.navigate(R.id.productInventory)
        } else if (v == binding.mcvAdd) {

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