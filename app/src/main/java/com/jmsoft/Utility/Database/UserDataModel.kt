package com.jmsoft.basic.Database

import android.database.sqlite.SQLiteDatabase
import com.jmsoft.basic.UtilityTools.Utils.E
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.sql.Blob

class UserDataModel {

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

    @SerializedName("token")
    @Expose
    var token: String? = null

    @SerializedName("password")
    @Expose
    var password: String? = null

    companion object {

        const val TABLE_NAME_USER_SESSION = "User_session"
        const val TABLE_NAME_USER = "User"

        //    All Key
        const val KEY_ID = "_id"
        const val Key_userType = "userType"
        const val Key_firstName = "firstName"
        const val Key_lastName = "lastName"
        const val Key_email = "email"
        const val Key_phoneNumber = "phoneNumber"
        const val Key_profileName = "profileName"
        const val Key_token = "token"
        const val Key_password = "password"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableUserSessionQuery = ("create table " + TABLE_NAME_USER_SESSION + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_userType + " text," +
                    Key_firstName + " text," +
                    Key_lastName + " text," +
                    Key_email + " text," +
                    Key_phoneNumber + " text," +
                    Key_profileName + " text," +
                    Key_token + " text"  +
                    " )")

            val CreateTableUserQuery = ("create table " + TABLE_NAME_USER + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_userType + " text," +
                    Key_firstName + " text," +
                    Key_lastName + " text," +
                    Key_email + " text," +
                    Key_phoneNumber + " text," +
                    Key_profileName + " text," +
                    Key_token + " text,"  +
                    Key_password + " text"  +
                    " )")

            E("CreateTableQuery::$CreateTableUserSessionQuery")
            db.execSQL(CreateTableUserSessionQuery)
            db.execSQL(CreateTableUserQuery)
        }

        /**
         * @param db SQLiteDatabase
         */
        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER_SESSION)
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER)
        }
    }

}