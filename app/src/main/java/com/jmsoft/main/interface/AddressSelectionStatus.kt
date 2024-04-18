package com.jmsoft.main.`interface`

import com.jmsoft.Utility.Database.AddressDataModel

interface AddressSelectionStatus {

    fun addressSelected(addressDataModel: AddressDataModel)
    fun addressUnselected()

}