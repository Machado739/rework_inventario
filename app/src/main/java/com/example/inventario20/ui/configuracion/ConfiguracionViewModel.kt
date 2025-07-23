package com.example.inventario20.ui.configuracion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConfiguracionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Configuracion Fragment"
    }
    val text: LiveData<String> = _text
}