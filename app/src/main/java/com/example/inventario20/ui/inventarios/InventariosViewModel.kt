package com.example.inventario20.ui.inventarios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InventariosViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Inventarios Fragment"
    }
    val text: LiveData<String> = _text
}