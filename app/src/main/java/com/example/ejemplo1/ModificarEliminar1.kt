package com.example.ejemplo1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import android.os.Handler
import android.os.Looper

class ModificarEliminar1 : AppCompatActivity() {
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

        // Regla: Solo letras (con acentos/eñes) y espacios
        val letrasRegex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()

        // 1. ACCIÓN: GUARDAR
        btnGuardar.setOnClickListener {
            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (nombres.isEmpty() || apellidos.isEmpty() || email.isEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Todos los campos son obligatorios.")
                    .show()
            } else if (!letrasRegex.matches(nombres) || !letrasRegex.matches(apellidos)) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Advertencia")
                    .setContentText("Nombres y Apellidos solo aceptan letras y espacios.")
                    .show()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Advertencia")
                    .setContentText("Formato de correo inválido.")
                    .show()
            } else {
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("¡Éxito!")
                    .setContentText("Modificación guardada correctamente.")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                        finish() // Cierra la pantalla y vuelve atrás
                    }
                    .show()
            }
        }

        // 2. ACCIÓN: ELIMINAR
        btnEliminar.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Estás seguro?")
                .setContentText("Esta acción eliminará al usuario.")
                .setConfirmText("Sí")
                .setCancelText("No")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()

                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("¡Éxito!")
                        .setContentText("El usuario ha sido eliminado.")
                        .setConfirmClickListener { exitoDialog ->
                            exitoDialog.dismissWithAnimation()
                            finish() // Cierra la pantalla
                        }
                        .show()
                }
                .setCancelClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }
    }
}