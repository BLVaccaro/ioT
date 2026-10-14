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

        // En nombres y apellidos no se pueden escribir números ni símbolos
        nombresReg.filters = arrayOf(Validaciones.filtroLetras)
        apellidosReg.filters = arrayOf(Validaciones.filtroLetras)

        btnRegistrar.setOnClickListener {
            val nombres = nombresReg.text.toString().trim()
            val apellidos = apellidosReg.text.toString().trim()
            val email = emailReg.text.toString().trim()
            val clave = claveReg.text.toString().trim()
            val repetirClave = repetirClaveReg.text.toString().trim()
            val errorClave = Validaciones.errorClave(clave)

            if (nombres.isEmpty() || apellidos.isEmpty() || email.isEmpty() || clave.isEmpty() || repetirClave.isEmpty()) {
                mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error", "Todos los campos son obligatorios.")
            } else if (!Validaciones.soloLetras.matches(nombres) || !Validaciones.soloLetras.matches(apellidos)) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Nombres y Apellidos solo aceptan letras y espacios.")
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Formato de correo inválido.")
            } else if (errorClave != null) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", errorClave)
            } else if (clave != repetirClave) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Las contraseñas no coinciden.")
            } else {
                RegistroHelper.registrar(this, nombres, apellidos, email, clave) {
                    startActivity(Intent(this@Registrar, Login::class.java))
                    finish()
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
}