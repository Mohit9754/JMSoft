package com.jmsoft.Utility.database

import android.database.sqlite.SQLiteDatabase

class CollectionDataModel {

    var collectionUUID:String? = null
    var collectionName:String? = null
    var collectionImageUri:String? = null

    companion object {

        // Table name

        const val TABLE_NAME_COLLECTION = "Collection"

        //All keys of collection table

        const val Key_collectioId = "collectionId"
        const val Key_collectionUUID = "collectionUUID"
        const val Key_collectionName = "collectionName"
        const val Key_collectionImageUri = "collectionImageUri"

        @JvmStatic
        fun createTable(db: SQLiteDatabase) {

            val createTableCollectionQuery = ("create table " + TABLE_NAME_COLLECTION + " ("
                    + Key_collectioId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_collectionUUID + " text," +
                    Key_collectionName + " text," +
                    Key_collectionImageUri + " text" +
                    " )")

            db.execSQL(createTableCollectionQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_COLLECTION")
        }


    }
}