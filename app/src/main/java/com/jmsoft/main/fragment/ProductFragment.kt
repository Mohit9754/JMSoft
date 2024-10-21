package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.provider.Settings
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
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.Database.CategoryDataModel
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
import com.jmsoft.main.enum.ProductColumnName
import com.jmsoft.main.`interface`.ExcelReadSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil

class ProductFragment : Fragment(), View.OnClickListener, ExcelReadSuccess {

    private var productListAdapter: ProductListAdapter? = null

    private lateinit var binding: FragmentProductBinding

    private var collectionUUID: String? = null

    private var isPurchase: Boolean? = false

    private var productDataList = ArrayList<ProductDataModel>()

    private var selectedProductUUIDList = ArrayList<String>()

    private var isRunFilter = false

    private var categoryDataList = ArrayList<CategoryDataModel>()

    private var selectedCategoryIndex = 0

    private lateinit var excelReader: ExcelReader

    private var isEmptyRfidProduct = false

    private var offset = 0

    private var file: File? = null

    private var fileUri: Uri? = null

    private var isImport = true

    private val CHANNEL_ID = "file_creation_channel"

    // Permission for External Storage
    private val permissionsForExternalStorage = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // Checks All the necessary permission related to External Storage
    private var customPermissionLauncher = registerForActivityResult(

        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allPermissionsGranted = true // Flag to track permission status
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value
            if (!isGranted) {
                // If any permission is not granted, set the flag to false
                allPermissionsGranted = false
                // Permission is not granted
                // Handle the denied permission accordingly
                if (!shouldShowRequestPermissionRationale(permission)) {
                    // Permission denied ,Show Open Setting Dialog
                    showOpenSettingDialog()

                }
            } else {
                Utils.E(permission)
            }
        }

        // Check if all permissions are granted or not
        if (allPermissionsGranted) {

            if (isImport) {
                openDocument()
            } else {

                GetProgressBar.getInstance(requireActivity())?.show()

                lifecycleScope.launch(Dispatchers.IO) {
                    createExcel(Utils.getAllProductsWithOutLimit())
                }


            }

        }
    }

