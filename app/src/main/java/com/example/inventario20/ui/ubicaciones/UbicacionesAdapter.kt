package com.example.inventario20.ui.ubicaciones

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.inventario20.DBHelper
import com.example.inventario20.R

class UbicacionesAdapter(
    private val contex: Context,
    private var ubicasiones: List<DBHelper.Ubicacion>
) : ArrayAdapter<DBHelper.Ubicacion>(contex, 0, ubicasiones) {
    private var selectedIndex = -1  // Aquí guardamos el índice seleccionado

    fun setSelectedIndex(index: Int) {
        selectedIndex = index
        notifyDataSetChanged() // Esto fuerza que se refresque la lista
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_ubicacion, parent, false)
        val tv = view.findViewById<TextView>(R.id.textNombre)
        val item = ubicasiones[position]
        tv.text = item.ubicacion
        return view
    }
}
