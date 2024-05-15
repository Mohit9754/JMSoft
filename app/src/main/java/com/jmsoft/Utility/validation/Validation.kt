package com.jmsoft.basic.validation

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.jmsoft.basic.UtilityTools.SavedData.getCountryRegion
import com.jmsoft.basic.UtilityTools.Utils.E
import com.jmsoft.R
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber.PhoneNumber
import java.util.regex.Pattern

class Validation {

    var EditTextPointer: EditText? = null
    var errorMessage: String? = null
    var phoneNumberUtil: PhoneNumberUtil? = null
    var textViewPointer:TextView? = null

    /**
     * Check Validation
     *
     * @param context               Page Reference
     * @param errorValidationModels List of the Field On which we have to check the error
     * @return ResultReturn  return the Data of the Validation
     */
    fun CheckValidation(
        context: Context,
        errorValidationModels: List<ValidationModel>
    ): ResultReturn {
        var validationCheck = false
        phoneNumberUtil = PhoneNumberUtil.createInstance(context)
        var type: Type? = null
        var errorMessage: String? = null
        var parameter: String? = null
        var errorTextView: TextView? = null
        for (validationModel in errorValidationModels) {
            type = validationModel.type
            errorMessage = validationModel.errorMessage
            parameter = validationModel.Parameter
            errorTextView = validationModel.errorTextView
            if (errorTextView != null) {
                errorTextView.visibility = View.GONE
            }
            when (validationModel.type) {

                Type.NoSpecialChar -> validationCheck = isNoSpecialChar(context,validationModel.editText)

                Type.ImageSelect -> validationCheck = isImageSelected(context,validationModel.isImageSelected)
                
                Type.Barcode -> validationCheck = isBarCodeGenerate(context,validationModel.arrayListSize,validationModel.editText)

                Type.AtLeastTwo -> validationCheck = isTwoImageSelected(context,validationModel.arrayListSize)

                Type.EmptyTextView -> validationCheck = isEmptyTextView(context,validationModel.textView)

                Type.EmptyArrayList -> validationCheck = isEmptyArrayList(context,validationModel.arrayListSize)
                Type.Phone -> validationCheck =
                    isValidPhoneNumber(context, validationModel.editText)
                Type.Email -> validationCheck = isEmailValid(context, validationModel.editText)
                Type.EmptyString -> validationCheck = isEmptyString(context, validationModel.field)
                Type.Amount -> validationCheck = isValidAmount(context, validationModel.editText)
                Type.AadhaarNumber -> validationCheck =
                    isValidAadhaarNumber(context, validationModel.editText)
                Type.PasswordMatch -> validationCheck =
                    isPasswordMatch(context, validationModel.editText, validationModel.editText1)
                Type.PasswordStrong -> validationCheck =
                    isPasswordStrong(context, validationModel.editText)
                Type.ZipCode -> validationCheck =
                    isValidZipCode(context, validationModel.editText)

                Type.PAN -> validationCheck = isValidPAN(context, validationModel.editText)
                Type.IFSC -> validationCheck = isValidIFSC(context, validationModel.editText)
                Type.Empty -> validationCheck = isEmpty(context, validationModel.editText)
                Type.MPIN -> validationCheck = isMPinValid(context, validationModel.editText)
                Type.AccountNumber -> validationCheck =
                    isValidAccountNumber(context, validationModel.editText)
                else -> {}
            }
            if (!validationCheck) {
                break
            }
        }
        return ResultReturn(type, validationCheck, errorMessage, parameter, errorTextView)
    }

    private fun isImageSelected(context: Context,isImageSelected:Boolean?): Boolean {

        return if(isImageSelected == false){

            errorMessage = context.getString(R.string.select_collection_image)
            false

        } else {
            true
        }

    }

