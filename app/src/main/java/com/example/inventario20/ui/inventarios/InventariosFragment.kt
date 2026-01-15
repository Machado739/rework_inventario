package com.example.inventario20.ui.inventarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.inventario20.DBHelper
import com.example.inventario20.databinding.FragmentInventariosBinding
import java.util.Date
import java.util.Locale

class InventariosFragment : Fragment() {

    private var _binding: FragmentInventariosBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inventariosViewModel =
            ViewModelProvider(this).get(InventariosViewModel::class.java)

        _binding = FragmentInventariosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val accionBTN = binding.accionBTN

        accionBTN.setOnClickListener {
            // Acción al hacer clic en el botón
        }




        return root
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