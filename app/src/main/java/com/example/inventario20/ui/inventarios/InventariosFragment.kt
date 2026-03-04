package com.example.inventario20.ui.inventarios

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventario20.DBHelper
import com.example.inventario20.R
import com.example.inventario20.databinding.FragmentInventariosBinding
import com.example.inventario20.ui.home.HomeFragment
import java.util.Date
import java.util.Locale
import kotlin.toString
import androidx.core.view.isVisible


class InventariosFragment : Fragment() {
    private val PASSWORD_REABRIR = "222431"

    private var _binding: FragmentInventariosBinding? = null
    private var idInventarioSeleccionado: Int? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val inventariosViewModel =
            ViewModelProvider(this)[InventariosViewModel::class.java]

        _binding = FragmentInventariosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val accionBTN = binding.accionBTN

        cargarInventarios()

        actualizarEstadoBoton()

        accionBTN.setOnClickListener {
            accionInventario()
        }


        val registrosBTN = binding.registrosBTN

        registrosBTN.setOnClickListener {
            val id = idInventarioSeleccionado ?: return@setOnClickListener

            cargarRegistros(id)
            if (binding.rvInventarios.isVisible) {
                binding.rvInventarios.visibility = View.GONE
                binding.rvRegistros.visibility = View.VISIBLE
                binding.registrosBTN.text="Ocultar Registros"

            } else {
                binding.rvInventarios.visibility = View.VISIBLE
                binding.rvRegistros.visibility = View.GONE
                binding.registrosBTN.text="Ver Registros"
            }

        }




        return root
    }




    private fun accionInventario() {
        val dbHelper = DBHelper(requireContext())
        val idActivo = dbHelper.obtenerInventarioActivo()

        when {
            idActivo != null -> {
                confirmarCierreInventario()
            }
            idInventarioSeleccionado != null -> {
                reabrirInventario()
            }
            else -> {
                Toast.makeText(requireContext(), "No hay inventario disponible", Toast.LENGTH_SHORT).show()
            }
        }

        actualizarEstadoBoton()
    }



    private fun cerrarInventarioActual() {
        val dbHelper = DBHelper(requireContext())
        val idActivo = dbHelper.obtenerInventarioActivo()

        if (idActivo == null) {
            Toast.makeText(
                requireContext(),
                "No hay inventario activo",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        dbHelper.cerrarInventario(
            idActivo,
            obtenerFechaActual()
        )

        Toast.makeText(
            requireContext(),
            "Inventario cerrado",
            Toast.LENGTH_SHORT
        ).show()
        cargarInventarios()

    }

    private fun cargarTotalRegistros(idInventario: Int){
        val dbHelper = DBHelper(requireContext())
        val total = dbHelper.contarRegistrosPorInventario(idInventario)

        binding.CantidadRegTXT.animate()
            .alpha(0f)
            .setDuration(100)
            .withEndAction {
                binding.CantidadRegTXT.text = "Cant Registros: $total"
                binding.CantidadRegTXT.animate()
                    .alpha(1f)
                    .setDuration(100)
                    .start()

            }
            .start()
    }
    private fun reabrirInventario() {

        val dialogView = layoutInflater.inflate(R.layout.dialog_password, null)
        val etPassword = dialogView.findViewById<android.widget.EditText>(R.id.etPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Reabrir inventario")
            .setView(dialogView)
            .setPositiveButton("Confirmar") { _, _ ->

                val passwordIngresada = etPassword.text.toString()

                if (passwordIngresada != PASSWORD_REABRIR) {
                    Toast.makeText(
                        requireContext(),
                        "Contraseña incorrecta",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val id = idInventarioSeleccionado ?: return@setPositiveButton
                val dbHelper = DBHelper(requireContext())

                if (dbHelper.obtenerInventarioActivo() != null) {
                    Toast.makeText(
                        requireContext(),
                        "Ya hay un inventario activo",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                dbHelper.reabrirInventario(id)

                Toast.makeText(
                    requireContext(),
                    "Inventario reabierto",
                    Toast.LENGTH_SHORT
                ).show()

                cargarInventarios()
                actualizarEstadoBoton()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarCierreInventario() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar inventario")
            .setMessage("¿Estás seguro de cerrar el inventario actual?")
            .setPositiveButton("Sí") { _, _ ->
                cerrarInventarioActual()
                actualizarEstadoBoton()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarEstadoBoton() {
        val dbHelper = DBHelper(requireContext())
        val accionBTN = binding.accionBTN
        accionBTN.text = when {
            dbHelper.obtenerInventarioActivo() != null -> "Cerrar inventario"
            idInventarioSeleccionado != null -> "Reabrir inventario"
            else -> "Inventario no disponible"
        }
    }

    private fun estadoTexto(estado: Int): String =
        when (estado) {
            DBHelper.EstadoInventario.ABIERTO -> "ABIERTO"
            DBHelper.EstadoInventario.CERRADO -> "CERRADO"
            DBHelper.EstadoInventario.REABIERTO -> "REABIERTO"
            else -> "DESCONOCIDO"
        }

    private fun cargarInventarios() {
        val dbHelper = DBHelper(requireContext())
        val lista = dbHelper.obtenerTodosLosInventarios() // <- ESTE MeTODO DEBE EXISTIR

        Toast.makeText(
            requireContext(),
            "Inventarios: ${lista.size}",
            Toast.LENGTH_SHORT
        ).show()

        val adapter = InventarioAdapter(lista) { inventario ->
            idInventarioSeleccionado = inventario.idinventarios
            mostrarInventario(inventario)
            actualizarEstadoBoton()
        }

        binding.rvInventarios.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvInventarios.adapter = adapter
    }

    private fun cargarRegistros(inventarioId: Int) {

        val dbHelper = DBHelper(requireContext())
        val registros = dbHelper.obtenerRegistrosPorInventarioNombre(inventarioId)

        val adapter = InventarioRegistroAdapter(registros) { registro ->
            // Acción al hacer clic en un registro (si es necesario)
        }

        binding.rvRegistros.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvRegistros.adapter = adapter
    }

    private fun mostrarInventario(inventario: DBHelper.InventarioItem) {

        binding.NomInvTXT.animate()
            .alpha(0f)
            .setDuration(100)
            .withEndAction {
                binding.NomInvTXT.text = inventario.nombre_inventario
                binding.NomInvTXT.animate()
                    .alpha(1f)
                    .setDuration(100)
                    .start()
            }
            .start()

        binding.FechaHoraInvTXT.animate()
            .alpha(0f)
            .setDuration(100)
            .withEndAction {
                binding.FechaHoraInvTXT.text =
                    if (inventario.fechaCierre == null)
                        "Creado: ${inventario.fechaCreacion}\nCerrado: "
                    else
                        "Creado: ${inventario.fechaCreacion}\nCerrado: ${inventario.fechaCierre}"
                binding.FechaHoraInvTXT.animate()
                    .alpha(1f)
                    .setDuration(100)
                    .start()
            }
            .start()

        binding.EstatusTXT.animate()
            .alpha(0f)
            .setDuration(100)
            .withEndAction {
                binding.EstatusTXT.text = estadoTexto(inventario.activo)
                binding.EstatusTXT.animate()
                    .alpha(1f)
                    .setDuration(100)
                    .start()
            }
            .start()

        cargarTotalRegistros(inventario.idinventarios)

    }

    fun obtenerFechaActual(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}