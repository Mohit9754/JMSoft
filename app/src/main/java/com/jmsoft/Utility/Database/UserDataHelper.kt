package com.jmsoft.basic.Database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.UtilityTools.Utils.E

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
            UserDataModel.TABLE_NAME_USER_SESSION, UserDataModel.Key_userType + " = '"
                    + userData.userType + "'", null
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

    /**
     * is Exist in table
     *
     * @param userData //
     * @return //
     */
    private fun isExist(userData: UserDataModel): Boolean {
        read()
        @SuppressLint("Recycle") val cur = db!!.rawQuery(
            "select * from " + UserDataModel.TABLE_NAME_USER_SESSION + " where " + UserDataModel.Key_email + "='"
                    + userData.email + "'", null
        )
        return cur.moveToFirst()
    }

    // Check if User is valid or not
    fun isValidUser(email:String,password:String): Boolean {
        read()
        val selection = "${UserDataModel.Key_email} = ? AND ${UserDataModel.Key_password} = ?"
        val selectionArgs = arrayOf(email, password)

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

    // Checks is Phone Number Already Exist
    @SuppressLint("Recycle")
    fun isPhoneNumberExist(phoneNumber:String):Boolean{

        read()
        val cursor = db!!.rawQuery("SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_phoneNumber} = '$phoneNumber' ",null)

        return cursor.moveToFirst()
    }

    // Checks is Email Already Exist
    @SuppressLint("Recycle")
    fun isEmailExist(email: String):Boolean{

        read()
        val cursor = db!!.rawQuery("SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_email} = '$email' ",null)

        return cursor.moveToFirst()
    }


    // get User Details through email from the User table
    @SuppressLint("Range")
    fun getUserDetailThroughEmail(email:String): UserDataModel {
        read()
        @SuppressLint("Recycle") val cursor = db!!.rawQuery(
            "select * from " + UserDataModel.TABLE_NAME_USER + " where " + UserDataModel.Key_email + "='"
                    + email + "'" , null
        )

        cursor.moveToFirst()
        val userData = UserDataModel()

        if (cursor != null && cursor.count > 0) {

                userData.userType =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userType))
                userData.token =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_token))
                userData.firstName =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
                userData.lastName =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
                userData.email =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
                userData.phoneNumber =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
                userData.profileName =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profileName))

            cursor.close()
        }
        close()

        return userData
    }

    // Check is User Table Empty
    fun isUserTableEmpty():Boolean{

        read()
        @SuppressLint("Recycle") val cur = db!!.rawQuery(
            "select * from " + UserDataModel.TABLE_NAME_USER, null)
        return !cur.moveToFirst()
    }

    //Updating the User Profile
    fun updateProfile(profileName: String,email:String):Boolean{

        open()

        val contentValues = ContentValues().apply {
            put(UserDataModel.Key_profileName, profileName)
        }

        val whereClause = "${UserDataModel.Key_email} = ?"
        val whereArgs = arrayOf(email)

        val rowsAffected =
            db?.update(UserDataModel.TABLE_NAME_USER, contentValues, whereClause, whereArgs)

        if (rowsAffected!! > 0) {
            return true
        } else {
            return  false
        }
    }

    //Insert and Update  data of User in the User table
    fun insetDataInUserTable(userData: UserDataModel){

        open()
        val values = ContentValues()

        // values.put(UserData.KEY_ID, userData.userId);
        values.put(UserDataModel.Key_userType, userData.userType)
        values.put(UserDataModel.Key_firstName, userData.firstName)
        values.put(UserDataModel.Key_lastName, userData.lastName)
        values.put(UserDataModel.Key_email, userData.email)
        values.put(UserDataModel.Key_phoneNumber, userData.phoneNumber)
        values.put(UserDataModel.Key_profileName, userData.profileName)
        values.put(UserDataModel.Key_token, userData.token)
        values.put(UserDataModel.Key_password, userData.password)

        db!!.insert(UserDataModel.TABLE_NAME_USER, null, values)

        close()

    }
    /**
     * insert Data in Session table
     *
     * @param userData //
     */
    fun insertDataInSessionTable(userData: UserDataModel) {
        open()
        val values = ContentValues()

        // values.put(UserData.KEY_ID, userData.userId);
        values.put(UserDataModel.Key_userType, userData.userType)
        values.put(UserDataModel.Key_firstName, userData.firstName)
        values.put(UserDataModel.Key_lastName, userData.lastName)
        values.put(UserDataModel.Key_email, userData.email)
        values.put(UserDataModel.Key_phoneNumber, userData.phoneNumber)
        values.put(UserDataModel.Key_profileName, userData.profileName)
        values.put(UserDataModel.Key_token, userData.token)


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
                "${UserDataModel.Key_email} = '${userData.email}'",
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

                    userData.userType =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userType))
                    userData.token =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_token))
                    userData.firstName =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
                    userData.lastName =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
                    userData.email =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
                    userData.phoneNumber =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
                    userData.profileName =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profileName))

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