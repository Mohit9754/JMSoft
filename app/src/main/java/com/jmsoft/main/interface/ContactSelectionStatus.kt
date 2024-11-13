package com.jmsoft.main.`interface`

import com.jmsoft.Utility.database.ContactDataModel

interface ContactSelectionStatus {
    fun contactSelected(contactDataModel: ContactDataModel)
    fun contactUnselected()

}