package com.example.inventario20.ui

import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.inventario20.R
import com.google.android.material.snackbar.Snackbar

    object UiNotifier {
        private const val TAG = "UiNotifier"

        fun info(view: View, message: String) {
            Log.d(TAG, message)
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        }

        fun warning(view: View, message: String) {
            Log.w(TAG, message)
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).setBackgroundTint(
                ContextCompat.getColor(view.context, R.color.teal_700)
            ).show()
        }

        fun error(view: View, message: String) {
            Log.e(TAG, message)
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).setBackgroundTint(
                ContextCompat.getColor(view.context, R.color.design_default_color_error)
            ).show()
        }

        fun action(view: View, message: String, actionText: String, action: () -> Unit) {
            Log.d(TAG, "action: $message")
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText) { action() }
                .show()
        }

        fun fieldError(field: EditText, message: String) {
            Log.w(TAG, "field error: $message")
            field.error = message
            field.requestFocus()
        }
    }

//UiNotifier.info(binding.root, "Algo")
//UiNotifier.warning(binding.root, "Advertencia")
//UiNotifier.error(binding.root, "Mensaje de error")