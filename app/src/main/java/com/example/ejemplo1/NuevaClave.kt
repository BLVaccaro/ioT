package com.example.ejemplo1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

class NuevaClave : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nueva_clave)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Conectar las cajas de texto y el botón (Verifica tus IDs del XML)
        val nuevaClave = findViewById<EditText>(R.id.clavenueva)
        val repetirClave = findViewById<EditText>(R.id.clavenueva1)
        val btnGuardarClave = findViewById<Button>(R.id.btnGuardarClave)

        // Regla estricta: Mín. 8 caracteres, 1 mayúscula, 1 minúscula, 1 número, 1 carácter especial
        val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$".toRegex()

        btnGuardarClave.setOnClickListener {
            // Obtenemos los textos
            val clave = nuevaClave.text.toString().trim()
            val repetirClave = repetirClave.text.toString().trim()

            // Validación 1: Campos vacíos
            if (clave.isEmpty() || repetirClave.isEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Todos los campos son obligatorios.")
                    .show()
            }
            // Validación 2: Contraseña robusta
            else if (!passwordPattern.matches(clave)) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Advertencia")
                    .setContentText("La contraseña debe tener al menos 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial.")
                    .show()
            }
            // Validación 3: Contraseñas idénticas
            else if (clave != repetirClave) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Advertencia")
                    .setContentText("Las contraseñas no coinciden.")
                    .show()
            }
            // Validación 4: Si todo está perfecto, lanzamos la confirmación
            else {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("¿Confirmar cambio?")
                    .setContentText("¿Estás seguro de que deseas guardar la nueva contraseña?")
                    .setConfirmText("Sí, cambiar")
                    .setCancelText("Cancelar")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()

                        // Si dice que sí, mostramos Éxito
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("¡Éxito!")
                            .setContentText("Tu contraseña ha sido actualizada.")
                            .setConfirmText("Ir al Login")
                            .setConfirmClickListener { exitoDialog ->
                                exitoDialog.dismissWithAnimation()

                                // Viaje final al Login
                                val viajeLogin = Intent(this@NuevaClave, Login::class.java)
                                startActivity(viajeLogin)
                                finish()
                            }
                            .show()
                    }
                    .setCancelClickListener { dialog ->
                        // Si cancela, solo cerramos la alerta
                        dialog.dismissWithAnimation()
                    }
                    .show()
            }
        }
    }
}