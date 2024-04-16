package com.jmsoft.Utility.Database

import android.database.sqlite.SQLiteDatabase

class CollectionDataModel {

    var collectionId:Int? = null
    var collectionUUID:String? = null
    var collectionName:String? = null

    companion object {

        // Table name

        const val TABLE_NAME_COLLECTION = "Collection"

        //All keys of collection table

        const val Key_collectioId = "collectionId"
        const val Key_collectionUUID = "collectionUUID"
        const val Key_collectionName = "collectionName"

        @JvmStatic
        fun CreateTable(db: SQLiteDatabase) {

            val CreateTableCollectionQuery = ("create table " + TABLE_NAME_COLLECTION + " ("
                    + Key_collectioId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_collectionUUID + " text," +
                    Key_collectionName + " text" +
                    " )")

            db.execSQL(CreateTableCollectionQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_COLLECTION)
        }


    }
}