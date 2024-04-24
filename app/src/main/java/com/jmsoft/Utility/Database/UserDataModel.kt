package com.jmsoft.basic.Database

import android.database.sqlite.SQLiteDatabase
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserDataModel {

    @SerializedName("userUUID")
    @Expose
    var userUUID: String? = null

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

    @SerializedName("profileUri")
    @Expose
    var profileUri: String? = null

    @SerializedName("password")
    @Expose
    var password: String? = null

    companion object {

        const val TABLE_NAME_USER_SESSION = "User_session"
        const val TABLE_NAME_USER = "User"

        //All Key of User Table
        const val Key_Id = "Id"
        const val Key_userId = "userId"
        const val Key_userUUID = "userUUID"
        const val Key_userType = "userType"
        const val Key_firstName = "firstName"
        const val Key_lastName = "lastName"
        const val Key_email = "email"
        const val Key_phoneNumber = "phoneNumber"
        const val Key_profileUri = "profileUri"
        const val Key_password = "password"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableUserSessionQuery = ("create table " + TABLE_NAME_USER_SESSION + " ("
                    + Key_Id + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_userUUID + " text UNIQUE," +
                    Key_userType + " text," +
                    Key_firstName + " text," +
                    Key_lastName + " text," +
                    Key_email + " text," +
                    Key_phoneNumber + " text," +
                    Key_profileUri + " text" +
                    " )")

            val CreateTableUserQuery = ("create table " + TABLE_NAME_USER + " ("
                    + Key_userId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_userUUID + " text UNIQUE," +
                    Key_userType + " text," +
                    Key_firstName + " text," +
                    Key_lastName + " text," +
                    Key_email + " text," +
                    Key_phoneNumber + " text," +
                    Key_profileUri + " text," +
                    Key_password + " text" +
                    " )")

            db.execSQL(CreateTableUserSessionQuery)
            db.execSQL(CreateTableUserQuery)

            // Insert the admin
            DatabaseHelper.instance.insertAdmin(db)

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