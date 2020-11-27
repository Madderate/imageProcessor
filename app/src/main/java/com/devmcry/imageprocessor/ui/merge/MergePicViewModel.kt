package com.devmcry.imageprocessor.ui.merge

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MergePicViewModel : ViewModel() {

    private val _text by lazy {
        MutableLiveData<String>().apply {
            value = "请选择图片"
        }
    }
    val text: LiveData<String> = Transformations.map(_text) { it.toString() }

    fun append(string: String) {
        _text.value = _text.value.plus("\n").plus(string)
    }

    fun clear() {
        _text.value = ""
    }

    private val _bitmap by lazy { MutableLiveData<Bitmap>() }

    val bitmap: LiveData<Bitmap> = Transformations.map(_bitmap) { it }

    fun mergeImage(context: Context, imagePaths: List<String>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val bitmap = MergePicUtils.merge(context, imagePaths)
                if (bitmap != null) {
                    _bitmap.postValue(bitmap)
                }
            }
        }
    }
}