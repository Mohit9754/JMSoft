package com.jmsoft.main.`interface`

import android.widget.CheckBox
import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.main.model.SelectedCollectionModel

interface CollectionStatusCallback {

    fun collectionSelected(selectedCollectionModel: SelectedCollectionModel)

    fun collectionUnSelected(selectedCollectionModel: SelectedCollectionModel)


}