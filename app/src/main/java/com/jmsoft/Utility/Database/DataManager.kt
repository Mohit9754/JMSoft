package com.jmsoft.basic.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.jmsoft.Utility.Database.AddressDataModel
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.Utility.Database.ContactDataModel
import com.jmsoft.Utility.Database.DeviceDataModel
import com.jmsoft.Utility.Database.MetalTypeDataModel
import com.jmsoft.Utility.Database.OrderDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Utils

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
        ProductDataModel.CreateTable(db)

        // Creating Category table
        CategoryDataModel.CreateTable(db)

        // Creating Cart table
        CartDataModel.CreateTable(db)

        // Creating Address table
        AddressDataModel.CreateTable(db)

        // Creating Device table
        DeviceDataModel.CreateTable(db)

        // Creating Collection table
        CollectionDataModel.CreateTable(db)

        // Creating Metal_Type table
        MetalTypeDataModel.CreateTable(db)

        // Creating order table
        OrderDataModel.CreateTable(db)

        // Creating contact table
        ContactDataModel.CreateTable(db)

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

        onCreate(db)

    }

    companion object {

        const val DATABASE_VERSION = 149
        const val DATABASE_NAME = "JM_Soft"

    }
}