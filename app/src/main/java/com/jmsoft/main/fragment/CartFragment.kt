package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.Database.AddressDataModel
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.ContactDataModel
import com.jmsoft.Utility.Database.OrderDataModel
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants.Companion.address
import com.jmsoft.basic.UtilityTools.Constants.Companion.confirmation
import com.jmsoft.basic.UtilityTools.Constants.Companion.firstName
import com.jmsoft.basic.UtilityTools.Constants.Companion.information
import com.jmsoft.basic.UtilityTools.Constants.Companion.lastName
import com.jmsoft.basic.UtilityTools.Constants.Companion.phoneNumber
import com.jmsoft.basic.UtilityTools.Constants.Companion.state
import com.jmsoft.basic.UtilityTools.Constants.Companion.verification
import com.jmsoft.basic.UtilityTools.Constants.Companion.zipCode
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentCartBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CartListAdapter
import com.jmsoft.main.adapter.CartAddressAdapter
import com.jmsoft.main.`interface`.AddressSelectionStatus
import com.jmsoft.main.`interface`.BluetoothOffCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.jmsoft.Utility.pdf_helper.FailureResponse
import com.jmsoft.Utility.pdf_helper.PdfGenerator
import com.jmsoft.Utility.pdf_helper.PdfGeneratorListener
import com.jmsoft.Utility.pdf_helper.SuccessResponse
import com.jmsoft.basic.UtilityTools.Constants.Companion.Data
import com.jmsoft.basic.UtilityTools.Utils.getCartThroughUserUUID
import com.jmsoft.databinding.InvoiceItemBinding
import com.jmsoft.databinding.PdfInvoiceBinding
import com.jmsoft.main.adapter.PdfInvoiceAdapter
import com.jmsoft.main.model.Data
import kotlinx.coroutines.NonDisposableHandle.parent

class CartFragment : Fragment(), View.OnClickListener {

    var binding: FragmentCartBinding? = null

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

    private var content: PdfGenerator.Build? = null

    private var cardList:ArrayList<CartDataModel>? = null

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

            generatePDF()

        }
    }

    private fun generatePDF() {

        content?.build(object : PdfGeneratorListener() {

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

                Utils.E("Show to generate")

            }

            override fun onSuccess(response: SuccessResponse?) {

                super.onSuccess(response)
                Utils.E("Success to generate")

            }
        })
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

        cardList = Utils.GetSession().userUUID?.let { Utils.getCartThroughUserUUID(it) }

        if (cardList?.isNotEmpty() == true) {

            val adapter = cardList.let {
                binding?.let { it1 ->
                    it?.let { it2 ->
                        CartListAdapter(
                            requireActivity(), it2,
                            it1,
                            CoroutineScope(SupervisorJob() + Dispatchers.Default)
                        )
                    }
                }
            }

            binding?.rvCartList?.layoutManager = LinearLayoutManager(
                requireActivity(),
                RecyclerView.VERTICAL, false
            )
            binding?.rvCartList?.adapter = adapter

        }
        // If cart is empty
        else {

            GetProgressBar.getInstance(requireActivity())?.dismiss()

            binding?.rlCartManagement?.visibility = View.GONE
            binding?.llCartEmpty?.visibility = View.VISIBLE

        }
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

    //set the Clicks , initialization and setup
    private suspend fun init() {

        //Set underline on Back to Home page
        setUnderLine()

        //Setting the Selector on Material Card View When EditText has focus
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

        //Setting Up Address List Recycler View
        val jobAddress = lifecycleScope.launch(Dispatchers.Main) { setUpAddressListRecyclerView() }

        // Set progress value to 33 percent
        binding?.progressBar?.progress = 33

        // Set Click on Checkout button
        binding?.llCheckOut?.setOnClickListener(this)

        // Set Click on Save button
        binding?.mcvSave?.setOnClickListener(this)

        // Set Click on Place Order button
        binding?.mcvPlaceOrder?.setOnClickListener(this)

        // Set Click on Back To Home  TextView
        binding?.tvBackToHomePage?.setOnClickListener(this)

        // Set Click on Radio Button
        binding?.llRadioButton?.setOnClickListener(this)

        // Set Click on Share Button
        binding?.mcvShare?.setOnClickListener(this)

        jobCart.join()
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

    // insert order
    private fun insertOrder() {

        val orderDataModel = OrderDataModel()

        orderDataModel.orderUUID = Utils.generateUUId()

        val productUUIDList = cardList?.map { it.productUUID }?.joinToString()?.replace(" ", "")

        orderDataModel.productUUID = productUUIDList

        val productQuantityList = cardList?.map { it.productQuantity }?.joinToString()?.replace(" ", "")

        orderDataModel.productQuantity = productQuantityList

        orderDataModel.userUUID = Utils.GetSession().userUUID

        orderDataModel.addressUUID = selectedAddressData?.addressUUID

        Utils.insertOrder(orderDataModel)

        // Cart delete
        Utils.GetSession().userUUID?.let { Utils.deleteCart(it) }

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

                GetProgressBar.getInstance(requireActivity())?.show()

                (requireActivity() as DashboardActivity).currentState = confirmation
                currentState = confirmation

                binding?.llProgressStatusName?.visibility = View.GONE
                binding?.progressBar?.visibility = View.GONE
                binding?.rlInformation?.visibility = View.GONE
                binding?.llConfirmation?.visibility = View.VISIBLE

                val cardList = Utils.GetSession().userUUID?.let { getCartThroughUserUUID(it) }
                val pdfInvoiceBinding = PdfInvoiceBinding.inflate(LayoutInflater.from(context))

                pdfInvoiceBinding.tvOrderNo.text = Utils.getRowCount().toString()

                pdfInvoiceBinding.tvClientName.text =
                    "${selectedAddressData?.firstName} ${selectedAddressData?.lastName}"

                pdfInvoiceBinding.tvDate.text = Utils.currentDate
                pdfInvoiceBinding.rvItems.setLayoutManager(LinearLayoutManager(requireActivity()))

                pdfInvoiceBinding.rvItems.setAdapter(cardList?.let {
                    PdfInvoiceAdapter(
                        requireActivity(), pdfInvoiceBinding.tvTotalAmount,
                        it
                    )
                })

                content = PdfGenerator.getBuilder()
                    .setContext(requireActivity())
                    .fromViewSource()
                    .fromView(pdfInvoiceBinding.root)
                    .setFileName(Utils.generateUUId())
                    .setFolderNameOrPath("MyFolder/MyDemoList/")
                    .actionAfterPDFGeneration(PdfGenerator.ActionAfterPDFGeneration.OPEN)

                insertOrder()

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
        else if (v == binding?.mcvShare) {

            customPermissionLauncher.launch(permissionsForExternalStorage)

        }

    }

}