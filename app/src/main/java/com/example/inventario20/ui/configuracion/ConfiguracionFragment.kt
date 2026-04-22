package com.example.inventario20.ui.configuracion

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.inventario20.databinding.FragmentConfiguracionBinding
import com.example.inventario20.ui.UiNotifier

class ConfiguracionFragment : Fragment() {

    private var _binding: FragmentConfiguracionBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    // Constantes para las preferencias
    companion object {
        private const val PREFS_NAME = "inventario_config"
        private const val KEY_PERMITIR_EXPORTACION = "permitir_exportacion"
        private const val KEY_MODIFICAR_INV_ANTERIORES = "modificar_inv_anteriores"
        private const val KEY_HABILITAR_SUELTOS = "habilitar_sueltos"
        private const val KEY_HABILITAR_QR = "habilitar_qr_experimental"
        private const val KEY_HABILITAR_ADMIN = "habilitar_administrador"

        // Métodos estáticos para acceder a las configuraciones desde otros fragments
        fun isExportacionPermitida(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_PERMITIR_EXPORTACION, true)
        }

        fun isModificarInvAnterioresPermitido(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_MODIFICAR_INV_ANTERIORES, false)
        }

        fun isSueltosHabilitado(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_HABILITAR_SUELTOS, true)
        }

        fun isQRExperimentalHabilitado(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_HABILITAR_QR, false)
        }

        fun isModoAdminHabilitado(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_HABILITAR_ADMIN, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfiguracionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicializar SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Configurar chips con valores guardados
        setupChips()

        return root
    }

    private fun setupChips() {
        // Chip: Permitir Exportación
        binding.chip2.apply {
            isChecked = getConfigValue(KEY_PERMITIR_EXPORTACION, true)
            setOnCheckedChangeListener { _, isChecked ->
                saveConfigValue(KEY_PERMITIR_EXPORTACION, isChecked)
                UiNotifier.info(binding.root, "Exportación ${if (isChecked) "habilitada" else "deshabilitada"}")
            }
        }

        // Chip: Modificar Inv. Anteriores
        binding.chip3.apply {
            isChecked = getConfigValue(KEY_MODIFICAR_INV_ANTERIORES, false)
            setOnCheckedChangeListener { _, isChecked ->
                saveConfigValue(KEY_MODIFICAR_INV_ANTERIORES, isChecked)
                UiNotifier.info(binding.root, "Modificación de inventarios anteriores ${if (isChecked) "habilitada" else "deshabilitada"}")
            }
        }

        // Chip: Habilitar Sueltos
        binding.chip4.apply {
            isChecked = getConfigValue(KEY_HABILITAR_SUELTOS, true)
            setOnCheckedChangeListener { _, isChecked ->
                saveConfigValue(KEY_HABILITAR_SUELTOS, isChecked)
                UiNotifier.info(binding.root, "Campo sueltos ${if (isChecked) "habilitado" else "deshabilitado"}")
            }
        }

        // Chip: Habilitar QR Experimental
        binding.chip5.apply {
            isChecked = getConfigValue(KEY_HABILITAR_QR, false)
            setOnCheckedChangeListener { _, isChecked ->
                saveConfigValue(KEY_HABILITAR_QR, isChecked)
                UiNotifier.info(binding.root, "QR experimental ${if (isChecked) "habilitado" else "deshabilitado"}")
            }
        }

        // Chip: Habilitar Administrador
        binding.chip6.apply {
            isChecked = getConfigValue(KEY_HABILITAR_ADMIN, false)
            setOnCheckedChangeListener { _, isChecked ->
                saveConfigValue(KEY_HABILITAR_ADMIN, isChecked)
                UiNotifier.info(binding.root, "Modo administrador ${if (isChecked) "habilitado" else "deshabilitado"}")
            }
        }
    }

    private fun getConfigValue(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    private fun saveConfigValue(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}