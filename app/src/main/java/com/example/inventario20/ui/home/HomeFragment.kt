package com.example.inventario20.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.inventario20.R
import com.example.inventario20.databinding.FragmentHomeBinding
import androidx.core.view.isVisible
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventario20.DBHelper
import com.example.inventario20.DBHelper.Registro



class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var registroSeleccionado: Registro? = null

    private var idInventarioActivo: Int = -1
    private lateinit var registroAdapter: RegistroAdapter
    private lateinit var autoCompleteAdapter: ArrayAdapter<DBHelper.Codigo>


    private var idEmpresa: Int = -1 // Variable para almacenar la empresa seleccionada
    private var idProveedor: Int = -1 // Variable para almacenar el proveedor seleccionado
    private var totalProducto: Int = 1

    private lateinit var botonesEmpresa: List<Button>
    private lateinit var botonesProveedor: List<Button>



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

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
        configurarEditText(binding.cajasEDTXT)
        configurarSueltoEditText(binding.sueltoEDTXT)
        // Escucha los toques fuera de los EditText
        root.setOnTouchListener { _, _ ->
            ocultarTeclado()
            false
        }
        // Evento para mostrar el layout de suelto
        binding.sueltoBTN.setOnClickListener { viewSuelto() }



        // configurrar botones empresa
        fun seleccionarEmpresa(boton: Button, seleccionEmp: Int) {
            // Reiniciar el color de todos los botones
            botonesEmpresa.forEach { it.setBackgroundColor(Color.DKGRAY) }
            // Cambiar el color del botón seleccionado
            boton.setBackgroundColor(Color.BLUE)
            idEmpresa = seleccionEmp
            actualizarUbicaciones() // Actualiza las ubicaciones al seleccionar una empresa
        }

        //configurar botones proveedor
        fun seleccionarProveedor(boton: Button, seleccionPro: Int) {
            // Reiniciar el color de todos los botones
            botonesProveedor.forEach { it.setBackgroundColor(Color.DKGRAY) }
            // Cambiar el color del botón seleccionado
            boton.setBackgroundColor(Color.BLUE)
            idProveedor = seleccionPro
        }
        mextlanBTN.setOnClickListener { seleccionarEmpresa(mextlanBTN,3) }
        cosmarBTN.setOnClickListener { seleccionarEmpresa(cosmarBTN,2) }
        agrimexBTN.setOnClickListener { seleccionarEmpresa(agrimexBTN,1) }

        coastalBTN.setOnClickListener { seleccionarProveedor(coastalBTN,2) }
        rainfieldBTN.setOnClickListener { seleccionarProveedor(rainfieldBTN,3) }
        muranakaBTN.setOnClickListener { seleccionarProveedor(muranakaBTN,1) }


        val listcodigos: AutoCompleteTextView = root.findViewById(R.id.AutoCompleteListaCodigos)
    // Configurar el evento de clic para mostrar la lista completa
        listcodigos.setOnClickListener {
            actualizarAutoCompleteTextView() // Actualiza los datos antes de mostrar
            listcodigos.showDropDown() // Forzar la apertura del dropdown

        }


        listcodigos.setOnItemClickListener { parent: AdapterView<*>, _: View, position: Int, _: Long ->
            // Cuando el usuario selecciona un elemento, actualizamos el nombre en toolbar
            cargarNombreProducto()
            // Ocultamos el teclado y quitamos el foco del AutoCompleteTextView
            ocultarTeclado()
            listcodigos.clearFocus()
        }



        // Configurar el evento de enfoque para mostrar la lista completa
        listcodigos.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                actualizarAutoCompleteTextView() // Actualiza los datos antes de mostrar
                listcodigos.showDropDown() // Mostrar el dropdown
            }
        }



        // Calcular el total cuando cambien los valores
        val watcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val tarimas = binding.tarimasEDTXT.text.toString().toIntOrNull() ?: 1
                val cajas = binding.cajasEDTXT.text.toString().toIntOrNull() ?: 1
                val piezas = binding.piezasEDTXT.text.toString().toIntOrNull() ?: 1
                val suelto = binding.sueltoEDTXT.text.toString().toIntOrNull() ?: 0
                calcularTotal(tarimas, cajas, piezas, suelto)
            }
        }

        binding.tarimasEDTXT.addTextChangedListener(watcher)
        binding.cajasEDTXT.addTextChangedListener(watcher)
        binding.piezasEDTXT.addTextChangedListener(watcher)
        binding.sueltoEDTXT.addTextChangedListener(watcher)

        binding.guardarBTN.setOnClickListener{
            val codigoProducto = listcodigos.text.toString()
            if (codigoProducto.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, ingrese un código de producto.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (idEmpresa == -1) {
                Toast.makeText(requireContext(), "Por favor, seleccione una empresa.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (idProveedor == -1) {
                Toast.makeText(requireContext(), "Por favor, seleccione un proveedor.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tarimas = binding.tarimasEDTXT.text.toString().toIntOrNull() ?: 1
            val cajas = binding.cajasEDTXT.text.toString().toIntOrNull() ?: 1
            val piezas = binding.piezasEDTXT.text.toString().toIntOrNull() ?: 1
            val suelto = binding.sueltoEDTXT.text.toString().toIntOrNull() ?: 0
            val total = totalProducto

            val ubicacionSeleccionada = binding.ubicacionSPN.selectedItem as DBHelper.Ubicacion
            val idUbicacion = ubicacionSeleccionada.idubicacion
            val db = DBHelper(requireContext())
            val idInventarioActivo = db.obtenerInventarioActivo()

            if(registroSeleccionado==null) {
                val nuevoRegistro = Registro(
                    idregistro = 0,
                    idproducto = codigoProducto,
                    tarimas = tarimas,
                    cajas = cajas,
                    unidades = piezas,
                    suelto = suelto,
                    total = total,
                    idempresa = idEmpresa,
                    idcliente = idProveedor,
                    idubicacion = idUbicacion
                )

                val idRegistro = db.insertarRegistro(nuevoRegistro)


                if (idRegistro > 0) {

                    db.insertarRegistroInventario(
                        idregistro = idRegistro.toInt(),
                        idinventarios = idInventarioActivo
                    )

                    actualizarContadorRegistro(idRegistro.toInt())

                    binding.guardarBTN.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(80)
                        .withEndAction {
                            binding.guardarBTN.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()
                        }
                        .start()
                    Toast.makeText(
                        requireContext(),
                        "Registro guardado exitosamente.",
                        Toast.LENGTH_SHORT
                    ).show()
                    limpiarFormulario()
                    viewSuelto()
                    //cargarRegistros()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar el registro.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else{
                // 🟡 ACTUALIZAR REGISTRO

                val registroActualizado = registroSeleccionado!!.copy(
                    idproducto = codigoProducto,
                    tarimas = tarimas,
                    cajas = cajas,
                    unidades = piezas,
                    suelto = suelto,
                    total = total,
                    idempresa = idEmpresa,
                    idcliente = idProveedor,
                    idubicacion = idUbicacion
                )

                val filas = db.actualizarRegistro(registroActualizado)

                if (filas > 0) {

                    actualizarContadorRegistro(registroActualizado.idregistro)
                    mostrarAnimacionGuardado()

                    Toast.makeText(requireContext(), "Registro actualizado.", Toast.LENGTH_SHORT).show()

                    registroSeleccionado = null
                    binding.guardarBTN.text = "Guardar"

                    limpiarFormulario()
                    viewSuelto()

                } else {
                    Toast.makeText(requireContext(), "Error al actualizar.", Toast.LENGTH_SHORT).show()
                }
            }


        }


        binding.listaRegistrosBTN.setOnClickListener {

            if (binding.listaContainer.isGone) {
                cargarLista()
                binding.listaContainer.visibility = View.VISIBLE
                binding.capturaLayout.visibility = View.GONE
            } else {
                binding.listaContainer.visibility = View.GONE
                binding.capturaLayout.visibility = View.VISIBLE
            }
        }

        registroAdapter = RegistroAdapter(emptyList()) { registro ->
            cargarRegistroEnFormulario(registro)
        }

        binding.registrosRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.registrosRecycler.adapter = registroAdapter







        return root
    }
    private fun mostrarAnimacionGuardado() {
        binding.guardarBTN.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(80)
            .withEndAction {
                binding.guardarBTN.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start()
            }
            .start()
    }

    private fun cargarLista() {
        val db = DBHelper(requireContext())
        val registros = db.obtenerRegistrosPorInventario(idInventarioActivo)
        registroAdapter.actualizarLista(registros)
    }
    private fun cargarRegistroEnFormulario(registro: Registro) {

        registroSeleccionado = registro

        binding.tarimasEDTXT.setText(registro.tarimas.toString())
        binding.cajasEDTXT.setText(registro.cajas.toString())
        binding.piezasEDTXT.setText(registro.unidades.toString())
        binding.sueltoEDTXT.setText(registro.suelto.toString())

        binding.listaContainer.visibility = View.GONE

        actualizarContadorRegistro(registro.idregistro)
    }


    private fun limpiarFormulario() {
       binding.sueltoEDTXT.setText("0")
        binding.tarimasEDTXT.setText("1")
        binding.cajasEDTXT.setText("1")
        binding.piezasEDTXT.setText("1")
        binding.sueltoEDTXT.setText("0")
        binding.totalTXT.text = "0"
        binding.ubicacionSPN.setSelection(0)
        binding.AutoCompleteListaCodigos.text.clear()
        idEmpresa = -1
        idProveedor = -1
        botonesEmpresa.forEach { it.setBackgroundColor(requireContext().getColor(R.color.morado)) }
        botonesProveedor.forEach { it.setBackgroundColor(requireContext().getColor(R.color.morado)) }
        cargarNombreProducto()
    }


    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onResume() {
        super.onResume()


        val activity = activity as AppCompatActivity

        // Mantiene el botón hamburguesa
        activity.supportActionBar?.setDisplayShowTitleEnabled(false)
        activity.supportActionBar?.setDisplayShowCustomEnabled(true)

        // Inflamos nuestro layout
        val customView = layoutInflater.inflate(R.layout.toolbar_home, null)
     //   customView.findViewById<TextView>(R.id.tvRegistro).text = "Registro: 1"
        customView.findViewById<TextView>(R.id.tvNombre).text = "Sin nombre"

        // Lo agregamos sin quitar el botón
        activity.supportActionBar?.customView = customView
        actualizarContadorRegistro()
        actualizarAutoCompleteTextView()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val autoCodigo =
            view.findViewById<AutoCompleteTextView>(R.id.AutoCompleteListaCodigos)

        val listaCodigos = obtenerCodigos()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            listaCodigos
        )

        autoCodigo.setAdapter(adapter)

        autoCodigo.setOnClickListener {
            autoCodigo.showDropDown()
        }

        // Añadimos el listener para cuando se seleccione un elemento desde este view también
        autoCodigo.setOnItemClickListener { _: AdapterView<*>, _: View, _: Int, _: Long ->
            cargarNombreProducto()
            // ocultar el teclado y quitar focoy
            ocultarTeclado()
            autoCodigo.clearFocus()
        }



    }

    private fun actualizarAutoCompleteTextView() {
        val db = DBHelper(requireContext())
        val nuevosCodigos = db.obtenerCodigosProductosMedidas()
        val codigosActualizados = nuevosCodigos.map { it.first }

        val listcodigos = view?.findViewById<AutoCompleteTextView>(R.id.AutoCompleteListaCodigos)
        if (listcodigos == null) {
            Toast.makeText(requireContext(), "View not found", Toast.LENGTH_SHORT).show()
            return
        }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            codigosActualizados
        )
        listcodigos.setAdapter(adapter)
    }

    private fun cargarNombreProducto() {

        val listcodigos =
            view?.findViewById<AutoCompleteTextView>(R.id.AutoCompleteListaCodigos)
                ?: return

        val codigoIngresado = listcodigos.text.toString()

        val db = DBHelper(requireContext())
        val nombreProducto = db.obtenerNombreProductoPorCodigo(codigoIngresado)
            ?: "Producto no encontrado"

        val activity = requireActivity() as AppCompatActivity
        val toolbarView = activity.supportActionBar?.customView

        val tvNombre = toolbarView?.findViewById<TextView>(R.id.tvNombre)
        tvNombre?.text = nombreProducto
    }

/*

    private fun recargarCodigos() {
        val db = DBHelper(requireContext())
        val nuevosCodigos = db.obtenerCodigos()

        codigos.clear()
        codigos.addAll(nuevosCodigos)

        adapter.notifyDataSetChanged()
    }

 */
/*
    private fun isInventarioAbierto(): Boolean {
        val prefs = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val iniciado = prefs.getBoolean("inventario_iniciado", false)
        val id = prefs.getLong("inventario_id_activo", -1)

        return iniciado && id != -1L
    }*/




    private fun viewSuelto()
    {
        if (binding.sueltoLayout.isVisible)
        {
            binding.sueltoSPC1.visibility=View.VISIBLE
            binding.sueltoSPC2.visibility=View.VISIBLE
            binding.sueltoLayout.visibility=View.GONE
            binding.sueltoEDTXT.setText("0")
            return
        }else if (binding.sueltoLayout.isGone)
        {
            binding.sueltoSPC1.visibility=View.GONE
            binding.sueltoSPC2.visibility=View.GONE
            binding.sueltoLayout.visibility=View.VISIBLE
            binding.sueltoEDTXT.setText("0")

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

    private fun configurarSueltoEditText(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && editText.text.toString() == "0") {
                editText.text.clear() // Borra el "1" al tocar
            } else if (!hasFocus && editText.text.isEmpty()) {
                editText.setText("0") // Si el usuario no escribe nada, vuelve a "1"
            }
        }
    }

    private fun obtenerCodigos(): List<DBHelper.Codigo> {
        val dbHelper = DBHelper(requireContext())
        return dbHelper.listaCodigos()
    }


    private fun actualizarUbicaciones(){
        val db = DBHelper(requireContext())
        val ubicacionSPN = binding.ubicacionSPN

        val ubicaciones = db.obtenerUbicaciones()
            .filter { it.idempresas == idEmpresa }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            ubicaciones
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ubicacionSPN.adapter = adapter
    }

    private fun calcularTotal(tarimas: Int, cajas: Int, piezas: Int, suelto: Int){
        totalProducto = (tarimas * cajas * piezas ) + suelto
        binding.totalTXT.text = "$totalProducto"
    }





    private fun ocultarTeclado() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val view = requireActivity().currentFocus
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }

    private fun actualizarContadorRegistro(idRegistroActual: Int? = null){
        val db = DBHelper(requireContext())
        val idInventarioActivo = db.obtenerInventarioActivo()

        val total = db.contarRegistrosPorInventario(idInventarioActivo)

        val indice = if (idRegistroActual != null){
            db.obtenerIndiceRegistro(idInventarioActivo, idRegistroActual)
        } else {
            total
        }

        val activity = activity as AppCompatActivity
        val toolbarView = activity.supportActionBar?.customView
        val tvRegistro = toolbarView?.findViewById<TextView>(R.id.tvRegistro)
        tvRegistro?.text = "Reg: $indice / $total"
    }







}
