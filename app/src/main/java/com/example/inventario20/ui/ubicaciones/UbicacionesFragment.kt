package com.example.inventario20.ui.ubicaciones

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.inventario20.DBHelper
import com.example.inventario20.databinding.FragmentUbicacionesBinding

class UbicacionesFragment : Fragment() {

    private var _binding: FragmentUbicacionesBinding? = null

    private lateinit var dbHelper: DBHelper

    private var empresaSeleccionada: Int = 0

    private lateinit var botonesEmpresa: List<TextView>

    private var botonSeleccionado: TextView? = null

    private var ubicacionesOriginal = mutableListOf<DBHelper.Ubicacion>()
    private var ubicacionesFiltradas = mutableListOf<DBHelper.Ubicacion>()

    private var ubicacionSeleccionada: DBHelper.Ubicacion? = null

    private var itemSeleccionadoIndex: Int = -1

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dbHelper = DBHelper(requireContext())

        val ubicacionesViewModel =
            ViewModelProvider(this).get(UbicacionesViewModel::class.java)

        _binding = FragmentUbicacionesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val ubicacionAgrimexBTN = binding.ubicacionAgrimexBTN
        val ubicacionCosmarBTN = binding.ubicacionCosmarBTN
        val ubicacionMextlanBTN = binding.ubicacionMextlanBTN
        val guardarUbicacionBTN = binding.guardarUbicacionBTN


        botonesEmpresa = listOf(ubicacionAgrimexBTN, ubicacionCosmarBTN, ubicacionMextlanBTN)

        val listView = binding.ubicasionLIST
        ubicacionesOriginal = dbHelper.obtenerUbicaciones().toMutableList()
        ubicacionesFiltradas = ubicacionesOriginal.toMutableList()

        val adapter = UbicacionesAdapter(requireContext(), ubicacionesFiltradas)
        listView.adapter = adapter

        fun filtrarPorEmpresa(idEmpresa: Int){
            ubicacionesFiltradas.clear()
            ubicacionesFiltradas.addAll(
                ubicacionesOriginal.filter { it.idempresas == idEmpresa }
            )
            adapter.notifyDataSetChanged()
        }

        fun quitarFiltro(){
            ubicacionesFiltradas.clear()
            ubicacionesFiltradas.addAll(ubicacionesOriginal)
            adapter .notifyDataSetChanged()
        }

        fun manejarSeleccion(boton: TextView, idEmpresa: Int){

            if (botonSeleccionado == boton){
                boton.setBackgroundColor(Color.DKGRAY)
                botonSeleccionado = null
                empresaSeleccionada = 0
                quitarFiltro()
                return
            }

            botonesEmpresa.forEach { it.setBackgroundColor(Color.DKGRAY) }
            boton.setBackgroundColor(Color.BLUE)
            empresaSeleccionada = idEmpresa
            botonSeleccionado = boton
            filtrarPorEmpresa(idEmpresa)
        }


        ubicacionAgrimexBTN .setOnClickListener { manejarSeleccion(ubicacionAgrimexBTN,1) }
        ubicacionCosmarBTN .setOnClickListener { manejarSeleccion(ubicacionCosmarBTN,2) }
        ubicacionMextlanBTN .setOnClickListener { manejarSeleccion(ubicacionMextlanBTN,3) }
        var empresaOriginalUbicacion: Int? = null

        fun seleccionarUbicacion(ubicacion: DBHelper.Ubicacion) {
            ubicacionSeleccionada = ubicacion
            empresaOriginalUbicacion = ubicacion.idempresas

            binding.ubicacionEDTXT.setText(ubicacion.ubicacion)

            botonesEmpresa.forEach { it.setBackgroundColor(Color.DKGRAY) }
            val boton = when (ubicacion.idempresas){
                1 -> ubicacionAgrimexBTN
                2 -> ubicacionCosmarBTN
                3 -> ubicacionMextlanBTN
                else -> null
            }
            boton?.setBackgroundColor(Color.BLUE)
            botonSeleccionado = boton
            empresaSeleccionada = ubicacion.idempresas
        }


