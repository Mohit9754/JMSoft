package com.jmsoft.main.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.Database.PurchasingDataModel
import com.jmsoft.Utility.Database.StockLocationDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.FragmentAddPurchaseBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.ProductNameDropDownAdapter
import com.jmsoft.main.adapter.StockLocationDropdownAdapter
import com.jmsoft.main.`interface`.SelectedCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okio.Utf8
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddPurchaseFragment : Fragment(), View.OnClickListener, SelectedCallback {

    lateinit var binding: FragmentAddPurchaseBinding

    private var productNameDropDownList = ArrayList<ProductDataModel>()

    private var productNameDropDownAdapter: ProductNameDropDownAdapter? = null

    private var selectedProductNameUUID: String? = null

    private var purchasingDataModel: PurchasingDataModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentAddPurchaseBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    private fun setFocusChangeLis() {

        binding.etOrderNo?.let {
            binding.mcvOrderNo?.let { it1 ->
                Utils.setFocusChangeLis(
                    requireActivity(), it,
                    it1
                )
            }
        }

        binding.etSupplier?.let {
            binding.mcvSupplier?.let { it1 ->
                Utils.setFocusChangeLis(
                    requireActivity(), it,
                    it1
                )
            }
        }

        binding.etTotalAmount?.let {
            binding.mcvTotalAmount?.let { it1 ->
                Utils.setFocusChangeLis(
                    requireActivity(), it,
                    it1
                )
            }
        }

    }

    private fun setTextChangLis() {

        binding.etOrderNo?.let {
            binding.tvOrderNoError?.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

        binding.etSupplier?.let {
            binding.tvSupplierError?.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

        binding.etTotalAmount?.let {
            binding.tvTotalAmountError?.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

    }

    // Set product name dropdown
    private suspend fun setProductNameDropDown() {

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.getAllProductName()
        }

        productNameDropDownList = result.await()

        productNameDropDownAdapter = ProductNameDropDownAdapter(
            requireActivity(),
            productNameDropDownList,
            this
        )

        if (selectedProductNameUUID != null) {

            val index = productNameDropDownList.indexOfFirst { it.productUUID == selectedProductNameUUID }

            productNameDropDownAdapter?.selectedProductNamePosition = index

            val productDataModel = productNameDropDownList[index].productUUID?.let { Utils.getProductThroughProductUUID(it) }
            binding.tvProductName?.text = productDataModel?.productName

//            binding.tvProductName?.text =
        }

        binding.rvProductName?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvProductName?.adapter = productNameDropDownAdapter

    }

    private fun checkAddOrEditStatus() {

        val purchasingUUID = arguments?.getString(Constants.purchasingUUID)

        if (purchasingUUID != null) {

            purchasingDataModel = Utils.getPurchaseByUUID(purchasingUUID)

            if (purchasingDataModel != null) {

                selectedProductNameUUID = purchasingDataModel?.productUUID
                binding.etOrderNo?.setText(purchasingDataModel!!.orderNo)
                binding.etSupplier?.setText(purchasingDataModel!!.supplier)

                val totalAmount =
                    purchasingDataModel!!.totalAmount?.let { Utils.getThousandSeparate(it) }

                binding.etTotalAmount?.setText(totalAmount)

                binding.tvDate?.text = purchasingDataModel!!.date

            }
        }

    }

    private fun init() {

        checkAddOrEditStatus()

        setFocusChangeLis()

        setTextChangLis()

        lifecycleScope.launch(Dispatchers.Main) {
            setProductNameDropDown()
        }

        binding.llProductName?.setOnClickListener(this)

        binding.mcvBackBtn?.setOnClickListener(this)

        binding.mcvDate?.setOnClickListener(this)

        binding.mcvSave?.setOnClickListener(this)

//        if (purchasingDataModel != null)
//            showOrHideProductNameDropDown()

    }

    // Show or hide Product name drop down
    private fun showOrHideProductNameDropDown() {

        if (binding.mcvProductNameList?.visibility == View.VISIBLE) {

            binding.ivProductName.let { it?.let { it1 -> Utils.rotateView(it1, 0f) } }
            binding.mcvProductNameList.let { it?.let { it1 -> Utils.collapseView(it1) } }

        } else {

            binding.ivProductName.let { it?.let { it1 -> Utils.rotateView(it1, 180f) } }
            binding.mcvProductNameList.let { it?.let { it1 -> Utils.expandView(it1) } }

        }
    }

    private fun showDateDialog() {

        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireActivity(),
            { _, year, month, dayOfMonth ->
                // Create a Calendar object with the selected date
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                // Format the selected date as a string (e.g., "dd/MM/yyyy")
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                val selectedDate = dateFormat.format(selectedCalendar.time)

                // Now you can use the selectedDate as needed
                binding.tvDate?.text = selectedDate
                binding.tvDateError?.visibility = View.GONE

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the maximum selectable date to today
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()

    }

    private fun addOrEditPurchase() {

        val purchaseDataModel = PurchasingDataModel()

        purchaseDataModel.purchasingUUID =
            if (purchasingDataModel != null) purchasingDataModel!!.purchasingUUID else Utils.generateUUId()
        purchaseDataModel.productUUID = selectedProductNameUUID
        purchaseDataModel.orderNo = binding.etOrderNo?.text.toString().trim()
        purchaseDataModel.supplier = binding.etSupplier?.text.toString().trim()
        purchaseDataModel.totalAmount = binding.etTotalAmount?.text.toString().toDouble()
        purchaseDataModel.date = binding.tvDate?.text.toString()

        if (purchasingDataModel != null) {

            Utils.updatePurchase(purchaseDataModel)
            Utils.T(requireActivity(), getString(R.string.purchase_updated_successfully))

        } else {

            Utils.addPurchase(purchaseDataModel)
            Utils.T(requireActivity(), getString(R.string.purchase_added_successfully))

        }

        (requireActivity() as DashboardActivity).navController?.popBackStack()

    }

    /* validate input details */
    private fun validate() {

        val errorValidationModel: MutableList<ValidationModel> = ArrayList()

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.EmptyTextView, binding.tvProductName, binding.tvProductNameError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etOrderNo, binding.tvOrderNoError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etSupplier, binding.tvSupplierError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etTotalAmount, binding.tvTotalAmountError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.EmptyTextView, binding.tvDate, binding.tvDateError
            )
        )

        val validation: Validation? = Validation.instance

        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModel)

        if (resultReturn?.aBoolean == true) {

            addOrEditPurchase()

        } else {

            GetProgressBar.getInstance(requireActivity())?.dismiss()

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
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)

                validation?.EditTextPointer = null

            }
        }
    }

    override fun onClick(view: View?) {

        when (view) {

            binding.mcvBackBtn -> {

                (requireActivity() as DashboardActivity).navController?.popBackStack()

            }

            binding.llProductName -> {
                showOrHideProductNameDropDown()
            }

            binding.mcvDate -> {
                showDateDialog()
            }

            binding.mcvSave -> {
                validate()
            }

        }

    }

    override fun selected(data: Any) {

        val productDataModel = data as ProductDataModel

        if (binding.tvProductName?.text.toString() != productDataModel.productName) {

            selectedProductNameUUID = productDataModel.productUUID
            binding.tvProductName?.text = productDataModel.productName
            binding.tvProductNameError?.visibility = View.GONE
            binding.ivProductName.let { it?.let { it1 -> Utils.rotateView(it1, 0f) } }
            binding.mcvProductNameList.let { it?.let { it1 -> Utils.collapseView(it1) } }

        }
    }

    override fun unselect() {

        selectedProductNameUUID = null
        binding.tvProductName?.text = ""
        binding.tvProductNameError?.visibility = View.GONE

        showOrHideProductNameDropDown()
    }

}