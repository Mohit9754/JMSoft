package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.PRINT_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.intuit.ssp.BuildConfig
import com.jmsoft.R
import com.jmsoft.Utility.Database.AddressDataModel
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.OrderDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.Utility.pdf_helper.FailureResponse
import com.jmsoft.Utility.pdf_helper.PdfGenerator
import com.jmsoft.Utility.pdf_helper.PdfGeneratorListener
import com.jmsoft.Utility.pdf_helper.SuccessResponse
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.address
import com.jmsoft.basic.UtilityTools.Constants.Companion.arabic
import com.jmsoft.basic.UtilityTools.Constants.Companion.confirmation
import com.jmsoft.basic.UtilityTools.Constants.Companion.firstName
import com.jmsoft.basic.UtilityTools.Constants.Companion.information
import com.jmsoft.basic.UtilityTools.Constants.Companion.lastName
import com.jmsoft.basic.UtilityTools.Constants.Companion.path
import com.jmsoft.basic.UtilityTools.Constants.Companion.phoneNumber
import com.jmsoft.basic.UtilityTools.Constants.Companion.state
import com.jmsoft.basic.UtilityTools.Constants.Companion.verification
import com.jmsoft.basic.UtilityTools.Constants.Companion.zipCode
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.UtilityTools.Utils.GetSession
import com.jmsoft.basic.UtilityTools.Utils.getCartThroughUserUUID
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentCartBinding
import com.jmsoft.databinding.PdfInvoiceBinding
import com.jmsoft.databinding.PdfInvoiceSecondBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.activity.PdfViewActivity
import com.jmsoft.main.adapter.CartAddressAdapter
import com.jmsoft.main.adapter.CartListAdapter
import com.jmsoft.main.adapter.PdfInvoiceAdapter
import com.jmsoft.main.`interface`.AddressSelectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class CartFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentCartBinding? = null

    // Validation Mode object
    private var errorValidationModels: MutableList<ValidationModel> = ArrayList()

    // Current state for recreating the fragment
    private var currentState: String = verification

    // Selected Address Data Model
    private var selectedAddressData: AddressDataModel? = null

    private var addressListAdapter: CartAddressAdapter? = null

    // Permission for External Storage
    private val permissionsForExternalStorage = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var cardList = ArrayList<CartDataModel>()

    private val pdfName = Utils.generateUUId()

    private var cartListAdapter:CartListAdapter? = null

    private var orderUUID:String? = null

    private var newOrderDataModel:OrderDataModel? = null

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

            // Generate pdf
            generatePDF()

        }
    }

    // Initialize your permission result launcher
    @RequiresApi(Build.VERSION_CODES.R)
    val storagePermissionResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            // Check if permission is granted
            if (Environment.isExternalStorageManager()) {

                // Permission granted. Now resume your workflow.
                // Call your method or handle the permission granted state here
                generatePDF()
            }
            else {

                requestStoragePermission()
            }
        }
        else if (result.resultCode == Activity.RESULT_CANCELED) {

            // Handle case where user cancels the permission request
            if (Environment.isExternalStorageManager()) {

                // Permission granted. Now resume your workflow.
                // Call your method or handle the permission granted state here
                generatePDF()
            }
            else {
                requestStoragePermission()
            }
        }
    }

    // Method to request MANAGE_EXTERNAL_STORAGE permission
    @RequiresApi(Build.VERSION_CODES.R)
    fun requestStoragePermission() {

        val packageName = requireContext().packageName
        val uri = Uri.parse("package:$packageName")

        val intent = Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
        storagePermissionResultLauncher.launch(intent)

    }

    // Generate pdf
    @SuppressLint("SetTextI18n")
    private fun generatePDF() {

        newOrderDataModel = if (orderUUID != null) Utils.getOrderByUUID(orderUUID!!) else Utils.getOrderUUID(requireActivity())
            ?.let { Utils.getOrderByUUID(it) }

        GetProgressBar.getInstance(requireActivity())?.show()

//        val cardList = Utils.GetSession().userUUID?.let { getCartThroughUserUUID(it) }

        val pdfInvoiceBinding = PdfInvoiceBinding.inflate(LayoutInflater.from(context))

        pdfInvoiceBinding.tvOrderNo.text = newOrderDataModel?.orderNo

        pdfInvoiceBinding.tvClientName.text =
            "${selectedAddressData?.firstName} ${selectedAddressData?.lastName}"

        pdfInvoiceBinding.tvClientNameAr.text =
            "${selectedAddressData?.firstName} ${selectedAddressData?.lastName}"

        pdfInvoiceBinding.tvDate.text = Utils.currentDate()
        pdfInvoiceBinding.tvDateAr.text = Utils.currentDate()

        val viewList = mutableListOf<View>()

        if (10 >= cardList.size) {

            val pdfInvoiceAdapter = PdfInvoiceAdapter(requireActivity(), cardList,pdfInvoiceBinding.tvTotalAmount,null)
            pdfInvoiceBinding.rvItems.setLayoutManager(LinearLayoutManager(requireActivity()))
            pdfInvoiceBinding.rvItems.setAdapter(pdfInvoiceAdapter)

            viewList.add(pdfInvoiceBinding.root)

        }
        else {

            pdfInvoiceBinding.mcvTotalPrice.visibility = View.GONE

            val chunkedList = cardList.chunked(10)

            val pdfInvoiceAdapterFirstPart = PdfInvoiceAdapter(requireActivity(), chunkedList[0],null,null)
            pdfInvoiceBinding.rvItems.setLayoutManager(LinearLayoutManager(requireActivity()))
            pdfInvoiceBinding.rvItems.setAdapter(pdfInvoiceAdapterFirstPart)
            viewList.add(pdfInvoiceBinding.root)

            for (i in 1 until chunkedList.size) {

                val pdfInvoiceSecondBinding = PdfInvoiceSecondBinding.inflate(LayoutInflater.from(context))

                val tvTotalAmount: TextView? = if (i+1 == chunkedList.size) pdfInvoiceSecondBinding.tvTotalAmount else null

                val pdfInvoiceAdapterSecondPart =  PdfInvoiceAdapter(requireActivity(), chunkedList[i],tvTotalAmount,pdfInvoiceSecondBinding.mcvTotalPrice)
                pdfInvoiceSecondBinding.rvItems.setLayoutManager(LinearLayoutManager(requireActivity()))
                pdfInvoiceSecondBinding.rvItems.setAdapter(pdfInvoiceAdapterSecondPart)
                viewList.add(pdfInvoiceSecondBinding.root)

            }
        }

        PdfGenerator.getBuilder()
            .setContext(requireActivity())
            .fromViewSource()
            .fromViewList(viewList)
            .setFileName(pdfName)
            .setFolderNameOrPath(path)
            .actionAfterPDFGeneration(PdfGenerator.ActionAfterPDFGeneration.NONE)
            .build ( object : PdfGeneratorListener() {

                override fun onFailure(failureResponse: FailureResponse?) {
                    super.onFailure(failureResponse)

                    Utils.E("Failed to generate")
                }

                override fun onStartPDFGeneration() {

                    Utils.E("Start to generate")

                }

                override fun onFinishPDFGeneration() {

                    Utils.E("Finish to generate")

                }

                override fun showLog(log: String?) {
                    super.showLog(log)

                    Utils.E("Show to generate $log")
                }

                override fun onSuccess(response: SuccessResponse?) {

                    super.onSuccess(response)
                    Utils.E("Success to generate")
                }
            })

        // update in the order table
        updateOrder()

        // remove rfid data of product
        removeRfIdData()

    }

    // remove rfid data of product
    private fun removeRfIdData() {

        for (cardData in cardList) {

            cardData.productUUID?.let { Utils.removeRfidCode(it) }
        }

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    // Open Setting Dialog
    private fun showOpenSettingDialog() {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogOpenSettingBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvTitle.text = getString(R.string.permission_request)
        dialogBinding.tvMessage.text =
            getString(R.string.we_need_your_permission_to_access_storage_services_in_order_to_provide_the_full_functionality_of_our_app_your_cooperation_is_appreciated)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentCartBinding.inflate(layoutInflater)

        // Setting the savedInstanceState Data when language switches
        binding?.etFirstName?.setText(savedInstanceState?.getString(firstName) ?: "")
        binding?.etLastName?.setText(savedInstanceState?.getString(lastName) ?: "")
        binding?.etAddress?.setText(savedInstanceState?.getString(address) ?: "")
        binding?.etPhoneNumber?.setText(savedInstanceState?.getString(phoneNumber) ?: "")
        binding?.etZipCode?.setText(savedInstanceState?.getString(zipCode) ?: "")

        // Calling the setState method for Setting the state when fragment recreate
        savedInstanceState?.getString(state)?.let { setState(it) }

        // Hide the Search option
        (requireActivity() as DashboardActivity).binding?.mcvSearch?.visibility = View.INVISIBLE

        //set the Clicks , initialization and setup
        lifecycleScope.launch(Dispatchers.Main) {
            init()
        }

        return binding?.root
    }

    //Setting Up Card List Recycler View
    private fun setUpCardListRecyclerView() {

        orderUUID = arguments?.getString(Constants.orderUUID)

        if (orderUUID != null) {

            binding?.llSaveToLater?.visibility = View.GONE
            binding?.llBarcode?.visibility = View.GONE

            val cardDataList = ArrayList<CartDataModel>()

            val orderDataModel = Utils.getOrderByUUID(orderUUID!!)

            val productUUIDList = orderDataModel.productUUIDUri?.split(",")

            val isListNotEmpty = orderDataModel.productQuantityUri?.isNotEmpty() == true

            val productQuantityList = orderDataModel.productQuantityUri?.split(",")

            if (productUUIDList != null && productQuantityList != null) for (i in productUUIDList.indices) {

//                Utils.E("Product quantity is ##${productQuantityList[i]}##")

                val cartDataModel = CartDataModel()
                cartDataModel.productUUID = productUUIDList[i]
                cartDataModel.productQuantity = if (isListNotEmpty) productQuantityList[i].toInt() else 1
                cartDataModel.cartUUID = orderDataModel.orderUUID

                cardDataList.add(0,cartDataModel)

            }
            cardList = cardDataList

            cartListAdapter = cardList.let {
                binding?.let { it1 ->
                    CartListAdapter(
                        requireActivity(), it,
                        it1,true
                    )
                }
            }
        }

        else {

            cardList = Utils.GetSession().userUUID?.let { Utils.getCartThroughUserUUID(it) } ?: ArrayList()

            if (cardList.isNotEmpty()) {

                cartListAdapter = cardList.let {
                    binding?.let { it1 ->
                        CartListAdapter(
                            requireActivity(), it,
                            it1,false
                        )
                    }
                }

            }
            // If cart is empty
            else {

                GetProgressBar.getInstance(requireActivity())?.dismiss()

                binding?.rlCartManagement?.visibility = View.GONE
                binding?.llCartEmpty?.visibility = View.VISIBLE

            }
        }

        binding?.rvCartList?.layoutManager = LinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL, false
        )
        binding?.rvCartList?.adapter = cartListAdapter

    }

    //Setting Up Address List Recycler View
    private fun setUpAddressListRecyclerView() {

        val addressList =
            Utils.GetSession().userUUID?.let { Utils.getAllAddressThroughUserUUID(it) }

        // Address list is empty
        if (addressList?.isEmpty() == true || addressList == null) {

            binding?.ivNoAddress?.visibility = View.VISIBLE

        } else {

            binding?.ivNoAddress?.visibility = View.GONE
            addressListAdapter =

                binding?.let {
                    CartAddressAdapter(
                        requireActivity(),
                        addressList,
                        it,
                        selectedAddressData,
                        object : AddressSelectionStatus {

                            override fun addressSelected(addressDataModel: AddressDataModel) {
                                selectedAddressData = addressDataModel

                                binding?.radioButton?.isChecked = false
                                binding?.etFirstName?.setText(addressDataModel.firstName)
                                binding?.etLastName?.setText(addressDataModel.lastName)
                                binding?.etAddress?.setText(addressDataModel.address)
                                binding?.etPhoneNumber?.setText(addressDataModel.phoneNumber)
                                binding?.etZipCode?.setText(addressDataModel.zipCode)
                            }

                            override fun addressUnselected() {

                                selectedAddressData = null
//                                binding?.radioButton?.isChecked = true

                                // Make empty all the edittext
                                removeData()

                            }
                        }
                    )
                }

            binding?.rvAddressList?.layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            binding?.rvAddressList?.adapter = addressListAdapter
        }
    }

    // Make empty all the edittext
    private fun removeData() {

        binding?.etFirstName?.setText("")
        binding?.etLastName?.setText("")
        binding?.etAddress?.setText("")
        binding?.etPhoneNumber?.setText("")
        binding?.etZipCode?.setText("")

    }

    //setting the selector on material card view
    private fun setFocusChangeLis(editText: EditText, materialCardView: MaterialCardView) {

        editText.setOnFocusChangeListener { _, hasFocus ->

            if (hasFocus) {
                materialCardView.strokeColor = requireActivity().getColor(R.color.theme)
            } else {
                materialCardView.strokeColor = requireActivity().getColor(R.color.text_hint)
            }
        }
    }

    // Removing Error when text entered
    private fun setTextChangeLis(editText: EditText, textView: TextView) {

        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s.toString().isNotEmpty()) {
                    textView.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    //Setting the underline on "Back To Home" textView
    private fun setUnderLine() {

        // Create a SpannableString with underline style
        val underlinedText =
            SpannableString(requireActivity().getString(R.string.back_to_main_page))
        underlinedText.setSpan(UnderlineSpan(), 0, underlinedText.length, 0)

        // Set the SpannableString to the TextView
        binding?.tvBackToHomePage?.text = underlinedText
    }

    // SetState method for Setting the state when fragment recreate
    private fun setState(state: String) {

        if (state == information) {

            binding?.rlInformation?.visibility = View.VISIBLE
            binding?.llConfirmation?.visibility = View.GONE
            binding?.rlVerification?.visibility = View.GONE
            currentState = information
            binding?.progressBar?.progress = 66


        } else if (state == confirmation) {

            binding?.rlInformation?.visibility = View.GONE
            binding?.rlVerification?.visibility = View.GONE
            binding?.llProgressStatusName?.visibility = View.GONE
            binding?.progressBar?.visibility = View.GONE
            binding?.llConfirmation?.visibility = View.VISIBLE
            currentState = confirmation


        } else if (state == verification) {

            binding?.rlInformation?.visibility = View.GONE
            binding?.llConfirmation?.visibility = View.GONE
            binding?.rlVerification?.visibility = View.VISIBLE


        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setBarcodeEditText() {

        binding?.etBarcode?.requestFocus()

        val editText = binding?.etBarcode

        // Disable keyboard on touch
        editText?.setOnTouchListener { v, event ->

            editText.requestFocus()

            // Hide the keyboard
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)

            true // Return true to consume the touch event
        }

        editText?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                editText.requestFocus() // Re-request focus if it is lost
            }
        }

    }

    //set the Clicks , initialization and setup
    private suspend fun init() {

        setBarcodeEditText()

        // Set underline on Back to Home page
        setUnderLine()

        // Setting the Selector on Material Card View When EditText has focus
        binding?.etFirstName?.let {
            binding?.mcvFirstName?.let { it1 ->
                setFocusChangeLis(
                    it,
                    it1
                )
            }
        }
        binding?.mcvLastName?.let { binding?.etLastName?.let { it1 -> setFocusChangeLis(it1, it) } }
        binding?.mcvAddress?.let { binding?.etAddress?.let { it1 -> setFocusChangeLis(it1, it) } }
        binding?.mcvPhoneNumber?.let {
            binding?.etPhoneNumber?.let { it1 ->
                setFocusChangeLis(
                    it1,
                    it
                )
            }
        }

        binding?.mcvZipCode?.let { binding?.etZipCode?.let { it1 -> setFocusChangeLis(it1, it) } }

        //Removing the Error When text Entered
        binding?.etFirstName?.let {
            binding?.tvFirstNameError?.let { it1 ->
                setTextChangeLis(
                    it,
                    it1
                )
            }
        }
        binding?.tvLastNameError?.let {
            binding?.etLastName?.let { it1 ->
                setTextChangeLis(
                    it1,
                    it
                )
            }
        }
        binding?.tvPhoneNumberError?.let {
            binding?.etPhoneNumber?.let { it1 ->
                setTextChangeLis(
                    it1,
                    it
                )
            }
        }
        binding?.etAddress?.let {
            binding?.tvAddressError?.let { it1 ->
                setTextChangeLis(
                    it,
                    it1
                )
            }
        }
        binding?.tvZipCodeError?.let {
            binding?.etZipCode?.let { it1 ->
                setTextChangeLis(
                    it1,
                    it
                )
            }
        }

        //Setting Up Card List Recycler View
        val jobCart = lifecycleScope.launch(Dispatchers.Main) { setUpCardListRecyclerView() }

        jobCart.join()

        //Setting Up Address List Recycler View
        val jobAddress = lifecycleScope.launch(Dispatchers.Main) { setUpAddressListRecyclerView() }

        // Set progress value to 33 percent
        binding?.progressBar?.progress = 33

        // Set Click on Checkout button
        binding?.llCheckOut?.setOnClickListener(this)

        // Set Click on Save button
        binding?.mcvSave?.setOnClickListener(this)

        binding?.mcvClear?.setOnClickListener(this)

        binding?.mcvAdd?.setOnClickListener(this)

        // Set Click on Place Order button
        binding?.mcvPlaceOrder?.setOnClickListener(this)

        binding?.llSaveToLater?.setOnClickListener(this)

        // Set Click on Back To Home  TextView
        binding?.tvBackToHomePage?.setOnClickListener(this)

        // Set Click on Radio Button
        binding?.llRadioButton?.setOnClickListener(this)

        // Set Click on Open Button
        binding?.mcvOpen?.setOnClickListener(this)

        // Set Click on Print Button
        binding?.mcvPrint?.setOnClickListener(this)

        jobAddress.join()

//        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    // Add new Address
    private fun addNewAddress() {

        val addressDataModel = AddressDataModel()

        addressDataModel.addressUUID = Utils.generateUUId()
        addressDataModel.firstName = binding?.etFirstName?.text.toString().trim()
        addressDataModel.lastName = binding?.etLastName?.text.toString().trim()
        addressDataModel.address = binding?.etAddress?.text.toString().trim()
        addressDataModel.phoneNumber = binding?.etPhoneNumber?.text.toString().trim()
        addressDataModel.zipCode = binding?.etZipCode?.text.toString().trim()
        addressDataModel.userUUID = Utils.GetSession().userUUID

        // Make empty all the edittext
        removeData()

        Utils.insertAddressInAddressTable(addressDataModel)

        selectedAddressData = addressDataModel

        //Setting Up Address List Recycler View
        setUpAddressListRecyclerView()

    }

    // Show phone number already exist error
    private fun showPhoneNumberAlreadyExistError() {

        binding?.tvPhoneNumberError?.also {

            it.visibility = View.VISIBLE
            it.text = requireActivity().getString(R.string.mobile_number_already_exist)
            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.top_to_bottom))
        }
    }

    // Update Address
    private fun updateAddress() {

        val addressDataModel = AddressDataModel()

        addressDataModel.addressUUID = selectedAddressData?.addressUUID
        addressDataModel.firstName = binding?.etFirstName?.text.toString().trim()
        addressDataModel.lastName = binding?.etLastName?.text.toString().trim()
        addressDataModel.address = binding?.etAddress?.text.toString().trim()
        addressDataModel.phoneNumber = binding?.etPhoneNumber?.text.toString().trim()
        addressDataModel.zipCode = binding?.etZipCode?.text.toString().trim()
        addressDataModel.userUUID = Utils.GetSession().userUUID

        Utils.updateAddressInTheAddressTable(addressDataModel)

        selectedAddressData = addressDataModel

        //Setting Up Address List Recycler View
        setUpAddressListRecyclerView()

        Utils.T(requireActivity(), getString(R.string.address_updated_successfully))

    }

    // Check whether we have to add new address or update the address , and act accordingly
    private fun checkIfAddNewAddressOrUpdateAddress() {

        val phoneNumber = binding?.etPhoneNumber?.text.toString().trim()

        // Update address
        if (selectedAddressData != null) {

            val isPhoneNumberExist = selectedAddressData!!.addressUUID?.let {
                Utils.isPhoneNumberExistInAddressTableAcceptMine(
                    phoneNumber,
                    it
                )
            }

            if (isPhoneNumberExist == true) {
                showPhoneNumberAlreadyExistError()

            } else {

                // Update Address
                updateAddress()
            }

        }
        // Add new address
        else {

            // Check if phone Number exist
            if (!Utils.isPhoneNumberExistInAddressTable(phoneNumber)) {

                //Add new address
                addNewAddress()

            } else {
                showPhoneNumberAlreadyExistError()
            }
        }
    }

    //Validating Sign Up details
    private fun validate() {

        errorValidationModels.clear()

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty, binding?.etFirstName, binding?.tvFirstNameError
            )
        )
        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty, binding?.etLastName, binding?.tvLastNameError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty, binding?.etAddress, binding?.tvAddressError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Phone, binding?.etPhoneNumber, binding?.tvPhoneNumberError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.ZipCode, binding?.etZipCode, binding?.tvZipCodeError
            )
        )

        val validation: Validation? = Validation.instance
        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModels)
        if (resultReturn?.aBoolean == true) {

            // Check whether we have to add new address or update the address , and act accordingly
            checkIfAddNewAddressOrUpdateAddress()

        } else {
            resultReturn?.errorTextView?.visibility = View.VISIBLE
            if (resultReturn?.type === Validation.Type.EmptyString) {
                resultReturn.errorTextView?.text = resultReturn.errorMessage
            } else {
                resultReturn?.errorTextView?.text = validation?.errorMessage
                val animation =
                    AnimationUtils.loadAnimation(requireActivity(), R.anim.top_to_bottom)
                resultReturn?.errorTextView?.startAnimation(animation)
                validation?.EditTextPointer?.requestFocus()

                val imm =
                    requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    // For recreating the activity data store
    override fun onSaveInstanceState(outState: Bundle) {

        outState.putString(firstName, binding?.etFirstName?.text.toString())
        outState.putString(lastName, binding?.etLastName?.text.toString())
        outState.putString(address, binding?.etAddress?.text.toString())
        outState.putString(phoneNumber, binding?.etPhoneNumber?.text.toString())
        outState.putString(zipCode, binding?.etZipCode?.text.toString())

        outState.putString(state, currentState)

        super.onSaveInstanceState(outState)

    }

    // update Order
    private fun updateOrder() {

//        val newOrderDataModel = if (orderUUID != null) Utils.getOrderByUUID(orderUUID!!) else Utils.getOrderUUID(requireActivity())
//            ?.let { Utils.getOrderByUUID(it) }

//        Utils.E("****************${newOrderDataModel?.orderUUID}&&&&&&&&&&&&&&&&&&&")

        val confirmOrderDataModel = OrderDataModel()

        confirmOrderDataModel.orderUUID = newOrderDataModel?.orderUUID

        confirmOrderDataModel.addressUUID = selectedAddressData?.addressUUID

        confirmOrderDataModel.status = Constants.Confirm

        confirmOrderDataModel.pdfName = pdfName

        confirmOrderDataModel.date = Utils.currentDate()

        Utils.updateOrderStatus(confirmOrderDataModel)

        val newOrderUUID = Utils.getOrderUUID(requireActivity())

        Utils.E("######${newOrderUUID} ${orderUUID}######")

        if (orderUUID == null || newOrderUUID == orderUUID) {

            // Cart delete
            Utils.GetSession().userUUID?.let { Utils.deleteCart(it) }

            // generate new order UUID
            Utils.storeOrderUUID(requireActivity(), Utils.generateUUId())

        }

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    //Handle all the clicks
    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {

        // Set Click on Add to Card button
        if (v == binding?.llCheckOut) {

            binding?.tvTotalPriceInformation?.text = binding?.tvTotalPriceVerification?.text

            binding?.rlVerification?.visibility = View.GONE
            binding?.rlInformation?.visibility = View.VISIBLE
            binding?.progressBar?.progress = 66 // Set progress value
            (requireActivity() as DashboardActivity).currentState = information
            currentState = information

        }

        // Set Click on Save button
        else if (v == binding?.mcvSave) {
            validate()
        }

        // Set Click on Back To Home  TextView
        else if (v == binding?.tvBackToHomePage) {

            // Back to home page
            (requireActivity() as DashboardActivity).navController?.popBackStack(R.id.home, false)
        }

        // Set Click Place Order button
        else if (v == binding?.mcvPlaceOrder) {

            if (selectedAddressData != null) {

                (requireActivity() as DashboardActivity).currentState = confirmation
                currentState = confirmation

                binding?.llProgressStatusName?.visibility = View.GONE
                binding?.progressBar?.visibility = View.GONE
                binding?.rlInformation?.visibility = View.GONE
                binding?.llConfirmation?.visibility = View.VISIBLE

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                    // For Android 10 and below, request traditional storage permissions
                    customPermissionLauncher.launch(permissionsForExternalStorage)

                } else {

                    // For Android 11 and above, request manage external storage permission
                    if (Environment.isExternalStorageManager()) {

                        generatePDF()

                    } else {

                        requestStoragePermission()

                    }
                }

            } else {

                Utils.T(requireActivity(), getString(R.string.please_select_address))
            }
        }

        // Click on Radio button
        else if (v == binding?.llRadioButton) {

            if (selectedAddressData == null) {

                if (binding?.radioButton?.isChecked == true) {
                    binding?.radioButton?.isChecked = false
                } else {
                    binding?.radioButton?.isChecked = true
                }
            } else {

                selectedAddressData = null
                binding?.radioButton?.isChecked = true

                addressListAdapter?.unSelectAddress()

            }
        }

        // Click on Share button
        else if (v == binding?.mcvOpen) {

            val intent = Intent(requireActivity(),PdfViewActivity::class.java)
            intent.putExtra(Constants.pdfName,pdfName)
            startActivity(intent)

        }

        // Click on print button
        else if (v == binding?.mcvPrint) {

            val pdfFile = File(requireActivity().getExternalFilesDir(null), "$path${pdfName}.pdf")

            if (pdfFile.exists()) {

                Utils.printPdf(requireActivity(),pdfFile)
            }
            else {
                Utils.T(requireActivity(), getString(R.string.file_not_found))
            }
        }

        else if (v == binding?.llSaveToLater) {

            Utils.GetSession().userUUID?.let { Utils.deleteCart(it) }

            // generate new order UUID
            Utils.storeOrderUUID(requireActivity(), Utils.generateUUId())

            binding?.rlCartManagement?.visibility = View.GONE

            binding?.llCartEmpty?.visibility = View.VISIBLE

        }

        else if (v == binding?.mcvClear) {
            binding?.etBarcode?.setText("")
        }

        else if (v == binding?.mcvAdd) {

            val barcodeData = binding?.etBarcode?.text.toString().trim()

            if (barcodeData.isNotEmpty()) {

                if (Utils.isBarcodeExist(barcodeData) == true) {

                    val productUUID = Utils.getProductUUIDByBarcode(barcodeData)

                    val isProductExistInCart = Utils.GetSession().userUUID?.let { productUUID?.let { it1 ->
                        Utils.isProductExistInCartTable(it,
                            it1
                        )
                    } }

                    if (isProductExistInCart == true) {

                        Utils.T(requireActivity(),
                            getString(R.string.a_product_with_this_barcode_already_exists_in_the_cart))

                    }
                    else {

                        val cartDataModel = CartDataModel()
                        cartDataModel.cartUUID = Utils.generateUUId()
                        cartDataModel.productUUID = productUUID
                        cartDataModel.productQuantity = 1
                        cartDataModel.userUUID = Utils.GetSession().userUUID

                        Utils.insertProductInCartTable(cartDataModel)

                        val orderUUID = Utils.getOrderUUID(requireActivity())

                        val orderDataModel = orderUUID?.let { Utils.getOrderByUUID(it) }

                        val productUUIDList = orderDataModel?.productUUIDUri?.split(",")?.toMutableList()

                        productUUID?.let { productUUIDList?.add(it) }

                        val productQuantityList = orderDataModel?.productQuantityUri?.split(",")?.toMutableList()

                        productQuantityList?.add("1")

                        val newOrderDataModel = OrderDataModel()

                        val productDataModel = productUUID?.let { Utils.getProductThroughProductUUID(it) }

                        newOrderDataModel.orderUUID = orderUUID
                        newOrderDataModel.productUUIDUri = productUUIDList?.joinToString()?.replace(" ","")
                        newOrderDataModel.productQuantityUri = productQuantityList?.joinToString()?.replace(" ","")
                        newOrderDataModel.totalAmount = productDataModel?.productPrice?.let { orderDataModel?.totalAmount?.plus(it) }

                        Utils.updateOrder(newOrderDataModel)

                        binding?.etBarcode?.setText("")

                        Utils.T(requireActivity(),
                            getString(R.string.product_has_been_added_to_the_cart))

                        GetProgressBar.getInstance(requireActivity())?.show()

                        lifecycleScope.launch(Dispatchers.Main) {
                            init()
                        }

                    }

                }

                else {

                    Utils.T(requireActivity(), getString(R.string.no_product_exists_with_this_barcode))
                }

            }
            else {

                Utils.T(requireActivity(), getString(R.string.the_barcode_field_is_empty))

            }
        }

    }

}