        listView.setOnItemClickListener {parent, view, position,     id ->
                val seleccionado = ubicacionesFiltradas[position]

                // Guardar selección
                ubicacionSeleccionada = seleccionado
                (listView.adapter as UbicacionesAdapter).setSelectedIndex(position)
                itemSeleccionadoIndex = position

                // Cargar en los EditText
            seleccionarUbicacion(seleccionado)

                // Cambiar color del item seleccionado
                (listView.adapter as UbicacionesAdapter).setSelectedIndex(position)
                Toast.makeText(requireContext(), "Seleccionaste: ${seleccionado.ubicacion}", Toast.LENGTH_SHORT).show()
        }




        guardarUbicacionBTN.setOnClickListener {

            val ubicacion = binding.ubicacionEDTXT.text.toString().trim()

            // 1️⃣ Validar empresa
            if (empresaSeleccionada == 0) {
                Toast.makeText(requireContext(), "Seleccione una empresa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2️⃣ Validar ubicación
            if (ubicacion.isEmpty()) {
                Toast.makeText(requireContext(), "Ingrese una ubicación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3️⃣ Validar duplicado (misma empresa)
            val existe = ubicacionesOriginal.any {
                it.ubicacion.equals(ubicacion, ignoreCase = true) &&
                        it.idempresas == empresaSeleccionada &&
                        it.idubicacion != ubicacionSeleccionada?.idubicacion
            }

            if (existe) {
                Toast.makeText(
                    requireContext(),
                    "La ubicación ya existe para la empresa seleccionada",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 4️⃣ ACTUALIZAR
            if (ubicacionSeleccionada != null) {

                // 🔒 BLOQUEAR CAMBIO DE EMPRESA
                if (empresaSeleccionada != empresaOriginalUbicacion) {
                    Toast.makeText(
                        requireContext(),
                        "No puedes cambiar la empresa de una ubicación existente",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val rows = dbHelper.actualizarUbicacion(
                    ubicacionSeleccionada!!.idubicacion,
                    ubicacion,
                    empresaSeleccionada
                )

                if (rows > 0) {
                    Toast.makeText(requireContext(), "Ubicación actualizada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar la ubicación", Toast.LENGTH_SHORT).show()
                }

                // Limpiar selección
                ubicacionSeleccionada = null
                empresaOriginalUbicacion = null
                binding.ubicacionEDTXT.text.clear()
                actualizarListaUbicaiones()
                (listView.adapter as UbicacionesAdapter).setSelectedIndex(-1)
                filtrarPorEmpresa(empresaSeleccionada)

                return@setOnClickListener
            }

            // 5️⃣ INSERTAR (solo si NO hay ubicación seleccionada)
            val exito = dbHelper.insertarUbicacion(ubicacion, empresaSeleccionada)

            if (exito > 0) {
                Toast.makeText(requireContext(), "Ubicación guardada correctamente", Toast.LENGTH_SHORT).show()
                binding.ubicacionEDTXT.text.clear()
                actualizarListaUbicaiones()
                filtrarPorEmpresa(empresaSeleccionada)
            } else {
                Toast.makeText(requireContext(), "Error al guardar la ubicación", Toast.LENGTH_SHORT).show()
            }
        }




        listView.setOnItemLongClickListener { parent, view, position, id ->
            val seleccionado = ubicacionesFiltradas[position]

            //Mostrar Confirmación
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirmación")
                .setMessage("¿Deseas eliminar la ubicación: ${seleccionado.ubicacion}?")
                .setPositiveButton("Sí") { dialog, which ->
                    val rows = dbHelper.eliminarUbicacion(seleccionado.idubicacion)
                    if (rows > 0) {
                        Toast.makeText(requireContext(), "Ubicación eliminada", Toast.LENGTH_SHORT).show()
                        actualizarListaUbicaiones()
                    } else {
                        Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
            true
        }




        return root
    }




    private fun actualizarListaUbicaiones() {
       val listView = binding.ubicasionLIST
        ubicacionesOriginal.clear()
        ubicacionesOriginal.addAll(dbHelper.obtenerUbicaciones().sortedBy { it.idempresas })
        ubicacionesFiltradas.clear()
        ubicacionesFiltradas.addAll(ubicacionesOriginal)
        (listView.adapter as UbicacionesAdapter).notifyDataSetChanged()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}