    /**
     * Password Strong Validation Method
     *
     * @param context  Page Reference
     * @param editText Edit Text To Check
     * @return true/false
     */
    private fun isPasswordStrong(context: Context, editText: EditText?): Boolean {
        return if (editText?.text == null || TextUtils.isEmpty(editText.text)) {
            errorMessage = context.getString(R.string.empty_error)
            EditTextPointer = editText
            false
        } else {
            true
           /* val p = Pattern.compile(
                "^" +
                        "(?=.*[0-9])" +  //at least 1 digit
                        "(?=.*[a-z])" +  //at least 1 lower case letter
                        "(?=.*[A-Z])" +  //at least 1 upper case letter
                        "(?=.*[a-zA-Z])" +  //any letter
                        "(?=.*[@#$%^&+=!()\\-_*\\[\\]{}|\\\\;:'\",<.>/?])" + // at least 1 special character
                        "(?=\\S+$)" +  //no white spaces
                        ".{8,}" +  //at least 8 characters
                        "$"
            )
            val s = editText.text.toString().trim { it <= ' ' }
            val m = p.matcher(s.trim { it <= ' ' })
            if (m.matches()) {
                true
            } else {
                EditTextPointer = editText
                errorMessage = context.getString(R.string.passwordStrong)
                false
            }*/
        }
    }
    
    private fun isBarCodeGenerate(context: Context,arrayListSize: Int?,editText: EditText?): Boolean {
        
        return if(arrayListSize == 0){
            errorMessage = context.getString(R.string.please_generate_barcode)
            EditTextPointer = editText

            false

        } else {
            true
        } 
    }

    /**
     * Email All Type Validation
     *
     * @param context  Page Reference
     * @param editText Edit Text To Check
     * @return true/false
     */
    private fun isEmailValid(context: Context, editText: EditText?): Boolean {
        //add your own logic
        return if (TextUtils.isEmpty(editText?.text.toString().trim { it <= ' ' })) {
            EditTextPointer = editText
            errorMessage = context.getString(R.string.empty_error)
            false
        } else {
            if (Patterns.EMAIL_ADDRESS.matcher(editText?.text.toString().trim { it <= ' ' })
                    .matches()
            ) {
                true
            } else {
                EditTextPointer = editText
                errorMessage = context.getString(R.string.invalid_email)
                false
            }
        }
    }

    private fun isEmptyArrayList(context: Context,arrayListSize: Int?):Boolean{

        return if (arrayListSize == 0) {
            errorMessage = context.getString(R.string.empty_error)
            false
        } else {
            true
        }

    }

    private fun isEmptyTextView(context: Context,textView: TextView?):Boolean{

        return if(textView?.text?.isEmpty() == true){
            errorMessage = context.getString(R.string.empty_error)
//            this@Validation.textView = textView
            textViewPointer = textView
            false

        } else {
            true
        }
    }

    private fun isTwoImageSelected(context: Context,arrayListSize: Int?):Boolean {

        if (arrayListSize != null) {
            return if (arrayListSize < 2) {
                errorMessage = context.getString(R.string.please_select_at_least_two_images)
                false

            } else {
                true
            }
        }
        return false
    }


        /**
     * is String Empty
     *
     * @param context Page Reference
     * @param string  string To Check
     * @return true/false
     */
    private fun isEmptyString(context: Context, string: String?): Boolean {
        //add your own logic
        return if (string == null || TextUtils.isEmpty(string.trim { it <= ' ' })) {
            errorMessage = context.getString(R.string.empty_error)
            false
        } else {
            true
        }
    }

    /**
     * Mobile All Type Validation
     *
     * @param context  Page Reference
     * @param editText Edit Text To Check
     * @return true/false
     */

    private fun isValidPhoneNumber(context: Context, editText: EditText?): Boolean {

        return if (editText?.text == null || TextUtils.isEmpty(editText.text)) {
            errorMessage = context.getString(R.string.empty_error)
            EditTextPointer = editText
            false

        } else {
            if (validateMobileNumber(editText.text.toString().trim { it <= ' ' })) {
                true
            } else {
                EditTextPointer = editText
                errorMessage = context.getString(R.string.enter_a_valid_number)
                false
            }

            /* if (editText.getText().toString().length() != 10) {
                EditTextPointer = editText;
                errorMessage = context.getString(R.string.enter_ten_digits_number);
                return false;
            } else {
                if (android.util.Patterns.PHONE.matcher(editText.getText()).matches()) {
                    return true;
                } else {
                    EditTextPointer = editText;
                    errorMessage = context.getString(R.string.valid_number);
                    return false;
                }
            }*/
        }
    }

