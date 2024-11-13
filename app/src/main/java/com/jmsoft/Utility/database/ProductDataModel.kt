package com.jmsoft.Utility.database

import android.database.sqlite.SQLiteDatabase

class ProductDataModel {

    var productUUID: String? = null
    var productName: String? = null
    var metalTypeUUID: String? = null
    var collectionUUID: String? = null
    var productOrigin:String? = null
    var productWeight: Double? = null
    var productCarat: Int? = null
    var productPrice: Double? = null
    var productCost: Double? = null
    var categoryUUID:String? = null
    var productDescription: String? = null
    var productRFIDCode: String? = null
    var productBarcodeData:String? = null
    var productBarcodeUri:String? = null
    var productImageUri: String? = null
    var stockLocationUUID:String? = null


    companion object {

        //Table name
        const val TABLE_NAME_PRODUCT = "Product"

        //All keys of product table
        const val Key_productId = "productId"
        const val Key_productUUID = "productUUID"
        const val Key_productName = "productName"
        const val Key_metalTypeUUID = "metalTypeUUID"
        const val Key_collectionUUID = "collectionUUID"
        const val Key_productOrigin = "productOrigin"
        const val Key_productWeight = "productWeight"
        const val Key_productCarat = "productCarat"
        const val Key_productPrice = "productPrice"
        const val Key_productCost = "productCost"
        const val Key_categoryUUID = "categoryUUID"
        const val Key_productDescription = "productDescription"
        const val Key_productRFIDCode = "productRFID"
        const val Key_productBarcodeData = "productBarcodeData"
        const val Key_productBarcodeUri = "productBarcodeUri"
        const val Key_productImageUri = "productImageUri"
        const val Key_stockLocationUUID = "stockLocationUUID"

        @JvmStatic
        fun createTable(db: SQLiteDatabase) {

            val createTableProductQuery = ("CREATE TABLE " + TABLE_NAME_PRODUCT + " ("
                    + Key_productId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Key_productUUID + " TEXT UNIQUE,"
                    + Key_productName + " TEXT,"
                    + Key_productOrigin + " TEXT,"
                    + Key_productWeight + " REAL,"
                    + Key_productCarat + " INTEGER,"
                    + Key_productPrice + " REAL,"
                    + Key_productCost + " REAL,"
                    + Key_productDescription + " TEXT,"
                    + Key_productRFIDCode + " TEXT,"
                    + Key_productBarcodeData + " TEXT,"
                    + Key_productBarcodeUri + " TEXT,"
                    + Key_productImageUri + " TEXT,"
                    + Key_metalTypeUUID + " TEXT,"
                    + Key_collectionUUID + " TEXT,"  // Include this line for Key_collectionUUID
                    + Key_categoryUUID + " TEXT,"
                    + Key_stockLocationUUID + " TEXT,"
                    + " FOREIGN KEY (" + Key_metalTypeUUID + ") REFERENCES " + MetalTypeDataModel.TABLE_NAME_METAL_TYPE + "(" + MetalTypeDataModel.Key_metalTypeUUID + ") ON DELETE CASCADE ,"
                    + " FOREIGN KEY (" + Key_categoryUUID + ") REFERENCES " + CategoryDataModel.TABLE_NAME_CATEGORY + "(" + CategoryDataModel.Key_categoryUUID + ") ON DELETE CASCADE"
                    + " )")

            db.execSQL(createTableProductQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PRODUCT")

        }
    }
}