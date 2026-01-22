package com.example.inventario20.ui.iniciar

import android.annotation.SuppressLint
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

            val dbHelper = DBHelper(requireContext())

            val fechaActual = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

            // IMPORTANTE:
            // Opcional: cerrar inventarios previos activos (por seguridad)
            val inventarioActivo = dbHelper.obtenerInventarioActivo()
            if (inventarioActivo != null) {
                dbHelper.cerrarInventario(inventarioActivo, fechaActual)
            }

            // Crear nuevo inventario activo
            val nuevoId = dbHelper.insertarInventario(
                nombreInventario = "Inventario ${fechaActual}",
                fechaCreacion = fechaActual,
                activo = 1
            )

            if (nuevoId != -1L) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_main, HomeFragment())
                    .commit()

            } else {
                Toast.makeText(
                    context,
                    "Error al crear inventario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }





        return root
    }

}


