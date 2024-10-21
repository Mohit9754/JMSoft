package com.jmsoft.Utility.Database

import android.database.sqlite.SQLiteDatabase
import com.jmsoft.basic.Database.UserDataModel

class ContactDataModel {

    var contactId:Int? = null
    var contactUUID:String? = null
    var firstName:String? = null
    var lastName:String? = null
    var phoneNumber:String? = null
    var emailAddress:String? = null
    var type:String? = null
    var userUUID:String? = null

    companion object {

        // Table name
        const val TABLE_NAME_CONTACT = "Contact"

        //All keys of Contact table

        const val Key_contactId = "contactId"
        const val Key_contactUUID = "contactUUID"
        const val Key_firstName= "firstName"
        const val Key_lastName= "lastName"
        const val Key_phoneNumber= "phoneNumber"
        const val Key_emailAddress = "emailAddress"
        const val Key_type = "type"
        const val Key_userUUID = "userUUID"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableAddressQuery = ("create table " + TABLE_NAME_CONTACT + " ("
                    + Key_contactId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_contactUUID + " text," +
                    Key_firstName + " text," +
                    Key_lastName + " text," +
                    Key_phoneNumber + " text," +
                    Key_emailAddress + " text," +
                    Key_type + " text," +
                    Key_userUUID + " text," +
                    " FOREIGN KEY (" + CartDataModel.Key_userUUID + ") REFERENCES " + UserDataModel.TABLE_NAME_USER + "(" + UserDataModel.Key_userUUID + ") ON DELETE CASCADE" +
                    " )")

            db.execSQL(CreateTableAddressQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CONTACT)
        }


    }
}