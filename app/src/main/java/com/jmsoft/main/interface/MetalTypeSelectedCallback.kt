package com.jmsoft.main.`interface`

import com.jmsoft.Utility.Database.MetalTypeDataModel

interface MetalTypeSelectedCallback {

    fun selectedMetalType(metalTypeDataModel: MetalTypeDataModel)

    fun unselectMetalType()

}