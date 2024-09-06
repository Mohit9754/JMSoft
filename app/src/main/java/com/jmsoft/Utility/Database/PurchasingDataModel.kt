package com.jmsoft.Utility.Database

import android.database.sqlite.SQLiteDatabase
import com.jmsoft.basic.Database.UserDataModel

class PurchasingDataModel {

    var purchasingId:Int? = null
    var purchasingUUID:String? = null
    var productUUID:String? = null
    var orderNo:String? = null
    var supplier:String? = null
    var totalAmount:Double? = null
    var date:String? = null

    companion object {

        // Table name
        const val TABLE_NAME_PURCHASING = "Purchasing"

        const val Key_purchasingId = "purchasingId"
        const val Key_purchasingUUID = "purchasingUUID"
        const val Key_productUUID = "productUUID"
        const val Key_orderNo = "orderNo"
        const val Key_supplier = "supplier"
        const val Key_totalAmount = "totalAmount"
        const val Key_date = "date"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableAddressQuery = ("create table " + TABLE_NAME_PURCHASING + " ("
                    + Key_purchasingId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_purchasingUUID + " text," +
                    Key_productUUID + " text," +
                    Key_orderNo + " text," +
                    Key_supplier + " text," +
                    Key_totalAmount + " REAL," +
                    Key_date + " text" +
                    " )")

            db.execSQL(CreateTableAddressQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PURCHASING)
        }


    }
}