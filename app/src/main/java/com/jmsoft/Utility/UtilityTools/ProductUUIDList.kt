package com.jmsoft.Utility.UtilityTools

object ProductUUIDList {

    private var productUUIDList =  ArrayList<String>()

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