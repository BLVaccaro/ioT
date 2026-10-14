package com.example.ejemplo1

import android.content.ContentValues
import android.content.Context
import java.text.Normalizer
import java.util.UUID

data class Usuario(
    val uuid: String, val nombre: String, val apellido: String,
    val email: String, val pendiente: String?
)

class UsuariosLocal(context: Context) {
    private val helper = ConexionDbHelper(context)

    private fun normalizar(s: String) =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "").lowercase()

    fun listar(filtro: String = ""): List<Usuario> {
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT UUID, NOMBRE, APELLIDO, EMAIL, PENDIENTE FROM USUARIOS WHERE ELIMINADO = 0 ORDER BY NOMBRE", null)
        val lista = mutableListOf<Usuario>()
        val f = normalizar(filtro.trim())
        while (c.moveToNext()) {
            val u = Usuario(c.getString(0), c.getString(1) ?: "", c.getString(2) ?: "", c.getString(3) ?: "", c.getString(4))
            if (f.isEmpty() || normalizar(u.nombre).contains(f) || normalizar(u.apellido).contains(f)) lista.add(u)
        }
        c.close(); db.close()
        return lista
    }

    private fun emailUsado(email: String, exceptoUuid: String?): Boolean {
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT 1 FROM USUARIOS WHERE lower(EMAIL) = lower(?) AND ELIMINADO = 0 AND UUID <> ?",
            arrayOf(email, exceptoUuid ?: ""))
        val usado = c.moveToFirst()
        c.close(); db.close()
        return usado
    }

    fun insertar(nombre: String, apellido: String, email: String, clave: String,
                 uuid: String = UUID.randomUUID().toString(), sincronizado: Boolean = false): Boolean {
        if (emailUsado(email, null)) return false
        val db = helper.writableDatabase
        val v = ContentValues().apply {
            put("UUID", uuid)
            put("NOMBRE", nombre); put("APELLIDO", apellido)
            put("EMAIL", email); put("CLAVE", clave)
            put("SINCRONIZADO", if (sincronizado) 1 else 0)
            if (!sincronizado) put("PENDIENTE", "INSERT")
            put("ELIMINADO", 0)
        }
        db.insert("USUARIOS", null, v)
        db.close()
        return true
    }

    fun modificar(uuid: String, nombre: String, apellido: String, email: String): Boolean {
        if (emailUsado(email, uuid)) return false
        val db = helper.writableDatabase
        val c = db.rawQuery("SELECT PENDIENTE FROM USUARIOS WHERE UUID = ?", arrayOf(uuid))
        val pendienteActual = if (c.moveToFirst()) c.getString(0) else null
        c.close()
        val v = ContentValues().apply {
            put("NOMBRE", nombre); put("APELLIDO", apellido); put("EMAIL", email)
            put("SINCRONIZADO", 0)
            // si nunca llegó al servidor, sigue siendo INSERT
            put("PENDIENTE", if (pendienteActual == "INSERT") "INSERT" else "UPDATE")
        }
        db.update("USUARIOS", v, "UUID = ?", arrayOf(uuid))
        db.close()
        return true
    }


    fun eliminar(uuid: String) {
        val db = helper.writableDatabase
        val c = db.rawQuery("SELECT PENDIENTE FROM USUARIOS WHERE UUID = ?", arrayOf(uuid))
        val pendiente = if (c.moveToFirst()) c.getString(0) else null
        c.close()
        if (pendiente == "INSERT") {
            db.delete("USUARIOS", "UUID = ?", arrayOf(uuid))
        } else {
            val v = ContentValues().apply {
                put("ELIMINADO", 1); put("SINCRONIZADO", 0); put("PENDIENTE", "DELETE")
            }
            db.update("USUARIOS", v, "UUID = ?", arrayOf(uuid))
        }
        db.close()
    }

    fun contarPendientes(): Int {
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT COUNT(*) FROM USUARIOS WHERE SINCRONIZADO = 0", null)
        c.moveToFirst()
        val n = c.getInt(0)
        c.close(); db.close()
        return n
    }

    fun obtener(uuid: String): Usuario? {
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT UUID, NOMBRE, APELLIDO, EMAIL, PENDIENTE FROM USUARIOS WHERE UUID = ? AND ELIMINADO = 0", arrayOf(uuid))
        val u = if (c.moveToFirst())
            Usuario(c.getString(0), c.getString(1) ?: "", c.getString(2) ?: "", c.getString(3) ?: "", c.getString(4))
        else null
        c.close(); db.close()
        return u
    }

    fun actualizarDesdeServidor(remotos: List<Usuario>) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val uuids = remotos.map { it.uuid }.toSet()
            for (r in remotos) {
                val c = db.rawQuery("SELECT SINCRONIZADO FROM USUARIOS WHERE UUID = ?", arrayOf(r.uuid))
                if (c.moveToFirst()) {
                    val sinc = c.getInt(0)
                    c.close()
                    if (sinc == 1) {
                        val v = ContentValues().apply {
                            put("NOMBRE", r.nombre); put("APELLIDO", r.apellido); put("EMAIL", r.email)
                        }
                        db.update("USUARIOS", v, "UUID = ?", arrayOf(r.uuid))
                    }
                } else {
                    c.close()
                    val v = ContentValues().apply {
                        put("UUID", r.uuid); put("NOMBRE", r.nombre); put("APELLIDO", r.apellido)
                        put("EMAIL", r.email); put("CLAVE", ""); put("SINCRONIZADO", 1); put("ELIMINADO", 0)
                    }
                    db.insert("USUARIOS", null, v)
                }
            }

            val c = db.rawQuery("SELECT UUID FROM USUARIOS WHERE SINCRONIZADO = 1", null)
            val borrar = mutableListOf<String>()
            while (c.moveToNext()) { val u = c.getString(0); if (u != null && u !in uuids) borrar.add(u) }
            c.close()
            for (u in borrar) db.delete("USUARIOS", "UUID = ?", arrayOf(u))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction(); db.close()
        }
    }
}