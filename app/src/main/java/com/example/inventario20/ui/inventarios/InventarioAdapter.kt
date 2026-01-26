package com.example.inventario20.ui.inventarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inventario20.DBHelper
import com.example.inventario20.R
import androidx.core.content.ContextCompat

class InventarioAdapter(
    private val lista: List<DBHelper.InventarioItem>,
    private val onClick: (DBHelper.InventarioItem) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.ViewHolder>() {
    private var idSeleccionado: Int? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val fecha: TextView = view.findViewById(R.id.txtFecha)
        val estado: TextView = view.findViewById(R.id.txtEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        val context = holder.itemView.context

        holder.nombre.text = item.nombre_inventario
        holder.fecha.text = item.fechaCreacion

        // TEXTO + COLOR DEL ESTADO
        when (item.activo) {
            1 -> {
                holder.estado.text = "ABIERTO"
                holder.estado.setTextColor(
                    ContextCompat.getColor(context, R.color.inv_abierto)
                )
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.inv_bg_abierto)
                )
            }
            0 -> {
                holder.estado.text = "CERRADO"
                holder.estado.setTextColor(
                    ContextCompat.getColor(context, R.color.inv_cerrado)
                )
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.inv_bg_cerrado)
                )
            }
            3 -> {
                holder.estado.text = "REABIERTO"
                holder.estado.setTextColor(
                    ContextCompat.getColor(context, R.color.inv_reabierto)
                )
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.inv_bg_reabierto)
                )
            }
        }

        // RESALTAR SELECCIONADO
        if (item.idinventarios == idSeleccionado) {
            holder.itemView.alpha = 1f
            holder.itemView.scaleX = 1.02f
            holder.itemView.scaleY = 1.02f
        } else {
            holder.itemView.alpha = 0.85f
            holder.itemView.scaleX = 1f
            holder.itemView.scaleY = 1f
        }

        holder.itemView.setOnClickListener {
            idSeleccionado = item.idinventarios
            notifyDataSetChanged()
            onClick(item)
        }
    }


    override fun getItemCount() = lista.size
}