    /**
     * Check Is Empty
     *
     * @param context Page Reference
     * @param arg     Multiple Edit Text To Check
     * @return true/false
     */
    private fun isEmpty(context: Context, vararg arg: EditText?): Boolean {
        for (editText in arg) {
            if (editText?.text.toString().trim { it <= ' ' }.length <= 0) {
                EditTextPointer = editText
                EditTextPointer!!.requestFocus()
                errorMessage = context.getString(R.string.empty_error)
                return false
            }
        }
        return true
    }

    /**
     * Check Is Valid Mpin
     *
     * @param context Page Reference
     * @param arg     Multiple Edit Text To Check
     * @return true/false
     */
    private fun isMPinValid(context: Context, vararg arg: EditText?): Boolean {
        for (editText in arg) {
            if (editText?.text.toString().trim { it <= ' ' }.isEmpty()) {
                EditTextPointer = editText
                EditTextPointer!!.requestFocus()
                errorMessage = context.getString(R.string.empty_error)
                return false
            }else if (editText?.getText().toString().length != 4) {
                EditTextPointer = editText;
                errorMessage = context.getString(R.string.please_enter_4_digit_mpin);
                return false;
            }
        }
        return true
    }

    /**
     * Check Is PasswordMatch
     *
     * @param context      Page Reference
     * @param pass         Edit Text To Check
     * @param confirm_pass Edit Text To Check
     * @return true/false
     */
    private fun isPasswordMatch(context: Context, pass: EditText?, confirm_pass: EditText?): Boolean {
        return if (pass?.text == null || TextUtils.isEmpty(pass.text)) {
            errorMessage = context.getString(R.string.empty_error)
            EditTextPointer = pass
            false
        } else if (confirm_pass?.text == null || TextUtils.isEmpty(confirm_pass.text)) {
            errorMessage = context.getString(R.string.empty_error)
            EditTextPointer = confirm_pass
            false
        } else {
            if (pass.text.toString() != confirm_pass.text.toString()) {
                EditTextPointer = confirm_pass
                errorMessage = context.getString(R.string.password_not_match)
                return false
            }
            true
        }
    }

    private fun isNoSpecialChar(context: Context, editText: EditText?): Boolean {
        return if (editText?.text == null || editText.text.toString().trim().isEmpty()) {
            errorMessage = context.getString(R.string.empty_error)
            EditTextPointer = editText
            false
        }  else {
            val pattern = Pattern.compile("^[\\p{L}\\s\\d]+\$")

            val s = editText.text.toString().trim { it <= ' ' }
            val m = pattern.matcher(s.trim { it <= ' ' })
            if (m.matches()) {
                true
            } else {
                EditTextPointer = editText
                errorMessage = context.getString(R.string.special_characters_are_not_allowed)
                false
            }
        }
    }

    /**
     * is Valid Aadhaar Number
     *
     * @param context  Page Reference
     * @param editText Edit Text To Check
     * @return true/false
     */
    private fun isValidAmount(context: Context, editText: EditText?): Boolean {
        return if (editText?.text == null || TextUtils.isEmpty(editText.text)) {
            errorMessage = context.getString(R.string.empty_error)
            EditTextPointer = editText
            false
        } else {
            val p = Pattern.compile("^(\\d*\\.)?\\d+$")
            val s = editText.text.toString().trim { it <= ' ' }
            val m = p.matcher(s.trim { it <= ' ' })
            if (m.matches()) {
                true
            } else {
                EditTextPointer = editText
                errorMessage = context.getString(R.string.amount_valid)
                false
            }
        }
    }

    /**
     * is Valid Aadhaar Number
     *
     * @param context  Page Reference
     * @param editText Edit Text To Check
     * @return true/false
     */
    private fun isValidAadhaarNumber(context: Context, editText: EditText?): Boolean {
        return if (editText?.text == null || TextUtils.isEmpty(editText.text)) {
            errorMessage = context.getString(R.string.empty_error)
            EditTextPointer = editText
            false
        } else {
            val p = Pattern.compile("^[2-9]{1}[0-9]{3}\\s[0-9]{4}\\s[0-9]{4}$")
            val s = editText.text.toString().replace("....".toRegex(), "$0 ")
            val m = p.matcher(s.trim { it <= ' ' })
            if (m.matches()) {
                true
            } else {
                EditTextPointer = editText
                errorMessage = context.getString(R.string.aadhaar_valid)
                false
            }
        }
    }

