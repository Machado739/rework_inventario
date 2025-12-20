package com.example.inventario20
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(context: Context) : SQLiteOpenHelper(context.applicationContext, "MiBaseDatos.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
                CREATE TABLE Inventarios (
                    idinventarios INTEGER PRIMARY KEY,
                    nombre_inventario TEXT NOT NULL,
                    fecha_creacion TEXT NOT NULL,      -- formato: 'YYYY-MM-DD HH:MM:SS'
                    fecha_cierre TEXT,                 -- puede ser NULL
                    activo INTEGER NOT NULL            -- 0 = falso, 1 = verdadero
                )    
            """)

        db?.execSQL("""
            CREATE TABLE Registros (
                idregistro INTEGER PRIMARY KEY,
                tarimas INTEGER,
                cajas INTEGER,
                unidades INTEGER,
                suelto INTEGER,
                total INTEGER,
                fecha TEXT NOT NULL,               -- formato: 'YYYY-MM-DD HH:MM:SS'
                idubicacion INTEGER,
                idproducto TEXT,
                idcliente INTEGER,
                FOREIGN KEY (idubicacion) REFERENCES Ubicaciones(idubicacion),
                FOREIGN KEY (idproducto) REFERENCES Codigos(idproducto),
                FOREIGN KEY (idcliente) REFERENCES Cliente(idcliente)
            )    
        """)

        db?.execSQL("""
            CREATE TABLE Registros_Inventario (
                idregistro INTEGER,
                idinventarios INTEGER,
                PRIMARY KEY (idregistro, idinventarios),
                FOREIGN KEY (idregistro) REFERENCES Registros(idregistro),
                FOREIGN KEY (idinventarios) REFERENCES Inventarios(idinventarios)
            )   
        """)

        db?.execSQL("""
            CREATE TABLE Ubicaciones (
                idubicacion INTEGER PRIMARY KEY,
                ubicacion TEXT NOT NULL,
                idempresas INTEGER,
                FOREIGN KEY (idempresas) REFERENCES Empresas(idempresas)
            )
        """)

        db?.execSQL("""
            CREATE TABLE Empresas (
                idempresas INTEGER PRIMARY KEY,
                empresa TEXT NOT NULL
            )
        """)

        db?.execSQL("""
            CREATE TABLE Codigos (
                idproducto TEXT PRIMARY KEY,
                producto TEXT NOT NULL,
                medida TEXT NOT NULL
            )
        """)

        db?.execSQL("""
            CREATE TABLE Cliente (
                idcliente INTEGER PRIMARY KEY,
                cliente TEXT NOT NULL
            )
        """)
        insertarProductosIniciales (db)
        insertatClientesIniciales(db)
        insertarEmpresasIniciales(db)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Inventarios")
        db.execSQL("DROP TABLE IF EXISTS Registros")
        db.execSQL("DROP TABLE IF EXISTS Registros_Inventario")
        db.execSQL("DROP TABLE IF EXISTS Ubicaciones")
        db.execSQL("DROP TABLE IF EXISTS Empresas")
        db.execSQL("DROP TABLE IF EXISTS Codigos")
        db.execSQL("DROP TABLE IF EXISTS Cliente")

        onCreate(db)
    }
    // Data classes para representar las tablas
    data class Inventario(
        val idinventarios: Int,
        val nombreInventario: String,
        val fechaCreacion: String,
        val fechaCierre: String?,   // puede ser null si aún no se ha cerrado
        val activo: Int             // 0 = cerrado, 1 = activo
    )

    // Data class para la tabla Registros
    data class Registro(
        val idregistro: Int,
        val tarimas: Int,
        val cajas: Int,
        val unidades: Int,
        val suelto: Int,
        val total: Int,
        val fecha: String,
        val idubicacion: Int,
        val idproducto: String,
        val idcliente: Int
    )

    // Data class para la tabla Ubicaciones
    data class Ubicacion(
        val idubicacion: Int,
        val ubicacion: String,
        val idempresas: Int
    )

    // Data class para la tabla Empresas
    data class Empresa(
        val idempresas: Int,
        val empresa: String
    )

    //Data class para la tabla Codigos
    data class Codigo(
        val idproducto: String,
        val producto: String,
        val medida: String
    )

    //Data class para la tabla Cliente
    data class Cliente(
        val idcliente: Int,
        val cliente: String
    )


    // Metodo para insertar un nuevo inventario
    fun insertarInventario(nombreInventario: String, fechaCreacion: String, activo: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nombre_inventario", nombreInventario)
            put("fecha_creacion", fechaCreacion)
            put("activo", activo)
        }
        return db.insert("Inventarios", null, values)
    }
    //Metodo para insertar un nuevo registro
    fun insertarRegistro(tarimas: Int, cajas: Int, unidades: Int, suelto: Int, total: Int, fecha: String, idubicacion: Int, idproducto: String, idcliente: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("tarimas", tarimas)
            put("cajas", cajas)
            put("unidades", unidades)
            put("suelto", suelto)
            put("total", total)
            put("fecha", fecha)
            put("idubicacion", idubicacion)
            put("idproducto", idproducto)
            put("idcliente", idcliente)

        }
        return db.insert("Registros", null, values)
    }

    //Metodo para insertar un nuevo registro_inventario
    fun insertarRegistroInventario(idregistro: Int, idinventarios: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("idregistro", idregistro)
            put("idinventarios", idinventarios)
        }
        return db.insert("Registros_Inventario", null, values)
    }

    //Metodo para insertar una nueva ubicacion
    fun insertarUbicacion(ubicacion: String, idempresas: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("ubicacion", ubicacion)
            put("idempresas", idempresas)
        }
        return db.insert("Ubicaciones", null, values)
    }

    //Metodo para insertar una nueva empresa
    fun insertarEmpresa(empresa: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("empresa", empresa)
        }
        return db.insert("Empresas", null, values)
    }

    //Metodo para insertar un nuevo codigo
    fun insertarCodigo(idproducto: String, producto: String, medida: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("idproducto", idproducto)
            put("producto", producto)
            put("medida", medida)
        }
        return db.insert("Codigos", null, values)
    }

    //Metodo para insertar un nuevo cliente
    fun insertarCliente(cliente: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("cliente", cliente)
        }
        return db.insert("Cliente", null, values)
    }

    // Metodo para cerrar un inventario
    fun cerrarInventario(idinventarios: Int, fechaCierre: String, activo: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("fecha_cierre", fechaCierre)
            put("activo", activo)
        }
        return db.update(
            "Inventarios",
            values,
            "idinventarios = ?",
            arrayOf(idinventarios.toString())
        )
    }

    /// Metodo para obtener todos los inventarios
    fun obtenerInventarios(): List<Inventario> {
        val inventarios = mutableListOf<Inventario>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Inventarios", null)

        with(cursor) {
            while (moveToNext()) {
                val idinventarios = getInt(getColumnIndexOrThrow("idinventarios"))
                val nombreInventario = getString(getColumnIndexOrThrow("nombre_inventario"))
                val fechaCreacion = getString(getColumnIndexOrThrow("fecha_creacion"))
                val fechaCierre = getString(getColumnIndexOrThrow("fecha_cierre"))
                val activo = getInt(getColumnIndexOrThrow("activo"))

                inventarios.add(
                    Inventario(
                        idinventarios,
                        nombreInventario,
                        fechaCreacion,
                        fechaCierre,
                        activo
                    )
                )
            }
        }
        cursor.close()
        return inventarios
    }


    // Metodo para obtener todos los registros
    fun obtenerRegistros(): List<Registro> {
        val registros = mutableListOf<Registro>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Registros", null)
        with(cursor) {
            while (moveToNext()) {
                val idregistro = getInt(getColumnIndexOrThrow("idregistro"))
                val tarimas = getInt(getColumnIndexOrThrow("tarimas"))
                val cajas = getInt(getColumnIndexOrThrow("cajas"))
                val unidades = getInt(getColumnIndexOrThrow("unidades"))
                val suelto = getInt(getColumnIndexOrThrow("suelto"))
                val total = getInt(getColumnIndexOrThrow("total"))
                val fecha = getString(getColumnIndexOrThrow("fecha"))
                val idubicacion = getInt(getColumnIndexOrThrow("idubicacion"))
                val idproducto = getString(getColumnIndexOrThrow("idproducto"))
                val idcliente = getInt(getColumnIndexOrThrow("idcliente"))
                registros.add(
                    Registro(
                        idregistro,
                        tarimas,
                        cajas,
                        unidades,
                        suelto,
                        total,
                        fecha,
                        idubicacion,
                        idproducto,
                        idcliente
                    )
                )
            }
        }
        cursor.close()
        return registros
    }

    // Metodo para obtener todas las ubicaciones
    fun obtenerUbicaciones(): List<Ubicacion> {
        val ubicaciones = mutableListOf<Ubicacion>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Ubicaciones", null)
        with(cursor) {
            while (moveToNext()) {
                val idubicacion = getInt(getColumnIndexOrThrow("idubicacion"))
                val ubicacion = getString(getColumnIndexOrThrow("ubicacion"))
                val idempresas = getInt(getColumnIndexOrThrow("idempresas"))
                ubicaciones.add(
                    Ubicacion(
                        idubicacion,
                        ubicacion,
                        idempresas
                    )
                )
            }
        }
        cursor.close()
        return ubicaciones
    }

    // Metodo para obtener todas las empresas
    fun obtenerEmpresas(): List<Empresa> {
        val empresas = mutableListOf<Empresa>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Empresas", null)
        with(cursor) {
            while (moveToNext()) {
                val idempresas = getInt(getColumnIndexOrThrow("idempresas"))
                val empresa = getString(getColumnIndexOrThrow("empresa"))
                empresas.add(
                    Empresa(
                        idempresas,
                        empresa
                    )
                )
            }
        }
        cursor.close()
        return empresas
    }

    // Metodo para obtener todos los codigos
    fun obtenerCodigos(): List<Codigo> {
        val codigos = mutableListOf<Codigo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Codigos", null)
        with(cursor) {
            while (moveToNext()) {
                val idproducto = getString(getColumnIndexOrThrow("idproducto"))
                val producto = getString(getColumnIndexOrThrow("producto"))
                val medida = getString(getColumnIndexOrThrow("medida"))
                codigos.add(
                    Codigo(
                        idproducto,
                        producto,
                        medida
                    )
                )
            }
        }
        cursor.close()
        return codigos
    }

    // Metodo para obtener todos los clientes
    fun obtenerClientes(): List<Cliente> {
        val clientes = mutableListOf<Cliente>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Cliente", null)
        with(cursor) {
            while (moveToNext()) {
                val idcliente = getInt(getColumnIndexOrThrow("idcliente"))
                val cliente = getString(getColumnIndexOrThrow("cliente"))
                clientes.add(
                    Cliente(
                        idcliente,
                        cliente
                    )
                )
            }
        }
        cursor.close()
        return clientes
    }

    // Metodo para obtener todos los registros de un inventario específico
    fun obtenerRegistrosDeInventario(idinventarios: Int): List<Registro> {
        val registros = mutableListOf<Registro>()
        val db = this.readableDatabase
        val query = """
            SELECT r.*
            FROM Registros r
            INNER JOIN Registros_Inventario ri ON r.idregistro = ri.idregistro
            WHERE ri.idinventarios = ?
        """
        val cursor = db.rawQuery(query, arrayOf(idinventarios.toString()))
        with(cursor) {
            while (moveToNext()) {
                val idregistro = getInt(getColumnIndexOrThrow("idregistro"))
                val tarimas = getInt(getColumnIndexOrThrow("tarimas"))
                val cajas = getInt(getColumnIndexOrThrow("cajas"))
                val unidades = getInt(getColumnIndexOrThrow("unidades"))
                val suelto = getInt(getColumnIndexOrThrow("suelto"))
                val total = getInt(getColumnIndexOrThrow("total"))
                val fecha = getString(getColumnIndexOrThrow("fecha"))
                val idubicacion = getInt(getColumnIndexOrThrow("idubicacion"))
                val idproducto = getString(getColumnIndexOrThrow("idproducto"))
                val idcliente = getInt(getColumnIndexOrThrow("idcliente"))
                registros.add(
                    Registro(
                        idregistro,
                        tarimas,
                        cajas,
                        unidades,
                        suelto,
                        total,
                        fecha,
                        idubicacion,
                        idproducto,
                        idcliente
                    )
                )
            }
        }
        cursor.close()
        return registros
    }


    // Metodo para eliminar un inventario por su ID
    fun eliminarInventario(idinventarios: Int): Int {
        val db = this.writableDatabase
        return db.delete("Inventarios", "idinventarios = ?", arrayOf(idinventarios.toString()))
    }

    // Metodo para eliminar un registro por su ID
    fun eliminarRegistro(idregistro: Int): Int {
        val db = this.writableDatabase
        return db.delete("Registros", "idregistro = ?", arrayOf(idregistro.toString()))
    }

    // Metodo para eliminar una ubicacion por su ID
    fun eliminarUbicacion(idubicacion: Int): Int {
        val db = this.writableDatabase
        return db.delete("Ubicaciones", "idubicacion = ?", arrayOf(idubicacion.toString()))
    }

    // Metodo para eliminar una empresa por su ID
    fun eliminarEmpresa(idempresas: Int): Int {
        val db = this.writableDatabase
        return db.delete("Empresas", "idempresas = ?", arrayOf(idempresas.toString()))
    }

    // Metodo para eliminar un codigo por su ID
    fun eliminarCodigo(idproducto: String): Int {
        val db = this.writableDatabase
        return db.delete("Codigos", "idproducto = ?", arrayOf(idproducto))
    }

    // Metodo para eliminar un cliente por su ID
    fun eliminarCliente(idcliente: Int): Int {
        val db = this.writableDatabase
        return db.delete("Cliente", "idcliente = ?", arrayOf(idcliente.toString()))
    }

    // Metodo para actualizar un inventario
    fun actualizarInventario(idinventarios: Int, nombreInventario: String, fechaCreacion: String, fechaCierre: String?, activo: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nombre_inventario", nombreInventario)
            put("fecha_creacion", fechaCreacion)
            put("fecha_cierre", fechaCierre)
            put("activo", activo)
        }
        return db.update(
            "Inventarios",
            values,
            "idinventarios = ?",
            arrayOf(idinventarios.toString())
        )
    }

    // Metodo para actualizar un registro
    fun actualizarRegistro(idregistro: Int, tarimas: Int, cajas: Int, unidades: Int, suelto: Int, total: Int, fecha: String, idubicacion: Int, idproducto: String, idcliente: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("tarimas", tarimas)
            put("cajas", cajas)
            put("unidades", unidades)
            put("suelto", suelto)
            put("total", total)
            put("fecha", fecha)
            put("idubicacion", idubicacion)
            put("idproducto", idproducto)
        }
        return db.update(
            "Registros",
            values,
            "idregistro = ?",
            arrayOf(idregistro.toString())
        )
    }

    // Metodo para actualizar una ubicacion
    fun actualizarUbicacion(idubicacion: Int, ubicacion: String, idempresas: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("ubicacion", ubicacion)
            put("idempresas", idempresas)
        }
        return db.update(
            "Ubicaciones",
            values,
            "idubicacion = ?",
            arrayOf(idubicacion.toString())
        )
    }

    // Metodo para actualizar una empresa
    fun actualizarEmpresa(idempresas: Int, empresa: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("empresa", empresa)
        }
        return db.update(
            "Empresas",
            values,
            "idempresas = ?",
            arrayOf(idempresas.toString())
        )
    }


    // Metodo para actualizar un codigo
    fun actualizarCodigo(idproducto: String, producto: String, medida: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("producto", producto)
            put("medida", medida)

        }
        return db.update(
            "Codigos",
            values,
            "idproducto = ?",
            arrayOf(idproducto)
        )
    }

    // Metodo para actualizar un cliente
    fun actualizarCliente(idcliente: Int, cliente: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("cliente", cliente)
        }
        return db.update(
            "Cliente",
            values,
            "idcliente = ?",
            arrayOf(idcliente.toString())
        )
    }

    fun insertatClientesIniciales(db: SQLiteDatabase?) {
        val clientesIniciales = listOf(
            Cliente(1, "Muranaka"),
            Cliente(2, "Coastal Fresh"),
            Cliente(3, "Rainfield")
        )
        for (cliente in clientesIniciales) {
            val values = ContentValues().apply {
                put("idcliente", cliente.idcliente)
                put("cliente", cliente.cliente)
            }
            db?.insert("Cliente", null, values)
        }
    }

    fun insertarEmpresasIniciales(db: SQLiteDatabase?) {
        val empresasIniciales = listOf(
            Empresa(1, "Agrimex"),
            Empresa(2, "Cosmar"),
            Empresa(3, "Mextlan")
        )
        for (empresa in empresasIniciales) {
            val values = ContentValues().apply {
                put("idempresas", empresa.idempresas)
                put("empresa", empresa.empresa)
            }
            db?.insert("Empresas", null, values)
        }
    }
    // Metodo para insertar productos  iniciales
    fun insertarProductosIniciales(db: SQLiteDatabase?) {
        val productosIniciales = listOf(
            Codigo("APCF0001", "CINCHOS PARA PALLETS", "PZA"),
            Codigo("APMK0708", "AMARRE PARA BOLSA 25 LBS", "PZA"),
            Codigo("APMK0709", "AMARRE PARA PALLETS", "PZA"),
            Codigo("APRF0753", "CINCHOS DE PLASTICO PARA BOLSA DE 25 LBS", "PZA"),
            Codigo("APRF0754", "CINCHOS PARA TARIMA", "PZA"),
            Codigo("AVCF0001", "AMARRE PARA CILANTRO AGRIMEX", "CAJ"),
            Codigo("AVCF0002", "AMARRES PARA CILANTRO COASTAL ", "CAJ"),
            Codigo("AVCF0003", "AMARRES DE PEREJIL CHINO Convencional ", "CAJ"),
            Codigo("AVCF0004", "AMARRES DE PEREJIL ITALIANO CONVENCIONAL ", "CAJ"),
            Codigo("AVMK0735", "AMARRES PARA CILANTRO", "CAJ"),
            Codigo("AVMK0736", "AMARRE PARA KALE", "CAJ"),
            Codigo("AVMK0737", "AMARRE PARA BETABEL", "CAJ"),
            Codigo("AVMK0738", "AMARRE PARA PEREJIL CHINO", "CAJ"),
            Codigo("AVMK0739", "AMARRE PARA PEREJIL ITALIANO", "CAJ"),
            Codigo("AVMK0740", "AMARRE P/CILANTRO ORGANICO", "CAJ"),
            Codigo("AVMK0741", "AMARRE P/BETABEL ORGANICO", "CAJ"),
            Codigo("AVMK0742", "AMARRE P/KALE ORGANICO", "CAJ"),
            Codigo("AVMK0743", "AMARRE P/PEREJIL ITALIANO ORGANICO", "CAJ"),
            Codigo("AVMK0744", "AMARRE P/PEREJIL CHINO ORGANICO", "CAJ"),
            Codigo("BLCF0001", "BOLSA DE PLASTICO 16X20 IMPRESA", "BOL"),
            Codigo("BLCF0002", "BOLSA DE PLASTICO 16X20 ", "BOL"),
            Codigo("BLCF0003", "BOLSA DE PLASTICO 14X16", "BOL"),
            Codigo("BLCF0004", "BOLSA PARA TARIMA ", "BOL"),
            Codigo("BLCF0005", "BOLSA DE PLASTICO 5.5 OZ", "BOL"),
            Codigo("BLCF0006", "BOLSA DE PLASTICO STEPAC 32X21", "BOL"),
            Codigo("BLMK0663", "BOLSA PARA TARIMA", "BOL"),
            Codigo("BLMK0664", "BOLSA DE PLASTICO 14X15.75", "BOL"),
            Codigo("BLMK0665", "BOLSA DE PLASTICO 16X20", "BOL"),
            Codigo("BLMK0666", "BOLSA DE PLASTICO STEPAC 32X21", "BOL"),
            Codigo("BLMK0668", "BOLSA DE PLASTICO RABANO 25LBS", "BOL"),
            Codigo("BLMK0670", "BOLSA DE PLASTICO 12.5X16.5", "BOL"),
            Codigo("BLMK0671", "BOLSA DE PLASTICO 16X20 IMPRESA", "BOL"),
            Codigo("BLMK0672", "BOLSA DE PLASTICO 16X 18", "PZA"),
            Codigo("BLRF0727", "G.O. BOLSA PARA TARIMA", "BOL"),
            Codigo("BLRF0728", "G.O. ICELESS BOLSA 16X20", "BOL"),
            Codigo("BLRF0729", "BOLSA DE PLASTICO 14.5X37 5.5OZ", "BOL"),
            Codigo("CBMK0738", "CUBIERTA DE PLASTICO PARA RPC 38", "PZA"),
            Codigo("CIMK0001", "RIBBON ZEBRA", "ROL"),
            Codigo("CPCF0002", "G.O. REGULAR MEDIANO PLASTICO ", "CAR"),
            Codigo("CPCF0004", "VEGETALES 60 BONCHES PLASTICO ( CILANTRO)", "PZA"),
            Codigo("CPCF0005", "VEGETALES 30 BONCHES PLASTICO (CILANTRO 2.5)", "PZA"),
            Codigo("CPMK0675", "RPC #6411", "CAJ"),
            Codigo("CPMK0677", "RPC #6416", "CAJ"),
            Codigo("CPMK0678", "RPC #6419", "CAJ"),
            Codigo("CPMK0679", "RPC #6425", "CAJ"),
            Codigo("CPRR0001", "G.O REGULAR PLASTICO", "PZA"),
            Codigo("CPRR0002", "G.O. SMALL PLASTICO", "PZA"),
            Codigo("CSCF0001", "CANDADOS PARA TROQUE", "PZA"),
            Codigo("CSMK0758", "CANDADOS PARA TROQUE", "PZA"),
            Codigo("CSRF0759", "CANDADOS PARA TROQUE", "PZA"),
            Codigo("CTCF0002", "G.O. ICELESS COASTAL FRESH", "CAR"),
            Codigo("CTCF0003", "G.O. EXPORT ICELESS", "CAR"),
            Codigo("CTCF0004", "G.O. REGULAR A&J COASTAL FRESH", "CAR"),
            Codigo("CTCF0005", "G.O. ORGANICO COASTAL FRESH", "CAR"),
            Codigo("CTCF0007", "VEGETALES COASTAL( CILANTRO)", "CAR"),
            Codigo("CTCF0009", "R.D. 4DZ ", "PZA"),
            Codigo("CTCF0010", "VEGETALES 30 BONCHES  ", "CAR"),
            Codigo("CTMK0773", "G.O. 20 LBS LOOSE", "PZA"),
            Codigo("CTMK0774", "G.O. ICELESS DOMESTICO SMALL NO. 1", "PZA"),
            Codigo("CTMK0775", "G.O. REGULAR SMALL", "PZA"),
            Codigo("CTMK0776", "G.O. REGULAR MEDIANO", "PZA"),
            Codigo("CTMK0777", "G.O. ICELESS LARGE", "PZA"),
            Codigo("CTMK0778", "RD 4DZ 14", "PZA"),
            Codigo("CTMK0779", "RD DOBLE BONCHE/LARGE 16", "PZA"),
            Codigo("CTMK0780", "CILANTRO 5DZ", "PZA"),
            Codigo("CTMK0781", "BEETS 1DZ", "PZA"),
            Codigo("CTMK0782", "PEREJIL ITALIANO 5DZ", "PZA"),
            Codigo("CTMK0783", "PEREJIL CHINO 5DZ", "PZA"),
            Codigo("CTMK0784", "G.O. ICELESS DOMESTICO SMALL NO. 3", "PZA"),
            Codigo("CTMK0786", "G.O. ICELESS ORGANICO", "PZA"),
            Codigo("CTMK0787", "BEETS 1DZ ORGANICO", "PZA"),
            Codigo("CTMK0788", "VEGETALES ORGANICO 30 BONCHES", "PZA"),
            Codigo("CTMK0789", "RABANO ORGANICO 4DZ", "PZA"),
            Codigo("CTMK0791", "PEREJIL CHINO 2.5 DZ", "PZA"),
            Codigo("CTMK0792", "PEREJIL ITALIANO 2.5 DZ", "PZA"),
            Codigo("CTMK0793", "CILANTRO 2.5 DZ", "PZA"),
            Codigo("CTMK0794", "VEGETALES 30 BONCHES CONVENCIONAL", "PZA"),
            Codigo("CTRF0747", "G.O. REGULAR MEDIANO SAKURA", "CAR"),
            Codigo("CTRF0748", "G.O. SAKURA ICELESS DOMESTICO", "CAR"),
            Codigo("ESCF0001", "ESQUINERO DE CARTON", "PZA"),
            Codigo("ESMK0702", "ESQUINEROS DE PLASTICO", "PZA"),
            Codigo("ESMK0703", "ESQUINEROS DE CARTON ", "PZA"),
            Codigo("ESRF0710", "ESQUINEROS DE CARTON ", "PZA"),
            Codigo("ETCF0001", "ETIQUETA DE CODIGO PARA CEBOLLIN", "PZA"),
            Codigo("ETCF0003", "ETIQUETA ADHESIVA PARA PALLET ", "PZA"),
            Codigo("ETCF0004", "ETIQUETA CON LIGA P/CEBOLLIN ORGANICO", "CAJ"),
            Codigo("ETCF0005", "ETIQUETA CON LIGA P/ CEBOLLIN CONVENCIONAL ", "CAJ"),
            Codigo("ETCF0006", "ETIQUETA CON LIGA P/RABANO CONVENCIONAL", "CAJ"),
            Codigo("ETMK0768", "CINTA ADHESIVA PARA CEBOLLIN (PRO-TAPE)", "ROL"),
            Codigo("ETMK0769", "ETIQUETA ADHESIVA G.O. 4DZ", "PZA"),
            Codigo("ETMK0770", "ETIQUETA ADHESIVA RABANO 4DZ", "PZA"),
            Codigo("ETMK0771", "ETIQUETA ADHESIVA RABANO 2DZ", "PZA"),
            Codigo("ETMK0772", "ETIQUETA ADHESIVA P/ CILANTRO 5DZ", "PZA"),
            Codigo("ETMK0773", "ETIQUETA ADHESIVA P/CILANTRO 2.5DZ", "PZA"),
            Codigo("ETMK0774", "ETIQUETA ADHESIVA P/BETABEL 1DZ", "PZA"),
            Codigo("ETMK0775", "ETIQUETA DE CODIGO P/ CEBOLLIN ", "PZA"),
            Codigo("ETMK0776", "ETIQUETA ADHESIVA ITALIANO 30 BONCHES", "PZA"),
            Codigo("ETMK0777", "ETIQUETA ADHESIVA ITALIANO 60 BONCHES", "PZA"),
            Codigo("ETMK0778", "ETIQUETA ADHESIVA PEREJIL CHINO 30 BONCHES", "PZA"),
            Codigo("ETMK0779", "ETIQUETA ADHESIVA PEREJIL CHINO 60 BONCHES", "PZA"),
            Codigo("ETMK0780", "ETIQUETA DE RPC PARA IMPRESORA ZEBRA", "PZA"),
            Codigo("ETMK0781", "ETIQUETA DE TARIMA PARA IMPRESORA ZEBRA", "PZA"),
            Codigo("ETMK0782", "ETIQUETA C/LIGA P/CEBOLLIN CONVENCIONAL", "CAJ"),
            Codigo("ETMK0785", "ETIQUETA C/LIGA PARA RABANO CONVENCIONAL", "CAJ"),
            Codigo("ETMK0786", "ETIQUETA C/LIGA PARA CEBOLLIN ORGANICO", "CAJ"),
            Codigo("ETMK0787", "ETIQUETA C/LIGA P/RABANO ORGANICO", "CAJ"),
            Codigo("ETMK0788", "ETIQUETA C/LIGA PARA LEEK", "CAJ"),
            Codigo("ETMK0790", "ETIQUETA PACKED ON", "ROL"),
            Codigo("ETMK0791", "ETIQUETA P/ORGANICO", "PZA"),
            Codigo("ETRF0782", "ETIQUETA PARA TARIMA", "PZA"),
            Codigo("ETRF0783", "ETIQUETA DE CODIGO P/CEBOLLIN", "PZA"),
            Codigo("ETRF0784", "ETIQUETA DE RPC MUSTARD GREEN", "PZA"),
            Codigo("ETRF0785", "ETIQUETA DE RPC MUSTARD MAROON", "PZA"),
            Codigo("ETRF0786", "ETIQUETA DE RPC COLLARDS", "PZA"),
            Codigo("ETRF0787", "ETIQUETA DE RPC RADISH DAIKON", "PZA"),
            Codigo("ETRF0788", "ETIQUETA CON LIGA PARA CEBOLLIN CONVENCIONAL ", "CAJ"),
            Codigo("FLCF0001", "FLEJE #18", "ROL"),
            Codigo("FLMK0739", "FLEJE #18", "ROL"),
            Codigo("FLRF0779", "FLEJE #18", "ROL"),
            Codigo("LGCF0001", "LIGA AZUL #14", "CAJ"),
            Codigo("LGMK0680", "LIGA AZUL #14", "CAJ"),
            Codigo("LGMK0681", "LIGA ROJA #32", "CAJ"),
            Codigo("LGMK0682", "LIGA AZUL #81", "CAJ"),
            Codigo("LGRF0706", "LIGA AZUL #14", "CAJ"),
            Codigo("MDMK0740", "MARCADORES PARA CARTON", "PZA"),
            Codigo("MDRF0001", "MARCADORES PARA CARTON ", "PZA"),
            Codigo("PLCF0001", "PAPEL SEPARADOR 10.5X14", "PZA"),
            Codigo("PLCF0002", "PAPEL SEPARADOR 13X19", "PZA"),
            Codigo("PLMK0684", "PAPEL SEPARADOR 11X14", "PZA"),
            Codigo("PLMK0685", "PAPEL SEPARADOR 13X19", "PZA"),
            Codigo("PLMK0686", "PAPEL SEPARADOR 15X24", "PZA"),
            Codigo("PLRF0707", "PAPEL SEPARADOR 10X14", "PZA"),
            Codigo("RGMK0756", "RYAN REGISTRADORES DE TEMPERATURA", "PZA"),
            Codigo("TACF0001", "TARIMA DE MADERA", "PZA"),
            Codigo("TAMK0724", "TARIMA DE MADERA #1 40X48", "PZA"),
            Codigo("TAMK0725", "TARIMA CHEP", "PZA"),
            Codigo("TARF0725", "TARIMA #1", "PZA")
        )
        for (producto in productosIniciales) {
            val values = ContentValues().apply {
                put("idproducto", producto.idproducto)
                put("producto", producto.producto)
                put("medida", producto.medida)
            }
            db?.insert("Codigos", null, values)
        }
    }

    //funcion para insertar clientes iniciales
    fun insertarClientesIniciales() {
        val clientesIniciales = listOf(
            Cliente(1, "MURANAKA"),
            Cliente(2, "COASTAL FRESH"),
            Cliente(3, "SAKURA"),

        )
        val db = this.writableDatabase
        for (cliente in clientesIniciales) {
            val values = ContentValues().apply {
                put("idcliente", cliente.idcliente)
                put("cliente", cliente.cliente)
            }
            db.insert("Cliente", null, values)
        }
    }

    //funcion para insertar empresas iniciales
    fun insertarEmpresasIniciales() {
        val empresasIniciales = listOf(
            Empresa(1, "AGRIMEX"),
            Empresa(2, "MEXTRAL"),
            Empresa(3, "COSMAR")
        )
        val db = this.writableDatabase
        for (empresa in empresasIniciales) {
            val values = ContentValues().apply {
                put("idempresas", empresa.idempresas)
                put("empresa", empresa.empresa)
            }
        }
    }

    /// Metodo para limpiar todas las tablas (para pruebas)
    fun limpiarTablas() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM Inventarios")
        db.execSQL("DELETE FROM Registros")
        db.execSQL("DELETE FROM Registros_Inventario")
        db.execSQL("DELETE FROM Ubicaciones")
        db.execSQL("DELETE FROM Empresas")
        db.execSQL("DELETE FROM Codigos")
        db.execSQL("DELETE FROM Cliente")
    }

    /// Metodo para contar registros en una tabla
    fun contarRegistros(tabla: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $tabla", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    /// Metodo para verificar si una tabla existe
    fun tablaExiste(tabla: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tabla)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }







}