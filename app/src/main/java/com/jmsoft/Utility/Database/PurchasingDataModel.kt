package com.jmsoft.Utility.Database

import android.database.sqlite.SQLiteDatabase
import com.jmsoft.basic.Database.UserDataModel

class PurchasingDataModel {

    var purchasingId:Int? = null
    var purchasingUUID:String? = null
    var orderNo:String? = null
    var supplierUUID:String? = null
    var totalAmount:Double? = null
    var date:String? = null
    var productImageUri:String? = null
    var productUUIDs:String? = null
    var productNames:String? = null
    var productRFIDs:String? = null
    var productWeights:String? = null
    var productPrices:String? = null
    var purchaseStatus:String? = null

    companion object {

        // Table name
        const val TABLE_NAME_PURCHASING = "Purchasing"

        const val Key_purchasingId = "purchasingId"
        const val Key_purchasingUUID = "purchasingUUID"
        const val Key_orderNo = "orderNo"
        const val Key_supplierUUID = "supplierUUID"
        const val Key_totalAmount = "totalAmount"
        const val Key_date = "date"
        const val Key_productUUIDs = "productUUIDs"
        const val Key_productImageUri = "productImageUri"
        const val Key_productNames = "productNames"
        const val Key_productPrices = "productPrices"
        const val Key_productRFIDs = "productRFIDs"
        const val Key_productWeights = "productWeights"
        const val Key_purchaseStatus = "productStatus"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableAddressQuery = ("create table " + TABLE_NAME_PURCHASING + " ("
                    + Key_purchasingId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_purchasingUUID + " text," +
                    Key_productUUIDs + " text," +
                    Key_productImageUri + " text," +
                    Key_productNames + " text," +
                    Key_productPrices + " text," +
                    Key_orderNo + " text," +
                    Key_supplierUUID + " text," +
                    Key_totalAmount + " REAL," +
                    Key_date + " text," +
                    Key_productRFIDs + " text," +
                    Key_productWeights + " text," +
                    Key_purchaseStatus + " text" +
                    " )")

            db.execSQL(CreateTableAddressQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PURCHASING)
        }


    }
}