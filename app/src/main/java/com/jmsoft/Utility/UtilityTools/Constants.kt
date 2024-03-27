package com.jmsoft.basic.UtilityTools

import com.google.android.datatransport.BuildConfig
import com.google.android.gms.maps.model.LatLng

interface Constants {
    companion object {
        const val cameraZoom: Float = 20f
        const val distanceInDegree = 111320.00
        val defaultCoordinates: LatLng = LatLng(9.0820, 8.6753)

        const val TEXT_PLAIN_TYPE = "text/plain"
        const val CONTENT_IMAGE = "image/jpeg"
        const val CONTENT_AUDIO = "audio/mp3"
        const val CONTENT_VIDEO = "video/mp4"
        const val CONTENT_ALL_IMAGE = "image/*"
        const val CONTENT_ALL_Video = "video/*"
        const val IMAGE_JPEG = ".jpeg"
        const val IMAGE_PNG = ".png"
        const val AUDIO_MP3 = ".mp3"
        const val VIDEO_MP4 = ".mp4"
        const val PDF = ".pdf"
        const val pdf = "pdf"
        const val dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        const val timeFormat = "HH:mm a"
        const val videoUrl = "videoUrl"
        const val urlType = "urlType"
        const val video = "Video"
        const val CONTENT_ALL_DOC = "application/pdf"
        const val Unauthorized = 401
        const val currency = "₦"
        const val doc = "doc"
        const val docx = "docx"
        const val jpg = "jpg"
        const val png = "png"
        const val PNG = ".png"
        const val jpeg = "jpeg"
        const val heic = "heic"
        const val JPG = "JPG"
        const val mp4 = "mp4"
        const val mov = "mov"
        const val avi = "avi"
        const val wav = "wav"
        const val studentLoginHistoryId = "studentLoginHistoryId"
        const val EXTRA_MEDIA_URL = "media_url"

        //Environment
        const val Debug = "Debug"
        const val Live = "Live"
        const val Test = "Test"

        //Bundle Constant and Keys
        const val From = "From"

        //Parameters
        const val Authorization = "Authorization"

        //Firebase
        const val Notification = "Notification"

        //Action Firebase
        const val action = "action"
        const val payload = "payload"
        const val data = "data"
        const val message = "message"
        const val title = "title"

        // Language
        const val blank = ""
        const val Default_Country_Code = "+91"
        const val Default_Country_Region = "IN"
        const val Nigeria_Country_Region = "NG"
        const val english = "en"
        const val arabic = "ar"
        const val forgotPassword = "forgot-password"
        const val Yes = "Yes"
        const val Android = "Android"
        const val mobile = "studentMobileNo"
        const val countryCode = "mobile"
        const val password = "password"
        const val otp = "otp"
        const val fcmId = "fcmId"
        const val uuid = "uuid"
        const val deviceType = "deviceType"
        const val loginDeviceType = "loginDeviceType"
        const val device_type = "device_type"
        const val user = "user"
        const val userId = "userId"
        const val admin = "admin"
        const val loginType = "loginType"
        const val studentName = "studentName"
        const val name = "name"
        const val email = "email"
        const val Email = "Email"
        const val Mobile = "Mobile"
        const val android = "android"
        const val userType = "userType"
        const val connected = "Connected"


        //String device_type = "device_type";
        const val Take_Photo = "Take Photo"
        const val Choose_From_Gallery = "Choose from Gallery"
        const val Exit = "Exit"
        const val Need_Permissions = "Need Permissions"
        const val GOTO_SETTINGS = "GOTO SETTINGS"
        const val Package = "package"
        const val Cancel = "Cancel"
        const val Data = "data"
        const val Google = "Google"
        const val Massage = "massage"
        const val Activity = "Activity"
        const val profilePic = "profilePic"
        const val DefaultCountryUuId = "d428a700-0bf7-11ed-9481-000c291151eb"
        const val type = "type"
        const val On = "On"
        const val all = "all"
        const val error = "error"
        const val token = "token"
        const val login = "login"
        const val recoverPassword = "recoverPassword"
        const val recoverMpin = "recoverMpin"
        const val UpdatePhoto = "UpdatePhoto"
        const val Pending = "Pending"
        const val Verified = "Verified"
        const val Rejected = "Rejected"
        const val ConfirmChangeMobileNumber = "ConfirmChangeMobileNumber"
        const val ChangeMobileNumber = "ChangeMobileNumber"
        const val LoginWithMobile = "LoginWithMobile"
        const val camera = "camera"
        const val gallery = "gallery"
        const val Avatar = "Avatar"
        const val All = "All"
        const val Uri = "Uri"
        const val voice = "voice"
        const val successActivityVoice = "successActivityVoice"
        const val successActivityVideo = "successActivityVideo"
        const val Url = "Url"
        const val phoneNumber = "phoneNumber"
        const val ViewAddressDetailActivity = "ViewAddressDetailActivity"
        const val CodeGenerationSuccessActivity = "CodeGenerationSuccessActivity"
        const val claimAddress = "claimAddress"
        const val firstName = "firstName"
        const val mpin = "mpin"
        const val currentMPIN = "currentMPIN"
        const val newMPIN = "newMPIN"
        const val profilePicture = "profilePicture"
        const val zoneCode = "zoneCode"
        const val lastName = "lastName"
        const val userToken = "userToken"
        const val notifications = "notifications"
        const val termsConditions = "termsConditions"
        const val privacyPolicy = "privacyPolicy"
        const val sms = "sms"
        const val clientId = "clientId"
        const val ChangeMobile = "ChangeMobile"
        const val VerifyPhone = "VerifyPhone"
        const val query = "query"
        const val aigCode = "aigCode"
        const val zipcode = "zipcode"
        const val latitude = "latitude"
        const val latlng = "latlng"
        const val longitude = "longitude"
        const val limit = "limit"
        const val city = "city"
        const val state = "state"
        const val addressTypeImage = "addressTypeImage"
        const val propertyPhoto = "propertyPhoto"
        const val country = "country"
        const val address = "address"
        const val blockCode = "blockCode"
        const val street = "street"
        const val LGA = "LGA"
        const val VerifyEmail = "VerifyEmail"
        const val addressType = "addressType"
        const val addressName = "addressName"
        const val buildingNo = "buildingNo"
        const val locality = "locality"
        const val landmark = "landmark"
        const val countryId = "countryId"
        const val stateId = "stateId"
        const val heading = "heading"
        const val countryName = "countryName"
        const val lgaName = "lgaName"
        const val stateName = "stateName"
        const val cityName = "cityName"
        const val id = "id"
        const val imageFile = "imageFile"
        const val description = "description"
        const val startPoint = "startPoint"
        const val duration = "duration"
        const val audioFile = "audioFile"
        const val EditAddressDetailActivity = "EditAddressDetailActivity"
        const val AddressDetailActivity = "AddressDetailActivity"
        const val info = "info"
        const val addressId = "addressId"
        const val requestorImage = "requestorImage"
        const val propertyImage = "propertyImage"
        const val ninNumber = "ninNumber"
        const val videoFile = "videoFile"
        const val addressVerificationPrice = "addressVerificationPrice"
        const val photoUri = "photoUri"
        const val houseName = "houseName"
        const val buildingNumber = "buildingNumber"
        const val landMark = "landMark"
        const val webUrl = "webUrl"

        // State

        const val bluetoothUuid = "00001101-0000-1000-8000-00805F9B34FB"
        const val rotation = "rotation"
        const val updateInSession = "updateInSession"


         // values have to be globally unique
        const val INTENT_ACTION_DISCONNECT: String = BuildConfig.APPLICATION_ID + ".Disconnect"
        const val NOTIFICATION_CHANNEL: String = BuildConfig.APPLICATION_ID + ".Channel"
        const val INTENT_CLASS_MAIN_ACTIVITY: String = BuildConfig.APPLICATION_ID + ".MainActivity"

        // values have to be unique within each app
        const val NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001

        private fun Constants() {}

    }
}
