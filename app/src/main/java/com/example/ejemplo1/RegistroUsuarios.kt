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

class RegistroUsuarios : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_usuarios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etNombres = findViewById<EditText>(R.id.nombres1)
        val etApellidos = findViewById<EditText>(R.id.apellidoRegistro)
        val etEmail = findViewById<EditText>(R.id.emailRegistro)
        val etClave = findViewById<EditText>(R.id.claveRegistro)
        val etRepetirClave = findViewById<EditText>(R.id.reclaveRegistro)
        val btnRegistrar = findViewById<Button>(R.id.btnReg)

        btnRegistrar.setOnClickListener {
            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val clave = etClave.text.toString().trim()
            val repetirClave = etRepetirClave.text.toString().trim()

            val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$".toRegex()

            if (nombres.isEmpty() || apellidos.isEmpty() || email.isEmpty() || clave.isEmpty() || repetirClave.isEmpty()) {
                mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error", "Todos los campos son obligatorios.")
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Formato de correo inválido.")
            } else if (!passwordPattern.matches(clave)) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Contraseña débil. Debe tener al menos 8 caracteres, letras y números.")
            } else if (clave != repetirClave) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Las contraseñas no coinciden.")
            } else {
                if (NetworkHelper.hayConexion(this)) {
                    guardarEnAWS(nombres, apellidos, email, clave)
                } else {
                    guardarEnSQLite(nombres, apellidos, email, clave)
                }
            }
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

    private fun guardarEnAWS(nombres: String, apellidos: String, email: String, clave: String) {
        val url = "http://98.89.107.89/registro.php"
        val queue = Volley.newRequestQueue(this)

        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Conectando con AWS..."
        pDialog.setCancelable(false)
        pDialog.show()

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.trim() == "exito") {
                    pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                    pDialog.titleText = "¡Éxito!"
                    pDialog.contentText = "Usuario registrado en AWS correctamente."
                    pDialog.setConfirmText("Aceptar")
                    pDialog.setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                        finish()
                    }
                } else {
                    pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                    pDialog.titleText = "Error en AWS"
                    pDialog.contentText = response
                    pDialog.setConfirmText("Cerrar")
                    pDialog.setConfirmClickListener { it.dismissWithAnimation() }
                }
            },
            { error ->
                pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                pDialog.titleText = "Modo Offline"
                pDialog.contentText = "AWS tardó en responder. Datos guardados localmente."
                pDialog.setConfirmText("Aceptar")
                pDialog.setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    finish()
                }
                guardarEnSQLiteInterno(nombres, apellidos, email, clave)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val parametros = HashMap<String, String>()
                parametros["nombres"] = nombres
                parametros["apellidos"] = apellidos
                parametros["email"] = email
                parametros["clave"] = clave
                return parametros
            }
        }
        queue.add(stringRequest)
    }

    private fun guardarEnSQLite(nombres: String, apellidos: String, email: String, clave: String) {
        guardarEnSQLiteInterno(nombres, apellidos, email, clave)
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Modo Offline")
            .setContentText("Datos guardados localmente. Se subirán a AWS cuando haya internet.")
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                finish()
            }
            .show()
    }

    private fun guardarEnSQLiteInterno(nombres: String, apellidos: String, email: String, clave: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        try {
            val datos = ContentValues().apply {
                put("NOMBRE", nombres)
                put("APELLIDO", apellidos)
                put("EMAIL", email)
                put("CLAVE", clave)
                put("SINCRONIZADO", 0)
            }
            db.insert("USUARIOS", null, datos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }
}