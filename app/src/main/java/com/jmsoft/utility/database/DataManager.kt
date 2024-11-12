package com.jmsoft.utility.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper


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

        // Creating User and User_session table
        UserDataModel.CreateTable(db)

        // Creating Product table
        ProductDataModel.createTable(db)

        // Creating Category table
        CategoryDataModel.createTable(db)

        // Creating Cart table
        CartDataModel.createTable(db)

        // Creating Address table
        AddressDataModel.createTable(db)

        // Creating Device table
        DeviceDataModel.createTable(db)

        // Creating Collection table
        CollectionDataModel.createTable(db)

        // Creating Metal_Type table
        MetalTypeDataModel.createTable(db)

        // Creating order table
        OrderDataModel.createTable(db)

        // Creating contact table
        ContactDataModel.createTable(db)

        StockLocationDataModel.createTable(db)

        PurchasingDataModel.createTable(db)

    }
//    override fun onOpen(db: SQLiteDatabase) {
//        super.onOpen(db)
//        db.execSQL("PRAGMA foreign_keys=ON")
//
//        Utils.E("Foreign key is enabled") // Assuming Utils.E logs a message
//    }


    /**
     * @param db        //
     * @param paramInt1 //
     * @param paramInt2 //
     */

    override fun onUpgrade(db: SQLiteDatabase, paramInt1: Int, paramInt2: Int) {

        // Drop User and User_session table
        UserDataModel.dropTable(db)

        // Drop Product table
        ProductDataModel.dropTable(db)

        // Drop Category table
        CategoryDataModel.dropTable(db)

        // Drop Cart table
        CartDataModel.dropTable(db)

        // Drop Address table
        AddressDataModel.dropTable(db)

        // Drop Device table
        DeviceDataModel.dropTable(db)

        // Drop Collection table
        CollectionDataModel.dropTable(db)

        // Drop Metal_Type table
        MetalTypeDataModel.dropTable(db)

        // Drop order table
        OrderDataModel.dropTable(db)

        // Drop contact table
        ContactDataModel.dropTable(db)

        StockLocationDataModel.dropTable(db)

        PurchasingDataModel.dropTable(db)

        onCreate(db)

    }

    companion object {

        const val DATABASE_VERSION = 213
        const val DATABASE_NAME = "jm_soft"

    }
}