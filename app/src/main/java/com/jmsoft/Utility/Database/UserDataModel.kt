package com.jmsoft.basic.Database

import android.database.sqlite.SQLiteDatabase
import com.jmsoft.basic.UtilityTools.Utils.E
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

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
    var profilePicture: String? = null

    @SerializedName("token")
    @Expose
    var token: String? = null


    companion object {

        const val TABLE_NAME = "User"

        //    All Key
        const val KEY_ID = "_id"
        const val Key_userType = "userType"
        const val Key_firstName = "firstName"
        const val Key_lastName = "lastName"
        const val Key_email = "email"
        const val Key_phoneNumber = "phoneNumber"
        const val Key_profilePicture = "profilePicture"
        const val Key_token = "token"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {
            val CreateTableQuery = ("create table " + TABLE_NAME + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_userType + " text," +
                    Key_firstName + " text," +
                    Key_lastName + " text," +
                    Key_email + " text," +
                    Key_phoneNumber + " text," +
                    Key_profilePicture + " text," +
                    Key_token + " text"  +
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