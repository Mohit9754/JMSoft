package com.jmsoft.basic.Database

import android.content.Context
import com.jmsoft.basic.Database.UserDataModel.Companion.CreateTable
import com.jmsoft.basic.Database.UserDataModel.Companion.dropTable
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jmsoft.Utility.Database.AddressDataModel
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.DeviceDataModel
import com.jmsoft.Utility.Database.ProductDataModel

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
        ProductDataModel.CreateTable(db)
        CategoryDataModel.CreateTable(db)
        CartDataModel.CreateTable(db)
        AddressDataModel.CreateTable(db)
        DeviceDataModel.CreateTable(db)
    }

    /**
     * @param db        //
     * @param paramInt1 //
     * @param paramInt2 //
     */
    override fun onUpgrade(db: SQLiteDatabase, paramInt1: Int, paramInt2: Int) {

        dropTable(db)
        ProductDataModel.dropTable(db)
        CategoryDataModel.dropTable(db)
        CartDataModel.dropTable(db)
        AddressDataModel.dropTable(db)
        DeviceDataModel.dropTable(db)

        onCreate(db)
    }

    companion object {

        const val DATABASE_VERSION = 20
        const val DATABASE_NAME = "JM_Soft"
    }
}