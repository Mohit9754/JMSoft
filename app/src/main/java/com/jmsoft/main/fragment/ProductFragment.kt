package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.UtilityTools.ExcelReader
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.Utility.UtilityTools.ProductUUIDList
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.All
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentProductBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.ProductListAdapter
import com.jmsoft.main.`interface`.ExcelReadSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProductFragment : Fragment(), View.OnClickListener, ExcelReadSuccess {

    private var productListAdapter: ProductListAdapter? = null

    private lateinit var binding: FragmentProductBinding

    private var collectionUUID: String? = null

    private var productDataList = ArrayList<ProductDataModel>()

    private var categoryFilterList = ArrayList<ProductDataModel>()

    private var selectedProductUUIDList = ArrayList<String>()

    private var isRunFilter = false

    private val searchFilterList  = ArrayList<ProductDataModel>()

    private lateinit var excelReader: ExcelReader

    private var file: File? = null

    private var fileUri: Uri? = null

    // Storage Permission Launcher
    private var storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean? ->

        if (isGranted == true) {

            openDocument()

        } else {
             showOpenSettingDialog()
          }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentProductBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    private fun openDocument() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }

     private fun Uri.getExtension(context: Context): String? {
        var extension: String? = ""
        extension = if (this.scheme == ContentResolver.SCHEME_CONTENT) {
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(this))
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                MimeTypeMap.getFileExtensionFromUrl(
                    FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        File(this.path)
                    )
                        .toString()
                )
            } else {
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(this.path)).toString())
            }
        }
        return extension
    }

    // Gallery result launcher
    @SuppressLint("NotifyDataSetChanged")
    val filePickerLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
//                Log.e("MainActivity", "Selected file Uri: $uri")
                val mimeTypeExtension = uri.getExtension(requireActivity())
//                Log.e("MainActivity", "Selected file mimeTypeExtension: $mimeTypeExtension")

                if (!mimeTypeExtension.isNullOrEmpty()) {
                    if (mimeTypeExtension == "xlsx" || mimeTypeExtension == "xls") {
//                        Log.e("MainActivity", "Selected file mimeTypeExtension valid: $mimeTypeExtension")
                        copyFileAndExtract(uri, mimeTypeExtension)
                    } else {
                        Utils.T(requireActivity(), getString(R.string.invalid_file_selected))
                    }
                }
            }
        }
    }

    private fun getContentFileName(uri: Uri): String? = runCatching {
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    .let(cursor::getString)
            } else {
                null
            }
        }
    }.getOrNull()


    fun getFileName(uri: Uri): String? = when (uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
        else -> uri.path?.let(::File)?.name
    }

    private fun copyFileAndExtract(uri: Uri, extension: String) {
        GetProgressBar.getInstance(requireActivity())?.show()

        val dir = File(requireActivity().filesDir, "doc")
        dir.mkdirs()
        val fileName = getFileName(uri)
        file = File(dir, fileName)
        file?.createNewFile()
        val fout = FileOutputStream(file)
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                fout.use { output ->
                    inputStream.copyTo(output)
                    output.flush()
                }
            }
            fileUri = FileProvider.getUriForFile(requireActivity(), requireContext().packageName + ".provider", file!!)
        } catch (e: Exception) {
//            hideProgress()
            fileUri = uri
            e.printStackTrace()
        }
        fileUri?.apply {
            file?.apply {
                Log.e(tag, this.absolutePath)
                if (excelReader.isEncrypt(this.absolutePath)) {

                    GetProgressBar.getInstance(requireActivity())?.dismiss()

                    Utils.T(requireContext(), getString(R.string.document_is_encrypted))

                } else {
                    excelReader.readExcelFileFromAssets(requireActivity(),this.absolutePath)
                }
            }
        }
    }

    // Open Setting Dialog
    private fun showOpenSettingDialog() {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogOpenSettingBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvTitle.text = getString(R.string.storage_permission)

        dialogBinding.tvMessage.text =
            getString(R.string.storage_permission_access_is_needed_in_order_to_import_excel_sheet_please_enable_it_from_the_settings)

        dialogBinding.mcvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.mcvOpenSetting.setOnClickListener {
            dialog.dismiss()
            Utils.openAppSettings(requireActivity())
        }

        dialog.setCancelable(true)

        dialog.show()
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

        excelReader = ExcelReader(this)

        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        );

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

        binding.mcvImport?.setOnClickListener(this)

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

    override fun onReadSuccess(productList :ArrayList<ProductDataModel>) {

        Utils.T(requireActivity(), getString(R.string.readed_successfully))

        for (product in productList) {

            lifecycleScope.launch(Dispatchers.IO) {
                Utils.addProduct(product)
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            setProductRecyclerView()
        }


        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    override fun onReadFail(){
        GetProgressBar.getInstance(requireActivity())?.dismiss()
    }

    // Handle all the clicks
    override fun onClick(v: View?) {

        // Clicked on back button
        if (v == binding.mcvBackBtn) {
            (requireActivity() as DashboardActivity).navController?.popBackStack()

        }

        // Clicked on add product button
        else if (v == binding.mcvAddProduct) {

            ProductUUIDList.setStatus(true)

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

        else if(v == binding.mcvImport) {


            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

            } else {

                openDocument()
            }
        }

    }

}