package com.jmsoft.main.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentCatalogBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CatalogAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatalogFragment : Fragment(), View.OnClickListener {


    private lateinit var binding: FragmentCatalogBinding

    private var catalogAdapter:CatalogAdapter? = null

    private var productList = ArrayList<ProductDataModel>()

    private val filterProductList = ArrayList<ProductDataModel>()

    private var etSearch:EditText? = null

    private var ivSearch:ImageView? = null

    private var offset = 0

    private var searchOffset = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentCatalogBinding.inflate(layoutInflater)

        // Set the Clicks And initialization
        lifecycleScope.launch(Dispatchers.Main) {
            init()
        }

        return binding.root
    }

    // Show the Search And Set the Search
    private suspend fun showSearch() {

        // Use withContext to switch to the Main dispatcher for UI operations
        withContext(Dispatchers.Main) {

            var binding = (requireActivity() as DashboardActivity).binding

            // Wait until binding is available or a timeout occurs

            while (binding == null ) {

                delay(100)  // Adjust delay as needed

                binding = (requireActivity() as DashboardActivity).binding
            }

            binding.mcvSearch?.visibility = View.VISIBLE
            etSearch = binding.etSearch
            ivSearch = binding.ivSearch

        }
    }

    // Check if catalog list is empty
    private fun checkEmptyList() {

        if (productList.isNotEmpty()) {

            binding.llEmptyCatalog?.visibility  = View.GONE

        }
        else {

            binding.llEmptyCatalog?.visibility  = View.VISIBLE

            if (isAdded) {
                GetProgressBar.getInstance(requireActivity())?.dismiss()
            }

        }

    }

    // Getting the Product list
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun getProducts() {

        val result = lifecycleScope.async(Dispatchers.IO) {
             return@async Utils.getProductsWithLimitAndOffset(offset)
        }

        val list = result.await()
        offset+=9

        if (list.isNotEmpty()) productList.addAll(list) else binding.progressBar?.visibility = View.GONE

        catalogAdapter?.notifyItemRangeInserted(offset,productList.size)

    }

    // Setting the RecyclerView
    private fun setRecyclerView() {

        checkEmptyList()

        catalogAdapter = CatalogAdapter(requireActivity(),productList,binding.progressBar)
        binding.rvCatalog?.layoutManager = GridLayoutManager(requireActivity(), 3) // Span Count is set to 3
        binding.rvCatalog?.adapter = catalogAdapter

    }


    // Set the Clicks And initialization
    private suspend fun init() {

        binding.nsvCatalog?.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->

            if (scrollY >= v.getChildAt(v.childCount - 1)
                    .measuredHeight - v.measuredHeight && scrollY > oldScrollY
            ) {
//                Utils.E("Api GetCourse call")

                binding.progressBar?.visibility = View.VISIBLE

                if(etSearch?.text?.isNotEmpty() == true) {

                    lifecycleScope.launch(Dispatchers.Main) { getDataOfSearch(etSearch?.text.toString(),false)}

                }
                else {

                    lifecycleScope.launch(Dispatchers.Main) { getProducts() }

                }
            }
        }

        // Getting all the Product list
        val job = lifecycleScope.launch(Dispatchers.Main) { getProducts() }

        job.join()

        // Show the Search And Set the Search
        lifecycleScope.launch {
            showSearch()
        }

        setRecyclerView()

        checkIfSearchIsEmpty()

        ivSearch?.setOnClickListener(this)

    }

    // Get the search data
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun getDataOfSearch(search:String,clearList:Boolean) {

        if (clearList) filterProductList.clear()

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.getProductsWithSearch(search,searchOffset)
        }

        filterProductList.addAll(result.await())

        Utils.E("Size of list is ${filterProductList.size}")

        catalogAdapter?.addFilterList(filterProductList)
        catalogAdapter?.notifyItemRangeInserted(searchOffset,filterProductList.size)
        searchOffset+=Constants.Limit
//        catalogAdapter?.notifyDataSetChanged()

        if (filterProductList.isNotEmpty()) {

            binding.llEmptyCatalog?.visibility = View.GONE
        }
        else {

            binding.llEmptyCatalog?.visibility = View.VISIBLE
        }

    }

    // Check if search is empty.
    @SuppressLint("NotifyDataSetChanged")
    private fun checkIfSearchIsEmpty() {

        etSearch?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

                if (s?.isEmpty() == true) {

                    Utils.E("Search is empty")

                    checkEmptyList()
                    searchOffset = 0
                    catalogAdapter?.addFilterList(productList)
                    catalogAdapter?.notifyDataSetChanged()

                }

            }

            override fun beforeTextChanged(search: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

    }

    // Clear Search Edittext when destroy
    override fun onDestroy() {

        super.onDestroy()
        (requireActivity() as DashboardActivity).etSearch?.setText("")
    }

    // Clear the Search
    override fun onResume() {
        super.onResume()

        etSearch?.setText("")
        searchOffset = 0
//        offset = 0
    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        if (v == ivSearch) {

            lifecycleScope.launch(Dispatchers.Main) {

                if (etSearch?.text?.isNotEmpty() == true) {

                    getDataOfSearch(etSearch?.text.toString(),true)

                }
            }
        }
    }

}