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
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.inventario20.databinding.ActivityMainBinding
import java.io.FileInputStream
import com.example.inventario20.ui.configuracion.ConfiguracionFragment
import com.example.inventario20.ui.exportacion.ExportacionFragment
import com.example.inventario20.ui.home.HomeFragment
import com.example.inventario20.ui.iniciar.IniciarInventarioFragment
import com.example.inventario20.ui.inventarios.InventariosFragment
import com.example.inventario20.ui.productos.ProductosFragment
import com.example.inventario20.ui.ubicaciones.UbicacionesFragment

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

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

        // Drawer
        binding.navView.setNavigationItemSelectedListener(this)


        if (savedInstanceState == null) {
            val dbHelper = DBHelper(this)
            val inventarioActivoId = dbHelper.obtenerInventarioActivo()

            val fragmentInicial = if (inventarioActivoId == null) {
                habilitarDrawer(true) // 👈 ahora SÍ se ve
                IniciarInventarioFragment()
            } else {
                habilitarDrawer(true)
                HomeFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, fragmentInicial)
                .commit()
        }

    }

    fun habilitarDrawer(habilitar: Boolean) {
        if (habilitar) {
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            drawerToggle.isDrawerIndicatorEnabled = true
        } else {
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            drawerToggle.isDrawerIndicatorEnabled = false
        }
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
                .replace(R.id.nav_host_fragment_content_main, it)
                .commit()
        }

        binding.drawerLayout.closeDrawers()
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
                Toast.makeText(this, "Base de datos Reiniciada", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportDatabase() {
        try {
            val dbName = "MiBaseDatos.db" //
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
            )

            if (uri == null) {
                Toast.makeText(this, "Error al crear archivo", Toast.LENGTH_SHORT).show()
                return
            }

            contentResolver.openOutputStream(uri).use { outputStream ->
                FileInputStream(dbFile).use { inputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream?.write(buffer, 0, length)
                    }
                }
            }

            Toast.makeText(this, "Base de datos exportada a Descargas", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al exportar BD", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarBaseDeDatos() {
        val dbHelper = DBHelper(this)
        dbHelper.limpiarTablas()
    }





    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }







}