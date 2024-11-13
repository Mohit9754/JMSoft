package com.jmsoft.Utility.database

import android.database.sqlite.SQLiteDatabase

class CartDataModel {

    var cartId: Int? = null
    var cartUUID: String? = null
    var productUUID: String? = null
    var productQuantity: Int? = null
    var userUUID: String? = null

    companion object {

        //Table name
        const val TABLE_NAME_CART = "Cart"

        //All keys of Card table
        const val Key_cartId = "cartId"
        const val Key_cartUUID = "cartUUID"
        const val Key_productUUID = "productUUID"
        const val Key_userUUID = "userUUID"
        const val Key_productQuantity = "productQuantity"

        @JvmStatic
        fun createTable(db: SQLiteDatabase) {

            val createTableCartQuery = ("create table " + TABLE_NAME_CART + " ("
                    + Key_cartId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_cartUUID + " text," +
                    Key_productUUID + " text," +
                    Key_productQuantity + " INTEGER," +
                    Key_userUUID + " text," +
                    " FOREIGN KEY (" + Key_productUUID + ") REFERENCES " + ProductDataModel.TABLE_NAME_PRODUCT + "(" + ProductDataModel.Key_productUUID + ") ON DELETE CASCADE," +
                    " FOREIGN KEY (" + Key_userUUID + ") REFERENCES " + UserDataModel.TABLE_NAME_USER + "(" + UserDataModel.Key_userUUID + ") ON DELETE CASCADE" +
                    " )")

            db.execSQL(createTableCartQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_CART")

        }
    }
}