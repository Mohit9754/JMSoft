package com.jmsoft.main.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.basic.Database.UserDataHelper
import com.jmsoft.basic.UtilityTools.Constants.Companion.arabic
import com.jmsoft.basic.UtilityTools.Constants.Companion.email
import com.jmsoft.basic.UtilityTools.Constants.Companion.english
import com.jmsoft.basic.UtilityTools.Constants.Companion.password
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.ActivityLoginBinding
import java.util.Locale

/**
 * Login Activity
 *
 *  Validating the login details
 *  Store the login respone to local database
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val activity: Activity = this@LoginActivity

    // For Showing the error
    private var errorValidationModels: MutableList<ValidationModel> = ArrayList()
    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    @RequiresApi(Build.VERSION_CODES.S)
    private val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADMIN
    )
    private val permissionsRequestCode = 100 // You can use any value for the request code


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        // Setting the savedInstanceState Data when language switches
        binding.etEmailAddress!!.setText(savedInstanceState?.getString(email) ?: "")
        binding.etPassword!!.setText(savedInstanceState?.getString(password) ?: "")

        setContentView(binding.root)

//        binding.btnBluetoothOn?.setOnClickListener {
//            if (hasBluetoothPermissions()) {
//                enableBluetooth(activity, 1000)
//            } else {
//                requestPermissions()
//            }
//        }

        //set the Clicks And initialization
        init()
    }

    //setting the selector on material card view
    private fun setFocusChangeLis(editText: EditText, materialCardView: MaterialCardView) {

        editText.setOnFocusChangeListener { _, hasFocus ->

            if (hasFocus) {
                materialCardView.strokeColor = getColor(R.color.theme)
            } else {
                materialCardView.strokeColor = getColor(R.color.text_hint)
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

    private fun init() {

        //Set Image for current Language
        Utils.setImageForCurrentLanguage(binding.ivJewellery!!)

        // Setting the Selector on Material Card View when edittext has focus
        binding.ivJewellery?.setImageDrawable(null)
        binding.ivJewellery?.setImageResource(R.drawable.img_jewellery)

        setFocusChangeLis(binding.etEmailAddress!!, binding.mcvEmailAddress!!)
        setFocusChangeLis(binding.etPassword!!, binding.mcvPassword!!)

        //set Password for  current Language
        Utils.toSetPasswordAsLanguage(binding.etPassword)


        // Removing the error when text Entered
        setTextChangeLis(binding.etEmailAddress!!, binding.tvEmailAddressError!!)
        setTextChangeLis(binding.etPassword!!, binding.tvPasswordError!!)

        //Set Click on login Button
        binding.mcvLogin?.setOnClickListener(this)

        //Set Click on language Switcher
        binding.ivLanguageSwitcher?.setOnClickListener(this)

        //Set Click on SignUp textView
        binding.llSignUp?.setOnClickListener(this)

        //Set Click on password Visibility icon
        binding.ivPasswordVisibility?.setOnClickListener(this)

    }

    private var bluetoothIntent: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {

            }
        }

    private fun enableBluetooth(activity: Activity, requestCode: Int) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            // Handle this case accordingly
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothIntent.launch(enableBtIntent)
        } else {

            // Bluetooth is already enabled
            // You can perform further actions here if needed
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, permissionsRequestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionsRequestCode) {
            // Check if all permissions are granted
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                // All permissions are granted, proceed with your logic
                // For example, start Bluetooth functionality
                enableBluetooth(activity, 1000)
            } else {
                Utils.T(activity, "Please allow the Permission To connect with your Device")
                // Permissions are not granted, handle the scenario
                // For example, show a message to the user or disable Bluetooth functionality
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun Context.hasBluetoothPermissions(): Boolean {
        val bluetoothPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
        val bluetoothAdminPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
        val bluetoothScanPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        val bluetoothConnectPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)

        return bluetoothPermission == PackageManager.PERMISSION_GRANTED &&
                bluetoothAdminPermission == PackageManager.PERMISSION_GRANTED &&
                bluetoothScanPermission == PackageManager.PERMISSION_GRANTED &&
                bluetoothConnectPermission == PackageManager.PERMISSION_GRANTED
    }

    //Validating Login details
    private fun validate() {

        errorValidationModels.clear()
        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Email,
                binding.etEmailAddress,
                binding.tvEmailAddressError
            )
        )
        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty,
                binding.etPassword,
                binding.tvPasswordError
            )
        )

        val validation: Validation? = Validation.instance
        val resultReturn: ResultReturn? =
            validation?.CheckValidation(activity, errorValidationModels)
        if (resultReturn?.aBoolean == true) {

            //Check if user is valid
            if (Utils.isValidUser(
                    binding.etEmailAddress?.text.toString().trim().lowercase(Locale.getDefault()),
                    binding.etPassword?.text.toString().trim()
                )
            ) {

                // Getting User Details through email and password
                val userDataModel = Utils.getUserThroughEmailAndPassword(
                    binding.etEmailAddress?.text.toString().trim().lowercase(Locale.getDefault()),
                    binding.etPassword?.text.toString().trim()
                )
                //Store User Details in the Session Table
                Utils.insertDataInSessionTable(userDataModel)

                //Intent to Dashboard Activity
                Utils.I_clear(activity, DashboardActivity::class.java, null)
            } else {
                // Invalid user
                Utils.T(activity, getString(R.string.invalid_credentials))
            }

        } else {
            resultReturn?.errorTextView?.visibility = View.VISIBLE
            if (resultReturn?.type === Validation.Type.EmptyString) {
                resultReturn.errorTextView?.text = resultReturn.errorMessage
            } else {
                resultReturn?.errorTextView?.text = validation?.errorMessage
                val animation =
                    AnimationUtils.loadAnimation(applicationContext, R.anim.top_to_bottom)
                resultReturn?.errorTextView?.startAnimation(animation)
                validation?.EditTextPointer?.requestFocus()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    //Handles All the Clicks
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(v: View?) {

        //When login button Click
        if (v == binding.mcvLogin) {
            validate()
        }

        // Switching language according to current language
        else if (v == binding.ivLanguageSwitcher) {

            val lang = Utils.getCurrentLanguage()

            if (lang == english) {
                Utils.setLocale(activity, arabic)

            } else {
                Utils.setLocale(activity, english)
            }
        } else if (v == binding.llSignUp) {
            Utils.I_clear(activity, SignUpActivity::class.java, null)
        }

        //Show or Hide password
        else if (v == binding.ivPasswordVisibility) {

            if (!isPasswordVisible) {
                isPasswordVisible = true
                binding.ivPasswordVisibility?.setImageResource(R.drawable.icon_hide)
                binding.etPassword?.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                binding.etPassword?.setSelection(binding.etPassword!!.length())

            } else {
                isPasswordVisible = false
                binding.ivPasswordVisibility?.setImageResource(R.drawable.icon_show)
                binding.etPassword?.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.etPassword?.setSelection(binding.etPassword!!.length())
            }

        }
    }

    // For recreating the activity data store
    override fun onSaveInstanceState(outState: Bundle) {

        outState.putString(email, binding.etEmailAddress?.text.toString())
        outState.putString(password, binding.etPassword?.text.toString())
        super.onSaveInstanceState(outState)
    }

    // Close the Keyboard when you touch the Screen
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}