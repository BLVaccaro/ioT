package com.example.ejemplo1

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.UUID

object SyncManager {
    private const val URL = "http://98.89.107.89/sincronizar.php"
    private val main = Handler(Looper.getMainLooper())
    private var cola: RequestQueue? = null
    private var enCurso = false
    private var reintentoProgramado = false
    private var estabaSinConexion = false
    private var completadoHasta = 0L
    private var restablecidoHasta = 0L
    private var ultimoMotivo = ""

    private class Fila(
        val id: Long, val uuid: String, val nombre: String, val apellido: String,
        val email: String, val clave: String, val operacion: String
    )

    private class Resultado {
        var ok = 0
        var temporales = 0
        var rechazados = 0
        var motivo = ""
    }

    fun iniciar(ctx: Context) {
        val app = ctx.applicationContext
        estabaSinConexion = !NetworkHelper.hayConexion(app)

        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    main.post {
                        if (estabaSinConexion) {
                            estabaSinConexion = false
                            restablecidoHasta = System.currentTimeMillis() + 4000
                        }
                        sincronizar(app)
                    }
                }
            }

            override fun onLost(network: Network) {
                main.postDelayed({
                    if (!NetworkHelper.hayConexion(app)) estabaSinConexion = true
                }, 1500)
            }
        })
        sincronizar(app)
    }

    fun sincronizar(app: Context) {
        main.post {
            if (enCurso || !NetworkHelper.hayConexion(app)) return@post
            val filas = leerPendientes(app)
            if (filas.isEmpty()) return@post
            enCurso = true
            enviar(app, filas, 0, Resultado())
        }
    }

    private fun enviar(app: Context, filas: List<Fila>, i: Int, r: Resultado) {
        if (i >= filas.size) {
            terminar(app, r)
            return
        }
        val f = filas[i]
        val params = mapOf(
            "accion" to f.operacion, "uuid" to f.uuid, "nombres" to f.nombre,
            "apellidos" to f.apellido, "email" to f.email, "clave" to f.clave
        )
        val req = object : StringRequest(Method.POST, URL,
            { resp ->
                val resultado = try {
                    JSONObject(resp.trim()).optString("resultado")
                } catch (e: Exception) { "" }

                when (resultado) {
                    "ok" -> {
                        marcarSincronizado(app, f)
                        r.ok++
                    }
                    "email_duplicado" -> {
                        marcarRechazado(app, f)
                        r.rechazados++
                        r.motivo = "el correo ${f.email} ya existe en el servidor"
                    }
                    "datos_invalidos" -> {
                        marcarRechazado(app, f)
                        r.rechazados++
                        r.motivo = "el servidor consideró inválidos los datos de ${f.nombre}"
                    }
                    else -> r.temporales++
                }
                enviar(app, filas, i + 1, r)
            },
            { _ ->

                r.temporales += filas.size - i
                terminar(app, r)
            }) {
            override fun getParams() = params
        }
        if (cola == null) cola = Volley.newRequestQueue(app)
        cola!!.add(req)
    }

    private fun terminar(app: Context, r: Resultado) {
        enCurso = false
        if (r.rechazados > 0) ultimoMotivo = r.motivo
        // Solo se muestra "completada" si todo salió bien
        if (r.temporales == 0 && r.rechazados == 0) {
            completadoHasta = System.currentTimeMillis() + 6000
        }
        if (r.temporales > 0) programarReintento(app)
    }

    private fun programarReintento(app: Context) {
        if (reintentoProgramado) return
        reintentoProgramado = true
        main.postDelayed({
            reintentoProgramado = false
            sincronizar(app)
        }, 15000)
    }

    private fun leerPendientes(app: Context): List<Fila> {
        val db = ConexionDbHelper(app).writableDatabase

        val ids = mutableListOf<Long>()
        val c0 = db.rawQuery("SELECT ID FROM USUARIOS WHERE SINCRONIZADO = 0 AND (UUID IS NULL OR UUID = '')", null)
        while (c0.moveToNext()) ids.add(c0.getLong(0))
        c0.close()
        for (id in ids) {
            val v = ContentValues().apply { put("UUID", UUID.randomUUID().toString()) }
            db.update("USUARIOS", v, "ID = ?", arrayOf(id.toString()))
        }
        db.execSQL("UPDATE USUARIOS SET PENDIENTE = 'INSERT' WHERE SINCRONIZADO = 0 AND PENDIENTE IS NULL")

        val lista = mutableListOf<Fila>()
        val c = db.rawQuery(
            "SELECT ID, UUID, NOMBRE, APELLIDO, EMAIL, CLAVE, PENDIENTE FROM USUARIOS " +
                    "WHERE SINCRONIZADO = 0 AND PENDIENTE <> 'RECHAZADO' ORDER BY ID", null)
        while (c.moveToNext()) {
            lista.add(Fila(c.getLong(0), c.getString(1), c.getString(2) ?: "", c.getString(3) ?: "",
                c.getString(4) ?: "", c.getString(5) ?: "", c.getString(6)))
        }
        c.close(); db.close()
        return lista
    }

    private fun marcarSincronizado(app: Context, f: Fila) {
        val db = ConexionDbHelper(app).writableDatabase
        if (f.operacion == "DELETE") {
            db.delete("USUARIOS", "ID = ?", arrayOf(f.id.toString()))
        } else {
            val v = ContentValues().apply { put("SINCRONIZADO", 1); putNull("PENDIENTE") }
            db.update("USUARIOS", v, "ID = ?", arrayOf(f.id.toString()))
        }
        db.close()
    }

    private fun marcarRechazado(app: Context, f: Fila) {
        val db = ConexionDbHelper(app).writableDatabase
        val v = ContentValues().apply { put("PENDIENTE", "RECHAZADO") }
        db.update("USUARIOS", v, "ID = ?", arrayOf(f.id.toString()))
        db.close()
    }

    private fun contar(app: Context, condicion: String): Int {
        val db = ConexionDbHelper(app).readableDatabase
        val c = db.rawQuery("SELECT COUNT(*) FROM USUARIOS WHERE $condicion", null)
        c.moveToFirst()
        val n = c.getInt(0)
        c.close(); db.close()
        return n
    }

    fun contarPendientes(app: Context): Int =
        contar(app, "SINCRONIZADO = 0 AND (PENDIENTE IS NULL OR PENDIENTE <> 'RECHAZADO')")

    fun contarRechazados(app: Context): Int = contar(app, "PENDIENTE = 'RECHAZADO'")

    fun estado(app: Context): Pair<String, Int>? {
        val conectado = NetworkHelper.hayConexion(app)
        val pend = contarPendientes(app)
        val rech = contarRechazados(app)
        val sufRech = if (rech > 0) " · $rech rechazado(s)" else ""
        val ahora = System.currentTimeMillis()

        return when {
            !conectado && pend > 0 ->
                Pair("Sin conexión · $pend cambio(s) pendiente(s)$sufRech", Color.parseColor("#E65100"))
            !conectado ->
                Pair("Sin conexión · trabajando en modo local$sufRech", Color.parseColor("#E65100"))
            enCurso ->
                Pair("Sincronizando $pend cambio(s)...", Color.parseColor("#1565C0"))
            pend > 0 ->
                Pair("$pend cambio(s) pendiente(s) · se reintentará$sufRech", Color.parseColor("#B26A00"))
            rech > 0 -> {
                val detalle = if (ultimoMotivo.isNotEmpty()) ": $ultimoMotivo" else " por el servidor"
                Pair("$rech cambio(s) rechazado(s)$detalle", Color.parseColor("#B71C1C"))
            }
            ahora < completadoHasta ->
                Pair("Sincronización completada", Color.parseColor("#2E7D32"))
            ahora < restablecidoHasta ->
                Pair("Conexión restablecida", Color.parseColor("#2E7D32"))
            else -> null
        }
    }
}