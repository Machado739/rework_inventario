package com.example.inventario20.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inventario20.DBHelper
import com.example.inventario20.R

class RegistroAdapter(
    private var lista: List<DBHelper.Registro>,
    private val onClick: (DBHelper.Registro) -> Unit
) : RecyclerView.Adapter<RegistroAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val codigo: TextView = view.findViewById(R.id.itemCodigo)
        val total: TextView = view.findViewById(R.id.itemTotal)

        init {
            view.setOnClickListener {
                onClick(lista[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registro, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val registro = lista[position]
        holder.codigo.text = registro.idproducto
        holder.total.text = "Total: ${registro.total}"
    }

    fun actualizarLista(nuevaLista: List<DBHelper.Registro>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