//    // Storage Permission Launcher
//    private var storagePermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted: Boolean? ->
//
//        if (isGranted == true) {
//
//            openDocument()
//
//        } else {
//
//            showOpenSettingDialog()
//        }
//    }

    // Method to request MANAGE_EXTERNAL_STORAGE permission
    @RequiresApi(Build.VERSION_CODES.R)
    fun requestStoragePermission() {

        val packageName = requireContext().packageName
        val uri = Uri.parse("package:$packageName")

        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
        storagePermissionResultLauncher.launch(intent)

    }

    // Initialize your permission result launcher
    @RequiresApi(Build.VERSION_CODES.R)
    val storagePermissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                // Check if permission is granted
                if (Environment.isExternalStorageManager()) {

                    // Permission granted. Now resume your workflow.
                    // Call your method or handle the permission granted state here
                    GetProgressBar.getInstance(requireActivity())?.show()

                    lifecycleScope.launch(Dispatchers.IO) {
                        createExcel(Utils.getAllProductsWithOutLimit())
                    }
                } else {

                    requestStoragePermission()
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {

                // Handle case where user cancels the permission request
                if (Environment.isExternalStorageManager()) {

                    // Permission granted. Now resume your workflow.
                    // Call your method or handle the permission granted state here
                    GetProgressBar.getInstance(requireActivity())?.show()

                    lifecycleScope.launch(Dispatchers.IO) {
                        createExcel(Utils.getAllProductsWithOutLimit())
                    }

                } else {
                    requestStoragePermission()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentProductBinding.inflate(layoutInflater)

        lifecycleScope.launch(Dispatchers.Main) {
            init()
        }

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

    private fun getFileName(uri: Uri): String? = when (uri.scheme) {
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
            fileUri = FileProvider.getUriForFile(
                requireActivity(),
                requireContext().packageName + ".provider",
                file!!
            )
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

                    excelReader.readExcelFileFromAssets(requireActivity(), this.absolutePath)
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

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.getAllCategory()
        }

        categoryDataList = result.await()

        val listSpinner = mutableListOf<String?>()
        listSpinner.add(All)
        categoryDataList.map { it.categoryName }.let { listSpinner.addAll(it) }

        withContext(Dispatchers.Main) {

            val spinnerAdapter = ArrayAdapter(requireActivity(), R.layout.item_spinner, listSpinner)
            spinnerAdapter.setDropDownViewResource(R.layout.item_custom_spinner_list)
            binding.spinner?.adapter = spinnerAdapter

            binding.spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                @RequiresApi(Build.VERSION_CODES.N)
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedCategoryIndex = position
                    offset = 0

                    if (isRunFilter) GetProgressBar.getInstance(requireActivity())?.show()

                    if (position == 0) {

                        if (isRunFilter) {

                            lifecycleScope.launch(Dispatchers.Main) {
                                getProductData()
                            }

                            binding.etSearch?.text?.clear()

                        } else {
                            isRunFilter = true
                        }

                    } else {

                        lifecycleScope.launch(Dispatchers.Main) {
                            getProductData()
                        }

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

    // Checks fragment state
    private fun checkState() {

        collectionUUID = arguments?.getString(Constants.collectionUUID)

        isPurchase = arguments?.getBoolean(Constants.addPurchase, false)

        if (collectionUUID != null || isPurchase == true) {

            binding.mcvAdd?.visibility = View.VISIBLE
            binding.tvTitle?.text = getString(R.string.select_products_to_add)
            binding.mcvExport?.visibility = View.GONE
            binding.mcvImport?.visibility = View.GONE
//            binding.mcvAddProduct?.visibility = View.GONE

        } else {
            binding.tvTitle?.text = getString(R.string.product)
        }

        if (isPurchase == true) {

            selectedProductUUIDList = ArrayList(Utils.SelectedProductUUIDList.getProductList())
//            Utils.SelectedProductUUIDList.clearList()

            isEmptyRfidProduct = true
        }

    }

    // make the isRunFilter false
    override fun onResume() {
        super.onResume()

        isRunFilter = false
        offset = 0
        selectedCategoryIndex = 0
        binding.etSearch?.text?.clear()
    }

    // Get Visible page no
    fun getVisiblePages(pageNoList: List<Int>, currentPage: Int, totalVisible: Int = 5): List<Int> {
        val totalPages = pageNoList.size

        if (totalPages == 0) return emptyList() // If there are no pages, return an empty list

        // Include first and last page numbers in the visible pages
        val firstPage = pageNoList.first()
        val lastPage = pageNoList.last()

        // Set to hold the visible pages ensuring no duplicates
        val visiblePages = mutableSetOf(firstPage, lastPage)

        // Ensure the current page is within valid range
        if (currentPage in firstPage..lastPage) {
            visiblePages.add(currentPage)

            // Include neighbors of the current page if they exist
            if (currentPage > firstPage) visiblePages.add(currentPage - 1)
            if (currentPage < lastPage) visiblePages.add(currentPage + 1)
        }

        // Calculate remaining pages to display, excluding the fixed ones
        val additionalPagesNeeded = totalVisible - visiblePages.size

        // Adjust the range if more pages are needed
        if (additionalPagesNeeded > 0) {
            // Determine start and end points for additional pages
            val remainingPages = pageNoList.filter { it !in visiblePages }

            // Add as many pages as needed to fill the visible slots
            for (i in 0 until additionalPagesNeeded) {
                if (i < remainingPages.size) {
                    visiblePages.add(remainingPages[i])
                } else {
                    break
                }
            }
        }

        // Ensure the size is exactly totalVisible by removing the farthest elements if necessary
        while (visiblePages.size > totalVisible) {
            // Remove the farthest elements from currentPage
            visiblePages.remove(visiblePages.minByOrNull { Math.abs(it - currentPage) })
        }

        return visiblePages.sorted()
    }

    // Set page no recycler view
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setPageNoRecyclerView() {

        binding.llPageIndicator?.visibility = View.VISIBLE

        val categoryUUID =
            if (selectedCategoryIndex - 1 == -1) All else categoryDataList[selectedCategoryIndex - 1].categoryUUID

        binding.rvPageNo?.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)

        // Inline adapter and view holder
        binding.rvPageNo?.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            val totalNumberOfProducts = if (binding.etSearch?.text?.isEmpty() == true) {

                if (collectionUUID != null)

                    categoryUUID?.let {
                        Utils.getTotalNumberOfProductsOfCollection(
                            collectionUUID!!,
                            it,
                            isEmptyRfidProduct
                        )
                    }
                else {

                    categoryUUID?.let { Utils.getTotalNumberOfProducts(it,isEmptyRfidProduct) }

                }
            } else {

                if (collectionUUID != null && categoryUUID != null) {

                    binding.etSearch?.text?.toString()?.trim()?.let {
                        Utils.getTotalNumberOfProductsOfDetailSearchAcceptCollection(
                            it, collectionUUID!!, categoryUUID,isEmptyRfidProduct
                        )
                    }

                } else {

                    binding.etSearch?.text?.toString()?.trim()
                        ?.let {
                            categoryUUID?.let { it1 ->
                                Utils.getTotalNumberOfProductsOfDetailSearch(
                                    it,
                                    it1,
                                    isEmptyRfidProduct
                                )
                            }
                        }
                }
            }

            val totalProducts = totalNumberOfProducts ?: 0 // default to 45 if null

            val pageNoList = (1..ceil(totalProducts / Constants.Limit.toFloat()).toInt()).toList()

            val currentPage = (offset / Constants.Limit) + 1

            val visiblePageNoList = getVisiblePages(pageNoList, currentPage)

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_page_no, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


                (holder as ViewHolder).bind(visiblePageNoList[position], position)
            }

            override fun getItemCount() = visiblePageNoList.size

            inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

                private val tvPageNo: TextView = itemView.findViewById(R.id.tvPageNo)
                private val tvDots: TextView = itemView.findViewById(R.id.tvDots)
                private val mcvPageNo: MaterialCardView = itemView.findViewById(R.id.mcvPageNo)

                fun bind(pageNo: Int, position: Int) {

                    if (visiblePageNoList[position] == currentPage) {
                        mcvPageNo.strokeColor = resources.getColor(R.color.theme, null)
                    }

                    if (currentPage == 1) {
                        binding.mcvPrevious?.visibility = View.GONE
                    } else {
                        binding.mcvPrevious?.visibility = View.VISIBLE
                    }

                    if (currentPage == visiblePageNoList[visiblePageNoList.size - 1]) {
                        binding.mcvNext?.visibility = View.GONE
                    } else {
                        binding.mcvNext?.visibility = View.VISIBLE
                    }

                    if (visiblePageNoList.size == 1) {
                        binding.llPageIndicator?.visibility = View.GONE
                    }


                    if (position == 3 && currentPage <= 3 && pageNoList.size > 5) {

                        tvDots.visibility = View.VISIBLE
                    }

                    if (position == 0 && (currentPage >= 4 && currentPage <= pageNoList.size - 2) && pageNoList.size > 5)
                        tvDots.visibility = View.VISIBLE

                    if (position == 3 && (currentPage >= 4 && currentPage <= pageNoList.size - 3) && pageNoList.size > 5)
                        tvDots.visibility = View.VISIBLE

                    if (position == 1 && (currentPage == pageNoList.size - 1) && pageNoList.size > 5)
                        tvDots.visibility = View.VISIBLE

                    if (position == 2 && (currentPage == pageNoList.size) && pageNoList.size > 5)
                        tvDots.visibility = View.VISIBLE


                    tvPageNo.text = pageNo.toString()

                    mcvPageNo.setOnClickListener {

                        offset = (visiblePageNoList[position] - 1) * Constants.Limit

                        GetProgressBar.getInstance(requireActivity())?.show()

                        if (binding.etSearch?.text?.isNotEmpty() == true) {

                            lifecycleScope.launch(Dispatchers.Main) {
                                getProductData()
                            }
                        } else {

                            lifecycleScope.launch(Dispatchers.Main) {
                                getProductWithDetailSearch()
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun init() {

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

        val job = lifecycleScope.launch(Dispatchers.Main) {
            getProductData()
        }

        job.join()

        // Set the spinner
        lifecycleScope.launch(Dispatchers.Default) {
            setSpinner()
        }

        setEditTextChangeLisOnSearch()

        // Set focus change listener on edittext search
        binding.etSearch?.let {
            binding.mcvSearch?.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
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

        binding.mcvExport?.setOnClickListener(this)

        binding.mcvPrevious?.setOnClickListener(this)

        binding.mcvNext?.setOnClickListener(this)

        binding.ivSearch?.setOnClickListener(this)

    }

    // edit text change listener on search
    private fun setEditTextChangeLisOnSearch() {

        binding.etSearch?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.isEmpty() == true) {

                    offset = 0

                    lifecycleScope.launch(Dispatchers.Main) {
                        getProductData()
                    }

                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

    }

    // Get Product data
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SuspiciousIndentation")
    private suspend fun getProductData() {

        val categoryUUID =
            if (selectedCategoryIndex - 1 == -1) All else categoryDataList[selectedCategoryIndex - 1].categoryUUID

        val job = if (collectionUUID != null && categoryUUID != null) {

            lifecycleScope.launch(Dispatchers.IO) {

                productDataList = Utils.getAllProductsAcceptCollection(
                    collectionUUID!!, offset, categoryUUID,isEmptyRfidProduct
                )
            }

        } else {

            lifecycleScope.launch(Dispatchers.IO) {
                if (categoryUUID != null)
                    productDataList = Utils.getAllProducts(offset, categoryUUID,isEmptyRfidProduct)
            }
        }

        job.join()

        setProductRecyclerView()

    }

    // Set Product Recycler View
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setProductRecyclerView() {

        if (isPurchase == true) {
            val selectedProductUUIDSet = selectedProductUUIDList.toSet()
            productDataList.removeIf { it.productUUID in selectedProductUUIDSet }
        }

        if (productDataList.isNotEmpty()) {

            binding.mcvAdd?.let { Utils.enableButton(it) }

            binding.mcvExport?.let { Utils.enableButton(it) }

            binding.mcvProductList?.visibility = View.VISIBLE
            binding.llEmptyProduct?.visibility = View.GONE

            val enableCheckBox = collectionUUID != null || isPurchase == true

            productListAdapter = ProductListAdapter(
                requireActivity(),
                productDataList,
                enableCheckBox,
                binding,
                selectedProductUUIDList
            )

            binding.rvProduct?.layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            binding.rvProduct?.adapter = productListAdapter

            setPageNoRecyclerView()

            // Scroll the NestedScrollView to the top position
            binding.nsvProduct?.post {
                binding.nsvProduct?.smoothScrollTo(0, 0)
            }

        } else {

            binding.mcvAdd?.let { Utils.disableButton(it) }

            binding.mcvExport?.let { Utils.disableButton(it) }

            binding.mcvProductList?.visibility = View.GONE
//            binding.mcvFilter?.visibility = View.GONE
            binding.llEmptyProduct?.visibility = View.VISIBLE
            binding.llPageIndicator?.visibility = View.GONE


            // Dismiss progress bar
            GetProgressBar.getInstance(requireActivity())?.dismiss()
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReadSuccess(productList: ArrayList<ProductDataModel>) {

        Utils.T(requireActivity(), getString(R.string.readed_successfully))

        if (productList.isNotEmpty())
            binding.mcvExport?.let { Utils.enableButton(it) }

        for (product in productList) {

//            Utils.E("Stock location uuid is ${product.stockLocationUUID}")

            lifecycleScope.launch(Dispatchers.IO) {
                Utils.addProduct(product)
            }
        }

        offset = 0

        lifecycleScope.launch(Dispatchers.Main) {
            getProductData()
        }
        binding.etSearch?.text?.clear()

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    override fun onReadFail() {
        GetProgressBar.getInstance(requireActivity())?.dismiss()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun getProductWithDetailSearch() {

        val categoryUUID =
            if (selectedCategoryIndex - 1 == -1) All else categoryDataList[selectedCategoryIndex - 1].categoryUUID

        val job = if (collectionUUID != null && categoryUUID != null) {

            lifecycleScope.launch(Dispatchers.IO) {
                productDataList = Utils.getProductsWithDetailSearchAcceptCollection(
                    binding.etSearch?.text.toString().trim(), offset, collectionUUID!!, categoryUUID,isEmptyRfidProduct
                )
            }
        } else {

            lifecycleScope.launch(Dispatchers.IO) {

                if (categoryUUID != null)
                    productDataList = Utils.getProductsWithDetailSearch(
                        binding.etSearch?.text.toString().trim(),
                        offset, categoryUUID,isEmptyRfidProduct
                    )
            }
        }

        job.join()

        setProductRecyclerView()

    }

    private fun createNotificationChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "File Creation Channel"
            val descriptionText = "Channel for file creation notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, fileName: String) {

        Utils.E("Notification has been created ..........")

        // Path to the file you want to open
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Excel"
        )

        // Create a Uri for the Downloads folder using FileProvider
        val folderUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Replace with your FileProvider authority
            file
        )

        // Create an Intent to open the Downloads folder
        val openFolderIntent = Intent(Intent.ACTION_VIEW).apply {
            setData(folderUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setType("vnd.android.document/directory")
        }

        // Create a PendingIntent to be triggered when the notification is clicked
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            openFolderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo) // Replace with your app icon
            .setContentTitle("Excel File Created")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Automatically removes the notification when clicked
            .setContentText("The file $fileName has been successfully created.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }

    private suspend fun createExcel(productDataList: ArrayList<ProductDataModel>) {

        val workbook: Workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sheet1")

        val headerRow: Row = sheet.createRow(0)

        for (i in 0..12) {

            val cell = headerRow.createCell(i)

            when (i) {

                0 -> {
                    cell.setCellValue(ProductColumnName.PRODUCT_NAME.displayName)
                }

                1 -> {
                    cell.setCellValue(ProductColumnName.CATEGORY_NAME.displayName)
                }

                2 -> {
                    cell.setCellValue(ProductColumnName.METAL_TYPE.displayName)
                }

                3 -> {
                    cell.setCellValue(ProductColumnName.DESCRIPTION.displayName)
                }

                4 -> {
                    cell.setCellValue(ProductColumnName.COLLECTION.displayName)
                }

                5 -> {
                    cell.setCellValue(ProductColumnName.ORIGIN.displayName)
                }

                6 -> {
                    cell.setCellValue(ProductColumnName.WEIGHT.displayName)
                }

                7 -> {
                    cell.setCellValue(ProductColumnName.CARAT.displayName)
                }

                8 -> {
                    cell.setCellValue(ProductColumnName.PRICE.displayName)
                }

                9 -> {
                    cell.setCellValue(ProductColumnName.COST.displayName)
                }

                10 -> {
                    cell.setCellValue(ProductColumnName.BARCODE.displayName)
                }

                11 -> {
                    cell.setCellValue(ProductColumnName.NAME.displayName)
                }

                12 -> {
                    cell.setCellValue(ProductColumnName.PARENT.displayName)
                }

            }

        }

        for ((rowNum, productData) in productDataList.withIndex()) {

            val row: Row = sheet.createRow(rowNum + 1)

            for (i in 0..12) {

                val cell = row.createCell(i)

                when (i) {

                    0 -> {
                        cell.setCellValue(productData.productName)
                    }

                    1 -> {

                        cell.setCellValue(productData.categoryUUID?.let {
                            Utils.getCategoryNameThroughCategoryUUID(
                                it
                            )
                        })

                    }

                    2 -> {

                        cell.setCellValue(productData.metalTypeUUID?.let {
                            Utils.getMetalTypeNameThroughMetalTypeUUID(
                                it
                            )
                        })

                    }

                    3 -> {
                        cell.setCellValue(productData.productDescription)
                    }

                    4 -> {

                        cell.setCellValue(productData.collectionUUID?.let {

                            Utils.getCollectionNameThroughCollectionUUID(
                                it
                            )
                        })
                    }

                    5 -> {
                        cell.setCellValue(productData.productOrigin)
                    }

                    6 -> {
                        productData.productWeight?.let { cell.setCellValue(it) }
                    }

                    7 -> {
                        cell.setCellValue(productData.productCarat.toString())
                    }

                    8 -> {
                        cell.setCellValue(productData.productPrice.toString())
                    }

                    9 -> {
                        cell.setCellValue(productData.productCost.toString())
                    }

                    10 -> {
                        cell.setCellValue(productData.productBarcodeData.toString())
                    }

                    11 -> {

                        val stockLocation = productData.stockLocationUUID?.let {
                            Utils.getStockLocation(
                                it
                            )
                        }

                        cell.setCellValue(stockLocation?.stockLocationName ?: "")

                    }

                    12 -> {

                        val stockLocation = productData.stockLocationUUID?.let {
                            Utils.getStockLocation(
                                it
                            )
                        }

                        val stockLocationParent = stockLocation?.stockLocationParentUUID?.let {
                            Utils.getStockLocation(
                                it
                            )
                        }

                        cell.setCellValue(stockLocationParent?.stockLocationName ?: "")

                    }

                }

            }
        }

        try {

            //  Get the app-specific directory within the Android folder
            //  val appFolder = File(context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Excel")
            //  val appFolder = File(context?.filesDir, "Excel")

            val appFolder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Excel"
            )

            // Ensure the directory exists
            if (!appFolder.exists()) {
                appFolder.mkdirs() // Create the directory if it doesn't exist
            }

            val fileName = "${Utils.generateUUId()}.xlsx"

            val file = File(appFolder, fileName)
            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            workbook.close()
            outputStream.close()

            withContext(Dispatchers.Main) {
                GetProgressBar.getInstance(requireActivity())?.dismiss()
            }

            openExcelFile(file)

            createNotificationChannel(requireActivity())

            showNotification(requireActivity(), fileName)

            val filePath = file.absolutePath

            Utils.E("Excel created successfully at $filePath")
            println("Excel file created successfully!")


        } catch (e: Exception) {

            withContext(Dispatchers.Main) {
                GetProgressBar.getInstance(requireActivity())?.dismiss()
            }

            e.printStackTrace()
        }

    }

    private fun openExcelFile(file: File) {

        val context = requireActivity()

        Utils.E("Provider is " + context.applicationContext.packageName + ".provider")

        // Always use FileProvider to get the file's URI
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )


        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                fileUri,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Check if there's an app to handle the intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {

            val handler = Handler(Looper.getMainLooper())

            handler.post {

                Utils.T(
                    requireActivity(),
                    getString(R.string.no_application_available_to_open_excel_file)
                )

            }
        }
    }

    // Handle all the clicks
    @RequiresApi(Build.VERSION_CODES.N)
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

            if (isPurchase == true) {

                GetProgressBar.getInstance(requireActivity())?.show()

                //  Utils.SelectedProductUUIDList.setProductList(selectedProductUUIDList)
                (requireActivity() as DashboardActivity).navController?.popBackStack()

            }
            else {

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

        } else if (v == binding.mcvImport) {

            isImport = true

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                customPermissionLauncher.launch(permissionsForExternalStorage)

            } else {

                openDocument()
            }

        } else if (v == binding.mcvExport) {

            isImport = false

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                // For Android 10 and below, request traditional storage permissions
                customPermissionLauncher.launch(permissionsForExternalStorage)

            } else {

                // For Android 11 and above, request manage external storage permission
                if (Environment.isExternalStorageManager()) {

                    GetProgressBar.getInstance(requireActivity())?.show()

                    lifecycleScope.launch(Dispatchers.IO) {
                        createExcel(Utils.getAllProductsWithOutLimit())
                    }

                } else {
                    requestStoragePermission()
                }
            }
        } else if (v == binding.mcvPrevious) {

            offset = ((offset / Constants.Limit) - 1) * Constants.Limit

            GetProgressBar.getInstance(requireActivity())?.show()

            lifecycleScope.launch(Dispatchers.Main) {
                getProductData()
            }

        } else if (v == binding.mcvNext) {

            offset = ((offset / Constants.Limit) + 1) * Constants.Limit

            GetProgressBar.getInstance(requireActivity())?.show()

            lifecycleScope.launch(Dispatchers.Main) {
                getProductData()
            }

        } else if (v == binding.ivSearch) {

            if (binding.etSearch?.text?.isNotEmpty() == true) {

                GetProgressBar.getInstance(requireActivity())?.show()

                offset = 0

                lifecycleScope.launch(Dispatchers.Main) {
                    getProductWithDetailSearch()
                }
            }
        }
    }
}