package com.jmsoft.main.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.basic.Database.UserDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.admin
import com.jmsoft.basic.UtilityTools.Constants.Companion.updateInSession
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.FragmentEditProfileBinding
import java.util.ArrayList
import com.jmsoft.basic.UtilityTools.Constants.Companion.userUUID
import com.jmsoft.main.activity.DashboardActivity
import java.util.Locale

@Suppress("LABEL_NAME_CLASH")
class EditProfileFragment : Fragment(), View.OnClickListener {

    lateinit var binding: FragmentEditProfileBinding
    private var errorValidationModels: MutableList<ValidationModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment

        binding = FragmentEditProfileBinding.inflate(layoutInflater)

        //set the Clicks And initialization
        init()

        return binding.root
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

    // Setting the User Details through the Database
    private fun setUserDetails(userUUID: String) {

        val userDataModel = Utils.getUserDetailsThroughUserUUID(userUUID)
        binding.etFirstName?.setText(userDataModel.firstName)
        binding.etLastName?.setText(userDataModel.lastName)
        binding.etPhoneNumber?.setText(userDataModel.phoneNumber)
        binding.etEmailAddress?.setText(userDataModel.email)
        binding.etPassword?.setText(userDataModel.password?.let { Utils.decodeText(it) })
    }

