package com.jmsoft.main.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentCatalogBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CatalogAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

                delay(100) // Adjust delay as needed

                binding = (requireActivity() as DashboardActivity).binding
            }

            binding.mcvSearch?.visibility = View.VISIBLE
            etSearch = binding.etSearch

            // Set the Search when come back from product fragment and activity recreate

            lifecycleScope.launch(Dispatchers.Default) {
                setSearchWhenBackPressed()
            }

            // Set the Search
            setSearch()

        }
    }

    // check if catalog list is empty
    private fun checkEmptyList(){

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

    // Getting all the Product list
    private suspend fun getAllProducts() {

        val result = lifecycleScope.async(Dispatchers.IO) {
             return@async Utils.getAllProducts()
        }

        productList = result.await()

    }

    // Setting the RecyclerView
    private fun setRecyclerView() {

        checkEmptyList()
        binding.rvCatalog?.layoutManager = GridLayoutManager(requireActivity(), 3) // Span Count is set to 3
        binding.rvCatalog?.adapter = catalogAdapter
    }

    // Set the Search when come back from product fragment and activity recreate
    private suspend fun setSearchWhenBackPressed(){

        filterProductList.clear()

        withContext(Dispatchers.Main) {

            if (etSearch?.text?.isNotEmpty() == true){

                for (product in productList) {

                    if (product.productName?.contains(etSearch?.text.toString(),true) == true){
                        filterProductList.add(product)
                    }
                }

                // Setting the RecyclerView with filtered product list
                catalogAdapter = CatalogAdapter(requireActivity(), filterProductList)
                setRecyclerView()

            }
            else {

                // Setting the RecyclerView with All the Product
                catalogAdapter = CatalogAdapter(requireActivity(), productList)
                setRecyclerView()
            }
        }
    }

    // Set the Clicks And initialization
    private suspend fun init() {

//        val displayMetrics = resources.displayMetrics
//        val densityDpi = displayMetrics.densityDpi
//
//        Utils.E("$densityDpi Width : ${Utils.getScreenWidth(requireActivity())} , Height: ${Utils.getScreenHeight(requireActivity())}")

        // Getting all the Product list
        val job = lifecycleScope.launch(Dispatchers.Main) { getAllProducts() }

        job.join()

        // Show the Search And Set the Search
        lifecycleScope.launch {
            showSearch()
        }

    }

    // Set the Search
    private fun setSearch(){

        etSearch?.addTextChangedListener(

            object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    filterProductList.clear()

                    for (product in productList){

                        if (product.productName?.contains(s.toString(),true) == true){
                            filterProductList.add(product)
                        }
                    }

                    checkEmptyList()
                    catalogAdapter?.addFilterList(filterProductList)
                }

                override fun afterTextChanged(s: Editable?) {}
            }
        )
    }

    // Clear Search Edittext when destroy
    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as DashboardActivity).etSearch?.setText("")
    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

    }

}