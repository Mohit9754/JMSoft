package com.jmsoft.utility.database

import android.database.sqlite.SQLiteDatabase

class DeviceDataModel {

    var deviceUUID: String? = null

    var deviceType: String? = null
    var deviceName: String? = null

    var deviceAddress: String? = null

    var userUUID: String? = null

    companion object {

        // Table name
        const val TABLE_NAME_DEVICE = "Device"

        //All keys of Device table

        const val Key_deviceUUID = "deviceUUID"
        const val Key_deviceName = "deviceName"
        const val Key_deviceType = "deviceType"
        const val Key_deviceAddress = "deviceAddress"
        const val Key_deviceId = "deviceId"
        const val Key_userUUID = "userUUID"

        @JvmStatic
        fun createTable(db: SQLiteDatabase) {

            val createTableDeviceQuery = ("create table " + TABLE_NAME_DEVICE + " ("
                    + Key_deviceId + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Key_deviceUUID + " text," +
                    Key_deviceName + " text," +
                    Key_deviceType + " text," +
                    Key_deviceAddress + " text," +
                    Key_userUUID + " text," +
                    " FOREIGN KEY (" + UserDataModel.Key_userUUID + ") REFERENCES " + UserDataModel.TABLE_NAME_USER + "(" + UserDataModel.Key_userUUID + ") ON DELETE CASCADE" +
                    " )")

            db.execSQL(createTableDeviceQuery)

        }

        @JvmStatic
        fun dropTable(db: SQLiteDatabase) {

            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_DEVICE")
        }


    }
}