    // Set up editor action listener on etFirstName so that after pressing enter it will move etLastName
    private fun setOnEditorActionListener(){

        // Set up editor action listener for editText1
        binding.etFirstName?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etLastName?.requestFocus() // Move focus to etLastName
                return@setOnEditorActionListener true
            }
            false
        }
    }

    // Disable Email and Phone Number if user is not admin
    private fun disableEmailAndPhoneNumber(){

        if(Utils.GetSession().userType != admin) {

            binding.etPhoneNumber?.isFocusable = false
            binding.etEmailAddress?.isFocusable = false

            //Set Click on Phone number and Email Address
            binding.etPhoneNumber?.setOnClickListener(this)
            binding.etEmailAddress?.setOnClickListener(this)

        }
    }

    //set the Clicks And initialization
    private fun init() {

        //set Password for  current Language
        Utils.toSetPasswordAsLanguage(binding.etPassword,requireActivity())

        // Disable Email and Phone Number if user is not admin
        disableEmailAndPhoneNumber()

        // Set up editor action listener on etFirstName so that after pressing enter it will move etLastName
        setOnEditorActionListener()

        // getting the userUUID
        val userUUID = arguments?.getString(Constants.userUUID)

        //if userId is not null then we have to update the user details
        if (userUUID != null) {
            // Setting the user details
            setUserDetails(userUUID)
        }

        //Setting  Click on Save Button
        binding.mcvSave?.setOnClickListener(this)

        //Removing the Error When text Entered
        setTextChangeLis(binding.etFirstName!!, binding.tvFirstNameError!!)
        setTextChangeLis(binding.etLastName!!, binding.tvLastNameError!!)
        setTextChangeLis(binding.etPhoneNumber!!, binding.tvPhoneNumberError!!)
        setTextChangeLis(binding.etEmailAddress!!, binding.tvEmailAddressError!!)
        setTextChangeLis(binding.etPassword!!, binding.tvPasswordError!!)

        //Setting the Selector on Material Card View When EditText has focus
        setFocusChangeLis(binding.etFirstName!!, binding.mcvFirstName!!)
        setFocusChangeLis(binding.etLastName!!, binding.mcvLastName!!)
        setFocusChangeLis(binding.etPhoneNumber!!, binding.mcvPhoneNumber!!)
        setFocusChangeLis(binding.etEmailAddress!!, binding.mcvEmailAddress!!)
        setFocusChangeLis(binding.etPassword!!, binding.mcvPassword!!)

        //Setting Click on password Visibility icon
        binding.ivPasswordVisibility?.setOnClickListener(this)

        //set Click on Back Btn
        binding.mcvBackBtn?.setOnClickListener(this)

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
                Validation.Type.Phone, binding.etPhoneNumber, binding.tvPhoneNumberError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Email, binding.etEmailAddress, binding.tvEmailAddressError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty, binding.etPassword, binding.tvPasswordError
            )
        )


        val validation: Validation? = Validation.instance
        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModels)
        if (resultReturn?.aBoolean == true) {

            val userUUID = arguments?.getString(userUUID)

            //if userUUID is not null , then we have to update the user details

            if (userUUID != null) {
                updateUserDetails(userUUID)
            }
            //Register new user
            else {
                registerNewUser()
            }

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
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    // Updating the data in the local database
    private fun updateUserDetails(userUUID: String) {

        //this method checks if any user has this phone number
        if (!Utils.isAnyUserHasThisPhoneNumber(
                binding.etPhoneNumber?.text.toString().trim(), userUUID
            )
        ) {

            //this method checks if any user has this email
            if (!Utils.isAnyUserHasThisEmail(
                    binding.etEmailAddress?.text.toString().trim().lowercase(),
                    userUUID
                )
            ) {

                val userDataModel = UserDataModel()

                userDataModel.userUUID = userUUID // userUUID is necessary for updating the details
                userDataModel.firstName = binding.etFirstName?.text.toString().trim()
                userDataModel.lastName = binding.etLastName?.text.toString().trim()
                //Store Email in the lower Case letter
                userDataModel.email = binding.etEmailAddress?.text.toString().trim()
                    .lowercase()
                userDataModel.phoneNumber = binding.etPhoneNumber?.text.toString().trim()

                userDataModel.password = Utils.encodeText(binding.etPassword?.text.toString().trim())

                //Update user details in the User Table
                Utils.updateUserDetails(userDataModel)
                Utils.T(requireActivity(), getString(R.string.updated_successfully))

                val updateInSession = arguments?.getBoolean(updateInSession)

                if (updateInSession != null){

                    if (updateInSession){

                        //Updating user details in the session table
                        val updatedUserData = Utils.getUserDetailsThroughUserUUID(userUUID)
                        Utils.insertDataInSessionTable(updatedUserData)
                    }
                }
                //Back to Setting fragment
                (context as DashboardActivity).navController?.popBackStack()


            } else {
                //Showing Email Already Exist Error
                showAlreadyExistError(
                    binding.tvEmailAddressError!!, getString(R.string.email_already_exist)
                )
            }
        } else {
            //Showing Mobile Number Already Exist Error
            showAlreadyExistError(
                binding.tvPhoneNumberError!!, getString(R.string.mobile_number_already_exist)
            )
        }
    }

    // Showing email and phone number already exist error
    private fun showAlreadyExistError(textView: TextView, msg: String) {

        textView.visibility = View.VISIBLE
        textView.text = msg
        textView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.top_to_bottom))
    }

    // Handles All the Clicks

    override fun onClick(v: View?) {

        //when Save button Click
        if (v == binding.mcvSave) {
            validate()
        }
        //show And hide Password
        else if (v == binding.ivPasswordVisibility) {

            showAndHidePassword(binding.etPassword!!, binding.ivPasswordVisibility!!)
        }

        //Back Button pressed
        else if(v == binding.mcvBackBtn){

            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }

        else if (v == binding.etPhoneNumber){
            Utils.T(requireActivity(), getString(R.string.only_admin_can_edit_phone_number))
        }

        else if (v == binding.etEmailAddress){
            Utils.T(requireActivity(), getString(R.string.only_admin_can_edit_email_address))
        }
    }

    // Show And hide , Password And Conform password
    private fun showAndHidePassword(editText: EditText, imageView: ImageView) {

        if (editText.transformationMethod == HideReturnsTransformationMethod.getInstance()) {
            imageView.setImageResource(R.drawable.icon_show)
            editText.transformationMethod = PasswordTransformationMethod.getInstance()

        } else {
            imageView.setImageResource(R.drawable.icon_hide)
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }

        editText.setSelection(editText.length())
    }

    //Register New User in the User Table
    private fun registerNewUser() {

        //Check if phone Number exist
        if (!Utils.isPhoneNumberExist(binding.etPhoneNumber?.text.toString().trim())) {

            //Checks if Email Already Exist
            if (!Utils.isEmailExist(binding.etEmailAddress?.text.toString().trim().lowercase())) {

                val userDataModel = UserDataModel()

                userDataModel.userUUID = Utils.generateUUId() // Generating the UUID
                userDataModel.userType = Constants.user
                userDataModel.firstName = binding.etFirstName?.text.toString().trim()
                userDataModel.lastName = binding.etLastName?.text.toString().trim()
                //Store Email in the lower Case letter
                userDataModel.email = binding.etEmailAddress?.text.toString().trim()
                    .lowercase()
                userDataModel.phoneNumber = binding.etPhoneNumber?.text.toString().trim()
                userDataModel.password = Utils.encodeText(binding.etPassword?.text.toString().trim())
                userDataModel.profileUri = ""

                //insert new user in the User Table
                Utils.insetDataInUserTable(userDataModel)

                //Clearing the data
                binding.etFirstName?.setText("")
                binding.etLastName?.setText("")
                binding.etEmailAddress?.setText("")
                binding.etPhoneNumber?.setText("")
                binding.etPassword?.setText("")

                Utils.T(requireActivity(), getString(R.string.new_user_registered_successfully))

                //Navigate to user management
                (requireActivity() as DashboardActivity).navController?.popBackStack()


            } else {
                //Showing Email Already Exist Error
                showAlreadyExistError(
                    binding.tvEmailAddressError!!, getString(R.string.email_already_exist)
                )
            }
        } else {
            //Showing Mobile Number Already Exist Error
            showAlreadyExistError(
                binding.tvPhoneNumberError!!, getString(R.string.mobile_number_already_exist)
            )
        }

    }

}