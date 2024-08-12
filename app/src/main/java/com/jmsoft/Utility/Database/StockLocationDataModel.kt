package com.jmsoft.Utility.Database

import android.database.sqlite.SQLiteDatabase

class StockLocationDataModel {

    var stockLocationId:Int? = null
    var stockLocationUUID:String? = null
    var stockLocationName:String? = null
    var stockLocationParentUUID:String? = null

    companion object {

        // Table name
        const val TABLE_NAME_STOCK_LOCATION = "Stock_location"

        // All keys of metal_type table
        const val Key_stockLocationId = "stockLocationId"
        const val Key_stockLocationUUID = "stockLocationUUID"
        const val Key_stockLocationName = "stockLocationName"
        const val Key_stockLocationParentUUID = "stockLocationParentUUID"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableCategoryQuery = ("create table " + TABLE_NAME_STOCK_LOCATION + " ("
                    + Key_stockLocationId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_stockLocationUUID + " text UNIQUE," +
                    Key_stockLocationName + " text," +
                    Key_stockLocationParentUUID + " text" +
                    " )")

            db.execSQL(CreateTableCategoryQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_STOCK_LOCATION)
        }


    }
}