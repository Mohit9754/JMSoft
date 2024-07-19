package com.jmsoft.Utility.Database

import android.database.sqlite.SQLiteDatabase

class OrderDataModel {

    var orderId:Int? = null
    var orderUUID:String? = null
    var orderNo:String? = null
    var productUUIDUri: String? = null
    var productQuantityUri: String? = null
    var userUUID: String? = null
    var addressUUID: String? = null
    var status: String? = null
    var totalAmount: Double? = null
    var pdfName: String? = null
    var date: String? = null

    companion object {

        // Table name
        const val TABLE_NAME_ORDER = "Orders"

        // All keys of order table
        const val Key_orderId = "orderId"
        const val Key_orderUUID = "orderUUID"
        const val Key_orderNo = "orderNo"
        const val Key_productUUIDUri = "productUUIDUri"
        const val Key_productQuantityUri = "productQuantityUri"
        const val Key_userUUID = "userUUID"
        const val Key_addressUUID = "addressUUID"
        const val Key_status = "status"
        const val Key_totalAmount = "totalAmount"
        const val Key_pdfName = "pdfName"
        const val Key_date = "date"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableOrderQuery = ("create table " + TABLE_NAME_ORDER + " ("

                    + Key_orderId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_orderUUID + " TEXT UNIQUE," +
                    Key_orderNo + " TEXT," +
                    Key_productUUIDUri + " TEXT,"+
                    Key_productQuantityUri + " TEXT,"+
                    Key_addressUUID + " TEXT,"+
                    Key_userUUID + " TEXT,"+
                    Key_status + " TEXT,"+
                    Key_totalAmount + " REAL,"+
                    Key_pdfName + " TEXT,"+
                    Key_date + " TEXT"+

            " )")

            db.execSQL(CreateTableOrderQuery)


        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ORDER)
        }


    }
}