package com.example.inventario20.ui.exportacion

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.inventario20.DBHelper

import com.example.inventario20.databinding.FragmentExportacionBinding

class ExportacionFragment : Fragment() {

    private var _binding: FragmentExportacionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val exportacionViewModel =
            ViewModelProvider(this).get(ExportacionViewModel::class.java)

        _binding = FragmentExportacionBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
