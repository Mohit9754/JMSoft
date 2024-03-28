package com.jmsoft.basic.Database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.jmsoft.basic.UtilityTools.Constants.Companion.user
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.UtilityTools.Utils.E
import com.jmsoft.main.model.DeviceModel
import java.util.UUID

class UserDataHelper(cx: Context) {

    private val dm: DataManager
    var cx: Context
    private var db: SQLiteDatabase? = null

    /**
     * UserDataHelper constructor
     *
     * @param cx //
     */
    init {
        instance = this
        this.cx = cx
        dm = DataManager(cx, DataManager.DATABASE_NAME, null, DataManager.DATABASE_VERSION)
    }

    /**
     * open db
     */
    fun open() {
        db = dm.writableDatabase
    }

    /**
     * close db
     */
    fun close() {
        //  db.close();
    }

    /**
     * read db
     */
    fun read() {
        db = dm.readableDatabase
    }

    /**
     * delete by user id from the table
     *
     * @param userData //
     */
    fun delete(userData: UserDataModel) {
        open()
        db!!.delete(
            UserDataModel.TABLE_NAME_USER_SESSION, UserDataModel.Key_userUUID + " = '"
                    + userData.userUUID + "'", null
        )
        close()
    }

    /**
     * delete All Data From the Session Table
     */
    fun deleteSession() {
        open()
        db!!.delete(UserDataModel.TABLE_NAME_USER_SESSION, null, null)
        close()
    }

    //Deleting the User through the UserUUId from the user table
    fun deleteUserThroughUserUUID(userUUID: String) {
        open()
        val selection = "${UserDataModel.Key_userUUID} = ?"
        val selectionArgs = arrayOf(userUUID)
        db!!.delete(UserDataModel.TABLE_NAME_USER, selection, selectionArgs)
        close()
    }

    /**
     * is Exist in table
     *
     * Checks if userUUID is same or not in the Session table
     * @param userData //
     * @return //
     */
    private fun isExist(userData: UserDataModel): Boolean {
        read()
        @SuppressLint("Recycle")val cur = db!!.rawQuery(
            "SELECT * FROM ${UserDataModel.TABLE_NAME_USER_SESSION} WHERE ${UserDataModel.Key_userUUID} = ?",
            arrayOf(userData.userUUID)
        )
        return cur.moveToFirst()
    }

