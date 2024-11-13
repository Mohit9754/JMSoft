package com.jmsoft.main.`interface`

import com.jmsoft.Utility.database.ProductDataModel

interface ExcelReadSuccess {

    fun onReadSuccess(productList :ArrayList<ProductDataModel>)

    fun onReadFail()


}