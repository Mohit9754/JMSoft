package com.jmsoft.Utility.Database

import android.database.sqlite.SQLiteDatabase
import com.jmsoft.basic.UtilityTools.Utils

class CategoryDataModel {

    var categoryId:Int? = null
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
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableCategoryQuery = ("create table " + TABLE_NAME_CATEGORY + " ("
                + Key_categoryId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Key_categoryUUID + " text," +
                Key_categoryName + " text" +
                " )")

            db.execSQL(CreateTableCategoryQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CATEGORY)
        }


    }
}