    /**
     * is Valid PAN Number
     *
     * @param context  Page Reference
     * @param editText Edit Text To Check
     * @return true/false
     */
    private fun isValidPAN(context: Context, editText: EditText?): Boolean {
        return if (editText?.text == null || TextUtils.isEmpty(editText.text)) {
            errorMessage = context.getString(R.string.empty_error)
            EditTextPointer = editText
            false
        } else {
            val p = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}")
            val m = p.matcher(editText.text.toString())
            if (m.matches()) {
                true
            } else {
                EditTextPointer = editText
                errorMessage = context.getString(R.string.pan_card_valid)
                false
            }
        }
    }

    /**
     * is Valid IFSC Code
     *
     * @param context  Page Reference
     * @param editText Edit Text To Check
     * @return true/false
     */
    private fun isValidIFSC(context: Context, editText: EditText?): Boolean {
        return if (editText?.text == null || TextUtils.isEmpty(editText.text)) {
            errorMessage = context.getString(R.string.empty_error)
            EditTextPointer = editText
            false
        } else {
            val p = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$")
            val m = p.matcher(editText.text.toString())
            if (m.matches()) {
                true
            } else {
                EditTextPointer = editText
                errorMessage = context.getString(R.string.ifsc_valid)
                false
            }
        }
    }

    /**
     * is Valid Account Number
     *
     * @param context  Page Reference
     * @param editText Edit Text To Check
     * @return true/false
     */
    private fun isValidAccountNumber(context: Context, editText: EditText?): Boolean {
        return if (editText?.text == null || TextUtils.isEmpty(editText.text)) {
            errorMessage = context.getString(R.string.empty_error)
            EditTextPointer = editText
            false
        } else {
            val p = Pattern.compile("[0-9]{9,18}")
            val m = p.matcher(editText.text.toString())
            if (m.matches()) {
                true
            } else {
                EditTextPointer = editText
                errorMessage = context.getString(R.string.account_valid)
                false
            }
        }
    }

    // is valid Zip code
    fun isValidZipCode(context: Context, editText: EditText?): Boolean {

        EditTextPointer = editText

        if (editText?.text == null || TextUtils.isEmpty(editText.text)) {
            errorMessage = context.getString(R.string.empty_error)
            return false
        }

        val zipCode = editText.getText().toString()
        errorMessage = context.getString(R.string.enter_a_valid_zipcode)

        // Check if the zip code has exactly 5 digits and if all characters are digits
        if (zipCode.length != 5 || !zipCode.all { it.isDigit() }) {
            return false
        }

        // Extract the region code from the first digit
        val regionCode = zipCode[0].toString().toInt()

        // Validate the region code (1 to 9)
        return regionCode in 1..9
    }

    fun validateMobileNumber(phoneNo: String): Boolean {

        return phoneNo.length <= 10

//        val phonenumber: PhoneNumber
//        val regionalCode = getCountryRegion()
////        E("regionalCode::$regionalCode")
//        val NationalPhoneNumber: String
//        try {
//            phonenumber = phoneNumberUtil!!.parse(phoneNo, regionalCode)
//            NationalPhoneNumber = phonenumber.nationalNumber.toString()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return false
//        }
//        return if (NationalPhoneNumber == phoneNo) {
//            phoneNumberUtil!!.isValidNumber(phonenumber)
//        } else {
//            false
//        }
    }

    /**
     * Enum of the Type of error we have
     */
    enum class Type(var label: String) {

         NoSpecialChar(""),ImageSelect(""),Barcode("") ,AtLeastTwo("") , Email(""),EmptyTextView(""), Phone(""),ZipCode("") ,EmptyString(""), Amount(""), AadhaarNumber(""), PasswordMatch(""), PasswordStrong(
            ""
        ),
        PAN(""), IFSC(""), Empty(""), EmptyArrayList(""), AccountNumber(""), MPIN("");
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var validation: Validation? = null

        /**
         * Singleton Object of the Class to access
         */
        val instance: Validation?
            get() {
                if (validation == null) validation = Validation()
                return validation
            }
    }
}