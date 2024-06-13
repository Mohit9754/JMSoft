package com.jmsoft.main.`interface`

import com.jmsoft.Utility.Database.ContactDataModel

interface ContactSelectionStatus {
    fun contactSelected(contactDataModel: ContactDataModel)
    fun contactUnselected()

}