    // Get User through Email And Password from the user table
    @SuppressLint("Range")
    fun getUserThroughEmailAndPassword(email:String, password:String): UserDataModel {

        read()
        val selection = "${UserDataModel.Key_email} = ? AND ${UserDataModel.Key_password} = ?"
        val selectionArgs = arrayOf(email, Utils.encodeText(password)) // match the password after encode it

        val cursor: Cursor? = db?.query(
            UserDataModel.TABLE_NAME_USER,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor?.moveToFirst()

        val userData = UserDataModel()

        if (cursor != null) {

            userData.userUUID = cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userUUID))

            userData.userType =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userType))

            userData.firstName =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
            userData.lastName =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
            userData.email =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
            userData.phoneNumber =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
            userData.profileUri =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profileUri))
            userData.password =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_password))

            cursor.close()
        }
        close()

        return userData
    }

    // Check if User is valid or not through user table
    fun isValidUser(email:String,password:String): Boolean {
        read()
        val selection = "${UserDataModel.Key_email} = ? AND ${UserDataModel.Key_password} = ?"

        val selectionArgs = arrayOf(email, Utils.encodeText(password)) // match the password after encode it

        val cursor: Cursor = db?.query(
            UserDataModel.TABLE_NAME_USER,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        ) ?: return false

        val isValid = cursor.moveToFirst()
        cursor.close() // Close the cursor to release resources
        return isValid
    }

    //this method checks if any user has this phone number
    @SuppressLint("Recycle")
    fun isAnyUserHasThisPhoneNumber(phoneNumber: String, userUUID: String): Boolean {

        read()
        val cursor = db!!.rawQuery("SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_phoneNumber} = '$phoneNumber' And ${UserDataModel.Key_userUUID} != '$userUUID' ",null)

        return cursor.moveToFirst()

    }

    //this method checks if any user has this email
    @SuppressLint("Recycle")
    fun isAnyUserHasThisEmail(email: String, userUUID: String): Boolean {

        read()
        val cursor = db!!.rawQuery("SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_email} = '$email' And ${UserDataModel.Key_userUUID} != '$userUUID'",null)

        return cursor.moveToFirst()
    }


    // Checks is Phone Number Already Exist in the user table
    @SuppressLint("Recycle")
    fun isPhoneNumberExist(phoneNumber:String):Boolean{

        read()
        val cursor = db!!.rawQuery("SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_phoneNumber} = '$phoneNumber' ",null)

        return cursor.moveToFirst()
    }

    // Checks is Email Already Exist in the user table
    @SuppressLint("Recycle")
    fun isEmailExist(email: String):Boolean{

        read()
        val cursor = db!!.rawQuery("SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_email} = '$email' ",null)

        return cursor.moveToFirst()
    }


    // getting All User Details Accept Admin form the user table
    @SuppressLint("Range")
    fun getAllUserDetails(): ArrayList<UserDataModel> {

        read()
        @SuppressLint("Recycle") val cursor = db!!.rawQuery(
            "select * from " + UserDataModel.TABLE_NAME_USER + " where " + UserDataModel.Key_userType + "='"
                    + user + "'" , null
        )

        val userList = ArrayList<UserDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val userData = UserDataModel()

                userData.userUUID = cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userUUID))

                userData.userType =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userType))

                userData.firstName =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
                userData.lastName =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
                userData.email =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
                userData.phoneNumber =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
                userData.profileUri =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profileUri))

                userList.add(userData)

            } while (cursor.moveToPrevious())
            cursor.close()
        }
        close()

        return userList
    }

    // get User Details through userUUID from the User table
    @SuppressLint("Range")
    fun getUserDetailsThroughUserUUID(userUUID: String): UserDataModel {
        read()
        @SuppressLint("Recycle") val cursor = db?.rawQuery(
            "SELECT * FROM ${UserDataModel.TABLE_NAME_USER} WHERE ${UserDataModel.Key_userUUID} = ?",
            arrayOf(userUUID)
        )
        cursor?.moveToFirst()
        val userData = UserDataModel()

        if (cursor != null && cursor.count > 0) {

                userData.userUUID = cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userUUID))

                userData.userType =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userType))

                userData.firstName =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
                userData.lastName =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
                userData.email =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
                userData.phoneNumber =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
                userData.profileUri =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profileUri))

                userData.password =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_password))

            cursor.close()
        }
        close()

        return userData
    }

    // Checks is User Table Empty
    fun isUserTableEmpty():Boolean{

        read()
        @SuppressLint("Recycle") val cur = db!!.rawQuery(
            "select * from " + UserDataModel.TABLE_NAME_USER, null)
        return !cur.moveToFirst()
    }

    //Updating the User Profile in the User Table
    fun updateProfileInUserTable(profileName: String,userUUID: String):Boolean{

        open()

        val contentValues = ContentValues().apply {
            put(UserDataModel.Key_profileUri, profileName)
        }

        val whereClause = "${UserDataModel.Key_userUUID} = ?"
        val whereArgs = arrayOf(userUUID)

        val rowsAffected =
            db?.update(UserDataModel.TABLE_NAME_USER, contentValues, whereClause, whereArgs)

        if (rowsAffected!! > 0) {
            return true
        } else {
            return  false
        }
    }

    //Insert new user in the User table
    fun insetDataInUserTable(userData: UserDataModel){

        open()
        val values = ContentValues()

        // values.put(UserData.KEY_ID, userData.userId);
        values.put(UserDataModel.Key_userType, userData.userType)
        values.put(UserDataModel.Key_userUUID, userData.userUUID)
        values.put(UserDataModel.Key_firstName, userData.firstName)
        values.put(UserDataModel.Key_lastName, userData.lastName)
        values.put(UserDataModel.Key_email, userData.email)
        values.put(UserDataModel.Key_phoneNumber, userData.phoneNumber)
        values.put(UserDataModel.Key_profileUri, userData.profileUri)
        values.put(UserDataModel.Key_password, userData.password)

        db!!.insert(UserDataModel.TABLE_NAME_USER, null, values)

        close()

    }

    //Insert Device Data in the Device Table
    fun insertNewDeviceData(deviceModel: DeviceModel){

        open()
        val values = ContentValues()

        values.put(UserDataModel.Key_deviceUUID, deviceModel.deviceUUID)
        values.put(UserDataModel.Key_deviceName, deviceModel.deviceName)
        values.put(UserDataModel.Key_deviceType, deviceModel.deviceType)
        values.put(UserDataModel.Key_deviceAddress, deviceModel.deviceAddress)
        values.put(UserDataModel.Key_userUUID, deviceModel.userUUID)

        db!!.insert(UserDataModel.TABLE_NAME_DEVICE, null, values)

        close()
    }

    //Delete Device from Device table through DeviceUUID
    fun deleteDeviceThoughDeviceUUID(deviceUUID: String){
        open()
        db?.delete(
            UserDataModel.TABLE_NAME_DEVICE,
            "${UserDataModel.Key_deviceUUID} = ?",
            arrayOf(deviceUUID)
        )
        close()
    }

    //Update User Details in the User Table
    fun updateUserDetails(userDataModel: UserDataModel){
        open()

        val values = ContentValues()

        values.put(UserDataModel.Key_firstName, userDataModel.firstName)
        values.put(UserDataModel.Key_lastName, userDataModel.lastName)
        values.put(UserDataModel.Key_email, userDataModel.email)
        values.put(UserDataModel.Key_phoneNumber, userDataModel.phoneNumber)
        values.put(UserDataModel.Key_password, userDataModel.password)

        db!!.update(
            UserDataModel.TABLE_NAME_USER,
            values,
            "${UserDataModel.Key_userUUID} = '${userDataModel.userUUID}'",
            null
        )

        close()
    }

    //getting All the Devices of particular from the device table through userUUID
    @SuppressLint("Recycle", "Range")
    fun getDevicesThroughUserUUID(userUUID: String): ArrayList<DeviceModel> {

        read()

        val cursor = db?.rawQuery("SELECT * FROM ${UserDataModel.TABLE_NAME_DEVICE} WHERE ${UserDataModel.Key_userUUID} = ?", arrayOf(userUUID))

        val deviceItem = ArrayList<DeviceModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val deviceData = DeviceModel()

                deviceData.deviceUUID = cursor.getString(cursor.getColumnIndex(UserDataModel.Key_deviceUUID))
                deviceData.deviceName = cursor.getString(cursor.getColumnIndex(UserDataModel.Key_deviceName))
                deviceData.deviceType = cursor.getString(cursor.getColumnIndex(UserDataModel.Key_deviceType))
                deviceData.deviceAddress = cursor.getString(cursor.getColumnIndex(UserDataModel.Key_deviceAddress))

                deviceItem.add(deviceData)

            } while (cursor.moveToPrevious())
            cursor.close()
        }
        close()
        return deviceItem
    }

    // Check if no device for particular user in the Device Table
    @SuppressLint("Recycle")
    fun isNoDeviceForUser(userUUID: String):Boolean {
        read()

        val cursor = db?.rawQuery("SELECT * FROM ${UserDataModel.TABLE_NAME_DEVICE} WHERE ${UserDataModel.Key_userUUID} = ?", arrayOf(userUUID))
        val result = cursor!!.moveToFirst()
        close()
        return  !result
    }

     /**
     * insert Data in Session table
      * if user id is same it will update the user details
     *
     * @param userData //
     */
    fun insertDataInSessionTable(userData: UserDataModel) {
        open()
        val values = ContentValues()

        values.put(UserDataModel.Key_userUUID, userData.userUUID)
        values.put(UserDataModel.Key_userType, userData.userType)
        values.put(UserDataModel.Key_firstName, userData.firstName)
        values.put(UserDataModel.Key_lastName, userData.lastName)
        values.put(UserDataModel.Key_email, userData.email)
        values.put(UserDataModel.Key_phoneNumber, userData.phoneNumber)
        values.put(UserDataModel.Key_profileUri, userData.profileUri)

        if (!isExist(userData)) {
            E("insert successfully")
            E("Values::$values")
            db!!.insert(UserDataModel.TABLE_NAME_USER_SESSION, null, values)
        } else {
            E("Values::$values")
            E("update successfully")
            db!!.update(
                UserDataModel.TABLE_NAME_USER_SESSION,
                values,
                "${UserDataModel.Key_userUUID} = '${userData.userUUID}'",
                null
            )
        }
        close()
    }// userData.userId = cursor.getString(cursor.getColumnIndex(UserData.KEY_ID));

    /**
     * Return User Array List From Session Table
     *
     * @return //
     */
    @get:SuppressLint("Range")
    val list: ArrayList<UserDataModel>
        get() {
            val userItem = ArrayList<UserDataModel>()
            read()
            val cursor = db!!.rawQuery("select * from " + UserDataModel.TABLE_NAME_USER_SESSION, null)
            if (cursor != null && cursor.count > 0) {
                cursor.moveToLast()
                do {

                    val userData = UserDataModel()

                    userData.userUUID =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userUUID))

                    userData.userType =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userType))

                    userData.firstName =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
                    userData.lastName =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
                    userData.email =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
                    userData.phoneNumber =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
                    userData.profileUri =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profileUri))

                    userItem.add(userData)

                } while (cursor.moveToPrevious())
                cursor.close()
            }
            close()
            return userItem
        }

    companion object {
        /**
         * UserDataHelper instance
         *
         * @return //
         */
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: UserDataHelper
            private set
    }
}