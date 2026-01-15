package com.example.inventario20.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.inventario20.R
import com.example.inventario20.databinding.FragmentHomeBinding
import androidx.core.view.isVisible
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null


    private var empresaSeleccionada: String = ""
    private var proveedorSeleccionado: String = ""

    private lateinit var botonesEmpresa: List<Button>
    private lateinit var botonesProveedor: List<Button>


    private lateinit var sueltoBTN: Button
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root









        val mextlanBTN = binding.mextlanBTN
        val cosmarBTN = binding.cosmarBTN
        val agrimexBTN = binding.agrimexBTN

        val coastalBTN = binding.coastalBTN
        val rainfieldBTN = binding.rainfieldBTN
        val muranakaBTN = binding.muranakaBTN

        //botones empresa y proveedor
        botonesProveedor = listOf(coastalBTN, rainfieldBTN, muranakaBTN)
        botonesEmpresa = listOf(cosmarBTN,mextlanBTN, agrimexBTN)

        configurarEditText(binding.tarimasEDTXT)
        configurarEditText(binding.piezasEDTXT)
        configurarEditText(binding.unidadesEDTXT)

        // Escucha los toques fuera de los EditText
        root.setOnTouchListener { _, _ ->
            ocultarTeclado()
            false
        }
        // Evento para mostrar el layout de suelto
        binding.sueltoBTN.setOnClickListener { viewSuelto() }



        // configurrar botones empresa
        fun seleccionarEmpresa(boton: Button, nombreEmpresa: String) {
            // Reiniciar el color de todos los botones
            botonesEmpresa.forEach { it.setBackgroundColor(Color.DKGRAY) }
            // Cambiar el color del botón seleccionado
            boton.setBackgroundColor(Color.BLUE)
            empresaSeleccionada = nombreEmpresa
        }

        //configurar botones proveedor
        fun seleccionarProveedor(boton: Button, nombreProveedor: String) {
            // Reiniciar el color de todos los botones
            botonesProveedor.forEach { it.setBackgroundColor(Color.DKGRAY) }
            // Cambiar el color del botón seleccionado
            boton.setBackgroundColor(Color.BLUE)
            proveedorSeleccionado = nombreProveedor
        }
        mextlanBTN.setOnClickListener { seleccionarEmpresa(mextlanBTN,"mextlan") }
        cosmarBTN.setOnClickListener { seleccionarEmpresa(cosmarBTN,"cosmar") }
        agrimexBTN.setOnClickListener { seleccionarEmpresa(agrimexBTN,"agrimex") }

        coastalBTN.setOnClickListener { seleccionarProveedor(coastalBTN,"coastal") }
        rainfieldBTN.setOnClickListener { seleccionarProveedor(rainfieldBTN,"rainfield") }
        muranakaBTN.setOnClickListener { seleccionarProveedor(muranakaBTN,"muranaka") }


        return root
    }

    override fun onResume() {
        super.onResume()

        val activity = activity as AppCompatActivity
        val toolbar = activity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        // Mantiene el botón hamburguesa
        activity.supportActionBar?.setDisplayShowTitleEnabled(false)
        activity.supportActionBar?.setDisplayShowCustomEnabled(true)

        // Inflamos nuestro layout
        val customView = layoutInflater.inflate(R.layout.toolbar_home, null)
        customView.findViewById<TextView>(R.id.tvRegistro).text = "Registro: 1"
        customView.findViewById<TextView>(R.id.tvNombre).text = "Sin nombre"

        // Lo agregamos sin quitar el botón
        activity.supportActionBar?.customView = customView





    }
    override fun onStop() {
        super.onStop()
        val activity = activity as AppCompatActivity
        activity.supportActionBar?.setDisplayShowCustomEnabled(false)
        activity.supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isInventarioAbierto(): Boolean {
        val prefs = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val iniciado = prefs.getBoolean("inventario_iniciado", false)
        val id = prefs.getLong("inventario_id_activo", -1)

        return iniciado && id != -1L
    }




    private fun viewSuelto()
    {
        if (binding.sueltoLayout.isVisible)
        {
            binding.sueltoSPC1.visibility=View.VISIBLE
            binding.sueltoSPC2.visibility=View.VISIBLE
            binding.sueltoLayout.visibility=View.GONE
            return
        }else if (binding.sueltoLayout.isGone)
        {
            binding.sueltoSPC1.visibility=View.GONE
            binding.sueltoSPC2.visibility=View.GONE
            binding.sueltoLayout.visibility=View.VISIBLE
            return
        }

    }

    private fun configurarEditText(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && editText.text.toString() == "1") {
                editText.text.clear() // Borra el "1" al tocar
            } else if (!hasFocus && editText.text.isEmpty()) {
                editText.setText("1") // Si el usuario no escribe nada, vuelve a "1"
            }
        }
    }

    private fun ocultarTeclado() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val view = requireActivity().currentFocus
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }





}

