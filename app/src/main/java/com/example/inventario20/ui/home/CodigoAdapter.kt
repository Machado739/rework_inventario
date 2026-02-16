package com.example.inventario20.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.inventario20.DBHelper
import android.widget.Filter
import android.widget.Filter.FilterResults
import kotlin.math.PI
class CodigoAdapter(
    context: Context,
    private val codigosOriginales: List<DBHelper.Codigo>
) : ArrayAdapter<DBHelper.Codigo>(context, android.R.layout.simple_dropdown_item_1line, ArrayList(codigosOriginales)) {

    private val codigosFiltrados = _root_ide_package_.java.util.ArrayList<DBHelper.Codigo>(codigosOriginales)

    override fun getCount(): Int = codigosFiltrados.size

    override fun getItem(position: Int): DBHelper.Codigo = codigosFiltrados[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)

        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = codigosFiltrados[position].idproducto   // 🔥 SOLO EL CÓDIGO

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val resultados = FilterResults()

                if (constraint.isNullOrEmpty()) {
                    resultados.values = codigosOriginales
                    resultados.count = codigosOriginales.size
                } else {
                    val filtro = constraint.toString().uppercase()
                    val listaFiltrada = codigosOriginales.filter {
                        it.idproducto.uppercase().contains(filtro)
                    }
                    resultados.values = listaFiltrada
                    resultados.count = listaFiltrada.size
                }

                return resultados
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                codigosFiltrados.clear()
                codigosFiltrados.addAll(results.values as List<DBHelper.Codigo>)
                notifyDataSetChanged()
            }
        }
    }
}
