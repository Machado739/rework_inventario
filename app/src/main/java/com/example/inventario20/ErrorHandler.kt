package com.example.inventario20

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Clase para manejo centralizado de errores en la aplicación
 */
object ErrorHandler {

    private const val TAG = "ErrorHandler"

    /**
     * Maneja excepciones de base de datos
     */
    fun manejarErrorBD(context: Context, exception: Exception, operacion: String = "operación") {
        Log.e(TAG, "Error en BD durante $operacion: ${exception.message}", exception)

        val mensaje = when {
            exception.message?.contains("foreign key", ignoreCase = true) == true ->
                "Error de integridad referencial en $operacion"
            exception.message?.contains("unique", ignoreCase = true) == true ->
                "Dato duplicado en $operacion"
            exception.message?.contains("not null", ignoreCase = true) == true ->
                "Campo requerido faltante en $operacion"
            else -> "Error en base de datos durante $operacion"
        }

        mostrarMensajeUsuario(context, mensaje)
    }

    /**
     * Maneja errores generales de la aplicación
     */
    fun manejarErrorGeneral(context: Context, exception: Exception, operacion: String = "operación") {
        Log.e(TAG, "Error general en $operacion: ${exception.message}", exception)
        mostrarMensajeUsuario(context, "Error inesperado en $operacion")
    }

    /**
     * Maneja errores de validación
     */
    fun manejarErrorValidacion(context: Context, mensaje: String) {
        Log.w(TAG, "Error de validación: $mensaje")
        mostrarMensajeUsuario(context, mensaje)
    }

    /**
     * Muestra mensaje al usuario
     */
    private fun mostrarMensajeUsuario(context: Context, mensaje: String) {
        try {
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar mensaje: ${e.message}")
        }
    }

    /**
     * Ejecuta una operación con manejo de errores
     */
    fun <T> ejecutarConManejoErrores(
        context: Context,
        operacion: String,
        bloque: () -> T
    ): T? {
        return try {
            bloque()
        } catch (e: Exception) {
            manejarErrorGeneral(context, e, operacion)
            null
        }
    }

    /**
     * Ejecuta una operación de BD con manejo específico de errores
     */
    fun <T> ejecutarOperacionBD(
        context: Context,
        operacion: String,
        bloque: () -> T
    ): T? {
        return try {
            bloque()
        } catch (e: Exception) {
            manejarErrorBD(context, e, operacion)
            null
        }
    }
}

/**
 * Extensión para Fragment para manejo de errores
 */
fun Fragment.manejarError(mensaje: String) {
    ErrorHandler.manejarErrorValidacion(requireContext(), mensaje)
}

fun Fragment.manejarErrorBD(exception: Exception, operacion: String = "operación") {
    ErrorHandler.manejarErrorBD(requireContext(), exception, operacion)
}
