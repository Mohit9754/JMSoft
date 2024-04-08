package com.jmsoft.main.fragment

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.basic.Database.UserDataModel
import com.jmsoft.basic.UtilityTools.Constants.Companion.information
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.FragmentCardBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CardListAdapter
import com.jmsoft.main.adapter.CardUserAdapter
import com.jmsoft.main.model.CardModel

class CardFragment : Fragment(), View.OnClickListener {

    lateinit var binding: FragmentCardBinding

    //Validation Mode object
    private var errorValidationModels: MutableList<ValidationModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentCardBinding.inflate(layoutInflater)

        // Visible the Search on Toolbar
        (requireActivity() as DashboardActivity).mcvSearch?.visibility = View.VISIBLE

        //set the Clicks , initialization and setup
        init()

        return binding.root
    }

    //Setting Up Card List Recycler View
    private fun setUpCardListRecyclerView() {

        val arr = ArrayList<CardModel>()

        val model = CardModel()
        model.jewelleryImage = R.drawable.img_ring_product
        model.jewelleryName = "Royal Gold Ring"
        model.jewelleryQuantity = 1
        model.jewelleryPrice = 12000

        val model2 = CardModel()
        model2.jewelleryImage = R.drawable.img_ring_product
        model2.jewelleryName = "Royal Gold Ring"
        model2.jewelleryQuantity = 1
        model2.jewelleryPrice = 12000

        val model3 = CardModel()
        model3.jewelleryImage = R.drawable.img_ring_product
        model3.jewelleryName = "Royal Gold Ring"
        model3.jewelleryQuantity = 1
        model3.jewelleryPrice = 12000

        val model4 = CardModel()
        model4.jewelleryImage = R.drawable.img_ring_product
        model4.jewelleryName = "Royal Gold Ring"
        model4.jewelleryQuantity = 1
        model4.jewelleryPrice = 12000

        val model5 = CardModel()
        model5.jewelleryImage = R.drawable.img_ring_product
        model5.jewelleryName = "Royal Gold Ring"
        model5.jewelleryQuantity = 1
        model5.jewelleryPrice = 12000

        val model6 = CardModel()
        model6.jewelleryImage = R.drawable.img_ring_product
        model6.jewelleryName = "Royal Gold Ring"
        model6.jewelleryQuantity = 1
        model6.jewelleryPrice = 12000

        arr.add(model)
        arr.add(model2)
        arr.add(model3)
        arr.add(model4)
        arr.add(model5)
        arr.add(model6)

        val adapter = CardListAdapter(requireActivity(), arr,binding)

        binding.rvCardList?.layoutManager = LinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL, false
        )
        binding.rvCardList?.adapter = adapter

    }

    //Setting Up User List Recycler View
    private fun setUpUserListRecyclerView() {

        // getting All User Details Accept Admin

//        val userList = Utils.getAllUserDetails()

        val userList = ArrayList<UserDataModel>()

        val userDataModel = UserDataModel()
        userDataModel.firstName = "Mohit"
        userDataModel.lastName = "Chouhan"

        userList.add(userDataModel)
        userList.add(userDataModel)
        userList.add(userDataModel)
        userList.add(userDataModel)
        userList.add(userDataModel)
        userList.add(userDataModel)
        userList.add(userDataModel)
        userList.add(userDataModel)


        val userManagementAdapter =
            binding.ivNoUser?.let { CardUserAdapter(requireActivity(), userList, it) }

        binding.rvUserList?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvUserList?.adapter = userManagementAdapter

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
    private fun setUnderLine(){

        // Create a SpannableString with underline style
        val underlinedText = SpannableString(requireActivity().getString(R.string.back_to_main_page))
        underlinedText.setSpan(UnderlineSpan(), 0, underlinedText.length, 0)

        // Set the SpannableString to the TextView
        binding.tvBackToHomePage?.text   = underlinedText
    }

    //set the Clicks , initialization and setup
    private fun init() {

        //Set underline on Back to Home page
        setUnderLine()

        //Setting the Selector on Material Card View When EditText has focus
        binding.etFirstName?.let { binding.mcvFirstName?.let { it1 -> setFocusChangeLis(it, it1) } }
        binding.mcvLastName?.let { binding.etLastName?.let { it1 -> setFocusChangeLis(it1, it) } }
        binding.mcvAddress?.let { binding.etAddress?.let { it1 -> setFocusChangeLis(it1, it) } }
        binding.mcvPhoneNumber?.let {
            binding.etPhoneNumber?.let { it1 ->
                setFocusChangeLis(
                    it1,
                    it
                )
            }
        }
        binding.mcvZipCode?.let { binding.etZipCode?.let { it1 -> setFocusChangeLis(it1, it) } }

        //Removing the Error When text Entered
        binding.etFirstName?.let {
            binding.tvFirstNameError?.let { it1 ->
                setTextChangeLis(
                    it,
                    it1
                )
            }
        }
        binding.tvLastNameError?.let {
            binding.etLastName?.let { it1 ->
                setTextChangeLis(
                    it1,
                    it
                )
            }
        }
        binding.tvPhoneNumberError?.let {
            binding.etPhoneNumber?.let { it1 ->
                setTextChangeLis(
                    it1,
                    it
                )
            }
        }
        binding.etAddress?.let { binding.tvAddressError?.let { it1 -> setTextChangeLis(it, it1) } }
        binding.tvZipCodeError?.let { binding.etZipCode?.let { it1 -> setTextChangeLis(it1, it) } }

        //Setting Up Card List Recycler View
        setUpCardListRecyclerView()

        //Setting Up User List Recycler View
        setUpUserListRecyclerView()

        // Set progress value to 33 percent
        binding.progressBar?.progress = 33

        // Set Click on Add to Card button
        binding.llCheckOut?.setOnClickListener(this)

        // Set Click on Save button
        binding.mcvSave?.setOnClickListener(this)

        // Set Click on Place Order button
        binding.mcvPlaceOrder?.setOnClickListener(this)

        // Set Click on Back To Home  TextView
        binding.tvBackToHomePage?.setOnClickListener(this)

    }

    //Validating Sign Up details
    private fun validate() {

        errorValidationModels.clear()

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty, binding.etFirstName, binding.tvFirstNameError
            )
        )
        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty, binding.etLastName, binding.tvLastNameError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty, binding.etAddress, binding.tvAddressError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Phone, binding.etPhoneNumber, binding.tvPhoneNumberError
            )
        )


        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty, binding.etZipCode, binding.tvZipCodeError
            )
        )

        val validation: Validation? = Validation.instance
        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModels)
        if (resultReturn?.aBoolean == true) {

            Utils.T(requireActivity(), "Data Saved")


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

//    // For recreating the activity data store
//    override fun onSaveInstanceState(outState: Bundle) {
//
//        outState.putString(Constants.firstName, binding.etFirstName?.text.toString())
//
//        super.onSaveInstanceState(outState)
//    }

    //Handle all the clicks
    override fun onClick(v: View?) {

        // Set Click on Add to Card button
        if (v == binding.llCheckOut) {

            binding.rlVerification?.visibility = View.GONE
            binding.rlInformation?.visibility = View.VISIBLE
            binding.progressBar?.progress = 66 // Set progress value
            (requireActivity() as DashboardActivity).currentState = information
        }

        // Set Click on Save button
        else if (v == binding.mcvSave) {

            validate()
        }

        // Set Click Place Order button
        else if(v == binding.mcvPlaceOrder){

            (requireActivity() as DashboardActivity).currentState = ""

            binding.llProgressStatusName?.visibility  = View.GONE
            binding.progressBar?.visibility = View.GONE
            binding.rlInformation?.visibility  = View.GONE
            binding.llConfirmation?.visibility = View.VISIBLE

        }

        // Set Click on Back To Home  TextView
        else if (v == binding.tvBackToHomePage){

            // Back to home page
            (requireActivity() as DashboardActivity).navController?.popBackStack(R.id.home, false)
        }

    }

}