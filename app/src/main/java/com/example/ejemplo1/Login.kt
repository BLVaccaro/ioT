package com.example.ejemplo1

import android.content.Intent
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
                // Evaluamos conexión para el Login
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
        // Asegúrate de que el nombre del archivo PHP sea el correcto en tu AWS
        val url = "http://98.89.107.89/apiconsultausu.php?usu=$email&pass=$clave"
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
                    val estado = response.getString("estado")
                    if (estado == "0") {
                        mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Acceso Denegado", "Usuario o contraseña incorrectos.")
                    } else {
                        // Éxito: Viajamos al menú principal
                        startActivity(Intent(this@Login, MenuPrincipal::class.java))
                        finish()
                    }
                } catch (e: JSONException) {
                    mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error", "Respuesta inválida del servidor.")
                }
            },
            { error ->
                pDialog.dismissWithAnimation()
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Sin red", "Fallo AWS. Intentando acceso local...")
                loginSQLite(email, clave)
            }
        )
        queue.add(request)
    }

    private fun loginSQLite(email: String, clave: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.readableDatabase

        // Buscamos si existe el usuario guardado localmente
        val cursor = db.rawQuery("SELECT * FROM USUARIOS WHERE EMAIL=? AND CLAVE=?", arrayOf(email, clave))

        if (cursor.moveToFirst()) {
            cursor.close()
            db.close()
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
            cursor.close()
            db.close()
            mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Acceso Denegado", "Credenciales incorrectas o usuario no sincronizado en el celular.")
        }
    }
}