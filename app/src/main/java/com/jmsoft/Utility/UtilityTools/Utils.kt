package com.jmsoft.basic.UtilityTools

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.webkit.MimeTypeMap
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.jmsoft.R
import com.jmsoft.Utility.Database.AddressDataModel
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.DeviceDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.UtilityTools.loadingButton.LoadingButton
import com.jmsoft.basic.Database.DatabaseHelper
import com.jmsoft.basic.Database.UserDataModel
import com.jmsoft.basic.UtilityTools.Constants.Companion.CONFIG_FILE
import com.jmsoft.basic.UtilityTools.Constants.Companion.arabic
import com.jmsoft.basic.UtilityTools.Constants.Companion.dimen
import com.jmsoft.basic.UtilityTools.Constants.Companion.email
import com.jmsoft.basic.UtilityTools.Constants.Companion.name
import com.jmsoft.basic.UtilityTools.Constants.Companion.password
import com.jmsoft.basic.UtilityTools.Constants.Companion.statusBarHeight
import com.jmsoft.databinding.AlertdialogBinding
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
import java.io.IOException
import java.io.OutputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

object Utils {

    // Get thousand separate price
    fun getThousandSeparate(price: Int): String {

        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        return numberFormat.format(price).toString()
    }

    // Get Status bar height
    @SuppressLint("InternalInsetResource")
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
        return UUID.randomUUID().toString()
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
    fun getImageFromInternalStorage(context: Context, imageFileName: String): Bitmap? {
        val directory = context.filesDir
        val file = File(directory, imageFileName)
        return BitmapFactory.decodeStream(FileInputStream(file))
    }

    //delete image from the internal storage
    fun deleteImageFromInternalStorage(context: Context, imageFileName: String): Boolean {
        val dir = context.filesDir
        val file = File(dir, imageFileName)
        return file.delete()
    }

    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

//        context.createConfigurationContext(configuration)

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

    //this method checks if any user has this phone number
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

    // Checks is Phone Number Already Exist in the Address table
    fun isPhoneNumberExistInAddressTable(phoneNumber: String): Boolean {
        return DatabaseHelper.instance.isPhoneNumberExistInAddressTable(phoneNumber)
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

    // Inserting Category in Category table
    fun insertCategoryInCategoryTable(categoryDataModel: CategoryDataModel) {
        DatabaseHelper.instance.insertCategoryInCategoryTable(categoryDataModel)
    }

    // Inserting Product in Product table
    fun insertProductInProductTable(
        categoryName: String,
        productName: String,
        productPrice: Int,
        bitmapOne: Bitmap,
        bitmapTwo: Bitmap,
        context: Context
    ) {

        val productDataModel = ProductDataModel()
        productDataModel.productUUId = Utils.generateUUId()
        productDataModel.categoryUUID = Utils.getCategoryUUIDThroughCategoryName(categoryName)
        productDataModel.productName = productName

        val nameOfImageOne = Utils.getImageFileName()
        val nameOfImageTwo = Utils.getImageFileName()

        bitmapOne.let { Utils.saveToInternalStorage(context, it, nameOfImageOne) }
        bitmapTwo.let { Utils.saveToInternalStorage(context, it, nameOfImageTwo) }


        productDataModel.productImage = "$nameOfImageOne,$nameOfImageTwo"

        productDataModel.productPrice = productPrice
        productDataModel.productDescription = "No Description"
        productDataModel.productWeight = "5"
        productDataModel.productMetalType = "Gold"
        productDataModel.productUnitOfMeasurement = "g"
        productDataModel.productCarat = "24"
        productDataModel.productRFID = "100"
        productDataModel.productCategory = categoryName


        DatabaseHelper.instance.insertProductInProductTable(productDataModel)
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

    //Get All Products of particular category  from the Product table
    fun getProductsThroughCategory(
        productCategory: String,
        productUUID: String
    ): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getProductsThroughCategory(productCategory, productUUID)
    }

    // Check if Category exist in the category table
    fun isCategoryExist(categoryName: String): Boolean? {
        return DatabaseHelper.instance.isCategoryExist(categoryName)
    }

    //Getting the Category UUId through Category Name
    fun getCategoryUUIDThroughCategoryName(categoryName: String): String {
        return DatabaseHelper.instance.getCategoryUUIDThroughCategoryName(categoryName)
    }

    //Get All Products from the Product table
    fun getAllProducts(): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getAllProducts()
    }

