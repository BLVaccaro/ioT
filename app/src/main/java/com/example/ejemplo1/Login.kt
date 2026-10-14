package com.example.ejemplo1

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.email)
        val etPassword = findViewById<EditText>(R.id.cla)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)

        btnIngresar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error", "Campos obligatorios en blanco.")
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Formato de correo inválido.")
            } else {
                if (NetworkHelper.hayConexion(this)) {
                    loginAWS(email, password)
                } else {
                    loginSQLite(email, password)
                }
            }
        }

        findViewById<TextView>(R.id.olvidar).setOnClickListener {
            startActivity(Intent(this, RecuperarContrasena::class.java))
        }

        findViewById<TextView>(R.id.registrar).setOnClickListener {
            startActivity(Intent(this, Registrar::class.java))
        }
    }

    private fun mostrarAlerta(tipo: Int, titulo: String, mensaje: String) {
        SweetAlertDialog(this, tipo)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Cerrar")
            .setConfirmClickListener { it.dismissWithAnimation() }
            .show()
    }

    private fun loginAWS(email: String, clave: String) {
        val url = "http://98.89.107.89/apiconsultausu.php?usu=${Uri.encode(email)}&pass=${Uri.encode(clave)}"
        val queue = Volley.newRequestQueue(this)

        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Verificando en AWS..."
        pDialog.setCancelable(false)
        pDialog.show()

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                pDialog.dismissWithAnimation()
                try {
                    // estado: 0 = incorrecto, 1 = ok, 2 = cuenta bloqueada
                    when (response.getString("estado")) {
                        "0" -> mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Acceso Denegado", "Usuario o contraseña incorrectos.")
                        "2" -> mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Cuenta bloqueada", "Tu cuenta está bloqueada. Termina la recuperación de contraseña para reactivarla.")
                        else -> {
                            guardarUsuarioLocal(
                                response.optString("uuid"),
                                response.optString("nombres"),
                                response.optString("apellidos"),
                                email,
                                clave
                            )
                            startActivity(Intent(this@Login, MenuPrincipal::class.java))
                            finish()
                        }
                    }
                } catch (e: JSONException) {
                    mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error", "Respuesta inválida del servidor.")
                }
            },
            { _ ->
                // Si AWS no responde, se intenta el acceso local
                pDialog.dismissWithAnimation()
                loginSQLite(email, clave)
            }
        )
        queue.add(request)
    }

    private fun guardarUsuarioLocal(uuid: String, nombre: String, apellido: String, email: String, clave: String) {
        val db = ConexionDbHelper(this).writableDatabase
        val valores = ContentValues().apply {
            put("UUID", uuid); put("NOMBRE", nombre); put("APELLIDO", apellido)
            put("EMAIL", email); put("CLAVE", clave)
            put("SINCRONIZADO", 1); put("PENDIENTE", null as String?); put("ELIMINADO", 0)
        }
        // Solo se refresca si no tiene cambios pendientes (para no perderlos)
        val filas = db.update("USUARIOS", valores, "EMAIL = ? AND SINCRONIZADO = 1", arrayOf(email))
        if (filas == 0) {
            val c = db.rawQuery("SELECT 1 FROM USUARIOS WHERE EMAIL = ?", arrayOf(email))
            val existe = c.moveToFirst()
            c.close()
            if (!existe) db.insert("USUARIOS", null, valores)
        }
        db.close()
    }

    private fun loginSQLite(email: String, clave: String) {
        val db = ConexionDbHelper(this).readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM USUARIOS WHERE EMAIL = ? AND CLAVE = ? AND ELIMINADO = 0",
            arrayOf(email, clave)
        )
        val existe = cursor.moveToFirst()
        cursor.close()
        db.close()

        if (existe) {
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Modo Offline")
                .setContentText("Sesión iniciada localmente.")
                .setConfirmText("Entrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    startActivity(Intent(this@Login, MenuPrincipal::class.java))
                    finish()
                }
                .show()
        } else {
            mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Acceso Denegado", "Credenciales incorrectas o usuario no sincronizado en el celular.")
        }
    }
}