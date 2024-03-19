package com.jmsoft.basic.Database

import android.database.sqlite.SQLiteDatabase
import com.jmsoft.basic.UtilityTools.Utils.E
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class UserDataModel {
    @SerializedName("accessToken")
    @Expose
    var accessToken: String? = null

    @SerializedName("userId")
    @Expose
    var userId: String? = null

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

    @SerializedName("gender")
    @Expose
    var gender: String? = null

    @SerializedName("dob")
    @Expose
    var dob: String? = null

    @SerializedName("profilePicture")
    @Expose
    var profilePicture: String? = null

    @SerializedName("userBio")
    @Expose
    var userBio: String? = null

    @SerializedName("isEmailVerified")
    @Expose
    var isEmailVerified: String? = null

    @SerializedName("isPhoneVerified")
    @Expose
    var isPhoneVerified: String? = null

    companion object {
        const val TABLE_NAME = "User"

        //    All Key
        const val KEY_ID = "_id"
        const val Key_userId = "userId"
        const val Key_accessToken = "accessToken"
        const val Key_firstName = "firstName"
        const val Key_lastName = "lastName"
        const val Key_email = "email"
        const val Key_phoneNumber = "phoneNumber"
        const val Key_gender = "gender"
        const val Key_dob = "dob"
        const val Key_profilePicture = "profilePicture"
        const val Key_userBio = "userBio"
        const val Key_isEmailVerified = "isEmailVerified"
        const val Key_isPhoneVerified = "isPhoneVerified"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {
            val CreateTableQuery = ("create table " + TABLE_NAME + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_userId + " text," +
                    Key_accessToken + " text," +
                    Key_firstName + " text," +
                    Key_lastName + " text," +
                    Key_email + " text," +
                    Key_phoneNumber + " text," +
                    Key_gender + " text," +
                    Key_dob + " text," +
                    Key_profilePicture + " text," +
                    Key_isEmailVerified + " text," +
                    Key_isPhoneVerified + " text," +
                    Key_userBio + " text" +
                    " )")
            E("CreateTableQuery::$CreateTableQuery")
            db.execSQL(CreateTableQuery)
        }

        /**
         * @param db SQLiteDatabase
         */
        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        }
    }
}