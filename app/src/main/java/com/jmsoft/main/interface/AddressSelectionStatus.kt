package com.jmsoft.main.`interface`

import com.jmsoft.utility.database.AddressDataModel

interface AddressSelectionStatus {

    fun addressSelected(addressDataModel: AddressDataModel)
    fun addressUnselected()

}