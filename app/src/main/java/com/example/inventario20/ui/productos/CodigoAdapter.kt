package com.example.inventario20.ui.productos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.inventario20.DBHelper
import com.example.inventario20.R

class CodigoAdapter(
    private val context: Context,
    private var codigos: List<DBHelper.Codigo>
) : ArrayAdapter<DBHelper.Codigo>(context, 0, codigos) {
    private var selectedIndex = -1  // Aquí guardamos el índice seleccionado

    fun setSelectedIndex(index: Int) {
        selectedIndex = index
        notifyDataSetChanged() // Esto fuerza que se refresque la lista
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_codigo, parent, false)

        val tvCodigo = view.findViewById<TextView>(R.id.tvCodigo)
        val tvProducto = view.findViewById<TextView>(R.id.tvProducto)
        val tvMedida = view.findViewById<TextView>(R.id.tvMedida)

        val item = codigos[position]

        tvCodigo.text = item.idproducto
        tvProducto.text = item.producto
        tvMedida.text = item.medida

        return view
    }
}