    //Get All Products from the Product table Accept Category one Category
    fun getAllProductsAcceptCategory(categoryName: String): ArrayList<ProductDataModel> {
        return DatabaseHelper.instance.getAllProductsAcceptCategory(categoryName)
    }

    //Getting the Category Name through Category UUID
    fun getCategoryNameThroughCategoryUUID(categoryUUID: String): String {
        return DatabaseHelper.instance.getCategoryNameThroughCategoryUUID(categoryUUID)
    }

    //Inserting Address in Address table
    fun insertAddressInAddressTable(addressDataModel: AddressDataModel) {
        return DatabaseHelper.instance.insertAddressInAddressTable(addressDataModel)
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

    //Deleting the cart from the cart table
    fun deleteProductFromCart(cardUUID: String) {
        DatabaseHelper.instance.deleteProductFromCart(cardUUID)
    }

    //Deleting the address from the address table
    fun deleteAddress(addressUUID: String) {
        DatabaseHelper.instance.deleteAddress(addressUUID)
    }

    //Get All Address of particular user from the Address table
    fun getAllAddressThroughUserUUID(userUUID: String): ArrayList<AddressDataModel> {
        return DatabaseHelper.instance.getAllAddressThroughUserUUID(userUUID)
    }

    //Getting the Product through Product UUID
    fun getProductThroughProductUUID(productUUID: String): ProductDataModel {
        return DatabaseHelper.instance.getProductThroughProductUUID(productUUID)
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


    fun textChanger(editText: EditText, errorTextView: TextView) {
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


    fun expandView(view: View) {
        view.visibility = View.VISIBLE
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = view.measuredHeight
        view.layoutParams.height = 1
        view.requestLayout()

        view.animate()
            .setDuration(100) // Adjust the duration as needed
            .setInterpolator(AccelerateDecelerateInterpolator())
            .translationY(0f)
            .alpha(1f)
            .setListener(null)
            .setUpdateListener {
                val params = view.layoutParams
                params.height = (targetHeight * it.animatedFraction).toInt()
                view.layoutParams = params
            }
    }

    fun collapseView(view: View) {
        val initialHeight = view.height
        view.animate()
            .setDuration(100) // Adjust the duration as needed
            .setInterpolator(AccelerateDecelerateInterpolator())
            .translationY(-initialHeight.toFloat())
            .alpha(0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    view.visibility = View.GONE
                }
            })
            .setUpdateListener {
                val params = view.layoutParams
                params.height = (initialHeight * (1 - it.animatedFraction)).toInt()
                view.layoutParams = params
            }
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

    val currentDate: String
        get() {
            val c = Calendar.getInstance().time
            E("Current time => $c")
            val df =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
        CustomeToast(context, "Saved Successfully")

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
        dialog.show()
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

    @SuppressLint("InflateParams")
    fun T(c: Context?, msg: String?) {
        val toast = Toast(c)
        val view = LayoutInflater.from(c).inflate(R.layout.item_custom_toast, null)
        val textView = view.findViewById<TextView>(R.id.tvMessage)
        textView.text = msg
        toast.view = view
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
        val itemCustomToastBinding = ItemCustomToastBinding.inflate(LayoutInflater.from(c))
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.view?.startAnimation(AnimationUtils.loadAnimation(c, R.anim.top_to_bottom))
        itemCustomToastBinding.tvMessage.text = msg
        itemCustomToastBinding.mcvToast.minimumWidth = getScreenWidth(c!!) - 12
        toast.view = itemCustomToastBinding.root //setting the view of custom toast layout
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
