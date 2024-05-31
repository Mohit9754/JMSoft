package com.jmsoft.main.`interface`

import com.jmsoft.Utility.Database.ProductDataModel

interface ExcelReadSuccess {

    fun onReadSuccess(productList :ArrayList<ProductDataModel>)

    fun onReadFail()


}