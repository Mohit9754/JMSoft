package com.jmsoft.basic.UtilityTools

//import com.jmsoft.databinding.AlertdialogBinding
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.text.format.DateFormat
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.webkit.MimeTypeMap
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.godex.Godex
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.card.MaterialCardView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.AppController
import com.jmsoft.Utility.UtilityTools.BitmapPrintAdapter
import com.jmsoft.Utility.UtilityTools.loadingButton.LoadingButton
import com.jmsoft.Utility.database.AddressDataModel
import com.jmsoft.Utility.database.CartDataModel
import com.jmsoft.Utility.database.CategoryDataModel
import com.jmsoft.Utility.database.CollectionDataModel
import com.jmsoft.Utility.database.ContactDataModel
import com.jmsoft.Utility.database.DeviceDataModel
import com.jmsoft.Utility.database.MetalTypeDataModel
import com.jmsoft.Utility.database.OrderDataModel
import com.jmsoft.Utility.database.ProductDataModel
import com.jmsoft.Utility.database.PurchasingDataModel
import com.jmsoft.Utility.database.StockLocationDataModel
import com.jmsoft.Utility.database.UserDataModel
import com.jmsoft.basic.Database.DatabaseHelper
import com.jmsoft.basic.UtilityTools.Constants.Companion.CONFIG_FILE
import com.jmsoft.basic.UtilityTools.Constants.Companion.arabic
import com.jmsoft.basic.UtilityTools.Constants.Companion.dimen
import com.jmsoft.basic.UtilityTools.Constants.Companion.email
import com.jmsoft.basic.UtilityTools.Constants.Companion.english
import com.jmsoft.basic.UtilityTools.Constants.Companion.name
import com.jmsoft.basic.UtilityTools.Constants.Companion.password
import com.jmsoft.basic.UtilityTools.Constants.Companion.statusBarHeight
import com.jmsoft.databinding.AlertdialogBinding
import com.jmsoft.databinding.FragmentPurchasingAndSalesBinding
import com.jmsoft.databinding.ItemCustomToastBinding
import com.jmsoft.main.model.DeviceModel
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.random.Random


object Utils {

    object TotalAmount {

        private var totalAmount = 0.0

        fun getTotalAmount() = totalAmount

        fun resetTotalAmount() {
            totalAmount = 0.0
        }

        fun addAmount(amount: Double) {
            totalAmount += amount
        }
    }

    fun isPrinterReady(): Boolean {

        val status = Godex.CheckStatus()
        if (status == null) {
            E("Failed to check printer status.")
            return false
        }
        E("Printer status: $status")

        // Check for specific messages. Update these conditions based on actual responses.
        if (status.contains("Ready")) {
            return true
        } else if (status.contains("Paper Out")) {
            E("Printer is out of paper.")
        } else if (status.contains("Error")) {
            E("Printer encountered an error.")
        } else {
            E("Unknown printer status: $status")
        }
        return false
    }


    // Function to generate the QR code
    fun generateQRCode(data: String): Bitmap? {

        try {
            val qrCodeWriter = QRCodeWriter()
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = 1 // Optional margin

            val bitMatrix: BitMatrix =
                qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // Set pixels based on the bitMatrix
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            return bmp
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    // Convert a view to a Bitmap
    fun getBitmapFromView(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


    // Handle printing the layout
    fun printLayout(context: Context, bitmap: Bitmap) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = BitmapPrintAdapter(bitmap)
        val jobName = "QR Print Job"
        val printAttributes =
            PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR).build()
        printManager.print(jobName, printAdapter, printAttributes)
    }

    object purchaseAndSalesBinding {

        private lateinit var fragmentPurchasingAndSalesBinding: FragmentPurchasingAndSalesBinding

        fun setBinding(fragmentPurchasingAndSalesBinding: FragmentPurchasingAndSalesBinding) {
            this.fragmentPurchasingAndSalesBinding = fragmentPurchasingAndSalesBinding
        }

        fun getBinding() = this.fragmentPurchasingAndSalesBinding

    }

    object Flag {

        private var flag = true
        fun getFlag(): Boolean {

            val flag = this.flag
            this.flag = false
            return flag
        }
    }

    object SelectedProductUUIDList {

        private var productUUIDList = ArrayList<String>()

        fun setProductList(productUUIDList: ArrayList<String>) {
            this.productUUIDList = productUUIDList
        }

        fun addProductUUID(productUUID: String) {
            productUUIDList.add(productUUID)
        }

        fun removeProductUUID(productUUID: String) {
            productUUIDList.remove(productUUID)
        }

        fun getSize() = productUUIDList.size

        fun clearList() {
            productUUIDList.clear()
        }

        fun getProductList() = productUUIDList

    }

    // Get the language from the shared preference
    private fun getLang(context: Context): String {
        val sh = context.getSharedPreferences(Constants.appLang, AppCompatActivity.MODE_PRIVATE)
        return sh.getString(Constants.lang, english) ?: english
    }

    // Store the language in the shared preference
    private fun storeLang(context: Context, lang: String) {

        val sharedPreferences = context.getSharedPreferences(
            Constants.appLang,
            AppCompatActivity.MODE_PRIVATE
        )
        val myEdit = sharedPreferences.edit()
        myEdit.putString(Constants.lang, lang)
        myEdit.apply()

        E("Store is $lang")
    }

    // Decimal digit filter (2 digit after point)
    class DecimalDigitsInputFilter : InputFilter {

        private val pattern: Pattern = Pattern.compile("[0-9]*+((\\.[0-9]?)?)||(\\.)?")

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val matcher: Matcher = pattern.matcher(dest)
            if (!matcher.matches()) {
                return ""
            }
            return null
        }
    }


    // Save picture in internal storage and return the uri
    fun getPictureUri(context: Context, bitmap: Bitmap): String {

        val pictureUri = generateUUId()

        saveToInternalStorage(context, bitmap, pictureUri)

        return pictureUri
    }

    // Capitalize the string data
    @SuppressLint("DefaultLocale")
    fun capitalizeData(data: String): String {
        return data.trim().lowercase(Locale.getDefault()).capitalize(Locale.ROOT)
    }

