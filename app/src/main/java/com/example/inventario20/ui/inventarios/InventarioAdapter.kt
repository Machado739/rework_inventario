// kotlin
package com.example.inventario20.ui.inventarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inventario20.DBHelper
import com.example.inventario20.R
import androidx.core.content.ContextCompat
import android.os.SystemClock
import android.view.animation.DecelerateInterpolator
import android.animation.ValueAnimator
import android.content.res.Resources

class InventarioAdapter(
    private val lista: List<DBHelper.InventarioItem>,
    private val onClick: (DBHelper.InventarioItem) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.ViewHolder>() {

    private var selectedId: Int? = null
    private var recyclerView: RecyclerView? = null
    private var lastClickTime = 0L
    private val animDuration = 200L
    private val animInterpolator = DecelerateInterpolator()
    // sombra en dp cuando está seleccionado
    private val selectedElevationDp = 6f

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return try {
            lista[position].idinventarios.toLong()
        } catch (_: Exception) {
            position.toLong()
        }
    }

    fun attachRecyclerView(rv: RecyclerView) {
        recyclerView = rv
    }

    // Actualizamos sólo los items que cambian (previo y nuevo) para evitar rebind masivo
    fun setSelected(id: Int?) {
        val prev = selectedId
        if (prev == id) return

        val prevIndex = prev?.let { pid -> lista.indexOfFirst { it.idinventarios == pid } } ?: -1
        val newIndex = id?.let { nid -> lista.indexOfFirst { it.idinventarios == nid } } ?: -1

        // actualizar selectedId antes de animaciones para que bindView refleje estado
        selectedId = id

        // Si el ViewHolder previo está visible, animarlo directamente; si no, notificar para bind cuando sea visible
        val prevVH = if (prevIndex >= 0) recyclerView?.findViewHolderForAdapterPosition(prevIndex) as? ViewHolder else null
        if (prevVH != null) {
            val prevItem = lista[prevIndex]
            animateDeselection(prevVH.itemView, prevItem.activo)
        } else if (prevIndex >= 0) {
            notifyItemChanged(prevIndex)
        }

        // Si el nuevo ViewHolder está visible, animarlo directamente; si no, notificar para bind cuando sea visible
        val newVH = if (newIndex >= 0) recyclerView?.findViewHolderForAdapterPosition(newIndex) as? ViewHolder else null
        if (newVH != null) {
            val newItem = lista[newIndex]
            animateSelection(newVH.itemView, newItem.activo)
        } else if (newIndex >= 0) {
            notifyItemChanged(newIndex)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val fecha: TextView = view.findViewById(R.id.txtFecha)
        val estado: TextView = view.findViewById(R.id.txtEstado)
        val colorStrip: View = view.findViewById(R.id.colorStrip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventario, parent, false)
        return ViewHolder(view)
    }

    // Binder sin animaciones (estado final)
    private fun bindView(holder: ViewHolder, position: Int) {
        // cancelar animaciones previas para evitar efectos residuales
        holder.itemView.animate().cancel()
        val item = lista[position]
        val context = holder.itemView.context

        holder.nombre.text = item.nombre_inventario
        holder.fecha.text = item.fechaCreacion

        when (item.activo) {
            1 -> {
                holder.estado.text = context.getString(R.string.estado_abierto)
                holder.estado.setTextColor(ContextCompat.getColor(context, R.color.inv_abierto))
            }
            0 -> {
                holder.estado.text = context.getString(R.string.estado_cerrado)
                holder.estado.setTextColor(ContextCompat.getColor(context, R.color.inv_cerrado))
            }
            3 -> {
                holder.estado.text = context.getString(R.string.estado_reabierto)
                holder.estado.setTextColor(ContextCompat.getColor(context, R.color.inv_reabierto))
            }
            else -> {
                holder.estado.text = context.getString(R.string.estado_desconocido)
            }
        }

        val isSelected = item.idinventarios == selectedId

        // Establecer color de la barra lateral según estado (no tocar el fondo)
        val bgColor = when (item.activo) {
            1 -> ContextCompat.getColor(context, R.color.inv_bg_abierto)
            0 -> ContextCompat.getColor(context, R.color.inv_bg_cerrado)
            3 -> ContextCompat.getColor(context, R.color.inv_bg_reabierto)
            else -> ContextCompat.getColor(context, R.color.white)
        }
        holder.colorStrip.setBackgroundColor(bgColor)

        // establecer estado final sin animar (usar solo escala; el indicador de estado es colorStrip)
        holder.itemView.scaleX = if (isSelected) 1.05f else 1f
        holder.itemView.scaleY = if (isSelected) 1.05f else 1f

        // aplicar elevación a la barra lateral si está seleccionado
        val elevationPx = if (isSelected) dpToPx() else 0f
        holder.colorStrip.elevation = elevationPx

        // click handler
        holder.itemView.setOnClickListener {
            // debounce clicks rápidos
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTime < 200L) return@setOnClickListener
            lastClickTime = now
            onClick(item)
        }
    }

    // onBind con payloads: si payloads contiene "selection" animamos la transición; si no, bind normal sin animación
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            bindView(holder, position)
        } else {
            // payloads contiene cambios puntuales (p. ej. selección/deselección)
            val payload = payloads.firstOrNull() as? String

            when (payload) {
                "selection" -> {
                    // Animar a estado seleccionado: escala (sin oscurecer fondo)
                    val estado = lista[position].activo
                    holder.itemView.animate().cancel()
                    holder.itemView.animate().scaleX(1.05f).scaleY(1.05f)
                        .setDuration(animDuration)
                        .setInterpolator(animInterpolator)
                        .start()
                    // marcar visualmente la selección en la barra lateral (oscurecer ligeramente el colorStrip)
                    val stripColor = getBgColorForState(estado, holder.itemView)
                    holder.colorStrip.setBackgroundColor(stripColor)
                    // animar elevación en la barra lateral
                    val toPx = dpToPx()
                    animateElevation(holder.colorStrip, toPx)
                }
                "deselection" -> {
                    // Animar a estado deseleccionado: escala normal + quitar borde
                    val estado = lista[position].activo
                    holder.itemView.animate().cancel()
                    holder.itemView.animate().scaleX(1f).scaleY(1f)
                        .setDuration(animDuration)
                        .setInterpolator(animInterpolator)
                        .start()
                    // restaurar colorStrip al color base
                    val base = getBgColorForState(estado, holder.itemView)
                    holder.colorStrip.setBackgroundColor(base)
                    // quitar elevación animada
                    animateElevation(holder.colorStrip, 0f)
                }
                else -> {
                    // fallback: bindar sin animación
                    bindView(holder, position)
                }
            }
        }
    }

    // Helpers para animar directamente una View (cuando el ViewHolder está visible)
    private fun animateSelection(view: View, estado: Int) {
        view.animate().cancel()
        view.animate().scaleX(1.05f).scaleY(1.05f)
            .setDuration(animDuration)
            .setInterpolator(animInterpolator)
            .start()
        // aplicar efecto visual en la barra lateral si el ViewHolder está visible
        val base = getBgColorForState(estado, view)
        val strip = view.findViewById<View?>(R.id.colorStrip)
        strip?.setBackgroundColor(base)
        // animar elevación a valor seleccionado
        strip?.let { s -> animateElevation(s, dpToPx()) }
    }

    private fun animateDeselection(view: View, estado: Int) {
        view.animate().cancel()
        view.animate().scaleX(1f).scaleY(1f)
            .setDuration(animDuration)
            .setInterpolator(animInterpolator)
            .start()
        // restaurar barra lateral al color base
        val strip2 = view.findViewById<View?>(R.id.colorStrip)
        strip2?.let { s ->
            s.setBackgroundColor(getBgColorForState(estado, view))
            // quitar elevación
            animateElevation(s, 0f)
        }
    }

    private fun getBgColorForState(activo: Int, view: View): Int {
        val context = view.context
        return when (activo) {
            1 -> ContextCompat.getColor(context, R.color.inv_bg_abierto)
            0 -> ContextCompat.getColor(context, R.color.inv_bg_cerrado)
            3 -> ContextCompat.getColor(context, R.color.inv_bg_reabierto)
            else -> ContextCompat.getColor(context, R.color.white)
        }
    }

    // Mantener override sin payloads para compatibilidad
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun getItemCount() = lista.size

    // convierte selectedElevationDp (dp) a px usando displayMetrics del sistema
    private fun dpToPx(): Float {
        return selectedElevationDp * Resources.getSystem().displayMetrics.density
    }

    // anima la elevación (sombra) de una vista hacia un valor en px
    private fun animateElevation(view: View, toPx: Float) {
        val from = view.elevation
        if (from == toPx) return
        val va = ValueAnimator.ofFloat(from, toPx)
        va.duration = animDuration
        va.interpolator = animInterpolator
        va.addUpdateListener { animation ->
            val v = (animation.animatedValue as Float)
            view.elevation = v
        }
        va.start()
    }
}
