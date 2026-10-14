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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class NuevaClave : AppCompatActivity() {

    private val url = "http://98.89.107.89/recuperar.php"
    private var enviando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nueva_clave)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val email = intent.getStringExtra("email") ?: ""
        val codigo = intent.getStringExtra("codigo") ?: ""

        val etNueva = findViewById<EditText>(R.id.clavenueva)
        val etRepetir = findViewById<EditText>(R.id.clavenueva1)
        val btnGuardarClave = findViewById<Button>(R.id.btnGuardarClave)

        btnGuardarClave.setOnClickListener {
            val clave = etNueva.text.toString().trim()
            val repetir = etRepetir.text.toString().trim()
            val errorClave = Validaciones.errorClave(clave)

            if (clave.isEmpty() || repetir.isEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Todos los campos son obligatorios.")
                    .show()
            } else if (errorClave != null) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Advertencia")
                    .setContentText(errorClave)
                    .show()
            } else if (clave != repetir) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Advertencia")
                    .setContentText("Las contraseñas no coinciden.")
                    .show()
            } else {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("¿Confirmar cambio?")
                    .setContentText("¿Estás seguro de que deseas guardar la nueva contraseña?")
                    .setConfirmText("Sí, cambiar")
                    .setCancelText("Cancelar")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                        cambiarClave(email, codigo, clave)
                    }
                    .setCancelClickListener { dialog ->
                        dialog.dismissWithAnimation()
                    }
                    .show()
            }
        }
    }

    private fun cambiarClave(email: String, codigo: String, clave: String) {
        if (enviando) return
        enviando = true

        val cargando = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        cargando.titleText = "Guardando..."
        cargando.setCancelable(false)
        cargando.show()

        val params = mapOf(
            "accion" to "verificar_y_cambiar",
            "email" to email,
            "codigo" to codigo,
            "clave" to clave
        )

        val req = object : StringRequest(Method.POST, url,
            { respuesta ->
                enviando = false
                cargando.dismissWithAnimation()
                when (respuesta.trim()) {
                    "exito" -> {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("¡Éxito!")
                            .setContentText("Tu contraseña ha sido actualizada.")
                            .setConfirmText("Ir al Login")
                            .setConfirmClickListener { d ->
                                d.dismissWithAnimation()
                                val viajeLogin = Intent(this@NuevaClave, Login::class.java)
                                viajeLogin.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(viajeLogin)
                                finish()
                            }
                            .show()
                    }
                    "codigo_invalido" -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("El código no es válido. Solicita uno nuevo.")
                            .show()
                    }
                    else -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("No se pudo actualizar la contraseña: ${respuesta.trim()}")
                            .show()
                    }
                }
            },
            { error ->
                enviando = false
                cargando.dismissWithAnimation()
                val detalle = error.cause?.toString() ?: error.networkResponse?.statusCode?.toString() ?: error.javaClass.simpleName
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de Red")
                    .setContentText("Falló la conexión con el servidor AWS. ($detalle)")
                    .show()
            }) {
            override fun getParams() = params
        }

        Volley.newRequestQueue(this).add(req)
    }
}