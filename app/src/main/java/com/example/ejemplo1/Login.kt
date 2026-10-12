package com.example.ejemplo1

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import cn.pedant.SweetAlert.SweetAlertDialog
import android.os.Handler
import android.os.Looper

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
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Campos obligatorios en blanco.")
                    .setConfirmText("Cerrar")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                    }
                    .show()
            }
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Advertencia")
                    .setContentText("Formato de correo inválido.")
                    .setConfirmText("Entiendo")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                    }
                    .show()
            }

            // NUEVO: Solo Mensaje de Progreso (Cargando)

            else {
                val pDialog = SweetAlertDialog(this@Login, SweetAlertDialog.PROGRESS_TYPE)
                pDialog.titleText = "Cargando..."
                pDialog.setCancelable(false)
                pDialog.show()

                // Espera 3 segundos, cierra la animación y viaja al menú
                Handler(Looper.getMainLooper()).postDelayed({
                    pDialog.dismissWithAnimation()

                    val viajeMenu = Intent(this@Login, MenuPrincipal::class.java)
                    startActivity(viajeMenu)
                }, 3000) // 3000 milisegundos = 3 segundos
            }
        }

        val miTexto = findViewById<TextView>(R.id.olvidar)
        miTexto.setOnClickListener {
            val viaje = Intent(this, RecuperarContrasena::class.java)
            startActivity(viaje)
        }

        val miTexto1 = findViewById<TextView>(R.id.registrar)
        miTexto1.setOnClickListener {
            val registro = Intent(this, Registrar::class.java)
            startActivity(registro)
        }


    }
}