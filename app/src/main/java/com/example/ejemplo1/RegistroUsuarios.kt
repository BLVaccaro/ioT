package com.example.ejemplo1

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.content.Context
import cn.pedant.SweetAlert.SweetAlertDialog

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

        // 1. Enlazamos los 5 campos de tu diseño
        // (Revisa que estos IDs coincidan con los que pusiste en tu XML)
        val etNombres = findViewById<EditText>(R.id.nombres1)
        val etApellidos = findViewById<EditText>(R.id.apellidoRegistro)
        val etEmail = findViewById<EditText>(R.id.emailRegistro)
        val etClave = findViewById<EditText>(R.id.claveRegistro)
        val etRepetirClave = findViewById<EditText>(R.id.reclaveRegistro)

        val btnRegistrar = findViewById<Button>(R.id.btnReg)

        // 2. Lógica del botón Registrar
        btnRegistrar.setOnClickListener {
            // Obtenemos el texto de los 5 campos
            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val clave = etClave.text.toString().trim()
            val repetirClave = etRepetirClave.text.toString().trim()

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
                // Guardamos los datos localmente (Persistencia básica)
                val sharedPreferences = getSharedPreferences("MisDatos", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("emailGuardado", email)
                editor.putString("claveGuardada", clave)
                editor.apply()

                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("¡Éxito!")
                    .setContentText("Usuario registrado correctamente.")
                    .setConfirmText("Aceptar")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()

                        // Redirigir al Login sin iniciar sesión
                        val viajeLogin = Intent(this@RegistroUsuarios, Login::class.java)
                        startActivity(viajeLogin)
                        finish() // Cierra la pantalla de registro
                    }
                    .show()
            }
        }
    }
}