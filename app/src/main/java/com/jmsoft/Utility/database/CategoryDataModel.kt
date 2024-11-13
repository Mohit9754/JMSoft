package com.jmsoft.Utility.database

import android.database.sqlite.SQLiteDatabase

class CategoryDataModel {

    var categoryUUID:String? = null
    var categoryName:String? = null

    companion object {

        // Table name
        const val TABLE_NAME_CATEGORY = "Category"

        //All keys of category table
        const val Key_categoryId = "categoryId"
        const val Key_categoryUUID = "categoryUUID"
        const val Key_categoryName = "categoryName"

        @JvmStatic
        fun createTable(db: SQLiteDatabase) {

            val createTableCategoryQuery = ("create table " + TABLE_NAME_CATEGORY + " ("
                + Key_categoryId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Key_categoryUUID + " text UNIQUE," +
                Key_categoryName + " text" +
                " )")

            db.execSQL(createTableCategoryQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_CATEGORY")
        }


    }
}