package com.jmsoft.Utility.database

import android.database.sqlite.SQLiteDatabase

class MetalTypeDataModel {

    var metalTypeId:Int? = null
    var metalTypeUUID:String? = null
    var metalTypeName:String? = null

    companion object {

        // Table name
        const val TABLE_NAME_METAL_TYPE = "Metal_Type"

        //All keys of metal_type table
        const val Key_metalTypeId = "metalTypeId"
        const val Key_metalTypeUUID = "metalTypeUUID"
        const val Key_metalTypeName = "metalTypeName"

        @JvmStatic
        fun createTable(db: SQLiteDatabase) {

            val createTableCategoryQuery = ("create table " + TABLE_NAME_METAL_TYPE + " ("
                    + Key_metalTypeId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_metalTypeUUID + " text UNIQUE," +
                    Key_metalTypeName + " text" +
                    " )")

            db.execSQL(createTableCategoryQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_METAL_TYPE")
        }


    }
}