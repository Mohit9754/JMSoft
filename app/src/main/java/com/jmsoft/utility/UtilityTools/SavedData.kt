package com.jmsoft.basic.UtilityTools

import android.content.SharedPreferences
import android.location.Location
import android.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng
import com.jmsoft.utility.UtilityTools.AppController

object SavedData {
    private const val FirebaseToken = "FirebaseToken"
    private const val Language = "Language"
    private const val ReferralCode = "ReferralCode"
    private const val popUp = "popUp"
    private const val latitude = "latitude"
    private const val CouponCode = "CouponCode"
    private const val longitude = "longitude"
    private const val CountryKey = "CountryKey"
    private const val CountryId = "CountryId"
    private const val CountryUuId = "CountryUuId"
    private const val WelcomeCheck = "WelcomeCheck"
    private const val Shared = "Shared"
    private const val AccessToken = "AccessToken"
    private const val GetStartClick = "GetStartClick"
    private const val isNotification = "isNotification"
    private const val isBiometricsEnabled = "isBiometricsEnabled"
    private const val mockTestList = "mockTestList"
    private var prefs: SharedPreferences? = null

    @JvmStatic
    val instance: SharedPreferences?
        get() {
            if (prefs == null) {
                prefs = PreferenceManager.getDefaultSharedPreferences(AppController.instance)
            }
            return prefs
        }

    fun getLanguage(): String? {
        return instance!!.getString(Language, Constants.english)
    }

    fun saveLanguage(role: String?) {
        val editor = instance!!.edit()
        editor.putString(Language, role)
        editor.apply()
    }

    fun getNotification(): Boolean {
        return instance!!.getBoolean(isNotification, true)
    }

    fun saveNotification(isEnabled: Boolean) {
        val editor = instance!!.edit()
        editor.putBoolean(isNotification, isEnabled)
        editor.apply()
    }

    fun getBioMetricsStatus(): Boolean {
        return instance!!.getBoolean(isBiometricsEnabled, true)
    }

    fun saveBiometricsStatus(isEnabled: Boolean) {
        val editor = instance!!.edit()
        editor.putBoolean(isBiometricsEnabled, isEnabled)
        editor.apply()
    }
  fun getWelcomeStatus(): Boolean {
        return instance!!.getBoolean(WelcomeCheck, false)
    }

    fun saveWelcomeStatus(isWelcome: Boolean) {
        val editor = instance!!.edit()
        editor.putBoolean(WelcomeCheck, isWelcome)
        editor.apply()
    }

    val mockTestHistory: String?
        get() = instance!!.getString(mockTestList, Constants.On)

    fun saveMockTestHistory(mockTestList: String?) {
        val editor = instance!!.edit()
        editor.putString(mockTestList, mockTestList)
        editor.apply()
    }

    fun getPopUp(): Boolean {
        return instance!!.getBoolean(popUp, false)
    }

    fun savePopUp(role: Boolean) {
        val editor = instance!!.edit()
        editor.putBoolean(popUp, role)
        editor.apply()
    }

    val startClick: Boolean
        get() = instance!!.getBoolean(GetStartClick, false)

    fun saveGetStartClick(role: Boolean) {
        val editor = instance!!.edit()
        editor.putBoolean(GetStartClick, role)
        editor.apply()
    }

    fun getFirebaseToken(): String? {
        return instance!!.getString(FirebaseToken, Constants.blank)
    }

    @JvmStatic
    fun saveFirebaseToken(token: String?) {
        val editor = instance!!.edit()
        editor.putString(FirebaseToken, token)
        editor.apply()
    }

    fun getAccessToken(): String? {
        return instance!!.getString(AccessToken, Constants.blank)
    }

    @JvmStatic
    fun saveAccessToken(token: String?) {
        val editor = instance!!.edit()
        editor.putString(AccessToken, token)
        editor.apply()
    }

    fun getCountryRegion(): String? {
        return instance!!.getString(CountryKey, Constants.Default_Country_Region)
    }

    fun saveCountryRegion(key: String?) {
        val editor = instance!!.edit()
        editor.putString(CountryKey, key)
        editor.apply()
    }

    fun getReferralCode(): String? {
        return instance!!.getString(ReferralCode, Constants.blank)
    }

    fun saveReferralCode(token: String?) {
        val editor = instance!!.edit()
        editor.putString(ReferralCode, token)
        editor.apply()
    }

    fun getCountryUuId(): String? {
        return instance!!.getString(CountryUuId, Constants.DefaultCountryUuId)
    }

    fun saveCountryUuId(Id: String?) {
        val editor = instance!!.edit()
        editor.putString(CountryUuId, Id)
        editor.apply()
    }

    /* public static String getBankStatus() {
        return getInstance().getString(Shared, Constants.blank);
    }

    public static void SaveBankStatus(String Id) {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putString(Shared, Id);
        editor.apply();

    }*/
    val currentLocation: LatLng
        get() {
            val lat = instance!!.getString(latitude, "0.0")!!.toDouble()
            val longi = instance!!.getString(longitude, "0.0")!!.toDouble()
            return LatLng(lat, longi)
        }

    fun saveCurrentLocation(location: Location) {
        val editor = instance!!.edit()
        editor.putString(latitude, location.latitude.toString())
        editor.putString(longitude, location.longitude.toString())
        editor.apply()
    }

    val couponcode: String?
        get() = instance!!.getString(CouponCode, Constants.blank)

    fun saveCouponCode(couponCode: String?) {
        val editor = instance!!.edit()
        editor.putString(CouponCode, couponCode)
        editor.apply()
    }

    val refferalCode: String?
        get() = instance!!.getString(ReferralCode, Constants.blank)

    fun saveRefferalCode(couponCode: String?) {
        val editor = instance!!.edit()
        editor.putString(ReferralCode, couponCode)
        editor.apply()
    }
}