package com.example.ejemplo1

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class ModificarEliminar1 : AppCompatActivity() {

    private var idUsuario: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_modificar_eliminar1)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etNombres = findViewById<EditText>(R.id.modNombres)
        val etApellidos = findViewById<EditText>(R.id.modApellidos)
        val etEmail = findViewById<EditText>(R.id.modEmail)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarMod)
        val btnEliminar = findViewById<Button>(R.id.btnEliminarMod)

        // Recibimos los datos enviados desde la lista
        idUsuario = intent.getStringExtra("ID") ?: ""
        etNombres.setText(intent.getStringExtra("NOMBRES"))
        etApellidos.setText(intent.getStringExtra("APELLIDOS"))
        etEmail.setText(intent.getStringExtra("EMAIL"))

        val letrasRegex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()

        // ACCIÓN: GUARDAR
        btnGuardar.setOnClickListener {
            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (nombres.isEmpty() || apellidos.isEmpty() || email.isEmpty()) {
                mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error", "Todos los campos son obligatorios.")
            } else if (!letrasRegex.matches(nombres) || !letrasRegex.matches(apellidos)) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Nombres y Apellidos solo aceptan letras y espacios.")
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Formato de correo inválido.")
            } else {
                if (NetworkHelper.hayConexion(this)) {
                    modificarAWS(idUsuario, nombres, apellidos, email)
                } else {
                    modificarSQLite(email, nombres, apellidos)
                }
            }
        }


        btnEliminar.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Estás seguro?")
                .setContentText("Esta acción eliminará al usuario.")
                .setConfirmText("Sí, eliminar")
                .setCancelText("No")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    if (NetworkHelper.hayConexion(this)) {
                        eliminarAWS(idUsuario)
                    } else {
                        eliminarSQLite(etEmail.text.toString().trim())
                    }
                }
                .setCancelClickListener { it.dismissWithAnimation() }
                .show()
        }
    }

    private fun mostrarAlerta(tipo: Int, titulo: String, mensaje: String) {
        SweetAlertDialog(this, tipo)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Entendido")
            .setConfirmClickListener { it.dismissWithAnimation() }
            .show()
    }


    private fun modificarAWS(id: String, nombres: String, apellidos: String, email: String) {
        val url = "http://98.89.107.89/modificar.php"
        val queue = Volley.newRequestQueue(this)
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Actualizando..."
        pDialog.setCancelable(false)
        pDialog.show()

        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                if (response.trim() == "exito") {
                    pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                    pDialog.titleText = "¡Éxito!"
                    pDialog.contentText = "Usuario actualizado en AWS."
                    pDialog.setConfirmText("Volver")
                    pDialog.setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                        finish()
                    }
                } else {
                    pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                    pDialog.titleText = "Error"
                    pDialog.contentText = "No se pudo actualizar."
                    pDialog.setConfirmClickListener { it.dismissWithAnimation() }
                }
            },
            { error ->
                pDialog.dismissWithAnimation()
                modificarSQLite(email, nombres, apellidos)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id
                params["nombres"] = nombres
                params["apellidos"] = apellidos
                params["email"] = email
                return params
            }
        }
        queue.add(request)
    }

    private fun eliminarAWS(id: String) {
        val url = "http://98.89.107.89/eliminar.php"
        val queue = Volley.newRequestQueue(this)
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Eliminando..."
        pDialog.setCancelable(false)
        pDialog.show()

        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                if (response.trim() == "exito") {
                    pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                    pDialog.titleText = "¡Eliminado!"
                    pDialog.contentText = "Usuario borrado de AWS."
                    pDialog.setConfirmText("Volver")
                    pDialog.setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                        finish()
                    }
                } else {
                    pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                    pDialog.titleText = "Error"
                    pDialog.contentText = "No se pudo eliminar."
                    pDialog.setConfirmClickListener { it.dismissWithAnimation() }
                }
            },
            { error ->
                pDialog.dismissWithAnimation()
                eliminarSQLite(intent.getStringExtra("EMAIL") ?: "")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id
                return params
            }
        }
        queue.add(request)
    }

    // Funciones SQLite (Modo Offline)
    private fun modificarSQLite(email: String, nombres: String, apellidos: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        try {
            val datos = ContentValues().apply {
                put("NOMBRE", nombres)
                put("APELLIDO", apellidos)
            }
            db.update("USUARIOS", datos, "EMAIL=?", arrayOf(email))
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Modo Offline")
                .setContentText("Usuario actualizado localmente.")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    finish()
                }.show()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }

    private fun eliminarSQLite(email: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        try {
            db.delete("USUARIOS", "EMAIL=?", arrayOf(email))
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Modo Offline")
                .setContentText("Usuario borrado localmente.")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    finish()
                }.show()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }
}