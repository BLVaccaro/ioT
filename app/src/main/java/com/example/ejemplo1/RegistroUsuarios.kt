package com.example.ejemplo1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        val etNombres = findViewById<EditText>(R.id.nombres1)
        val etApellidos = findViewById<EditText>(R.id.apellidoRegistro)
        val etEmail = findViewById<EditText>(R.id.emailRegistro)
        val etClave = findViewById<EditText>(R.id.claveRegistro)
        val etRepetirClave = findViewById<EditText>(R.id.reclaveRegistro)
        val btnRegistrar = findViewById<Button>(R.id.btnReg)

        etNombres.filters = arrayOf(Validaciones.filtroLetras)
        etApellidos.filters = arrayOf(Validaciones.filtroLetras)

        btnRegistrar.setOnClickListener {
            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val clave = etClave.text.toString().trim()
            val repetirClave = etRepetirClave.text.toString().trim()
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
                RegistroHelper.registrar(this, nombres, apellidos, email, clave) { finish() }
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