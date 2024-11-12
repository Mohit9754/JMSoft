package com.jmsoft.utility.database

import android.database.sqlite.SQLiteDatabase

class AddressDataModel {

    var addressId:Int? = null
    var addressUUID:String? = null
    var firstName:String? = null
    var lastName:String? = null
    var address:String? = null
    var phoneNumber:String? = null
    var zipCode:String? = null
    var userUUID:String? = null

    companion object {

        // Table name
        const val TABLE_NAME_ADDRESS = "Address"

        //All keys of Address table

        const val Key_addressId = "addressId"
        const val Key_addressUUID = "addressUUID"
        const val Key_firstName= "firstName"
        const val Key_lastName= "lastName"
        const val Key_address = "address"
        const val Key_phoneNumber= "phoneNumber"
        const val Key_zipCode = "zipCode"
        const val Key_userUUID = "userUUID"

        @JvmStatic
        fun createTable(db: SQLiteDatabase) {

            val createTableAddressQuery = ("create table " + TABLE_NAME_ADDRESS + " ("
                    + Key_addressId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_addressUUID + " text," +
                    Key_firstName + " text," +
                    Key_lastName + " text," +
                    Key_address + " text," +
                    Key_phoneNumber + " text," +
                    Key_zipCode + " text," +
                    Key_userUUID + " text," +
                    " FOREIGN KEY (" + CartDataModel.Key_userUUID + ") REFERENCES " + UserDataModel.TABLE_NAME_USER + "(" + UserDataModel.Key_userUUID + ") ON DELETE CASCADE" +
                    " )")

            db.execSQL(createTableAddressQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_ADDRESS")
        }


    }
}