package com.jmsoft.Utility.UtilityTools

import com.jmsoft.basic.UtilityTools.Utils
import okio.Utf8

object ProductUUIDList {

    private var productUUIDList =  ArrayList<String>()
    private var addStatus:Boolean = false

    fun getData(){
        productUUIDList = Utils.getAllProductsUUID()
    }

    fun setStatus(addStatus:Boolean){
        this.addStatus = addStatus
    }

    fun getStatus(): Boolean {
        return addStatus
    }

    fun getSize(): Int {
        return productUUIDList.size
    }

    fun getProductUUID(index:Int): String {
        return productUUIDList[index]
    }

    fun getIndexOf(productUUID: String): Int {
        return productUUIDList.indexOf(productUUID)
    }

    fun clearList(){
        productUUIDList.clear()
    }

    fun addUUID(productUUID:String) {
        this.productUUIDList.add(productUUID)
    }

    fun deleteUUID(productUUID:String) {
        productUUIDList.remove(productUUID)
    }

}