package com.example.inventario20.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.inventario20.R
import com.example.inventario20.databinding.FragmentHomeBinding
import androidx.core.view.isVisible
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventario20.DBHelper
import com.example.inventario20.DBHelper.Registro
import com.example.inventario20.ui.UiNotifier


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private var registroSeleccionado: Registro? = null    // Variable para almacenar el registro seleccionado para edición


    private var modoActualizacion: Boolean = false // Variable para controlar el modo de actualización

    //conexion con el adapter del recycler
    private lateinit var registroAdapter: RegistroAdapter
    private lateinit var autoCompleteAdapter: ArrayAdapter<DBHelper.Codigo>


    private var idEmpresa: Int = -1 // Variable para almacenar la empresa seleccionada
    private var idProveedor: Int = -1 // Variable para almacenar el proveedor seleccionado
    private var totalProducto: Int = 1 // Variable para almacenar el total calculado del producto


    private lateinit var botonesEmpresa: List<Button> // Lista para almacenar los botones de empresa
    private lateinit var botonesProveedor: List<Button> // Lista para almacenar los botones de proveedor


    private var listaRegistrosActual: List<Registro> = emptyList()
    private var indiceActual: Int = -1
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
                UiNotifier.info(binding.root, "Por favor, ingrese un código de producto.")
                return@setOnClickListener
            }

            if (idEmpresa == -1) {
                UiNotifier.info(binding.root, "Por favor, seleccione una empresa.")
                return@setOnClickListener
            }

            if (idProveedor == -1) {
                UiNotifier.info(binding.root, "Por favor, seleccione un proveedor.")

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
            if(!modoActualizacion) {
                val nuevoRegistro = Registro(
                    idregistro = 0,
                    idproducto = codigoProducto,
                    tarimas = tarimas,
                    cajas = cajas,
                    unidades = piezas,
                    suelto = suelto,
                    total = total,
                    idempresas = idEmpresa,
                    idcliente = idProveedor,
                    idubicacion = idUbicacion
                )

                val idRegistro = db.insertarRegistro(nuevoRegistro)


                if (idRegistro > 0) {

                    db.insertarRegistroInventario(
                        idregistro = idRegistro.toInt(),
                        idinventarios = idInventarioActivo
                    )

                    actualizarContadorRegistro2(idRegistro.toInt())

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
                    UiNotifier.info(
                        binding.root,
                        "Registro guardado exitosamente."
                    )

                    limpiarFormulario2()
                    viewSueltoOFF()
                    //cargarRegistros()
                } else {
                    UiNotifier.info(
                        binding.root,
                        "Error al guardar el registro."
                    )
                }
            } else {

                val registro = registroSeleccionado

                if (registro == null) {
                    UiNotifier.info(
                        binding.root,
                        "No hay registro seleccionado para actualizar."
                    )
                    return@setOnClickListener
                }

                // Verificar si hay cambios en los datos
                if (registro.idproducto == codigoProducto &&
                    registro.tarimas == tarimas &&
                    registro.cajas == cajas &&
                    registro.unidades == piezas &&
                    registro.suelto == suelto &&
                    registro.total == total &&
                    registro.idempresas == idEmpresa &&
                    registro.idcliente == idProveedor &&
                    registro.idubicacion == idUbicacion) {
                    UiNotifier.info(
                        binding.root,
                        "No se detectaron cambios, actualización no realizada."
                    )
                    return@setOnClickListener
                }

                val registroActualizado = registro.copy(
                    idproducto = codigoProducto,
                    tarimas = tarimas,
                    cajas = cajas,
                    unidades = piezas,
                    suelto = suelto,
                    total = total,
                    idempresas = idEmpresa,
                    idcliente = idProveedor,
                    idubicacion = idUbicacion
                )

                val filas = db.actualizarRegistro(registroActualizado)

                if (filas > 0) {
                    modoActualizacion = false
                    registroSeleccionado = null
                    animarNuevo{
                    actualizarModo()
                    limpiarFormulario2()
                    actualizarContadorRegistro2()
                    viewSueltoOFF()
                    viewRegistrar()
                    }
                } else {
                    UiNotifier.info(
                        binding.root,
                        "Error al actualizar el registro."
                    )
                }
            }


        }

        binding.sueltoBTN.setOnClickListener {
            viewSuelto()
        }

        binding.listaRegistrosBTN.setOnClickListener {

            if (binding.listaContainer.isGone) {
                cargarLista()
                binding.listaContainer.visibility = View.VISIBLE
                binding.listaContainer.scheduleLayoutAnimation()
                binding.capturaLayout.visibility = View.GONE
            } else {
                binding.listaContainer.visibility = View.GONE
                binding.capturaLayout.scheduleLayoutAnimation()

                binding.capturaLayout.visibility = View.VISIBLE
            }
        }

        registroAdapter = RegistroAdapter(emptyList()) { registro ->
            cargarRegistroEnFormulario(registro)

        }

        binding.registrosRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.registrosRecycler.adapter = registroAdapter

        val animation = AnimationUtils.loadLayoutAnimation(
            requireContext(),
            R.anim.layout_animation_fall_down
        )

        binding.listaContainer.layoutAnimation = animation
        binding.capturaLayout.layoutAnimation = animation

        binding.nuevoBTN.setOnClickListener {
            registroSeleccionado = null
            modoActualizacion = false
            animarNuevo{
            actualizarModo()
            actualizarContadorRegistro2()
                actualizarUbicaciones()
            viewRegistrar()
            limpiarFormulario()
            viewSueltoOFF()
            }
        }

        binding.delanteBTN.setOnClickListener {

            if (!modoActualizacion) return@setOnClickListener

            if (listaRegistrosActual.isEmpty()) return@setOnClickListener

            val nuevoIndice = indiceActual - 1

            if (nuevoIndice in listaRegistrosActual.indices) {

                animarSiguiente {
                indiceActual = nuevoIndice
                val registro = listaRegistrosActual[indiceActual]

                registroSeleccionado = registro
                llenarFormulario(registro)
                actualizarEstadoBotones()
                }
            } else {
                UiNotifier.info(binding.root, "Último registro")
            }
        }
        binding.atrasBTN.setOnClickListener {

            if (!modoActualizacion) return@setOnClickListener

            if (listaRegistrosActual.isEmpty()) return@setOnClickListener

            val nuevoIndice = indiceActual + 1

            if (nuevoIndice in listaRegistrosActual.indices) {




                animarAnterior {
                indiceActual = nuevoIndice
                val registro = listaRegistrosActual[indiceActual]

                registroSeleccionado = registro
                llenarFormulario(registro)
                actualizarEstadoBotones()
                }
            } else {
                UiNotifier.info(binding.root, "Último registro")
            }
        }

        binding.AutoCompleteListaCodigos.setOnItemClickListener { _, _, _, _ ->
            val codigo = binding.AutoCompleteListaCodigos.text.toString()
            analizarCodigoEmpresa(codigo)
        }

        binding.AutoCompleteListaCodigos.addTextChangedListener {
            val codigo = it.toString()
            analizarCodigoEmpresa(codigo)
        }

        return root
    }

    private fun analizarCodigoEmpresa(codigo: String) {

        val codigoUpper = codigo.uppercase()

        val coastalBTN = binding.coastalBTN
        val rainfieldBTN = binding.rainfieldBTN
        val muranakaBTN = binding.muranakaBTN

        when {
            codigoUpper.contains("CF") -> {
                seleccionarProveedor(coastalBTN,2)   // 👈 ID real de empresa CF
            //    actualizarUbicaciones()
            }

            codigoUpper.contains("MK") -> {
                seleccionarProveedor(muranakaBTN,1)   // 👈 ID real de empresa MK
              //  actualizarUbicaciones()

            }

            codigoUpper.contains("RF") -> {
                seleccionarProveedor(rainfieldBTN,3)   // 👈 ID real de empresa RF
             //   actualizarUbicaciones()

            }
        }
    }




    private fun actualizarEstadoBotones() {

        binding.delanteBTN.isEnabled = indiceActual > 0
        binding.atrasBTN.isEnabled = indiceActual < listaRegistrosActual.size - 1
    }


    private fun actualizarModo() {
        if (!modoActualizacion) {
            binding.guardarBTN.setBackgroundResource(R.drawable.save_33)

        } else {
            binding.guardarBTN.setBackgroundResource(R.drawable.ic_actualizar_doc2)

        }
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
        val idInventarioActivo = db.obtenerInventarioActivo()

        listaRegistrosActual = db.obtenerRegistrosPorInventario(idInventarioActivo)

        registroAdapter.actualizarLista(listaRegistrosActual)
        registroAdapter.notifyDataSetChanged()
    }

    // configurrar botones empresa
    private fun seleccionarEmpresa(boton: Button, seleccionEmp: Int) {
        // Reiniciar el color de todos los botones

        botonesEmpresa.forEach { it.setBackgroundColor(Color.DKGRAY) }
        // Cambiar el color del botón seleccionado
        boton.setBackgroundColor(Color.BLUE)
        idEmpresa = seleccionEmp
        actualizarUbicaciones() // Actualiza las ubicaciones al seleccionar una empresa
    }

    //configurar botones proveedor
    private fun seleccionarProveedor(boton: Button, seleccionPro: Int) {

        // Reiniciar el color de todos los botones
        botonesProveedor.forEach { it.setBackgroundColor(Color.DKGRAY) }
        // Cambiar el color del botón seleccionado
        boton.setBackgroundColor(Color.BLUE)
        idProveedor = seleccionPro
    }

    private fun cargarSeleccionEmpresa(id: Int) {
        when (id) {
            1 -> seleccionarEmpresa(binding.agrimexBTN, 1)
            2 -> seleccionarEmpresa(binding.cosmarBTN, 2)
            3 -> seleccionarEmpresa(binding.mextlanBTN, 3)
        }
    }


    private fun cargarSeleccionProveedor(id: Int) {

        when (id) {
            1 -> seleccionarProveedor(binding.muranakaBTN, 1)
            2 -> seleccionarProveedor(binding.coastalBTN, 2)
            3 -> seleccionarProveedor(binding.rainfieldBTN, 3)
        }
    }

    private fun cargarRegistroEnFormulario(registro: Registro) {

        val db = DBHelper(requireContext())
        val idInventarioActivo = db.obtenerInventarioActivo()
        listaRegistrosActual = db.obtenerRegistrosPorInventario(idInventarioActivo)
            .sortedByDescending { it.idregistro }

        registroSeleccionado = registro
        modoActualizacion = true
        actualizarModo()
        viewActualizar()

        // 👇 Encontrar índice del registro seleccionado
        indiceActual = listaRegistrosActual.indexOfFirst {
            it.idregistro == registro.idregistro
        }
        llenarFormulario(registro)
    }
    private fun llenarFormulario(registro: Registro) {

        binding.tarimasEDTXT.setText(registro.tarimas.toString())
        binding.cajasEDTXT.setText(registro.cajas.toString())
        binding.piezasEDTXT.setText(registro.unidades.toString())

        if (registro.suelto != 0) {
            viewSueltoON()
            binding.sueltoEDTXT.setText(registro.suelto.toString())
        } else {
            viewSueltoOFF()
        }

        binding.AutoCompleteListaCodigos.setText(registro.idproducto)

        cargarSeleccionEmpresa(registro.idempresas)
        cargarSeleccionProveedor(registro.idcliente)
        binding.listaContainer.visibility = View.GONE
        binding.capturaLayout.visibility = View.VISIBLE
        cargarNombreProducto()
        actualizarContadorRegistro(registro.idregistro)
    }


    private fun viewActualizar() {
        binding.listaRegistrosBTN.visibility=View.GONE
        binding.atrasBTN.visibility=View.VISIBLE
        binding.delanteBTN.visibility=View.VISIBLE


        binding.spacebotonesReg1.visibility=View.GONE
        binding.spacebotonesReg3.visibility=View.GONE

        binding.spacebotonesAct1.visibility=View.VISIBLE
    }


    private fun viewRegistrar() {
        binding.listaRegistrosBTN.visibility=View.VISIBLE
        binding.atrasBTN.visibility=View.GONE
        binding.delanteBTN.visibility=View.GONE


        binding.spacebotonesAct1.visibility=View.GONE

        binding.spacebotonesReg1.visibility=View.VISIBLE
        binding.spacebotonesReg3.visibility=View.VISIBLE
    }

    private fun limpiarFormulario() {
       binding.sueltoEDTXT.setText("0")
        binding.tarimasEDTXT.setText("1")
        binding.cajasEDTXT.setText("1")
        binding.piezasEDTXT.setText("1")
        binding.sueltoEDTXT.setText("0")
        binding.totalTXT.text = "0"
        binding.ubicacionSPN.setSelection(AdapterView.INVALID_POSITION)
        binding.AutoCompleteListaCodigos.text.clear()
        idEmpresa = -1
        idProveedor = -1
        botonesEmpresa.forEach { it.setBackgroundColor(requireContext().getColor(R.color.morado)) }
        botonesProveedor.forEach { it.setBackgroundColor(requireContext().getColor(R.color.morado)) }
        cargarNombreProducto()
    }

    private fun limpiarFormulario2() { // Limpia el formulario sin resetear empresa y por extencion las ubicaciones
        binding.sueltoEDTXT.setText("0")
        binding.tarimasEDTXT.setText("1")
        binding.cajasEDTXT.setText("1")
        binding.piezasEDTXT.setText("1")
        binding.sueltoEDTXT.setText("0")
        binding.totalTXT.text = "0"
        binding.ubicacionSPN.setSelection(AdapterView.INVALID_POSITION)
        binding.AutoCompleteListaCodigos.text.clear()
        idProveedor = -1
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
        customView.findViewById<TextView>(R.id.tvNombre).text = "Sin nombre"

        // Lo agregamos sin quitar el botón
        activity.supportActionBar?.customView = customView
        actualizarContadorRegistro2()
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
            UiNotifier.info(binding.root, "Error: AutoCompleteTextView no encontrado")
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


    private fun viewSueltoOFF()
    {
        binding.sueltoSPC1.visibility=View.VISIBLE
        binding.sueltoSPC2.visibility=View.VISIBLE
        binding.sueltoLayout.visibility=View.GONE
        binding.sueltoEDTXT.setText("0")

    }

    private fun viewSueltoON()
    {
        binding.sueltoSPC1.visibility=View.GONE
        binding.sueltoSPC2.visibility=View.GONE
        binding.sueltoLayout.visibility=View.VISIBLE
        binding.sueltoEDTXT.setText("0")

    }
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

    private fun actualizarContadorRegistro2(idRegistroActual: Int? = null){
        val db = DBHelper(requireContext())
        val idInventarioActivo = db.obtenerInventarioActivo()

        val total = db.contarRegistrosPorInventario(idInventarioActivo)

        val indice = if (idRegistroActual != null){
            db.obtenerIndiceRegistro(idInventarioActivo, idRegistroActual)
        } else {
            total
        }

        val indiceRegistro =indice+1 // Para mostrar el índice empezando desde 1 en lugar de 0

        val activity = activity as AppCompatActivity
        val toolbarView = activity.supportActionBar?.customView
        val tvRegistro = toolbarView?.findViewById<TextView>(R.id.tvRegistro)
        tvRegistro?.text = "Reg: $indiceRegistro / $total"
    }

    //animacion para cambio de registro
    private fun animarSiguiente(cambiarRegistro: () -> Unit) {

        val view = binding.capturaLayout //contenedor donde se captura y cargan los registros

        view.animate()
            .translationX(-view.width.toFloat())
            .alpha(0f)
            .setDuration(200)
            .withEndAction {

                cambiarRegistro() // aquí cargas el nuevo registro

                view.translationX = view.width.toFloat()
                view.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    private fun animarNuevo(cambiarRegistro: () -> Unit) {

        val view = binding.capturaLayout //contenedor donde se captura y cargan los registros

        view.animate()
            .translationX(-view.width.toFloat())
            .alpha(0f)
            .setDuration(50)
            .withEndAction {

                cambiarRegistro() // aquí cargas el nuevo registro

                view.translationX = view.width.toFloat()
                view.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(50)
                    .start()
            }
            .start()
    }
    private fun animarAnterior(cambiarRegistro: () -> Unit) {

        val view = binding.capturaLayout

        view.animate()
            .translationX(view.width.toFloat())
            .alpha(0f)
            .setDuration(200)
            .withEndAction {

                cambiarRegistro()

                view.translationX = -view.width.toFloat()
                view.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

}
