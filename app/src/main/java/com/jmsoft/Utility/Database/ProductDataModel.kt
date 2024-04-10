package com.jmsoft.Utility.Database

import android.database.sqlite.SQLiteDatabase

class ProductDataModel {

    var productId: Int? = null
    var productUUId: String? = null
    var productName: String? = null
    var productImage: String? = null
    var productPrice: Int? = null
    var productDescription: String? = null
    var productWeight: String? = null
    var productUnitOfMeasurement:String? = null
    var productMetalType: String? = null
    var productCarat: String? = null
    var productRFID: String? = null
    var categoryUUID: String? = null
    var productCategory: String? = null

    companion object {

        //Table name
        const val TABLE_NAME_PRODUCT = "Product"

        //All keys of product table
        const val Key_productId = "productId"
        const val Key_productUUID = "productUUID"
        const val Key_productName = "productName"
        const val Key_productImage = "productImage"
        const val Key_productPrice = "productPrice"
        const val Key_productUnitOfMeasurement = "productUnitOfMeasurement"
        const val Key_productCategory = "productCategory"
        const val Key_productDescription = "productDescription"
        const val Key_productWeight = "productWeight"
        const val Key_productMetalType = "productMetalType"
        const val Key_productCarat = "productCarat"
        const val Key_productRFID = "productRFID"
        const val Key_categoryUUID = "categoryUUID"


        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableProductQuery = ("create table " + TABLE_NAME_PRODUCT + " ("
                    + Key_productId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_productUUID + " text," +
                    Key_productName + " text," +
                    Key_productUnitOfMeasurement + " text," +
                    Key_productCategory + " text," +
                    Key_productImage + " text," +
                    Key_productPrice + " INTEGER," +
                    Key_productDescription + " text," +
                    Key_productWeight + " text," +
                    Key_productMetalType + " text," +
                    Key_productCarat + " text," +
                    Key_productRFID + " text," +
                    Key_categoryUUID + " text," +
                    " FOREIGN KEY (" + Key_categoryUUID + ") REFERENCES " + CategoryDataModel.TABLE_NAME_CATEGORY + "(" + CategoryDataModel.Key_categoryUUID + ")" +
                    " )")

            db.execSQL(CreateTableProductQuery)


        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PRODUCT)

        }
    }
}