    fun openWifiSettings(context: Context) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        context.startActivity(intent)
    }

    fun openLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    // Convert data to barcode
    fun genBarcodeBitmap(context: Context, data: String): Bitmap? {

        // Getting input value from the EditText
        if (data.isNotEmpty()) {
            // Initializing a MultiFormatWriter to encode the input value
            val mwriter = MultiFormatWriter()

            try {
                // Generating a barcode matrix
                val matrix = mwriter.encode(data, BarcodeFormat.CODE_128, 60, 30)

                // Creating a bitmap to represent the barcode
                val bitmap = Bitmap.createBitmap(60, 30, Bitmap.Config.RGB_565)

                // Iterating through the matrix and set pixels in the bitmap
                for (i in 0 until 60) {
                    for (j in 0 until 30) {
                        bitmap.setPixel(i, j, if (matrix[i, j]) Color.BLACK else Color.WHITE)
                    }
                }
                // Setting the bitmap as the image resource of the ImageView
                return bitmap

            } catch (e: Exception) {

                Utils.T(context, "Exception $e")

            }
        } else {
            // Showing an error message if the EditText is empty
        }
        return null
    }

    fun showError(context: Context, textView: TextView, msg: String) {

        textView.visibility = View.VISIBLE
        textView.text = msg
        textView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.top_to_bottom))

    }

    // Removing Error when text entered
    fun setTextChangeListener(editText: EditText, textView: TextView) {

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

    fun passwordVisibility(isPasswordVisible:Boolean,etPassword: EditText,imageVisibility: ImageView) {

        if (!isPasswordVisible) {

            imageVisibility.setImageResource(R.drawable.icon_hide)
            etPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            etPassword.setSelection(etPassword.length())

        } else {

            imageVisibility.setImageResource(R.drawable.icon_show)
            etPassword.transformationMethod =
                PasswordTransformationMethod.getInstance()
            etPassword.setSelection(etPassword.length())
        }
    }

    fun setFocusAndTextChangeListener(
        context: Context,
        editText: EditText,
        materialCardView: MaterialCardView,
        textView: TextView
    ) {
        setFocusChangeListener(context, editText, materialCardView)
        setTextChangeListener(editText, textView)
    }

    //setting the selector on material card view
    fun setFocusChangeListener(
        context: Context,
        editText: EditText,
        materialCardView: MaterialCardView
    ) {

        editText.setOnFocusChangeListener { _, hasFocus ->

            if (hasFocus) {
                materialCardView.strokeColor = context.getColor(R.color.theme)
            } else {
                materialCardView.strokeColor = context.getColor(R.color.text_hint)
            }
        }
    }

    fun formatArabicToTwoDecimalPoints(arabicValue: String): Double {
        // Replace Arabic numerals with their Latin counterparts
        val latinValue = convertToLatinNumerals(arabicValue)

        // Replace Arabic decimal separator with a dot (if present)
        val latinValueWithDot = latinValue.replace('٫', '.')

        try {
            // Parse the Latin numeral string to a Double value
            return latinValueWithDot.toDouble()
        } catch (e: NumberFormatException) {
            // Handle parsing errors here (e.g., return a default value)
            return 0.0
        }
    }

    fun convertToLatinNumerals(input: String): String {
        val arabicNumerals = listOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val latinNumerals = (0..9).map { (it + '0'.toInt()).toChar() }

        val convertedChars = input.map { char ->
            val index = arabicNumerals.indexOf(char)
            if (index != -1) latinNumerals[index] else char
        }

        return convertedChars.joinToString(separator = "") { it.toString() }
    }

    // Round off to two digit
    fun roundToTwoDecimalPlaces(value: Double): Double {

        val decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
        val decimalFormat = DecimalFormat("#.##", decimalFormatSymbols)

        // Format the value and parse it back to Double
        return decimalFormat.format(value).toDouble()

    }

    // Get the Screen height
    fun getScreenHeight(context: Context): Int {

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay
        val displayMetrics = context.resources.displayMetrics

        val screenHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            windowMetrics.bounds.height()
        } else {
            @Suppress("DEPRECATION")
            display.getRealMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
        return screenHeight
    }

    // Get thousand separate price
    fun getThousandSeparate(price: Double): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH)
        numberFormat.maximumFractionDigits = 2
        return numberFormat.format(price)
    }

    fun removeThousandSeparators(formattedNumber: String): String {
        return formattedNumber.replace(",", "")
    }

    fun thousandSeparatorEditText(editText: EditText) {

        val currencyFormat = NumberFormat.getNumberInstance(Locale.getDefault())

        // Set up TextWatcher to format input with thousand separators
        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                editText.removeTextChangedListener(this)

                // Remove previous formatting
                val cleanString = s.toString().replace("[,.]".toRegex(), "")

                // Format the clean input with thousand separators
                val formatted = currencyFormat.format(cleanString.toDoubleOrNull() ?: 0.0)
                editText.setText(formatted)
                editText.setSelection(formatted.length) // Move cursor to the end of text

                editText.addTextChangedListener(this)
            }
        })

    }

    fun formatNumberWithoutScientificNotation(number: Double): String {
        val decimalFormat = DecimalFormat("#,###.##")
        return decimalFormat.format(number)
    }

    // Get Status bar height
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun getStatusbarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier(statusBarHeight, dimen, Constants.android)
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    //Getting Name of the Admin from the config.properties file
    fun getName(context: Context): String? {
        val properties = Properties()
        return try {
            val inputStream = context.assets.open(CONFIG_FILE)
            properties.load(inputStream)
            inputStream.close()
            properties.getProperty(name)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    //Getting Password of the Admin from the config.properties file
    fun getPassword(context: Context): String? {
        val properties = Properties()
        return try {
            val inputStream = context.assets.open(CONFIG_FILE)
            properties.load(inputStream)
            inputStream.close()
            properties.getProperty(password)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    //setting the selector on material card view
    fun setFocusChangeLis(
        context: Context,
        editText: EditText,
        materialCardView: MaterialCardView
    ) {

        editText.setOnFocusChangeListener { _, hasFocus ->

            if (hasFocus) {
                materialCardView.strokeColor = context.getColor(R.color.theme)
            } else {
                materialCardView.strokeColor = context.getColor(R.color.text_hint)
            }
        }
    }

    //setting the selector on material card view
    fun setFocusChangeLisWhite(
        context: Context,
        editText: EditText,
        materialCardView: MaterialCardView
    ) {

        editText.setOnFocusChangeListener { _, hasFocus ->

            if (hasFocus) {
                materialCardView.strokeColor = context.getColor(R.color.theme)
            } else {
                materialCardView.strokeColor = context.getColor(R.color.white)
            }
        }
    }

    //Getting Email of the Admin from the config.properties file
    fun getEmail(context: Context): String? {
        val properties = Properties()
        return try {
            val inputStream = context.assets.open(CONFIG_FILE)
            properties.load(inputStream)
            inputStream.close()
            properties.getProperty(email)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    //    generate UUIDs (Universally Unique Identifiers) using the UUID
    fun generateUUId(): String {
        return UUID.randomUUID().toString().trim()
    }

    //encode the text
    fun encodeText(text: String): String {
        val bytes = text.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    //decode the text
    fun decodeText(encodedText: String): String {
        val bytes = Base64.decode(encodedText, Base64.DEFAULT)
        return String(bytes, Charsets.UTF_8)
    }

    //the function opens the setting
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    //get Image Name
    fun getImageFileName(): String {

        return Random.nextInt(1, 1001).toString() + Random.nextInt(1, 1001)
            .toString() + Random.nextInt(1, 1000).toString() + Random.nextInt(1, 1000).toString()
    }

    // this function save the image into internal storage
    fun saveToInternalStorage(
        context: Context,
        bitmapImage: Bitmap,
        imageFileName: String
    ): String {
        context.openFileOutput(imageFileName, Context.MODE_PRIVATE).use { fos ->
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 25, fos)
        }
        return context.filesDir.absolutePath
    }

    //get the image from the internal storage
//    fun getImageFromInternalStorage(context: Context, imageFileName: String): Bitmap? {
//        val directory = context.filesDir
//        val file = File(directory, imageFileName)
//        return BitmapFactory.decodeStream(FileInputStream(file))
//    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getImageFromInternalStorage(context: Context, imageFileName: String): Bitmap? {
        val directory = context.filesDir
        val file = File(directory, imageFileName)

        return if (file.exists()) {
            // Load the image if it exists
            BitmapFactory.decodeStream(FileInputStream(file))
        } else {
            // Load the default image from resources if the file is not found
            context.getDrawable(R.drawable.default_image)?.toBitmap()
        }
    }


    //delete image from the internal storage
    fun deleteImageFromInternalStorage(context: Context, imageFileName: String): Boolean {
        val dir = context.filesDir
        val file = File(dir, imageFileName)
        return file.delete()
    }

    fun setAppLanguage(context: Context) {

        if (Flag.getFlag()) {
            setLocale(context, getLang(context))
            Utils.E("language  is ${getLang(context)}")
        }
    }

    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
//        context.createConfigurationContext(configuration)

        // store the language
        storeLang(context, languageCode)

        // Recreate the current activity
        if (context is Activity) {
            context.recreate()

            // Set layout direction after recreation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                context.window.decorView.layoutDirection =
                    if (isRTL(languageCode)) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            }
        }

    }

    // Function to check if the language is right-to-left (RTL)
    fun isRTL(languageCode: String): Boolean {
        val locale = Locale(languageCode)
        val directionality = Character.getDirectionality(locale.displayName[0])
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
    }

    // set Images according to current language
    fun setImageForCurrentLanguage(imageView: ImageView) {

        val lang = getCurrentLanguage()

        if (lang == arabic) {
            imageView.setImageDrawable(null)
            imageView.setImageResource(R.drawable.img_jewellery)

        } else {
            imageView.setImageDrawable(null)
            imageView.setImageResource(R.drawable.img_jewellery)
        }
    }

    fun formatToSixDigitNumber(number: Int): String {
        return String.format("%06d", number)
    }

    // Extract the  current language
    fun getCurrentLanguage(): String {
        // Retrieve the default locale
        val locale = Locale.getDefault()
        return locale.language
    }

    @JvmStatic
    fun GetSession(): UserDataModel {
        return DatabaseHelper.instance.list[0]
    }

    @JvmStatic
    fun IS_LOGIN(): Boolean {
        return DatabaseHelper.instance.list.size > 0
    }

    // Checks if User Table is Empty
    fun isUserTableEmpty(): Boolean {
        return DatabaseHelper.instance.isUserTableEmpty()
    }

    // Checks if Email Already Exist in the User Table
    fun isEmailExist(email: String): Boolean {
        return DatabaseHelper.instance.isEmailExist(email)
    }

    //this method checks if any user has this phone number in the User table
    fun isAnyUserHasThisPhoneNumber(phoneNumber: String, userUUID: String): Boolean {
        return DatabaseHelper.instance.isAnyUserHasThisPhoneNumber(phoneNumber, userUUID)
    }

    //this method checks if any user has this email
    fun isAnyUserHasThisEmail(email: String, userUUID: String): Boolean {
        return DatabaseHelper.instance.isAnyUserHasThisEmail(email, userUUID)

    }

    // Checks if Phone Number Already Exist in the User Table
    fun isPhoneNumberExist(phoneNumber: String): Boolean {
        return DatabaseHelper.instance.isPhoneNumberExist(phoneNumber)
    }

    // Get all products uuid
    fun getAllProductsUUID(): ArrayList<String> {
        return DatabaseHelper.instance.getAllProductUUID()
    }

    // Checks if rFIDCode Exist in the product table
    fun isRFIDExist(rFIDCode: String, stockLocationUUID: String): Boolean? {
        return DatabaseHelper.instance.isRFIDExist(rFIDCode, stockLocationUUID)
    }

    // Getting the Product through RFIDCode
    fun getProductThroughRFIDCode(rFIDCode: String, stockLocationUUID: String): ProductDataModel {
        return DatabaseHelper.instance.getProductThroughRFIDCode(rFIDCode, stockLocationUUID)
    }


    // Checks is Phone Number Already Exist in the Address table accept my phone number
    fun isPhoneNumberExistInAddressTableAcceptMine(
        phoneNumber: String,
        addressUUID: String
    ): Boolean {
        return DatabaseHelper.instance.isPhoneNumberExistInAddressTableAcceptMine(
            phoneNumber,
            addressUUID
        )
    }

    // Checks is Phone Number Already Exist in the Contact table accept my phone number
    fun isPhoneNumberExistInContactTableAcceptMine(
        phoneNumber: String,
        contactUUID: String
    ): Boolean {
        return DatabaseHelper.instance.isPhoneNumberExistInContactTableAcceptMine(
            phoneNumber,
            contactUUID
        )
    }

    // Checks is email Already Exist in the Contact table accept my email
    fun isEmailExistInContactTableAcceptMine(email: String, contactUUID: String): Boolean {
        return DatabaseHelper.instance.isEmailExistInContactTableAcceptMine(email, contactUUID)
    }

    // Delete collection uuid from the product table
    fun deleteCollectionUUIDFromProductTable(collectionUUID: String) {
        DatabaseHelper.instance.deleteCollectionUUIDFromProductTable(collectionUUID)
    }

    // Remove rfid data of product
    fun removeRfidCode(productUUID: String) {
        DatabaseHelper.instance.removeRfidCode(productUUID)
    }

    // Checks is Phone Number Already Exist in the Address table
    fun isPhoneNumberExistInAddressTable(phoneNumber: String): Boolean {
        return DatabaseHelper.instance.isPhoneNumberExistInAddressTable(phoneNumber)
    }

    // Checks is Phone Number Already Exist in the Contact table
    fun isPhoneNumberExistInContactTable(phoneNumber: String): Boolean {
        return DatabaseHelper.instance.isPhoneNumberExistInContactTable(phoneNumber)
    }

    // Checks is Email Already Exist in the Contact table
    fun isEmailExistInContactTable(email: String): Boolean {
        return DatabaseHelper.instance.isEmailExistInContactTable(email)
    }

    // Insert new user in the User Table
    fun insetDataInUserTable(userDataModel: UserDataModel) {
        DatabaseHelper.instance.insetDataInUserTable(userDataModel)
    }

    // Check if User is Valid or not, through User Table
    fun isValidUser(email: String, password: String): Boolean {
        return DatabaseHelper.instance.isValidUser(email, password)
    }

    //Get User details through email and password from the user table
    fun getUserThroughEmailAndPassword(email: String, password: String): UserDataModel {
        return DatabaseHelper.instance.getUserThroughEmailAndPassword(email, password)
    }

    // get User through UserUUID from the User Table
    fun getUserDetailsThroughUserUUID(userUUID: String): UserDataModel {
        return DatabaseHelper.instance.getUserDetailsThroughUserUUID(userUUID)
    }

    // update collection in the product
    fun updateCollectionInProduct(productDataModel: ProductDataModel) {
        return DatabaseHelper.instance.updateCollectionInProduct(productDataModel)
    }

    // insert Data in the Session Table
    fun insertDataInSessionTable(userDataModel: UserDataModel) {
        DatabaseHelper.instance.insertDataInSessionTable(userDataModel)
    }

    // update user profile in the User Table
    fun updateProfileInUserTable(profileName: String, userUUID: String) {
        DatabaseHelper.instance.updateProfileInUserTable(profileName, userUUID)
    }

    // Checks if no device Available for particular user through userUUID
    fun isNoDeviceForUser(userUUID: String): Boolean {
        return DatabaseHelper.instance.isNoDeviceForUser(userUUID)
    }

    //get All Devices of particular user through userUUID
    fun getDevicesThroughUserUUID(userUUID: String): ArrayList<DeviceModel> {
        return DatabaseHelper.instance.getDevicesThroughUserUUID(userUUID)
    }

    // Insert New Device in the device table
    fun insertNewDeviceData(deviceDataModel: DeviceDataModel) {
        DatabaseHelper.instance.insertNewDeviceData(deviceDataModel)
    }

    //Delete Device from Device table through DeviceUUID
    fun deleteDeviceThoughDeviceUUID(deviceUUID: String) {
        DatabaseHelper.instance.deleteDeviceThoughDeviceUUID(deviceUUID)
    }

    // getting All User Details Accept Admin
    fun getAllUserDetails(): ArrayList<UserDataModel> {
        return DatabaseHelper.instance.getAllUserDetails()
    }

    //Deleting the User through the UserUUID from the user table
    fun deleteUserThroughUserUUID(userUUID: String) {
        DatabaseHelper.instance.deleteUserThroughUserUUID(userUUID)
    }

    //Update User Details in the User Table
    fun updateUserDetails(userDataModel: UserDataModel) {
        DatabaseHelper.instance.updateUserDetails(userDataModel)
    }

    // Add Collection in the Collection table
    fun addCollection(collectionDataModel: CollectionDataModel) {
        DatabaseHelper.instance.addCollection(collectionDataModel)
    }

    // Get All collection from the collection table
    fun getAllCollection(): ArrayList<CollectionDataModel> {
        return DatabaseHelper.instance.getAllCollection()
    }

    // Check if Category exist in the category table
    fun isCategoryExist(categoryName: String): Boolean? {
        return DatabaseHelper.instance.isCategoryExist(categoryName)
    }

    // Check if parent exist
    fun isParentExist(parentName: String): Boolean {
        return DatabaseHelper.instance.isParentExist(parentName)
    }

    // Check if stock Location exist
    fun isStockLocationExist(stockLocationName: String, parentUUID: String): Boolean {
        return DatabaseHelper.instance.isStockLocationExist(stockLocationName, parentUUID)
    }

    // get Stock location uuid
    fun getStockLocationUUID(stockLocationName: String, parentUUID: String): String {
        return DatabaseHelper.instance.getStockLocationUUID(stockLocationName, parentUUID)
    }

    /* Check if Category exist in the category table accept category uuid */
    fun isCategoryExistAccept(categoryDataModel: CategoryDataModel): Boolean? {
        return DatabaseHelper.instance.isCategoryExistAccept(categoryDataModel)
    }

    // Check if Collection exist in the category table
    fun isCollectionExist(collectionName: String): Boolean? {
        return DatabaseHelper.instance.isCollectionExist(collectionName)
    }

    /* Check if Collection exist in the category table accept collectionUUID */
    fun isCollectionExistAccept(collectionDataModel: CollectionDataModel): Boolean? {
        return DatabaseHelper.instance.isCollectionExistAccept(collectionDataModel)
    }

    // Check if Collection exist in the product section
    fun isCollectionExistInTheProduct(productUUID: String, collectionUUID: String): Boolean? {
        return DatabaseHelper.instance.isCollectionExistInTheProduct(productUUID, collectionUUID)
    }

    // add Category in the Category table
    fun addCategory(categoryDataModel: CategoryDataModel) {
        DatabaseHelper.instance.addCategory(categoryDataModel)
    }

    // update Category in the Category table
    fun updateCategory(categoryDataModel: CategoryDataModel) {
        DatabaseHelper.instance.updateCategory(categoryDataModel)
    }

    //Get All Products of particular category and collection  from the Product table
    fun getAllProductsThroughCategoryAndCollection(
        categoryUUID: String,
        collectionUUID: String
    ): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getAllProductsThroughCategoryAndCollection(
            categoryUUID,
            collectionUUID
        )
    }

    // get collection through Collection UUID
    fun getCollectionThroughUUID(collectionUUID: String): CollectionDataModel {
        return DatabaseHelper.instance.getCollectionThroughUUID(collectionUUID)
    }

    // update quantity in order table
    fun updateQuantityInOrder(orderDataModel: OrderDataModel) {
        DatabaseHelper.instance.updateQuantityInOrder(orderDataModel)
    }

    // Get All Category of the Particular Collection
    fun getAllCategoryOfParticularCollection(collectionUUID: String): ArrayList<CategoryDataModel> {
        return DatabaseHelper.instance.getAllCategoryOfParticularCollection(collectionUUID)

    }

    // Updating Collection in the Collection table
    fun updateCollection(collectionDataModel: CollectionDataModel) {
        DatabaseHelper.instance.updateCollection(collectionDataModel)
    }


    // Delete Category from the Category table
    fun deleteCategory(categoryUUID: String) {
        DatabaseHelper.instance.deleteCategory(categoryUUID)
    }

    // Delete Collection from the Collection table
    fun deleteCollection(collectionUUID: String) {
        DatabaseHelper.instance.deleteCollection(collectionUUID)
    }


    // Get all the category form the category table
    fun getAllCategory(): ArrayList<CategoryDataModel> {
        return DatabaseHelper.instance.getAllCategory()
    }

    // Check if Metal type already Exit in the metal Type Table
    fun isMetalTypeExist(metalTypeName: String): Boolean? {
        return DatabaseHelper.instance.isMetalTypeExist(metalTypeName)
    }

    // Check if Metal type exist in the metal type table accept metalTypeUUId
    fun isMetalTypeExistAccept(metalTypeDataModel: MetalTypeDataModel): Boolean? {
        return DatabaseHelper.instance.isMetalTypeExistAccept(metalTypeDataModel)
    }

    // Delete Metal Type from the metal type table
    fun deleteMetalType(metalTypeUUID: String) {
        DatabaseHelper.instance.deleteMetalType(metalTypeUUID)
    }

    // Delete Stock Location
    fun deleteStockLocation(stockLocationUUID: String) {
        DatabaseHelper.instance.deleteStockLocation(stockLocationUUID)
    }

    // Delete Product from the product table
    fun deleteProduct(productUUID: String) {
        DatabaseHelper.instance.deleteProduct(productUUID)
    }

    // Get All the metal type from the metal type table
    fun getAllMetalType(): ArrayList<MetalTypeDataModel> {
        return DatabaseHelper.instance.getAllMetalType()
    }

    // Updating metal type from the metal type table
    fun updateMetalType(metalTypeUUID: String, metalTypeName: String) {
        DatabaseHelper.instance.updateMetalType(metalTypeUUID, metalTypeName)
    }

    // Add Metal type in Metal_Type table
    fun addMetalTypeInTheMetalTypeTable(metalTypeDataModel: MetalTypeDataModel) {

        DatabaseHelper.instance.addMetalTypeInTheMetalTypeTable(metalTypeDataModel)
    }

    // Add new stock location
    fun addStockLocation(stockLocationDataModel: StockLocationDataModel) {
        DatabaseHelper.instance.addStockLocation(stockLocationDataModel)
    }

    // update stock location
    fun updateStockLocation(stockLocationDataModel: StockLocationDataModel) {
        DatabaseHelper.instance.updateStockLocation(stockLocationDataModel)
    }

    // Get All Stock location
    fun getAllStockLocation(): ArrayList<StockLocationDataModel> {
        return DatabaseHelper.instance.getAllStockLocation()
    }

    // Get All Stock location
    fun getStockLocation(stockLocationUUID: String): StockLocationDataModel {
        return DatabaseHelper.instance.getStockLocation(stockLocationUUID)
    }

    // Add Product in Product table
    suspend fun addProduct(productDataModel: ProductDataModel) {
        DatabaseHelper.instance.addProduct(productDataModel)
    }

    // Checks if Product is Exist in Cart table
    fun isProductExistInCartTable(userUUID: String, productUUID: String): Boolean? {
        return DatabaseHelper.instance.isProductExistInCartTable(userUUID, productUUID)
    }

    // get cartUUId Through userUUID and ProductUUID
    fun getCartUUID(userUUID: String, productUUID: String): String? {
        return DatabaseHelper.instance.getCartUUID(userUUID, productUUID)
    }

    //Inserting Cart in Card table
    fun insertProductInCartTable(cardDataModel: CartDataModel) {
        DatabaseHelper.instance.insertProductInCartTable(cardDataModel)
    }

    //Get All Products of particular Collection  from the Product table
    suspend fun getProductsThroughCollection(
        collectionUUIDList: List<String>,
        productUUID: String,
        numberOfItems: Int
    ): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getProductsThroughCollection(
            collectionUUIDList,
            productUUID,
            numberOfItems
        )
    }


    //Getting the Category UUId through Category Name
    fun getCategoryUUIDThroughCategoryName(categoryName: String): String? {

        return DatabaseHelper.instance.getCategoryUUIDThroughCategoryName(categoryName)
    }

    // return the total number of products
    fun getTotalNumberOfProducts(categoryUUID: String, isEmptyRfidProduct: Boolean): Int {
        return DatabaseHelper.instance.getTotalNumberOfProducts(categoryUUID, isEmptyRfidProduct)
    }

    // return the total number of products of collection
    fun getTotalNumberOfProductsOfCollection(
        collectionUUID: String,
        categoryUUID: String,
        isEmptyRfidProduct: Boolean
    ): Int {
        return DatabaseHelper.instance.getTotalNumberOfProductOfCollection(
            collectionUUID,
            categoryUUID,
            isEmptyRfidProduct
        )
    }

    //Get All Products from the Product table with limit and offset
    suspend fun getAllProducts(
        offset: Int,
        categoryUUID: String,
        isEmptyRfidProduct: Boolean
    ): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getAllProducts(offset, categoryUUID, isEmptyRfidProduct)
    }

    // get all product name that does not have rfid code
    suspend fun getAllProductName(limit: Int, offset: Int): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getProductName(limit, offset)
    }

    suspend fun getProductNameWithSearch(
        search: String,
        limit: Int,
        offset: Int
    ): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getProductNameWithSearch(search, limit, offset)
    }

    //Get All Products from the Product table without limit and offset
    suspend fun getAllProductsWithOutLimit(): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getAllProductsWithOutLimit()
    }

    // Get All Products that has RFID from the Product table
    suspend fun getAllProductsThatHasRFID(stockLocationUUID: String): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getAllProductsThatHasRFID(stockLocationUUID)
    }

    // Get Products with limit and offset
    suspend fun getProductsWithLimitAndOffset(offset: Int): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getProductsWithLimitAndOffset(offset)
    }

    // Get Products with search
    suspend fun getProductsWithDetailSearch(
        search: String,
        offset: Int,
        categoryUUID: String,
        isEmptyRfidProduct: Boolean
    ): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getProductsWithDetailSearch(
            search,
            offset,
            categoryUUID,
            isEmptyRfidProduct
        )
    }

    suspend fun getProductsWithDetailSearchAcceptCollection(
        search: String,
        offset: Int,
        collectionUUID: String,
        categoryUUID: String,
        isEmptyRfidProduct: Boolean
    ): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getProductsWithDetailSearchAcceptCollection(
            search,
            offset,
            collectionUUID,
            categoryUUID,
            isEmptyRfidProduct
        )
    }

    // Get total number of products of detail search
    fun getTotalNumberOfProductsOfDetailSearch(
        search: String,
        categoryUUID: String,
        isEmptyRfidProduct: Boolean
    ): Int {
        return DatabaseHelper.instance.getTotalNumberOfProductsOfDetailSearch(
            search,
            categoryUUID,
            isEmptyRfidProduct
        )
    }

    // Get total number of products of detail search accept collection
    fun getTotalNumberOfProductsOfDetailSearchAcceptCollection(
        search: String,
        collectionUUID: String,
        categoryUUID: String,
        isEmptyRfidProduct: Boolean
    ): Int {
        return DatabaseHelper.instance.getTotalNumberOfProductsOfDetailSearchAcceptCollection(
            search,
            collectionUUID,
            categoryUUID,
            isEmptyRfidProduct
        )
    }

    // Get Products with limit and offset
    suspend fun getProductsWithSearch(search: String, offset: Int): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getProductsWithSearch(search, offset)
    }

    //Get All Products Accept one Product from product table
    fun getAllProductsAcceptProduct(productUUID: String, offset: Int): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getAllProductsAcceptProduct(productUUID, offset)
    }

    /* Get All Products from the Product table Accept the collection */
    suspend fun getAllProductsAcceptCollection(
        collectionUUID: String,
        offset: Int,
        categoryUUID: String,
        isEmptyRfidProduct: Boolean
    ): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getAllProductsAcceptCollection(
            collectionUUID,
            offset,
            categoryUUID,
            isEmptyRfidProduct
        )
    }

    //Get All Products from the Product table Accept Category one Category
    fun getAllProductsAcceptCollection(
        collectionUUIDList: List<String>,
        offset: Int
    ): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getAllProductsAcceptCollection(collectionUUIDList, offset)
    }

    // Getting the Category Name through Category UUID
    fun getCategoryNameThroughCategoryUUID(categoryUUID: String): String? {
        return DatabaseHelper.instance.getCategoryNameThroughCategoryUUID(categoryUUID)
    }

    // Getting the MetalType Name through MetalType UUID
    fun getMetalTypeNameThroughMetalTypeUUID(metalTypeUUID: String): String? {
        return DatabaseHelper.instance.getMetalTypeNameThroughMetalTypeUUID(metalTypeUUID)
    }

    // Getting the MetalTypeUUID through MetalType name
    fun getMetalTypeUUIDThroughMetalTypeName(metalTypeName: String): String? {
        return DatabaseHelper.instance.getMetalTypeUUIDThroughMetalTypeName(metalTypeName)
    }

    // Getting the Collection UUID through Collection name
    fun getCollectionUUIDThroughCollectionName(collectionName: String): String? {
        return DatabaseHelper.instance.getCollectionUUIDThroughCollectionName(collectionName)
    }

    // Getting the Collection name through Collection uuid
    fun getCollectionNameThroughCollectionUUID(collectionName: String): String? {
        return DatabaseHelper.instance.getCollectionNameThroughCollectionUUID(collectionName)
    }

    // Getting the Parent UUID through parent name
    fun getParentUUIDThroughParentName(parentName: String): String? {
        return DatabaseHelper.instance.getParentUUIDThroughParentName(parentName)
    }

    // Inserting Contact in Contact table
    fun insertAddressInAddressTable(addressDataModel: AddressDataModel) {
        return DatabaseHelper.instance.insertAddressInAddressTable(addressDataModel)
    }

    // Inserting Address in Address table
    fun insertContact(contactDataModel: ContactDataModel) {
        return DatabaseHelper.instance.insertContact(contactDataModel)
    }

    // Inserting Order in Order table
    fun insertOrder(orderDataModel: OrderDataModel) {
        return DatabaseHelper.instance.insertOrder(orderDataModel)
    }

    // get  order of particular user
    fun getOrderByUUID(orderUUID: String): OrderDataModel {
        return DatabaseHelper.instance.getOrderByUUID(orderUUID)
    }

    // get orders of particular user
    fun getOrders(userUUID: String, status: String): ArrayList<OrderDataModel> {

        return DatabaseHelper.instance.getOrders(userUUID, status)
    }

    // print pdf
    fun printPdf(context: Context, file: File) {

        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = context.getString(R.string.app_name) + " Document"

        printManager.print(
            jobName, object : PrintDocumentAdapter() {

                override fun onLayout(

                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }

                    val pdi = PrintDocumentInfo.Builder(file.name)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()

                    callback?.onLayoutFinished(pdi, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    var input: FileInputStream? = null
                    var output: FileOutputStream? = null

                    try {
                        input = FileInputStream(file)
                        output = FileOutputStream(destination?.fileDescriptor)

                        val buf = ByteArray(1024)
                        var bytesRead: Int

                        while (input.read(buf).also { bytesRead = it } > 0) {
                            output.write(buf, 0, bytesRead)
                        }

                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: IOException) {
                        callback?.onWriteFailed(e.toString())
                    } finally {
                        try {
                            input?.close()
                            output?.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }, PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
                .setResolution(PrintAttributes.Resolution("id", "label", 600, 600))
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build()
        )

    }

    // checks if order exist
    fun isOrderExist(userUUID: String): Boolean? {
        return DatabaseHelper.instance.isOrderExist(userUUID)
    }

    // store OrderUUID in shared preference
    fun storeOrderUUID(context: Context, orderUUID: String) {

        val sharedPreferences = context.getSharedPreferences(
            Constants.orderData,
            AppCompatActivity.MODE_PRIVATE
        )

        val myEdit = sharedPreferences.edit()
        myEdit.putString(Constants.orderUUID, orderUUID)
        myEdit.apply()

    }

    // get Order UUID from shared preference
    fun getOrderUUID(context: Context): String? {

        val sharedPreferences = context.getSharedPreferences(
            Constants.orderData,
            AppCompatActivity.MODE_PRIVATE
        )

        return sharedPreferences.getString(Constants.orderUUID, null)
    }


    // insert Order in the order table
    fun insertOrder(context: Context, productUUID: String, productPrice: Double) {

        val orderUUID = getOrderUUID(context)

        val isOrderExist = orderUUID?.let { isOrderExist(it) }

        if (isOrderExist == true) {

            val orderDataModel = getOrderByUUID(orderUUID)

            val newOrderDataModel = OrderDataModel()

            if (orderDataModel.productUUIDUri?.isEmpty() == true) {
                newOrderDataModel.productUUIDUri = productUUID
            } else {

                val productUUIDList =
                    orderDataModel.productUUIDUri?.split(",")?.toMutableList() ?: mutableListOf()
                // Add the new product UUID to the list
                productUUIDList.add(productUUID)
                newOrderDataModel.productUUIDUri = productUUIDList.joinToString().replace(" ", "")

            }

            if (orderDataModel.productQuantityUri?.isEmpty() == true) {
                newOrderDataModel.productQuantityUri = ""
            } else {

                val productQuantityList =
                    orderDataModel.productQuantityUri?.split(",")?.toMutableList()
                        ?: mutableListOf()
                // Add the quantity to the list
                productQuantityList.add("1")
                newOrderDataModel.productQuantityUri =
                    productQuantityList.joinToString().replace(" ", "")

            }

            newOrderDataModel.orderUUID = orderDataModel.orderUUID

            newOrderDataModel.totalAmount = orderDataModel.totalAmount?.plus(productPrice)

            updateOrder(newOrderDataModel)

        } else {

            val orderDataModel = OrderDataModel()

            orderDataModel.orderUUID = orderUUID

            orderDataModel.orderNo =
                formatToSixDigitNumber(DatabaseHelper.instance.getTotalOrder() + 1)

            orderDataModel.productUUIDUri = productUUID

            orderDataModel.productQuantityUri = ""

            orderDataModel.userUUID = Utils.GetSession().userUUID

            orderDataModel.addressUUID = ""

            orderDataModel.pdfName = ""

            orderDataModel.date = ""

            orderDataModel.status = Constants.New

            orderDataModel.totalAmount = productPrice

            Utils.insertOrder(orderDataModel)

        }
    }

    // remove order from the order table
    fun removeOrder(context: Context, productUUID: String, productPrice: Double) {

        val orderDataModel = getOrderUUID(context)?.let { getOrderByUUID(it) }

        val productUUIDList = orderDataModel?.productUUIDUri?.split(",")?.toMutableList()

        productUUIDList?.remove(productUUID)

        if (productUUIDList?.isEmpty() == true) {

            orderDataModel.orderUUID?.let { DatabaseHelper.instance.deleteOrder(it) }

            // generate new order UUID
            storeOrderUUID(context, generateUUId())

        } else {

            val newOrderDataModel = OrderDataModel()

            newOrderDataModel.orderUUID = orderDataModel?.orderUUID

            newOrderDataModel.totalAmount = orderDataModel?.totalAmount?.minus(productPrice)

            newOrderDataModel.productUUIDUri = productUUIDList?.joinToString()?.replace(" ", "")

            updateOrder(newOrderDataModel)

        }
    }

    // update Order in Order table
    fun updateOrder(orderDataModel: OrderDataModel) {
        return DatabaseHelper.instance.updateOrder(orderDataModel)
    }

    // update order status to confirm
    fun updateOrderStatus(orderDataModel: OrderDataModel) {
        return DatabaseHelper.instance.updateOrderStatus(orderDataModel)
    }

    // Delete cart from the cart table
    fun deleteCart(userUUID: String) {
        return DatabaseHelper.instance.deleteCart(userUUID)
    }

    // Update Address in the Address Table
    fun updateAddressInTheAddressTable(addressDataModel: AddressDataModel) {
        return DatabaseHelper.instance.updateAddressInTheAddressTable(addressDataModel)
    }

    // Update Contact in the Contact Table
    fun updateContactInTheContactTable(contactDataModel: ContactDataModel) {
        return DatabaseHelper.instance.updateContactInTheContactTable(contactDataModel)
    }

    //get Cart through user UUId
    fun getCartThroughUserUUID(userUUID: String): ArrayList<CartDataModel> {
        return DatabaseHelper.instance.getCartThroughUserUUID(userUUID)
    }

    //Checks if cart is empty
    fun isCartEmpty(userUUID: String): Boolean {
        return DatabaseHelper.instance.isCartEmpty(userUUID)
    }

    // Update Quantity of Product in Card Table
    fun updateProductQuantity(quantity: Int, cardUUID: String) {
        DatabaseHelper.instance.updateProductQuantity(quantity, cardUUID)
    }

    // Update Quantity of Product in Card Table
    fun updateProductQuantityInCart(userUUID: String, productUUID: String, quantity: Int) {
        DatabaseHelper.instance.updateProductQuantityInCart(userUUID, productUUID, quantity)
    }

    //Deleting the cart from the cart table
    fun deleteProductFromCart(cardUUID: String) {
        DatabaseHelper.instance.deleteProductFromCart(cardUUID)
    }

    //Deleting the cart from the cart table
    fun deleteProductFromCart(userUUID: String, productUUID: String) {
        DatabaseHelper.instance.deleteProductFromCart(userUUID, productUUID)
    }

    // Add purchase
    fun addPurchase(purchasingDataModel: PurchasingDataModel) {
        DatabaseHelper.instance.addPurchase(purchasingDataModel)
    }

    fun updatePurchaseStatus(purchasingUUID: String) {
        DatabaseHelper.instance.updatePurchaseStatus(purchasingUUID)
    }

    // Update purchase
    fun updatePurchase(purchasingDataModel: PurchasingDataModel) {
        DatabaseHelper.instance.updatePurchase(purchasingDataModel)
    }

    // delete Purchase
    fun deletePurchase(purchaseUUID: String) {
        DatabaseHelper.instance.deletePurchase(purchaseUUID)
    }

    // Get All Purchase
    suspend fun getAllPurchase(): ArrayList<PurchasingDataModel> {
        return DatabaseHelper.instance.getAllPurchase()
    }

    // get purchase by uuid
    fun getPurchaseByUUID(purchaseUUID: String): PurchasingDataModel {
        return DatabaseHelper.instance.getPurchaseByUUID(purchaseUUID)
    }

    //Deleting the address from the address table
    fun deleteAddress(addressUUID: String) {
        DatabaseHelper.instance.deleteAddress(addressUUID)
    }

    // Deleting the contact from the contact table
    fun deleteContact(contactUUID: String) {
        DatabaseHelper.instance.deleteContact(contactUUID)
    }

    //Get All Address of particular user from the Address table
    fun getAllAddressThroughUserUUID(userUUID: String): ArrayList<AddressDataModel> {
        return DatabaseHelper.instance.getAllAddressThroughUserUUID(userUUID)
    }

    // is this address uuid exist
    fun isAddressUUIDExist(addressUUID: String): Boolean? {
        return DatabaseHelper.instance.isAddressUUIDExist(addressUUID)
    }

    // get address through address uuid
    fun getAddressThroughAddressUUID(addressUUID: String): AddressDataModel {
        return DatabaseHelper.instance.getAddressThroughAddressUUID(addressUUID)
    }

    // Check if barcode exit
    fun isBarcodeExist(barcodeData: String): Boolean? {
        return DatabaseHelper.instance.isBarcodeExist(barcodeData)
    }

    // get product uuid through barcode
    fun getProductUUIDByBarcode(barcodeData: String): String? {
        return DatabaseHelper.instance.getProductUUIDByBarcode(barcodeData)
    }

    // Get All Contact of particular user from the Contact table
    fun getAllContactThroughUserUUID(userUUID: String): ArrayList<ContactDataModel> {
        return DatabaseHelper.instance.getAllContactThroughUserUUID(userUUID)
    }

    fun getContactByUUID(contactUUID: String): ContactDataModel {
        return DatabaseHelper.instance.getContactByUUID(contactUUID)
    }

    // Getting the Product through Product UUID
    fun getProductThroughProductUUID(productUUID: String): ProductDataModel {
        return DatabaseHelper.instance.getProductThroughProductUUID(productUUID)
    }

    // update Product in the Product table
    suspend fun updateProduct(productDataModel: ProductDataModel) {
        DatabaseHelper.instance.updateProduct(productDataModel)
    }

    fun updateProductDetails(productDataModel: ProductDataModel) {
        DatabaseHelper.instance.updateProductDetails(productDataModel)
    }

    @JvmStatic
    fun LOGOUT() {
        DatabaseHelper.instance.deleteSession()
    }

    @JvmStatic
    fun I(cx: Context, startActivity: Class<*>?, data: Bundle?) {
        val i = Intent(cx, startActivity)
        if (data != null) i.putExtras(data)
        cx.startActivity(i)
    }


    @JvmStatic
    val deviceName: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }

    fun disableButton(loader: LoadingButton) {
        loader.alpha = 0.4f
        loader.isEnabled = false
    }

    fun enableButton(loader: View) {
        loader.alpha = 1.0f
        loader.isEnabled = true
    }

    fun disableButton(loader: View) {
        loader.alpha = 0.5f
        loader.isEnabled = false
    }

    fun hashMapTORequestBody(hm: HashMap<*, *>): RequestBody {
        val json = (hm as Map<*, *>?)?.let { JSONObject(it).toString() }
        return json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    }

    fun generateUniqueCodes(input: String): List<String> {
        val parts = input.split(" ")

        val codes = mutableListOf<String>()

        for (part in parts) {
            if (part.length >= 4) {
                val prefix = part.substring(0, 4)
                val suffix = part.takeLast(4)

                val random1 = (1..9999).random()
                val random2 = (1..9999).random()

                val code = "$prefix$random1$random2$suffix"
                codes.add(code)
            } else {
                val random1 = (1..9999).random()
                val code = "$part$random1"
                codes.add(code)
            }
        }

        return codes
    }

    fun getAddressFromLatLng(context: Context, latLng: LatLng): String {
        val geocoder = Geocoder(context)
        val addresses: List<Address> =
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) as List<Address>

        if (addresses.isNotEmpty()) {
            val address = addresses[0]
            val addressParts = mutableListOf<String>()

            // Get various address components (if available)
            if (address.featureName != null) {
                addressParts.add(address.featureName)
            }
            if (address.thoroughfare != null) {
                addressParts.add(address.thoroughfare)
            }
            if (address.subLocality != null) {
                addressParts.add(address.subLocality)
            }
            if (address.locality != null) {
                addressParts.add(address.locality)
            }
            if (address.adminArea != null) {
                addressParts.add(address.adminArea)
            }
            if (address.countryName != null) {
                addressParts.add(address.countryName)
            }

            return addressParts.joinToString(", ")
        } else {
            return "Address not found"
        }
    }

    fun isAlphanumeric7To11Characters(input: String): Boolean {
        val alphanumericPattern = "^[a-zA-Z0-9]{7,11}$".toRegex()
        return alphanumericPattern.matches(input)
    }


    fun applyAnimation(view: View, context: Context, animationID: Int) {
        view.visibility = View.VISIBLE
        val animation =
            AnimationUtils.loadAnimation(context, animationID)
        view.startAnimation(animation)
    }

    @Throws(IOException::class)
    fun createImageFile(c: Context): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = c.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    @Throws(IOException::class)
    fun createVideoFile(c: Context): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "MP4_" + timeStamp + "_"
        val storageDir: File? = c.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".mp4",  /* suffix */
            storageDir /* directory */
        )
    }

    @Throws(IOException::class)
    fun createAudioFile(c: Context): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFileName = "mp3_" + timeStamp + "_"

        // Get the external storage directory where audio files can be shared
        val storageDir: File? = c.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        if (storageDir != null && storageDir.exists()) {
            return File.createTempFile(
                audioFileName,  /* prefix */
                ".mp3",  /* suffix */
                storageDir /* directory */
            )
        } else {
            throw IOException("External storage directory is not available")
        }
    }

    fun getVideoDuration(context: Context, uri: Uri): String {
        E("uri::" + uri)
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, uri)
            val durationString =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMillis = durationString?.toLong() ?: 0
            val seconds = (durationMillis / 1000) % 60
            val minutes = (durationMillis / (1000 * 60))
            return String.format("%02d:%02d", minutes, seconds)
        } catch (e: Exception) {
            e.printStackTrace()
            return "00:00"
        } finally {
            retriever.release()
        }
    }

    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                if (columnIndex != -1) {
                    return it.getString(columnIndex)
                }
            }
        }
        return null
    }

    fun getAudioDuration(filePath: String): Long {
        E("filePath::$filePath")
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        E("getAudioDuration::$duration")
        return duration?.toLong() ?: 0
    }


    fun getVideoDuration(filePath: String): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        E("getVideoDuration::$duration")
        return duration?.toLong() ?: 0
    }


    fun addTextChangedListener(editText: EditText, errorTextView: TextView) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                errorTextView.visibility = View.GONE
            }

        })
    }

    fun isImageFileEmpty(imageFile: File): Boolean {
        // Check if the file exists and is a file (not a directory)
        if (!imageFile.exists() || !imageFile.isFile) {
            return true
        }

        // Check if the file's length is 0 bytes
        return imageFile.length() == 0L
    }


    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                )
    }

    fun getCountryCode(context: Context): String {
        val phoneNumberUtil = PhoneNumberUtil.createInstance(context)
        return "+" + phoneNumberUtil.getCountryCodeForRegion(SavedData.getCountryRegion())
    }

    private fun capitalize(s: String?): String {
        if (s == null || s.length == 0) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            first.uppercaseChar().toString() + s.substring(1)
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    fun setWebView(webView: WebView, data: String?) {
        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = true
        webView.loadData(data!!, "text/html; charset=utf-8", "UTF-8")
    }

    fun UnAuthorizationToken(cx: Context) {
        DatabaseHelper.instance.deleteSession()
        //  I_clear(cx, LoginActivity::class.java, null)
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    fun CustomAlertDialog(context: Context?, message: String?) {
        val dialog = Dialog(context!!, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val alertdialogBinding = AlertdialogBinding.inflate(LayoutInflater.from(context))
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(alertdialogBinding.root)
        alertdialogBinding.tvDesc.text = message
        dialog.findViewById<View>(R.id.tvPermittManually).setOnClickListener { view: View? ->
            if (AppController.instance.isOnline) {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    fun logoutAlertDialog(c: Context): Dialog {
        val dialog = Dialog(c, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        dialog.setCanceledOnTouchOutside(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.logout)
        dialog.findViewById<View>(R.id.tvOK).setOnClickListener { view: View? ->
            //logout(c)
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.tvCancel)
            .setOnClickListener { view: View? -> dialog.cancel() }
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }


//    fun expandView(view: View) {
//        view.visibility = View.VISIBLE
//        view.measure(
//            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//        )
//        val targetHeight = view.measuredHeight
//        view.layoutParams.height = 1
//        view.requestLayout()
//
//        view.animate()
//            .setDuration(100) // Adjust the duration as needed
//            .setInterpolator(AccelerateDecelerateInterpolator())
//            .translationY(0f)
//            .alpha(1f)
//            .setListener(null)
//            .setUpdateListener {
//                val params = view.layoutParams
//                params.height = (targetHeight * it.animatedFraction).toInt()
//                view.layoutParams = params
//            }
//    }

    fun expandView(view: View) {
        view.visibility = View.VISIBLE
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = view.measuredHeight
        view.layoutParams.height = 0  // Start from 0 height
        view.requestLayout()

        view.animate()
            .setDuration(100) // Adjust the duration as needed
            .setInterpolator(AccelerateDecelerateInterpolator())
            .alpha(1f)
            .setListener(null)
            .setUpdateListener {
                val params = view.layoutParams
                params.height = (targetHeight * it.animatedFraction).toInt()
                view.layoutParams = params
            }
            .withEndAction {
                // Ensure the final height is properly set
                view.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                view.requestLayout()
            }
    }


    fun collapseView(view: View) {
        val initialHeight = view.height

        view.animate()
            .setDuration(100) // Adjust the duration as needed
            .setInterpolator(AccelerateDecelerateInterpolator())
            .alpha(0f)
            .setUpdateListener {
                val params = view.layoutParams
                params.height = (initialHeight * (1 - it.animatedFraction)).toInt()
                view.layoutParams = params
            }
            .withEndAction {
                view.visibility = View.GONE
                view.layoutParams.height = 0
                view.requestLayout()
            }
    }


//    fun collapseView(view: View) {
//        val initialHeight = view.height
//        view.animate()
//            .setDuration(100) // Adjust the duration as needed
//            .setInterpolator(AccelerateDecelerateInterpolator())
//            .translationY(-initialHeight.toFloat())
//            .alpha(0f)
//            .setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    super.onAnimationEnd(animation)
//                    view.visibility = View.GONE
//                }
//            })
//            .setUpdateListener {
//                val params = view.layoutParams
//                params.height = (initialHeight * (1 - it.animatedFraction)).toInt()
//                view.layoutParams = params
//            }
//    }

    fun rotateView90Degrees(view: View) {
        // Create an ObjectAnimator to animate the rotation property
        val rotateAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 90f)

        // Set duration and interpolator for the animation
        rotateAnimator.duration = 1000 // 1000 milliseconds (1 second)
        rotateAnimator.interpolator = LinearInterpolator()

        // Start the animation
        rotateAnimator.start()
    }


    fun rotateView(view: View, degrees: Float) {
        // Create an ObjectAnimator to animate the rotation property
        val rotateAnimator = ObjectAnimator.ofFloat(view, "rotation", view.rotation, degrees)

        // Set duration and interpolator for the animation
        rotateAnimator.duration = 300
        rotateAnimator.interpolator = LinearInterpolator()

        // Start the animation
        rotateAnimator.start()
    }


    fun expandOrCollapseView(v: View, expand: Boolean) {
        if (expand) {
            v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val targetHeight = v.measuredHeight - 45
            v.layoutParams.height = 0
            v.visibility = View.VISIBLE
            val valueAnimator = ValueAnimator.ofInt(targetHeight)
            valueAnimator.addUpdateListener { animation ->
                v.layoutParams.height = animation.animatedValue as Int
                v.requestLayout()
            }
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.duration = 200
            valueAnimator.start()
        } else {
            val initialHeight = v.measuredHeight
            val valueAnimator = ValueAnimator.ofInt(initialHeight, 0)
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.addUpdateListener { animation ->
                v.layoutParams.height = animation.animatedValue as Int
                v.requestLayout()
                if (animation.animatedValue as Int == 0) v.visibility = View.GONE
            }
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.duration = 200
            valueAnimator.start()
        }
    }


    fun deleteAccountAlertDialog(c: Context): Dialog {
        val dialog = Dialog(c, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        dialog.setCanceledOnTouchOutside(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.logout)
        val textView = dialog.findViewById<TextView>(R.id.tvDesc)
        textView.setText(R.string.are_you_sure_want_to_delete_your_account)
        dialog.findViewById<View>(R.id.tvOK).setOnClickListener { view: View? ->
            //deleteUserAccount(c)
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.tvCancel)
            .setOnClickListener { view: View? -> dialog.cancel() }
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }


    fun Picasso(Url: String, imageView: ImageView?, dummy: Int) {
        E("Url::" + Url)
        val url = Url.replace("\\", "/")
        E("Url::" + url)
        if (imageView != null) {
            Glide.with(AppController.getContext())
                .load(/*Const.HOST_URL +*/ url)
                .error(dummy)
                .into(imageView)
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    //    val currentDate: String
//        get() {
//
//        }
    fun currentDate(): String {
        val c = Calendar.getInstance().time
        E("Current time => $c")
        val df =
            SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return df.format(c)
    }

    fun prettyCount(number: Number): String {
        val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
        val numValue = number.toLong()
        val value = Math.floor(Math.log10(numValue.toDouble())).toInt()
        val base = value / 3
        return if (value >= 3 && base < suffix.size) {
            E(
                "::prettyCount::" + numValue / Math.pow(
                    10.0,
                    (base * 3).toDouble()
                )
            )
            val i = "" + numValue / Math.pow(10.0, (base * 3).toDouble())
            val values = i.split("\\.").toTypedArray()
            if (values[1] == "0") {
                values[0] + suffix[base]
            } else {
                DecimalFormat("#0.0")
                    .format(numValue / Math.pow(10.0, (base * 3).toDouble())) + suffix[base]
            }
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
    }

    fun <T> removeDuplicates(list: List<T>): List<T> {

        // Create a new ArrayList
        val newList: MutableList<T> = ArrayList()

        // Traverse through the first list
        for (element in list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {
                newList.add(element)
            }
        }

        // return the new list
        return newList
    }

    /**
     * Change the status bar Color of the Activity to the Desired Color.
     *
     * @param activity - Activity
     * @param color    - Desired Color
     */
    fun changeStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(activity, color)
    }

//    fun printKeyHash(context: Activity): String? {
//        val packageInfo: PackageInfo
//        var key: String? = null
//        try {
////getting application package name, as defined in manifest
//            val packageName = context.applicationContext.packageName
//
////Retriving package info
//            packageInfo = context.packageManager.getPackageInfo(
//                packageName,
//                PackageManager.GET_SIGNATURES
//            )
//            E("Package Name=" + context.packageName)
//            for (signature in packageInfo.signatures) {
//                val md = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                key = String(Base64.encode(md.digest(), 0))
//                E("Key Hash=$key")
//            }
//        } catch (e1: PackageManager.NameNotFoundException) {
//            E("Name not found$e1")
//        } catch (e: NoSuchAlgorithmException) {
//            E("No such an algorithm$e")
//        } catch (e: Exception) {
//            E("Exception$e")
//        }
//        return key
//    }

    @JvmStatic
    fun I_finish(cx: Context, startActivity: Class<*>?, data: Bundle?) {
        val i = Intent(cx, startActivity)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        if (data != null) i.putExtras(data)
        cx.startActivity(i)
    }

    @JvmStatic
    fun I_clear(cx: Context, startActivity: Class<*>?, data: Bundle?) {
        val i = Intent(cx, startActivity)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        if (data != null) i.putExtras(data)
        cx.startActivity(i)
    }

    @JvmStatic
    fun E(msg: String?) {
        /*if (Const.Development == Constants.Debug) {
        }*/
        Log.e("Log.E", msg!!)

    }

    fun DetectUIMode(activity: Activity): Int {
        return activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
    }

    fun getFormattedDate(smsTimeInMilis: Long, context: Context): String {
        val smsTime = Calendar.getInstance()
        smsTime.timeInMillis = smsTimeInMilis
        val now = Calendar.getInstance()
        val timeFormatString = "h:mm aa"
        val dateTimeFormatString = "EEE, MMM d | h:mm aa"
        val HOURS = (60 * 60 * 60).toLong()
        return if (now[Calendar.DATE] == smsTime[Calendar.DATE]) {
            context.getString(R.string.today) + " " + DateFormat.format(
                timeFormatString,
                smsTime
            )
        } else if (now[Calendar.DATE] - smsTime[Calendar.DATE] == 1) {
            context.getString(R.string.yesterday) + " " + DateFormat.format(
                timeFormatString,
                smsTime
            )
        } else if (now[Calendar.YEAR] == smsTime[Calendar.YEAR]) {
            DateFormat.format(dateTimeFormatString, smsTime).toString()
        } else {
            DateFormat.format("MMM dd yyyy | h:mm aa", smsTime).toString()
        }
    }

    val dateAfterYear: String
        get() {
            val cal = Calendar.getInstance()
            cal.time = Date()
            cal.add(Calendar.YEAR, 1)
            val smsTime = Calendar.getInstance()
            smsTime.timeInMillis = cal.time.time
            return DateFormat.format(
                "dd'" + getOrdinal(
                    smsTime[Calendar.DAY_OF_MONTH]
                ) + "' MMM yyyy", smsTime
            ).toString()
        }

    fun getOrdinal(day: Int): String {
        val ordinal: String
        ordinal = when (day % 20) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> if (day > 30) "st" else "th"
        }
        return ordinal
    }


    fun getMimeType(context: Context, uri: Uri): String {
        var extension: String? = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }
        if (extension.isNullOrEmpty()) {
            val cR = context.contentResolver
            val mime = MimeTypeMap.getSingleton()
            extension = mime.getExtensionFromMimeType(cR.getType(uri))
        }
        return extension ?: Constants.blank
    }

    fun isMediaUrlForVIDEO(context: Context, mediaUri: String): Boolean {
        return getMimeType(context, mediaUri.toUri()) == Constants.mp4 || getMimeType(
            context, mediaUri.toUri()
        ) == Constants.mov || getMimeType(
            context, mediaUri.toUri()
        ) == Constants.avi || getMimeType(context, mediaUri.toUri()) == Constants.wav
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }


    fun saveImageToGallery(context: Context, bitmap: Bitmap) {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "JPEG_" + timeStamp + "_"
        val mimeType = "image/jpeg" // Change the MIME type as needed

        // Get the content resolver
        val contentResolver = context.contentResolver

        // Create a ContentValues object to describe the image
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }

        // Use the MediaStore API to insert the image into the gallery
        val imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageDetails)

        // Open an output stream to write the bitmap data to the content provider
        val imageOutputStream: OutputStream? = imageUri?.let {
            contentResolver.openOutputStream(it)
        }

        try {
            imageOutputStream?.use { outputStream ->
                // Compress and write the bitmap to the output stream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle any exceptions here
        }
//        CustomeToast(context, "Saved Successfully")

    }

    @SuppressLint("ResourceType")
    fun initProgressDialog(c: Context?): Dialog {

        val dialog = Dialog(c!!)
        dialog.setCanceledOnTouchOutside(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.utils_progress_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog

    }

    fun videoProgressDialog(c: Context?): Dialog {
        val dialog = Dialog(c!!)
        dialog.setCanceledOnTouchOutside(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)
        dialog.show()
        return dialog
    }

    fun pdfProgressDialog(c: Context?): Dialog {
        val dialog = Dialog(c!!)
        dialog.setCanceledOnTouchOutside(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnCancelListener { CustomeToast(c, "Unable to load Pdf") }
        dialog.show()
        return dialog
    }

    @SuppressLint("InflateParams", "MissingInflatedId")
    fun T(context: Context, msg: String?) {

        val toast = Toast(context)
        val binding = ItemCustomToastBinding.inflate(LayoutInflater.from(context))
        binding.tvMessage.text = msg
        toast.view = binding.root
        toast.duration = Toast.LENGTH_SHORT
        toast.show()
    }


    // Set input type dynamically based on locale
    fun toSetPasswordAsLanguage(etPassword: EditText?, context: Context) {
        val currentLocale = getCurrentLanguage()
        if (currentLocale == arabic) {
            etPassword?.inputType = InputType.TYPE_CLASS_TEXT
        } else {
            etPassword?.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        //Setting font family
        etPassword?.typeface = ResourcesCompat.getFont(context, R.font.montserrat_arabic_medium)
    }

    fun CustomeToast(c: Context?, msg: String?) {
        val toast = Toast(c)
//        val itemCustomToastBinding = ItemCustomToastBinding.inflate(LayoutInflater.from(c))
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.view?.startAnimation(AnimationUtils.loadAnimation(c, R.anim.top_to_bottom))
//        itemCustomToastBinding.tvMessage.text = msg
//        itemCustomToastBinding.mcvToast.minimumWidth = getScreenWidth(c!!) - 12
//        toast.view = itemCustomToastBinding.root //setting the view of custom toast layout
        toast.show()
    }

    fun getScreenWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun openMapView(context: Context, latitude: String, longitude: String, locationName: String?) {
        val gmmIntentUri =
            Uri.parse("http://maps.google.com/maps?saddr=&daddr=$latitude,$longitude&directionsmode=driving")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        }
    }

    fun makeCallIntent(context: Context, phoneNumber: String = "+91 1234567890") {

        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        context.startActivity(intent)
    }


    fun share(c: Context, subject: String?, shareBody: String?) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        c.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun T_Long(c: Context?, msg: String?) {
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show()
    }

    /*public static void setLanguage(String language, @NonNull Context context) {
        SavedData.saveLanguage(language);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
        LocaleHelper.setLocale(context, language);
    }*/
    fun alert(activity: Context, message: String?) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.app_name))
        builder.setMessage(message)
            .setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
        val alertdialog = builder.create()
        alertdialog.show()
    }
}
