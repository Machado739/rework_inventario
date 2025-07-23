package com.example.inventario20.ui.ubicaciones

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UbicacionesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is ubicaciones Fragment"
    }
    val text: LiveData<String> = _text
}