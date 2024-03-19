package com.jmsoft.basic.Database

import android.content.Context
import com.jmsoft.basic.Database.UserDataModel.Companion.CreateTable
import com.jmsoft.basic.Database.UserDataModel.Companion.dropTable
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase

/**
 * Created by Hritik on 2/10/2023.
 */
class DataManager
/**
 * @param context //
 * @param name    //
 * @param factory //
 * @param version //
 */
    (context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    /**
     * @param db //
     */
    override fun onCreate(db: SQLiteDatabase) {
        CreateTable(db)
    }

    /**
     * @param db        //
     * @param paramInt1 //
     * @param paramInt2 //
     */
    override fun onUpgrade(db: SQLiteDatabase, paramInt1: Int, paramInt2: Int) {
        dropTable(db)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "AIG"
    }
}