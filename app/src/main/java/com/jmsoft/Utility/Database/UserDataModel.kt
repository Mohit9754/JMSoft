package com.jmsoft.basic.Database

import android.database.sqlite.SQLiteDatabase
import com.jmsoft.basic.UtilityTools.Utils.E
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.jmsoft.basic.UtilityTools.Utils
import java.sql.Blob

class UserDataModel {

    @SerializedName("userId")
    @Expose
    var userId: Int? = null

    @SerializedName("userType")
    @Expose
    var userType: String? = null

    @SerializedName("firstName")
    @Expose
    var firstName: String? = null

    @SerializedName("lastName")
    @Expose
    var lastName: String? = null

    @SerializedName("email")
    @Expose
    var email: String? = null

    @SerializedName("phoneNumber")
    @Expose
    var phoneNumber: String? = null

    @SerializedName("profilePicture")
    @Expose
    var profileName: String? = null

    @SerializedName("password")
    @Expose
    var password: String? = null

    companion object {

        const val TABLE_NAME_USER_SESSION = "User_session"
        const val TABLE_NAME_USER = "User"
        const val TABLE_NAME_DEVICE = "Device"

        //All Key of User Table
        const val KEY_Id = "Id"
        const val KEY_userId = "userId"
        const val Key_userType = "userType"
        const val Key_firstName = "firstName"
        const val Key_lastName = "lastName"
        const val Key_email = "email"
        const val Key_phoneNumber = "phoneNumber"
        const val Key_profileName = "profileName"
        const val Key_password = "password"

        //All keys of Device table
        const val Key_deviceName = "deviceName"
        const val Key_deviceType = "deviceType"
        const val Key_deviceAddress = "deviceAddress"
        const val KEY_deviceId = "deviceId"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableUserSessionQuery = ("create table " + TABLE_NAME_USER_SESSION + " ("
                    + KEY_Id + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_userId + " INTEGER," +
                    Key_userType + " text," +
                    Key_firstName + " text," +
                    Key_lastName + " text," +
                    Key_email + " text," +
                    Key_phoneNumber + " text," +
                    Key_profileName + " text" +
                    " )")

            val CreateTableUserQuery = ("create table " + TABLE_NAME_USER + " ("
                    + KEY_userId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_userType + " text," +
                    Key_firstName + " text," +
                    Key_lastName + " text," +
                    Key_email + " text," +
                    Key_phoneNumber + " text," +
                    Key_profileName + " text," +
                    Key_password + " text"  +
                    " )")

            val CreateTableDeviceQuery = ("create table " + TABLE_NAME_DEVICE + " ("
                    + KEY_deviceId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_deviceName + " text," +
                    Key_deviceType + " text," +
                    Key_deviceAddress + " text," +
                    KEY_userId + " text," +
                    " FOREIGN KEY ("+KEY_userId+") REFERENCES "+ TABLE_NAME_USER+"("+ KEY_userId+")"+
                    " )")

            E("CreateTableQuery::$CreateTableUserSessionQuery")

            db.execSQL(CreateTableUserSessionQuery)
            db.execSQL(CreateTableUserQuery)
            db.execSQL(CreateTableDeviceQuery)

            Utils.E("#####################Table is created")
        }

        /**
         * @param db SQLiteDatabase
         */
        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER_SESSION)
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER)
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DEVICE)
        }
    }

}