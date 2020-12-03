package com.devmcry.imageprocessor.ui.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class EditPicViewModel : ViewModel() {

    private val _imagesPath = MutableLiveData<MutableList<String>>().apply {
        value = mutableListOf()
    }
    val imagePath: LiveData<List<String>> = Transformations.map(_imagesPath) { it.toList() }

    fun setEditImages(imagesList: List<String>) {
        _imagesPath.value?.run {
            clear()
            addAll(imagesList)
        }
        _imagesPath.value = _imagesPath.value
    }
}