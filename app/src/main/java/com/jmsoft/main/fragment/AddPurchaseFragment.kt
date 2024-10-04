package com.jmsoft.main.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.ContactDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.Database.PurchasingDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.FragmentAddPurchaseBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.SupplierDropDownAdapter
import com.jmsoft.main.adapter.SelectedProductAdapter
import com.jmsoft.main.`interface`.SelectedCallback
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddPurchaseFragment : Fragment(), View.OnClickListener, SelectedCallback {

    private lateinit var binding: FragmentAddPurchaseBinding

    private var supplierList = ArrayList<ContactDataModel>()

    private var productNameDropDownAdapter: SupplierDropDownAdapter? = null

    private var selectedSupplierUUID: String? = null

    private var purchasingDataModel: PurchasingDataModel? = null

    private var selectedProductList = ArrayList<String>()

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

        binding.etTotalAmount?.let {
            binding.tvTotalAmountError?.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setSupplierDropDown() {

        supplierList = Utils.GetSession().userUUID?.let { Utils.getAllContactThroughUserUUID(it) }?: ArrayList()

        productNameDropDownAdapter = SupplierDropDownAdapter(
            requireActivity(),
            supplierList,
            this
        )

        if (selectedSupplierUUID != null) {

            val index = supplierList.indexOfFirst { it.contactUUID == selectedSupplierUUID }

            productNameDropDownAdapter?.selectedSupplierPosition = index

            val contactDataModel = supplierList[index].contactUUID?.let { Utils.getContactByUUID(it) }
            binding.tvSupplierError?.text = "${contactDataModel?.firstName} ${contactDataModel?.lastName}"

        }

        binding.rvSupplier?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvSupplier?.adapter = productNameDropDownAdapter

    }

    @SuppressLint("SetTextI18n")
    private fun checkAddOrEditStatus() {

        val purchasingUUID = arguments?.getString(Constants.purchasingUUID)

        if (purchasingUUID != null) {

            binding.tvTitle?.text = getString(R.string.update_purchase)

            purchasingDataModel = Utils.getPurchaseByUUID(purchasingUUID)

            if (purchasingDataModel != null) {

                if (Utils.SelectedProductUUIDList.getSize() == 0) {

                    val productList = purchasingDataModel?.productUUIDUri?.split(",")?.toMutableList() as ArrayList<String>

                    Utils.SelectedProductUUIDList.setProductList(productList)

                }


                binding.etOrderNo?.setText(purchasingDataModel?.orderNo)
                selectedSupplierUUID = purchasingDataModel?.supplierUUID

                val contactDataModel = purchasingDataModel?.supplierUUID?.let {
                    Utils.getContactByUUID(
                        it
                    )
                }

                binding.tvSupplier?.text = "${contactDataModel?.firstName} ${contactDataModel?.lastName}"

                val totalAmount =
                    purchasingDataModel?.totalAmount?.let { Utils.getThousandSeparate(it) }

                binding.etTotalAmount?.setText(totalAmount)

                binding.tvDate?.text = purchasingDataModel?.date

            }
        }
    }
    private fun setSelectedProductRecyclerView() {

        selectedProductList = ArrayList(Utils.SelectedProductUUIDList.getProductList())

        Utils.SelectedProductUUIDList.clearList()

        if (selectedProductList.isNotEmpty()) {

            binding.tvProductName?.visibility = View.GONE
            binding.rvProduct?.visibility = View.VISIBLE

            val adapter = SelectedProductAdapter(requireActivity(),selectedProductList,binding)

            binding.rvProduct?.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)

            binding.rvProduct?.adapter = adapter

        }
    }

    override fun onResume() {
        super.onResume()

        checkAddOrEditStatus()

        setSelectedProductRecyclerView()
    }

    private fun init() {

        setFocusChangeLis()

        setTextChangLis()

        setSupplierDropDown()

        binding.llSupplier?.setOnClickListener(this)

        binding.mcvBackBtn?.setOnClickListener(this)

        binding.mcvDate?.setOnClickListener(this)

        binding.mcvSave?.setOnClickListener(this)

        binding.mcvAdd?.setOnClickListener(this)

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    // Show or hide Product name drop down
    private fun showOrHideSupplierDropDown() {

        if (binding.mcvSupplierList?.visibility == View.VISIBLE) {

            binding.ivSupplier.let { it?.let { it1 -> Utils.rotateView(it1, 0f) } }
            binding.mcvSupplierList.let { it?.let { it1 -> Utils.collapseView(it1) } }

        } else {

            binding.ivSupplier.let { it?.let { it1 -> Utils.rotateView(it1, 180f) } }
            binding.mcvSupplierList.let { it?.let { it1 -> Utils.expandView(it1) } }

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
//        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()

    }

    private fun addOrEditPurchase() {

        val purchaseDataModel = PurchasingDataModel()

        purchaseDataModel.purchasingUUID =
            if (purchasingDataModel != null) purchasingDataModel!!.purchasingUUID else Utils.generateUUId()
        purchaseDataModel.productUUIDUri =  selectedProductList.joinToString(",").replace(" ","")
        purchaseDataModel.orderNo = binding.etOrderNo?.text.toString().trim()
        purchaseDataModel.supplierUUID = selectedSupplierUUID
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
                Validation.Type.EmptyArrayList, selectedProductList.size, binding.tvProductNameError,null
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etOrderNo, binding.tvOrderNoError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.EmptyTextView, binding.tvSupplier, binding.tvSupplierError
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View?) {

        when (view) {

            binding.mcvBackBtn -> {

                (requireActivity() as DashboardActivity).navController?.popBackStack()

            }

            binding.llSupplier -> {
                showOrHideSupplierDropDown()
            }

            binding.mcvDate -> {
                showDateDialog()
            }

            binding.mcvSave -> {
                validate()
            }

            binding.mcvAdd -> {

                Utils.SelectedProductUUIDList.setProductList(ArrayList(selectedProductList))

//                requireActivity().runOnUiThread {
//                    if (isAdded && !requireActivity().isFinishing) {
//                        GetProgressBar.getInstance(requireActivity())?.show()
//                    }
//                }


                //Giving the fragment status
                val bundle = Bundle()
                bundle.putBoolean(Constants.addPurchase,true)

                (requireActivity() as DashboardActivity).navController?.navigate(R.id.product,bundle)

            }

        }

    }

    @SuppressLint("SetTextI18n")
    override fun selected(data: Any) {

        val contactDataModel = data as ContactDataModel

        if (binding.tvSupplier?.text.toString() != "${contactDataModel.firstName} ${contactDataModel.lastName}") {

            selectedSupplierUUID = contactDataModel.contactUUID
            binding.tvSupplier?.text = "${contactDataModel.firstName} ${contactDataModel.lastName}"
            binding.tvSupplierError?.visibility = View.GONE

            showOrHideSupplierDropDown()
        }
    }

    override fun unselect() {

//        selectedSupplierUUID = null
//        binding.tvSupplier?.text = ""
//        binding.tvSupplierError?.visibility = View.GONE
//
//        showOrHideSupplierDropDown()
    }

}