package com.example.inventario20.ui.iniciar
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.inventario20.R


class Iniciar : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_iniciar, container, false)

        val iniciarBTN = root.findViewById<Button>(R.id.iniciarBTN)

        iniciarBTN.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("inventario_iniciado", true).apply()

            parentFragmentManager.popBackStack()
        }

        return root
    }



}