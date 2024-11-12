package com.jmsoft.main.`interface`

import com.jmsoft.main.model.SelectedCollectionModel

interface CollectionStatusCallback {

    fun collectionSelected(selectedCollectionModel: SelectedCollectionModel,closeDropDown:Boolean)

    fun collectionUnSelected(selectedCollectionModel: SelectedCollectionModel)


}