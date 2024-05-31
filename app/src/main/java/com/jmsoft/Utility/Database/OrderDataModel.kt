package com.jmsoft.Utility.Database

import android.database.sqlite.SQLiteDatabase

class OrderDataModel {

    var orderId:Int? = null
    var orderUUID:String? = null
    var productUUID: String? = null
    var userUUID: String? = null

    companion object {

        // Table name
        const val TABLE_NAME_ORDER = "Orders"

        // All keys of order table
        const val Key_orderId = "orderId"
        const val Key_orderUUID = "categoryUUID"
        const val Key_productUUID = "productUUID"
        const val Key_userUUID = "userUUID"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableCategoryQuery = ("create table " + TABLE_NAME_ORDER + " ("

                    + Key_orderId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_orderUUID + " text UNIQUE," +
                    Key_productUUID + " TEXT,"+
                    Key_userUUID + " TEXT"+

            " )")

            db.execSQL(CreateTableCategoryQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ORDER)
        }


    }
}