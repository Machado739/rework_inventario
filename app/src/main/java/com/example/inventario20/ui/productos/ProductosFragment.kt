package com.example.inventario20.ui.productos

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
import com.example.inventario20.databinding.FragmentProductosBinding
import kotlin.collections.addAll
import kotlin.text.clear

class ProductosFragment : Fragment() {

    private var _binding: FragmentProductosBinding? = null
    private lateinit var dbHelper: DBHelper
    private var proveedorSeleccionado: String = ""

    private lateinit var botonesProveedor: List<TextView>

    private var botonSeleccionado: Button? = null

    private var codigosOriginal = mutableListOf<DBHelper.Codigo>()
    private var codigosFiltrados = mutableListOf<DBHelper.Codigo>()


    private var productoSeleccionado: DBHelper.Codigo? = null

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
        val productosViewModel =
            ViewModelProvider(this).get(ProductosViewModel::class.java)

        _binding = FragmentProductosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val productoCoastalBTN = binding.productoCoastalBTN
        val productoSakuraBTN = binding.productoSakuraBTN
        val productoMuranakaBTN = binding.productoMuranakaBTN
        val productoAgregarBTN = binding.productoAgregarBTN

        botonesProveedor = listOf(productoCoastalBTN, productoSakuraBTN, productoMuranakaBTN)

        val listView = binding.productoLIST
         codigosOriginal = dbHelper.obtenerCodigos().sortedBy { it.idproducto }.toMutableList()
         codigosFiltrados = codigosOriginal.toMutableList()

        val adapter = CodigoAdapter(requireContext(), codigosFiltrados)
        listView.adapter = adapter

        // filtro de productos por prefijo
        fun filtrarPorPrefix(prefijo: String) {
            codigosFiltrados.clear()
            codigosFiltrados.addAll(
                codigosOriginal.filter { it.idproducto.contains(prefijo, ignoreCase = true) }
            )
            adapter.notifyDataSetChanged()
        }
        // quitar filtro
        fun quitarFiltro() {
            codigosFiltrados.clear()
            codigosFiltrados.addAll(codigosOriginal)
            adapter.notifyDataSetChanged()
        }
        // manejar seleccion de botones
        fun manejarSeleccion(boton: Button, prefix: String) {

            // Caso 1: el botón ya estaba seleccionado → deseleccionar y quitar filtro
            if (botonSeleccionado == boton) {
                boton.setBackgroundColor(Color.DKGRAY)
                botonSeleccionado = null
                quitarFiltro()
                return
            }

            // Caso 2: seleccionar nuevo botón
            botonesProveedor.forEach { it.setBackgroundColor(Color.DKGRAY) }
            boton.setBackgroundColor(Color.BLUE)

            botonSeleccionado = boton
            filtrarPorPrefix(prefix)
        }


        productoCoastalBTN.setOnClickListener { manejarSeleccion(productoCoastalBTN, "CF") }
        productoSakuraBTN.setOnClickListener { manejarSeleccion(productoSakuraBTN, "RF") }
        productoMuranakaBTN.setOnClickListener { manejarSeleccion(productoMuranakaBTN, "MK") }

        // evento lista de productos
        listView.setOnItemClickListener { parent, view, position, id ->
            val seleccionado = codigosFiltrados[position]

            // Guardar selección
            productoSeleccionado = seleccionado
            (listView.adapter as CodigoAdapter).setSelectedIndex(position)
            itemSeleccionadoIndex = position

            // Cargar en los EditText
            binding.productoCodigoEDTXT.setText(seleccionado.idproducto)
            binding.productoNombreEDTXT.setText(seleccionado.producto)
            binding.productoUnidadEDTXT.setText(seleccionado.medida)

            // Cambiar color del item seleccionado
            (listView.adapter as CodigoAdapter).setSelectedIndex(position)

            Toast.makeText(requireContext(), "Seleccionaste: ${seleccionado.producto}", Toast.LENGTH_SHORT).show()
        }

        // evento agregar producto
        productoAgregarBTN.setOnClickListener {
            val codigoProducto = binding.productoCodigoEDTXT.text.toString()
            val nombreProducto = binding.productoNombreEDTXT.text.toString()
            val unidadProducto = binding.productoUnidadEDTXT.text.toString()
            var empresaOriginalUbicacion: Int? = null

            // Validaciones
            if(codigoProducto.isBlank() || nombreProducto.isBlank() || unidadProducto.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (productoSeleccionado != null) {
                // Actualizar
                val rows = dbHelper.actualizarCodigo(codigoProducto, nombreProducto, unidadProducto)
                if (rows > 0) {
                    binding.productoCodigoEDTXT.text.clear()
                    binding.productoNombreEDTXT.text.clear()
                    binding.productoUnidadEDTXT.text.clear()
                    Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
                productoSeleccionado = null // Limpiamos selección
                actualizarListaProductos()
                (listView.adapter as CodigoAdapter).setSelectedIndex(-1)
            } else {

                if(codigosOriginal.any { it.idproducto == codigoProducto }) {
                    Toast.makeText(requireContext(), "El código de producto ya existe", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Insertar en la base de datos
                val exito = dbHelper.insertarCodigo(codigoProducto, nombreProducto, unidadProducto)
                if (exito > 0) {
                    Toast.makeText(
                        requireContext(),
                        "Producto agregado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Limpiar campos
                    binding.productoCodigoEDTXT.text.clear()
                    binding.productoNombreEDTXT.text.clear()
                    binding.productoUnidadEDTXT.text.clear()
                    // Actualizar listas
                    actualizarListaProductos()
                    quitarFiltro()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al agregar el producto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

        listView.setOnItemLongClickListener { parent, view, position, id ->
            val seleccionado = codigosOriginal[position]

            // Mostrar diálogo de confirmación
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Eliminar producto")
                .setMessage("¿Deseas eliminar ${seleccionado.producto}?")
                .setPositiveButton("Sí") { dialog, _ ->
                    // Eliminar de la base de datos
                    val exito = dbHelper.eliminarCodigo(seleccionado.idproducto)
                    if (exito > 0) {
                        Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                        // Actualizar lista
                        actualizarListaProductos()
                    } else {
                        Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

            true // indica que el evento long click fue manejado
        }






        return root
    }

    private fun actualizarListaProductos() {
        val listView = binding.productoLIST
        codigosOriginal.clear()
        codigosOriginal.addAll(dbHelper.obtenerCodigos().sortedBy { it.idproducto })
        codigosFiltrados.clear()
        codigosFiltrados.addAll(codigosOriginal)
        (listView.adapter as CodigoAdapter).notifyDataSetChanged()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}