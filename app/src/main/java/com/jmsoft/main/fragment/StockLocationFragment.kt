package com.jmsoft.main.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.MetalTypeDataModel
import com.jmsoft.Utility.Database.StockLocationDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.FragmentStockLocationBinding
import com.jmsoft.databinding.ItemAddStockLocationBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.MetalTypeDropdownAdapter
import com.jmsoft.main.adapter.StockLocationAdapter
import com.jmsoft.main.adapter.StockLocationDropdownAdapter
import com.jmsoft.main.`interface`.SelectedCallback

class StockLocationFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentStockLocationBinding
    private var stockLocationList = ArrayList<StockLocationDataModel>()
    private var stockLocationDropdownList = ArrayList<StockLocationDataModel>()
    private var dialogBinding: ItemAddStockLocationBinding? = null
    private var stockLocationDropdownAdapter: StockLocationDropdownAdapter? = null
    private var selectedParentUUID: String? = null
    private var stockLocationAdapter: StockLocationAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentStockLocationBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    private fun getStockLocationList() {

        stockLocationList = Utils.getAllStockLocation()

        stockLocationDropdownList = ArrayList(stockLocationList)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun removeItemFromDropdownList(stockLocationUUID:String) {
        stockLocationDropdownList.removeIf { it.stockLocationUUID == stockLocationUUID }

        stockLocationList.forEach { item ->
            if (item.stockLocationParentUUID == stockLocationUUID) {
                item.stockLocationParentUUID = ""
            }
        }

        setStockLocationRecyclerView()
    }

    private fun setStockLocationRecyclerView() {

        if (stockLocationList.isNotEmpty()) {

            binding.llEmptyStockLocation?.visibility = View.GONE
            binding.mcvStockLocationList?.visibility = View.VISIBLE

            stockLocationAdapter =
                StockLocationAdapter(requireActivity(), stockLocationList, binding, this)

            binding.rvStockLocation?.layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            binding.rvStockLocation?.adapter = stockLocationAdapter

        } else {

            GetProgressBar.getInstance(requireActivity())?.dismiss()
            binding.mcvStockLocationList?.visibility = View.GONE
            binding.llEmptyStockLocation?.visibility = View.VISIBLE

        }
    }

    private fun init() {

        getStockLocationList()
        setStockLocationRecyclerView()

        binding.mcvBackBtn?.setOnClickListener(this)
        binding.mcvAddStockLocation?.setOnClickListener(this)

    }

    private fun setParentRecyclerView() {

        stockLocationDropdownAdapter = StockLocationDropdownAdapter(
            requireActivity(),
            stockLocationDropdownList,
            null,
            true,
            object : SelectedCallback {

                override fun selected(data: Any) {

                    val stockLocationDataModel = data as StockLocationDataModel

                    if (dialogBinding?.tvParent?.text.toString() != stockLocationDataModel.stockLocationName) {

                        selectedParentUUID = stockLocationDataModel.stockLocationUUID
                        dialogBinding?.tvParent?.text = stockLocationDataModel.stockLocationName

                        dialogBinding?.ivStockLocation.let {
                            it?.let { it1 ->
                                Utils.rotateView(
                                    it1,
                                    0f
                                )
                            }
                        }
                        dialogBinding?.mcvParentList.let { it?.let { it1 -> Utils.collapseView(it1) } }

                    }
                }

                override fun unselect() {

//                    selectedMetalTypeUUID = null
//                    binding.tvMetalType.text = ""
//                    binding.tvMetalTypeError.visibility = View.GONE
////                    binding.mcvMetalTypeList?.visibility = View.GONE
//                    showOrHideMetalTypeDropDown()

                }
            }
        )

        if (selectedParentUUID != null) {
            stockLocationDropdownAdapter?.selectedStockLocationPosition =
                stockLocationList.indexOfFirst { it.stockLocationUUID == selectedParentUUID }
        }

        dialogBinding?.rvParent?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        dialogBinding?.rvParent?.adapter = stockLocationDropdownAdapter

    }

    @SuppressLint("NotifyDataSetChanged")
    fun showAddStockLocationDialog(
        position: Int?,
        stockLocationDataModel: StockLocationDataModel?
    ) {

        val dialog = Dialog(requireActivity())

        dialogBinding = ItemAddStockLocationBinding.inflate(LayoutInflater.from(context))

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val parent = dialogBinding!!.root.parent as? ViewGroup
        parent?.removeView(dialogBinding!!.root)

        dialog.setContentView(dialogBinding!!.root)

        if (stockLocationDataModel != null && position != null) {

            dialogBinding?.tvTitle?.text = requireActivity().getString(R.string.edit_stock_location)

            dialogBinding?.tvParent?.text =
                stockLocationList[position].stockLocationParentUUID?.let { Utils.getStockLocation(it) }?.stockLocationName

            selectedParentUUID = stockLocationDataModel.stockLocationParentUUID
            dialogBinding?.etName?.setText(stockLocationDataModel.stockLocationName)
        }

        if (position != null) stockLocationDropdownList.removeAt(position)

        setParentRecyclerView()

        Utils.setFocusChangeLis(requireActivity(), dialogBinding!!.etName, dialogBinding!!.mcvName)

        Utils.addTextChangedListener(
            dialogBinding!!.etName,
            dialogBinding!!.tvNameError
        )

        dialogBinding?.llParent?.setOnClickListener {

            if (dialogBinding?.mcvParentList?.visibility == View.VISIBLE) {

                dialogBinding!!.ivStockLocation.let { Utils.rotateView(it, 0f) }

                dialogBinding!!.mcvParentList.let { Utils.collapseView(it) }

            } else {

                dialogBinding!!.ivStockLocation.let { Utils.rotateView(it, 180f) }
                dialogBinding!!.mcvParentList.let { Utils.expandView(it) }

            }
        }

        dialogBinding!!.mcvSave.setOnClickListener {

            val errorValidationModels: MutableList<ValidationModel> = ArrayList()

            errorValidationModels.add(
                ValidationModel(
                    Validation.Type.Empty,
                    dialogBinding!!.etName,
                    dialogBinding!!.tvNameError
                )
            )

            val validation: Validation? = Validation.instance
            val resultReturn: ResultReturn? =
                validation?.CheckValidation(requireActivity(), errorValidationModels)

            if (resultReturn?.aBoolean == true) {

                dialog.dismiss()

                val newStockLocationDataModel = StockLocationDataModel()
                newStockLocationDataModel.stockLocationUUID =
                    if (stockLocationDataModel != null) stockLocationDataModel.stockLocationUUID else Utils.generateUUId()
                newStockLocationDataModel.stockLocationParentUUID = selectedParentUUID ?: ""
                newStockLocationDataModel.stockLocationName =
                    Utils.capitalizeData(dialogBinding?.etName?.text.toString())

                if (position != null) {

                    Utils.updateStockLocation(newStockLocationDataModel)

                    stockLocationList[position] = newStockLocationDataModel
                    stockLocationAdapter?.notifyDataSetChanged()

                } else {

                    Utils.addStockLocation(newStockLocationDataModel)
                    stockLocationList.add(0, newStockLocationDataModel)
                    setStockLocationRecyclerView()
                }

                stockLocationDropdownList = ArrayList(stockLocationList)


            } else {

                resultReturn?.errorTextView?.visibility = View.VISIBLE
                if (resultReturn?.type === Validation.Type.EmptyString) {
                    resultReturn.errorTextView?.text = resultReturn.errorMessage
                } else {
                    resultReturn?.errorTextView?.text = validation?.errorMessage
                    val animation =
                        AnimationUtils.loadAnimation(context, R.anim.top_to_bottom)
                    resultReturn?.errorTextView?.startAnimation(animation)
                    validation?.EditTextPointer?.requestFocus()

                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)
                }
            }

        }

        dialogBinding?.mcvCancel?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            selectedParentUUID = null
        }

        dialog.setCancelable(true)
        dialog.show()

        stockLocationDropdownList = ArrayList(stockLocationList)

    }

    override fun onClick(v: View?) {

        if (v == binding.mcvBackBtn) {

            (requireActivity() as DashboardActivity).navController?.popBackStack()

        } else if (v == binding.mcvAddStockLocation) {

            showAddStockLocationDialog(null, null)
        }

    }

}