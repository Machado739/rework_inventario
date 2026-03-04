package com.example.inventario20.ui.inventarios

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inventario20.DBHelper
import com.example.inventario20.R

class InventarioRegistroAdapter(
    private val lista: List<DBHelper.RegistroInventarioItem>,
    private val onClick: (DBHelper.RegistroInventarioItem) -> Unit
) : RecyclerView.Adapter<InventarioRegistroAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val codigo: TextView =view.findViewById(R.id.regCodigoTXT)

        val total: TextView = view.findViewById(R.id.regTotalTXT)
        val empresa: TextView = view.findViewById(R.id.regEmpresaTXT)
        val ubicacion: TextView = view.findViewById(R.id.regUbicacionTXT)
        val numeros: TextView = view.findViewById(R.id.regNumerosTXT)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventario_registro, parent, false)
        return ViewHolder(view)
    }



    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.codigo.text = item.producto
        holder.total.text = item.total
        holder.empresa.text = item.empresa
        holder.ubicacion.text = item.ubicacion
        holder.numeros.text = "T:${item.tarimas} C:${item.cajas} P:${item.unidades} S:${item.suelto}"
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = lista.size
}