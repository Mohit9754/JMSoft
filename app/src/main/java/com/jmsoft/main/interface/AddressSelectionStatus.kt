package com.jmsoft.main.`interface`

import com.jmsoft.Utility.database.AddressDataModel

interface AddressSelectionStatus {

    fun addressSelected(addressDataModel: AddressDataModel)
    fun addressUnselected()

}