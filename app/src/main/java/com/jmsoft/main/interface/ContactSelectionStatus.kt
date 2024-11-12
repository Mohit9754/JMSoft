package com.jmsoft.main.`interface`

import com.jmsoft.utility.database.ContactDataModel

interface ContactSelectionStatus {
    fun contactSelected(contactDataModel: ContactDataModel)
    fun contactUnselected()

}