package com.example.inventario20.ui.inventarios

import android.app.AlertDialog
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventario20.DBHelper
import com.example.inventario20.R
import com.example.inventario20.databinding.FragmentInventariosBinding
import com.example.inventario20.ui.UiNotifier
import java.util.Date
import java.util.Locale
import androidx.core.view.isVisible
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.DataFormat
import org.apache.poi.ss.usermodel.Font
import java.io.File
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import androidx.recyclerview.widget.SimpleItemAnimator


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
                binding.registrosBTN.text = getString(R.string.ocultar_registros)

            } else {
                binding.rvInventarios.visibility = View.VISIBLE
                binding.rvRegistros.visibility = View.GONE
                binding.registrosBTN.text = getString(R.string.ver_registros)
            }

        }




        val generarExcelBTN = binding.generarExcelBTN

        generarExcelBTN.setOnClickListener {
            val id = idInventarioSeleccionado

            if(id == null){
                UiNotifier.warning(binding.root, getString(R.string.seleccionar_inventario_para_excel))
                return@setOnClickListener
            }

            exportarExcel(id)
        }

        binding.borrarInventarioBTN.setOnClickListener {

            val id = idInventarioSeleccionado ?: return@setOnClickListener

            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar inventario")
                .setMessage("Se hará un respaldo automático antes de eliminar. ¿Continuar?")
                .setPositiveButton("Sí") { _, _ ->

                    val dbHelper = DBHelper(requireContext())

                    // 🔒 VALIDAR SI ES TIPO 3 (REABIERTO)
                    val esReabierto = dbHelper.esInventarioReabierto(id)

                    if (!esReabierto) {
                        UiNotifier.warning(binding.root, getString(R.string.solo_reabiertos))
                        return@setPositiveButton
                    }

                    // 🔥 RESPALDO (marcar como BACKUP para añadir sufijo)
                    val respaldoOk = exportarExcel(id, isBackup = true)

                    if (respaldoOk) {

                        // 🔥 BORRADO
                        val eliminado = dbHelper.eliminarInventarioCompleto(id)

                        if (eliminado) {
                            UiNotifier.info(binding.root, getString(R.string.inventario_eliminado))
                            idInventarioSeleccionado = null
                            cargarInventarios()
                        } else {
                            UiNotifier.error(binding.root, getString(R.string.error_eliminar_inventario))
                        }

                    } else {
                        UiNotifier.error(binding.root, getString(R.string.error_respaldo_no_eliminado))
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }




        return root
    }


    // Añadido parámetro isBackup: si es true, se agrega el sufijo _BACKUP_ al nombre del archivo.
    private fun exportarExcel(inventarioId: Int, isBackup: Boolean = false): Boolean {
        return try {
            val dbHelper = DBHelper(requireContext())
            val registros = dbHelper.obtenerRegistrosPorInventarioNombre(inventarioId)

            val agrupados = mutableMapOf<String, DBHelper.RegistroAgrupado>()

            for (item in registros) {
                val key = "${item.idproducto}_${item.empresa}_${item.cliente}_${item.ubicacion}"

                if (!agrupados.containsKey(key)) {
                    agrupados[key] = DBHelper.RegistroAgrupado(
                        idproducto = item.idproducto,
                        producto = item.producto,
                        empresa = item.empresa,
                        ubicacion = item.ubicacion,
                        cliente = item.cliente,
                        medida = item.medida
                    )
                }

                val registro = agrupados[key]!!

                registro.tarimas.add(item.tarimas)
                registro.cajas.add(item.cajas)
                registro.unidades.add(item.unidades)
                registro.suelto.add(item.suelto)

                registro.total += item.total.toIntOrNull() ?: 0

            }

            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Inventario_$inventarioId")

            val headers = listOf("Codigo","Producto","Empresa","Proveedor","Ubicacion","Tarimas","Cajas","Unidades","Suelto","Total","Medida")

            // --- Estilos ---
            val headerStyle = workbook.createCellStyle() as XSSFCellStyle
            val headerFont: Font = workbook.createFont()
            headerFont.bold = true
            headerFont.color = IndexedColors.WHITE.index
            headerFont.fontHeightInPoints = 12.toShort()
            headerStyle.setFont(headerFont)
            headerStyle.fillForegroundColor = IndexedColors.DARK_BLUE.index
            headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
            headerStyle.alignment = HorizontalAlignment.CENTER
            headerStyle.verticalAlignment = VerticalAlignment.CENTER
            headerStyle.borderBottom = BorderStyle.MEDIUM
            headerStyle.borderTop = BorderStyle.MEDIUM
            headerStyle.borderLeft = BorderStyle.MEDIUM
            headerStyle.borderRight = BorderStyle.MEDIUM

            val dataStyle1 = workbook.createCellStyle() as XSSFCellStyle
            dataStyle1.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            dataStyle1.fillPattern = FillPatternType.SOLID_FOREGROUND
            dataStyle1.verticalAlignment = VerticalAlignment.CENTER
            dataStyle1.borderBottom = BorderStyle.THIN
            dataStyle1.borderTop = BorderStyle.THIN
            dataStyle1.borderLeft = BorderStyle.THIN
            dataStyle1.borderRight = BorderStyle.THIN

            val dataStyle2 = workbook.createCellStyle() as XSSFCellStyle
            // dejar blanco (sin color) -> no fill
            dataStyle2.verticalAlignment = VerticalAlignment.CENTER
            dataStyle2.borderBottom = BorderStyle.THIN
            dataStyle2.borderTop = BorderStyle.THIN
            dataStyle2.borderLeft = BorderStyle.THIN
            dataStyle2.borderRight = BorderStyle.THIN

            val numericStyle = workbook.createCellStyle() as XSSFCellStyle
            val df: DataFormat = workbook.createDataFormat()
            // Formato con separador de miles
            numericStyle.dataFormat = df.getFormat("#,##0")
            numericStyle.alignment = HorizontalAlignment.RIGHT
            numericStyle.verticalAlignment = VerticalAlignment.CENTER
            numericStyle.borderBottom = BorderStyle.THIN
            numericStyle.borderTop = BorderStyle.THIN
            numericStyle.borderLeft = BorderStyle.THIN
            numericStyle.borderRight = BorderStyle.THIN

            // estilo para columna Producto (wrap)
            val productStyle = workbook.createCellStyle() as XSSFCellStyle
            productStyle.cloneStyleFrom(dataStyle2)
            productStyle.wrapText = true
            productStyle.alignment = HorizontalAlignment.LEFT

            // Mapear estilos por columna (0..10)
            val columnStyles = arrayOfNulls<XSSFCellStyle>(headers.size)
            for (i in headers.indices) {
                columnStyles[i] = when (i) {
                    1 -> productStyle            // Producto
                    9 -> numericStyle            // Total
                    in 5..8 -> dataStyle1        // columnas numéricas/texto agrupado
                    else -> dataStyle2
                }
            }

            // --- Encabezado ---
            val headerRow = sheet.createRow(0)
            headerRow.heightInPoints = 18f
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            // Freeze header row y autofiltro
            sheet.createFreezePane(0, 1)
            sheet.setAutoFilter(CellRangeAddress(0, 0, 0, headers.size - 1))

            agrupados.values.forEachIndexed { rowIndex, item ->
                val row = sheet.createRow(rowIndex + 1)
                val isEven = rowIndex % 2 == 0

                val baseStyle: XSSFCellStyle = if (isEven) dataStyle2 else dataStyle1

                fun applyCell(cellIndex: Int, value: String) {
                    val cell = row.createCell(cellIndex)
                    cell.setCellValue(value)
                    // asignar estilo por columna (si existe) o usar base alternado
                    val estilo = columnStyles[cellIndex] ?: baseStyle
                    cell.cellStyle = estilo
                }

                applyCell(0, item.idproducto)
                applyCell(1, item.producto)
                applyCell(2, item.empresa)
                applyCell(3, item.cliente)
                applyCell(4, item.ubicacion)
                applyCell(5, item.tarimas.joinToString(","))
                applyCell(6, item.cajas.joinToString(","))
                applyCell(7, item.unidades.joinToString(","))
                applyCell(8, item.suelto.joinToString(","))

                // Total como número
                val totalCell = row.createCell(9)
                totalCell.setCellValue(item.total.toDouble())
                totalCell.cellStyle = columnStyles[9] ?: numericStyle

                applyCell(10, item.medida)
            }

            // Auto-ajustar ancho de columnas (usar after populate)
            // Calcular ancho aproximado por contenido (no usar autoSizeColumn en Android — evita dependencias AWT)
            val maxWidth = 10000
            for (i in headers.indices) {
                var maxChars = headers[i].length
                for (it in agrupados.values) {
                    val cellText = when (i) {
                        0 -> it.idproducto
                        1 -> it.producto
                        2 -> it.empresa
                        3 -> it.cliente
                        4 -> it.ubicacion
                        5 -> it.tarimas.joinToString(",")
                        6 -> it.cajas.joinToString(",")
                        7 -> it.unidades.joinToString(",")
                        8 -> it.suelto.joinToString(",")
                        9 -> java.text.NumberFormat.getIntegerInstance(Locale.getDefault()).format(it.total)
                        10 -> it.medida
                        else -> ""
                    }
                    if (cellText.length > maxChars) maxChars = cellText.length
                }

                // Añadir un pequeño padding y convertir a unidades POI (1/256 de char)
                val width = ( (maxChars + 4) * 256 ).coerceAtMost(maxWidth)
                sheet.setColumnWidth(i, width)
            }

            val nombreInventario = dbHelper.obtenerNombreInventario(inventarioId)
            val fecha = obtenerFechaCorta()
            val nombreLimpio = nombreInventario.replace(Regex("[^a-zA-Z0-9_]"), "_")

            val nombreArchivo = if (isBackup) {
                "${nombreLimpio}_BACKUP_$fecha"
            } else {
                // Nombre sin el sufijo BACKUP cuando el usuario genera el Excel manualmente
                "${nombreLimpio}_$fecha"
            }

            guardarExcel(nombreArchivo, workbook)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun guardarExcel(nombreArchivo: String, workbook: XSSFWorkbook){
        val resolver = requireContext().contentResolver
        val nombreCompleto = "$nombreArchivo.xlsx"

        val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreCompleto)
                put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            resolver.insert(MediaStore.Files.getContentUri("external"), values)
        }else{
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                nombreCompleto
            )
            Uri.fromFile(file)
        }
        uri?.let{
            val outputStream = resolver.openOutputStream(it)
            workbook.write(outputStream)
            outputStream?.close()
            workbook.close()

            UiNotifier.info(binding.root, getString(R.string.excel_guardado_descargas))
        }?: run {
            UiNotifier.error(binding.root, getString(R.string.error_guardar_excel))
        }
    }

    private fun obtenerFechaCorta(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
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
                UiNotifier.warning(binding.root, "No hay inventario disponible")
            }
        }

        actualizarEstadoBoton()
    }



    private fun cerrarInventarioActual() {
        val dbHelper = DBHelper(requireContext())
        val idActivo = dbHelper.obtenerInventarioActivo()

        if (idActivo == null) {
            UiNotifier.error(binding.root, "No hay inventario activo")
            return
        }
        dbHelper.cerrarInventario(
            idActivo,
            obtenerFechaActual()
        )

        // Limpiar selección para que el inventario cerrado no permanezca seleccionado
        idInventarioSeleccionado = null
        (binding.rvInventarios.adapter as? InventarioAdapter)?.setSelected(null)

        // Resetear UI: ocultar registros, mostrar lista, restaurar textos por defecto
        binding.rvRegistros.visibility = View.GONE
        binding.rvInventarios.visibility = View.VISIBLE
        binding.registrosBTN.text = getString(R.string.ver_registros)

        binding.NomInvTXT.text = getString(R.string.nombre_inventario_pred)
        binding.FechaHoraInvTXT.text = getString(R.string.creado_cerrado_pred)
        binding.EstatusTXT.text = getString(R.string.estatus_label)
        binding.CantidadRegTXT.text = getString(R.string.cant_registros_label)

        UiNotifier.info(binding.root, "Inventario cerrado")

        cargarInventarios()
        actualizarEstadoBoton()

    }

    private fun cargarTotalRegistros(idInventario: Int){
        val dbHelper = DBHelper(requireContext())
        val total = dbHelper.contarRegistrosPorInventario(idInventario)

        binding.CantidadRegTXT.animate()
            .alpha(0f)
            .setDuration(100)
            .withEndAction {
                binding.CantidadRegTXT.text = getString(R.string.cant_registros_valor, total)
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
                    UiNotifier.error(binding.root, getString(R.string.contrasena_incorrecta))
                    return@setPositiveButton
                }

                val id = idInventarioSeleccionado ?: return@setPositiveButton
                val dbHelper = DBHelper(requireContext())

                if (dbHelper.obtenerInventarioActivo() != null) {
                    UiNotifier.warning(binding.root, getString(R.string.ya_hay_inventario_activo))
                    return@setPositiveButton
                }

                dbHelper.reabrirInventario(id)

                UiNotifier.info(binding.root, getString(R.string.inventario_reabierto))
                cargarInventarios()
                actualizarEstadoBoton()
                // Asegurar estado correcto del botón Borrar tras reabrir
                val listaActual = dbHelper.obtenerTodosLosInventarios()
                actualizarBotonBorrar(listaActual)

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
                idInventarioSeleccionado = null
                cargarInventarios()
                actualizarEstadoBoton()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarEstadoBoton() {
        val dbHelper = DBHelper(requireContext())
        val accionBTN = binding.accionBTN
        accionBTN.text = when {
            dbHelper.obtenerInventarioActivo() != null -> getString(R.string.cerrar_inventario)
            idInventarioSeleccionado != null -> getString(R.string.reabrir_inventario)
            else -> getString(R.string.inventario_no_disponible)
         }
     }

    private fun estadoTexto(estado: Int): String =
        when (estado) {
            DBHelper.EstadoInventario.ABIERTO -> "ABIERTO"
            DBHelper.EstadoInventario.CERRADO -> "CERRADO"
            DBHelper.EstadoInventario.REABIERTO -> "REABIERTO"
            else -> "DESCONOCIDO"
        }

    // kotlin
    private fun cargarInventarios() {
        val dbHelper = DBHelper(requireContext())
        val lista = dbHelper.obtenerTodosLosInventarios()


        val adapter = InventarioAdapter(lista) { inventario ->
            if (idInventarioSeleccionado == inventario.idinventarios) {
                // Deseleccionar
                idInventarioSeleccionado = null
                // Delegar la actualización visual al adapter (usa payloads para animar)
                (binding.rvInventarios.adapter as? InventarioAdapter)?.setSelected(null)

                // Actualizar textos de la UI sin animación
                binding.NomInvTXT.text = getString(R.string.nombre_inventario_pred)
                binding.FechaHoraInvTXT.text = getString(R.string.creado_cerrado_pred)
                binding.EstatusTXT.text = getString(R.string.estatus_label)
                binding.CantidadRegTXT.text = getString(R.string.cant_registros_label)

            } else {
                // Seleccionar
                idInventarioSeleccionado = inventario.idinventarios
                (binding.rvInventarios.adapter as? InventarioAdapter)?.setSelected(idInventarioSeleccionado)
                mostrarInventario(inventario)
                // cargar registros automáticamente al seleccionar
                cargarRegistros(inventario.idinventarios)
            }

            actualizarEstadoBoton()
            actualizarBotonBorrar(lista)
        }

        binding.rvInventarios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInventarios.adapter = adapter
        // permitir que el adapter acceda al RecyclerView para animar ViewHolders visibles
        adapter.attachRecyclerView(binding.rvInventarios)
         // Evitar animaciones por cambios (notifyItemChanged) que pueden generar parpadeos al deseleccionar
         (binding.rvInventarios.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        // Seleccionar por defecto el inventario activo (si existe)
        val inventarioActivoId = dbHelper.obtenerInventarioActivo()
        if (inventarioActivoId != null) {
            val itemActivo = lista.find { it.idinventarios == inventarioActivoId }
            if (itemActivo != null) {
                idInventarioSeleccionado = inventarioActivoId
                (binding.rvInventarios.adapter as? InventarioAdapter)?.setSelected(idInventarioSeleccionado)
                mostrarInventario(itemActivo)
                // cargar registros del inventario activo
                cargarRegistros(itemActivo.idinventarios)
                actualizarEstadoBoton()
                actualizarBotonBorrar(lista)

                // desplazar la lista para mostrar el elemento seleccionado
                val index = lista.indexOf(itemActivo)
                if (index >= 0) {
                    binding.rvInventarios.smoothScrollToPosition(index)
                }
            }
        }

        // Asegurar visibilidad correcta del botón Borrar según la selección actual
        actualizarBotonBorrar(lista)
    }



    private fun actualizarBotonBorrar(lista: List<DBHelper.InventarioItem>) {
        val inventarioSeleccionado = lista.find { it.idinventarios == idInventarioSeleccionado }

        binding.borrarInventarioBTN.visibility =
            if (inventarioSeleccionado?.activo == 3) View.VISIBLE else View.GONE
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