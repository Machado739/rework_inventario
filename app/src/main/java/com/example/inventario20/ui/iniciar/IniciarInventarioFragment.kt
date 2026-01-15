package com.example.inventario20.ui.iniciar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.core.content.edit
import com.example.inventario20.DBHelper
import com.example.inventario20.MainActivity
import com.example.inventario20.R
import java.util.Date
import java.util.Locale

class IniciarInventarioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_iniciar, container, false)

        root.findViewById<Button>(R.id.iniciarBTN).setOnClickListener {
            iniciarInventario()
        }

        return root
    }

    private fun iniciarInventario() {
        val dbHelper = DBHelper(requireContext())

        val inventarioActivo = dbHelper.obtenerInventarioActivo()

        if (inventarioActivo != null) {
            Toast.makeText(
                requireContext(),
                "Ya hay un inventario abierto",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val fecha = obtenerFechaActual()

        dbHelper.insertarInventario(
            nombreInventario = "Inventario ${fecha}",
            fechaCreacion = fecha,
            activo = 1
        )

        (requireActivity() as MainActivity).invalidateOptionsMenu()
        findNavController().navigate(R.id.nav_home)
    }
    fun obtenerFechaActual(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }


}
