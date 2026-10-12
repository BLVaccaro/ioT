package com.example.ejemplo1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

class Registrar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nombresReg = findViewById<EditText>(R.id.Nombre1)
        val apellidosReg = findViewById<EditText>(R.id.Apellido1)
        val emailReg = findViewById<EditText>(R.id.Email1)
        val claveReg = findViewById<EditText>(R.id.Clave1)
        val repetirClaveReg = findViewById<EditText>(R.id.Clave2)

        val btnRegistrar = findViewById<Button>(R.id.btnRegistro)

        // 2. Lógica del botón Registrar
        btnRegistrar.setOnClickListener {
            // Obtenemos el texto de los 5 campos
            val nombres = nombresReg.text.toString().trim()
            val apellidos = apellidosReg.text.toString().trim()
            val email = emailReg.text.toString().trim()
            val clave = claveReg.text.toString().trim()
            val repetirClave = repetirClaveReg.text.toString().trim()

            // Regla para contraseña robusta (Mínimo 8 caracteres, con letras y números)
            val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$".toRegex()

            // Validación 1: Que NINGÚN campo esté vacío[cite: 1]
            if (nombres.isEmpty() || apellidos.isEmpty() || email.isEmpty() || clave.isEmpty() || repetirClave.isEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Todos los campos son obligatorios.")
                    .setConfirmText("Cerrar")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                    }
                    .show()
            }
            // Validación 2: Formato del E-mail[cite: 1]
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
            // Validación 3: Contraseña robusta[cite: 1]
            else if (!passwordPattern.matches(clave)) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Advertencia")
                    .setContentText("Contraseña débil. Debe tener al menos 8 caracteres, letras y números.")
                    .setConfirmText("Entiendo")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                    }
                    .show()
            }
            // Validación 4: Las contraseñas deben ser idénticas[cite: 1]
            else if (clave != repetirClave) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Advertencia")
                    .setContentText("Las contraseñas no coinciden.")
                    .setConfirmText("Entiendo")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                    }
                    .show()
            }
            // Si pasa todas las pruebas: Guardar y redirigir al Login
            else {
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("¡Éxito!")
                    .setContentText("Usuario registrado correctamente.")
                    .setConfirmText("Aceptar")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()

                        // Redirigir al Login directamente sin guardar datos
                        val viajeLogin = Intent(this@Registrar, Login::class.java)
                        startActivity(viajeLogin)
                        finish() // Cierra la pantalla de registro
                    }
                    .show()
            }
        }
    }
}