package com.example.inventario20.ui.exportacion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExportacionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Exportacion Fragment"
    }
    val text: LiveData<String> = _text
}