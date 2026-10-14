package com.example.ejemplo1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

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

        val uuid = intent.getStringExtra("uuid") ?: run { finish(); return }
        val repo = UsuariosLocal(this)
        val usuario = repo.obtener(uuid) ?: run { finish(); return }

        val etNombres = findViewById<EditText>(R.id.modNombres)
        val etApellidos = findViewById<EditText>(R.id.modApellidos)
        val etEmail = findViewById<EditText>(R.id.modEmail)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarMod)
        val btnEliminar = findViewById<Button>(R.id.btnEliminarMod)

        etNombres.setText(usuario.nombre)
        etApellidos.setText(usuario.apellido)
        etEmail.setText(usuario.email)
        etNombres.filters = arrayOf(Validaciones.filtroLetras)
        etApellidos.filters = arrayOf(Validaciones.filtroLetras)

        val letrasRegex = "^[\\p{L} ]+$".toRegex()

        btnGuardar.setOnClickListener {
            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (nombres.isEmpty() || apellidos.isEmpty() || email.isEmpty()) {
                alerta(SweetAlertDialog.ERROR_TYPE, "Error", "Todos los campos son obligatorios.")
            } else if (!letrasRegex.matches(nombres) || !letrasRegex.matches(apellidos)) {
                alerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Nombres y Apellidos solo aceptan letras y espacios.")
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                alerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Formato de correo inválido.")
            } else if (!repo.modificar(uuid, nombres, apellidos, email)) {
                alerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "El correo ya está asociado a otro usuario.")
            } else {
                SyncManager.sincronizar(applicationContext)
                val msg = if (NetworkHelper.hayConexion(this)) "Modificación guardada correctamente."
                else "Modificación guardada en el celular. Se enviará a AWS cuando haya internet."
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("¡Éxito!")
                    .setContentText(msg)
                    .setConfirmClickListener { it.dismissWithAnimation(); finish() }
                    .show()
            }
        }

        btnEliminar.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Estás seguro?")
                .setContentText("Esta acción eliminará al usuario.")
                .setConfirmText("Sí")
                .setCancelText("No")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    repo.eliminar(uuid)
                    SyncManager.sincronizar(applicationContext)
                    val msg = if (NetworkHelper.hayConexion(this)) "El usuario ha sido eliminado."
                    else "Usuario eliminado en el celular. Se eliminará de AWS cuando haya internet."
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("¡Éxito!")
                        .setContentText(msg)
                        .setConfirmClickListener { it.dismissWithAnimation(); finish() }
                        .show()
                }
                .setCancelClickListener { it.dismissWithAnimation() }
                .show()
        }
    }

    private fun alerta(tipo: Int, titulo: String, msg: String) {
        SweetAlertDialog(this, tipo).setTitleText(titulo).setContentText(msg)
            .setConfirmText("Entendido").setConfirmClickListener { it.dismissWithAnimation() }.show()
    }
}