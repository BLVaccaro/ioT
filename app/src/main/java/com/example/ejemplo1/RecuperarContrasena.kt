package com.example.ejemplo1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

class RecuperarContrasena : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_contrasena)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Enlazamos los elementos de la interfaz
        val emailRecuperar = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val miBoton1 = findViewById<Button>(R.id.btnRecuperar)

        miBoton1.setOnClickListener {
            val email = emailRecuperar.text.toString().trim()

            // Validación 1: Que el campo no esté vacío
            if (email.isEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Debe ingresar un correo electrónico.")
                    .setConfirmText("Cerrar")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                    }
                    .show()
            }
            // Validación 2: Formato del E-mail
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
            // Si pasa las validaciones, ejecutamos tu lógica de carga y éxito
            else {
                // Mostrar la alerta de Proceso (Cargando)
                val alertaProceso = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                alertaProceso.setTitleText("Procesando...")
                alertaProceso.setCancelable(false) // Para que no la cierren tocando afuera
                alertaProceso.show()

                // Esperar 2 segundos y cambiar la misma alerta a Éxito
                Handler(Looper.getMainLooper()).postDelayed({
                    alertaProceso.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                    alertaProceso.setTitleText("¡Éxito!")
                    alertaProceso.setContentText("Código enviado")
                    alertaProceso.setConfirmText("Aceptar")
                    alertaProceso.setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()

                        // Viajar a la siguiente pantalla al hacer clic en Aceptar
                        val viaje = Intent(this@RecuperarContrasena, NuevaClave::class.java)
                        startActivity(viaje)
                        finish() // Cierra esta pantalla
                    }
                }, 2000) // 2000 = 2 segundos de tiempo de carga
            }
        }
    }
}