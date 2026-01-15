package com.example.inventario20.ui.exportacion

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventario20.DBHelper
import com.example.inventario20.databinding.FragmentExportacionBinding

class ExportacionFragment : Fragment() {

    private var _binding: FragmentExportacionBinding? = null
    private val binding get() = _binding!!

    private lateinit var spinnerTablas: Spinner
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1️⃣ Inicializar DBHelper PRIMERO
        dbHelper = DBHelper(requireContext())

        // 2️⃣ Inicializar Spinner
        spinnerTablas = binding.spinnerTablas

        // 3️⃣ Cargar tablas en Spinner
        cargarTablasEnSpinner()

        // 4️⃣ Listener DESPUÉS de todo lo anterior
        spinnerTablas.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val tablaSeleccionada =
                        parent?.getItemAtPosition(position).toString()

                    cargarDatosDeTabla(tablaSeleccionada)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }



    }

    private fun cargarTablasEnSpinner() {
        val tablas = listOf(
            "Inventarios",
            "Registros",
            "Registros_Inventario",
            "Ubicaciones",
            "Empresas",
            "Codigos",
            "Cliente"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tablas
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerTablas.adapter = adapter
    }

    private fun cargarDatosDeTabla(tabla: String) {
        val datos = dbHelper.obtenerDatosTabla(tabla)

        Log.d("EXPORTACION", "Tabla: $tabla | Filas: ${datos.size}")

        if (datos.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "La tabla $tabla no tiene datos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (datos.isNotEmpty()) {
            binding.tableLayout.removeAllViews()

            val columnas = datos[0].keys.toList()
            crearHeader(columnas)
            crearFilas(columnas, datos)
        }
    }

    private fun crearHeader(columnas: List<String>) {
        val headerRow = TableRow(requireContext())

        columnas.forEach { columna ->
            val tv = TextView(requireContext()).apply {
                text = columna
                setPadding(16, 8, 16, 8)
                setTypeface(null, Typeface.BOLD)
            }
            headerRow.addView(tv)
        }

        binding.tableLayout.addView(headerRow)
    }

    private fun crearFilas(columnas: List<String>, datos: List<Map<String, Any>>) {
        datos.forEach { fila ->
            val tableRow = TableRow(requireContext())

            columnas.forEach { columna ->
                val tv = TextView(requireContext()).apply {
                    text = fila[columna]?.toString() ?: ""
                    setPadding(16, 8, 16, 8)
                }
                tableRow.addView(tv)
            }

            binding.tableLayout.addView(tableRow)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
