package com.example.inventario20

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.inventario20.databinding.ActivityMainBinding
import com.example.inventario20.ui.configuracion.ConfiguracionFragment
import com.example.inventario20.ui.exportacion.ExportacionFragment
import com.example.inventario20.ui.home.HomeFragment
import com.example.inventario20.ui.iniciar.IniciarInventarioFragment
import com.example.inventario20.ui.inventarios.InventariosFragment
import com.example.inventario20.ui.productos.ProductosFragment
import com.example.inventario20.ui.ubicaciones.UbicacionesFragment
import com.google.android.material.navigation.NavigationView
import java.io.FileInputStream

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appBarMain.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            irAFragmentInicial()
        }
    }

    // ---------------------------
    // NAVEGACIÓN CENTRALIZADA
    // ---------------------------
    private fun irAFragmentInicial() {
        val dbHelper = DBHelper(this)

        val fragment = if (dbHelper.obtenerInventarioActivo() == null) {
            IniciarInventarioFragment()
        } else {
            HomeFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_main, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val dbHelper = DBHelper(this)
        val inventarioActivoId = dbHelper.obtenerInventarioActivo()

        val fragment = when (item.itemId) {

            R.id.nav_home -> {
                if (inventarioActivoId == null) {
                    IniciarInventarioFragment()
                } else {
                    HomeFragment()
                }
            }

            R.id.nav_iniciar_inventario -> IniciarInventarioFragment()
            R.id.nav_productos -> ProductosFragment()
            R.id.nav_ubicaciones -> UbicacionesFragment()
            R.id.nav_inventarios -> InventariosFragment()
            R.id.nav_exportacion -> ExportacionFragment()
            R.id.nav_configuracion -> ConfiguracionFragment()

            else -> null
        }

        fragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_main, it)
                .commit()
        }

        binding.drawerLayout.closeDrawers()
        return true
    }

    // ---------------------------
    // OPCIONES DEL TOOLBAR
    // ---------------------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (drawerToggle.onOptionsItemSelected(item)) return true

        return when (item.itemId) {
            R.id.exportation_db -> {
                exportDatabase()
                true
            }

            R.id.clear_DB -> {
                limpiarBaseDeDatos()
                Toast.makeText(this, "Base de datos reiniciada", Toast.LENGTH_SHORT).show()
                irAFragmentInicial()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // ---------------------------
    // UTILIDADES
    // ---------------------------
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportDatabase() {
        try {
            val dbName = "MiBaseDatos.db"
            val dbFile = getDatabasePath(dbName)

            if (!dbFile.exists()) {
                Toast.makeText(this, "Base de datos no encontrada", Toast.LENGTH_SHORT).show()
                return
            }

            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, dbName)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            ) ?: return

            contentResolver.openOutputStream(uri).use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output!!)
                }
            }

            Toast.makeText(this, "BD exportada a Descargas", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al exportar BD", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarBaseDeDatos() {
        DBHelper(this).limpiarTablas()
    }
}
