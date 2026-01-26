package com.example.inventario20.ui.iniciar

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.inventario20.DBHelper
import com.example.inventario20.R
import com.example.inventario20.databinding.FragmentHomeBinding
import com.example.inventario20.databinding.FragmentIniciarInventarioBinding
import com.example.inventario20.ui.home.HomeFragment
import com.example.inventario20.ui.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IniciarInventarioFragment : Fragment() {

    private var _binding: FragmentIniciarInventarioBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentIniciarInventarioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val IniciarInventarioBTN = binding.IniciarInventarioBTN

        IniciarInventarioBTN.setOnClickListener {

            val nombreInventario = binding.NombreInvEDTXT.text.toString().trim()

            // 1️⃣ Validar nombre vacío
            if (nombreInventario.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Por favor escribe un nombre para el inventario",
                    Toast.LENGTH_SHORT
                ).show()
                binding.NombreInvEDTXT.requestFocus()
                return@setOnClickListener
            }

            // 2️⃣ Confirmar apertura de inventario
            AlertDialog.Builder(requireContext())
                .setTitle("Crear inventario")
                .setMessage("¿Estás seguro de que deseas Crear el inventario \"$nombreInventario\"?")
                .setPositiveButton("Sí, Crear") { _, _ ->

                    val dbHelper = DBHelper(requireContext())
                    val fechaActual = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date())

                    // Opcional: cerrar inventario activo previo
                    val inventarioActivo = dbHelper.obtenerInventarioActivo()
                    if (inventarioActivo != null) {
                        dbHelper.cerrarInventario(inventarioActivo, fechaActual)
                    }

                    // Crear nuevo inventario
                    val nuevoId = dbHelper.insertarInventario(
                        nombreInventario = nombreInventario,
                        fechaCreacion = fechaActual,
                        activo = 1
                    )

                    if (nuevoId != -1L) {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container_main, HomeFragment())
                            .commit()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al crear inventario",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }






        return root
    }

}


