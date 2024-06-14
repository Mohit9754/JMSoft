package com.jmsoft.Utility.Database

import android.database.sqlite.SQLiteDatabase

class OrderDataModel {

    var orderId:Int? = null
    var orderUUID:String? = null
    var productUUID: String? = null
    var productQuantity: String? = null
    var userUUID: String? = null
    var addressUUID: String? = null

    companion object {

        // Table name
        const val TABLE_NAME_ORDER = "Orders"

        // All keys of order table
        const val Key_orderId = "orderId"
        const val Key_orderUUID = "orderUUID"
        const val Key_productUUID = "productUUID"
        const val Key_productQuantity = "productQuantity"
        const val Key_userUUID = "userUUID"
        const val Key_addressUUID = "addressUUID"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableOrderQuery = ("create table " + TABLE_NAME_ORDER + " ("

                    + Key_orderId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_orderUUID + " text UNIQUE," +
                    Key_productUUID + " TEXT,"+
                    Key_productQuantity + " TEXT,"+
                    Key_addressUUID + " TEXT,"+
                    Key_userUUID + " TEXT"+

            " )")

            db.execSQL(CreateTableOrderQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ORDER)
        }


    }
}