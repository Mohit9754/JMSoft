package com.jmsoft.basic.Database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
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
            UserDataModel.TABLE_NAME, UserDataModel.Key_userId + " = '"
                    + userData.userId + "'", null
        )
        close()
    }

    /**
     * delete All Data From the Table
     */
    fun deleteAll() {
        open()
        db!!.delete(UserDataModel.TABLE_NAME, null, null)
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
            "select * from " + UserDataModel.TABLE_NAME + " where " + UserDataModel.Key_userId + "='"
                    + userData.userId + "'", null
        )
        return cur.moveToFirst()
    }

    /**
     * insert Data in table
     *
     * @param userData //
     */
    fun insertData(userData: UserDataModel) {
        open()
        val values = ContentValues()

        // values.put(UserData.KEY_ID, userData.userId);
        values.put(UserDataModel.Key_userId, userData.userId)
        values.put(UserDataModel.Key_accessToken, userData.accessToken)
        values.put(UserDataModel.Key_firstName, userData.firstName)
        values.put(UserDataModel.Key_lastName, userData.lastName)
        values.put(UserDataModel.Key_email, userData.email)
        values.put(UserDataModel.Key_phoneNumber, userData.phoneNumber)
        values.put(UserDataModel.Key_gender, userData.gender)
        values.put(UserDataModel.Key_dob, userData.dob)
        values.put(UserDataModel.Key_profilePicture, userData.profilePicture)
        values.put(UserDataModel.Key_isEmailVerified, userData.isEmailVerified)
        values.put(UserDataModel.Key_isPhoneVerified, userData.isPhoneVerified)
        values.put(UserDataModel.Key_userBio, userData.userBio)
        if (!isExist(userData)) {
            E("insert successfully")
            E("Values::$values")
            db!!.insert(UserDataModel.TABLE_NAME, null, values)
        } else {
            E("Values::$values")
            E("update successfully")
            db!!.update(
                UserDataModel.TABLE_NAME,
                values,
                UserDataModel.Key_userId + "=" + userData.userId,
                null
            )
        }
        close()
    }// userData.userId = cursor.getString(cursor.getColumnIndex(UserData.KEY_ID));

    /**
     * Return User Array List
     *
     * @return //
     */
    @get:SuppressLint("Range")
    val list: ArrayList<UserDataModel>
        get() {
            val userItem = ArrayList<UserDataModel>()
            read()
            val cursor = db!!.rawQuery("select * from " + UserDataModel.TABLE_NAME, null)
            if (cursor != null && cursor.count > 0) {
                cursor.moveToLast()
                do {

                    val userData = UserDataModel()
                    // userData.userId = cursor.getString(cursor.getColumnIndex(UserData.KEY_ID));
                    userData.userId =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userId))
                    userData.accessToken =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_accessToken))
                    userData.firstName =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
                    userData.lastName =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
                    userData.email =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
                    userData.phoneNumber =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
                    userData.gender =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_gender))
                    userData.dob = cursor.getString(cursor.getColumnIndex(UserDataModel.Key_dob))
                    userData.profilePicture =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profilePicture))
                    userData.isEmailVerified =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_isEmailVerified))
                    userData.isPhoneVerified =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_isPhoneVerified))
                    userData.userBio =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userBio))
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