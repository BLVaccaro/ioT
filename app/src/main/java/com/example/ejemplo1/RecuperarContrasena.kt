package com.example.ejemplo1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class RecuperarContrasena : AppCompatActivity() {

    private val url = "http://98.89.107.89/recuperar.php"
    private var codigoEnviado = false
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_contrasena)

        val scroll = findViewById<ScrollView>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { v, insets ->
            val s = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(s.left, s.top, s.right, maxOf(s.bottom, ime.bottom))
            if (ime.bottom > 0) {
                // con el teclado abierto, baja hasta mostrar las casillas y el temporizador
                scroll.post { scroll.smoothScrollTo(0, scroll.getChildAt(0).bottom) }
            }
            insets
        }

        val etEmail = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val btn = findViewById<Button>(R.id.btnRecuperar)
        val tvTimer = findViewById<TextView>(R.id.tvTemporizador)
        val cajas = listOf(R.id.codigo1, R.id.codigo2, R.id.codigo3, R.id.codigo4, R.id.codigo5)
            .map { findViewById<EditText>(it) }

        configurarCajas(cajas)

        btn.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                alerta(SweetAlertDialog.ERROR_TYPE, "Error", "Debe ingresar un correo electrónico.")
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                alerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Formato de correo inválido.")
                return@setOnClickListener
            }

            if (!codigoEnviado) {
                // Paso 1: pedir el código
                val cargando = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                cargando.titleText = "Procesando..."
                cargando.setCancelable(false)
                cargando.show()

                post(mapOf("accion" to "solicitar", "email" to email), cargando) { resp ->
                    when (resp) {
                        "exito" -> {
                            cargando.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                            cargando.titleText = "¡Éxito!"
                            cargando.contentText = "Código enviado a tu correo"
                            cargando.setConfirmText("Aceptar")
                            cargando.setConfirmClickListener { it.dismissWithAnimation() }
                            codigoEnviado = true
                            btn.text = "Verificar código"
                            cajas.forEach { it.text.clear() }
                            iniciarTimer(tvTimer, btn, cajas)
                        }
                        "no_existe" -> {
                            cargando.dismissWithAnimation()
                            alerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Ese correo no está registrado.")
                        }
                        "error_correo" -> {
                            cargando.dismissWithAnimation()
                            alerta(SweetAlertDialog.ERROR_TYPE, "Error", "No se pudo enviar el correo.")
                        }
                        else -> {
                            cargando.dismissWithAnimation()
                            alerta(SweetAlertDialog.ERROR_TYPE, "Error", "Error en el servidor: $resp")
                        }
                    }
                }
            } else {

                val codigo = cajas.joinToString("") { it.text.toString().trim() }
                if (codigo.length != 5) {
                    alerta(SweetAlertDialog.WARNING_TYPE, "Advertencia", "Ingresa los 5 dígitos del código.")
                    return@setOnClickListener
                }
                val verificando = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                verificando.titleText = "Verificando..."
                verificando.setCancelable(false)
                verificando.show()

                post(mapOf("accion" to "verificar_codigo", "email" to email, "codigo" to codigo), verificando) { resp ->
                    verificando.dismissWithAnimation()
                    if (resp == "exito") {
                        timer?.cancel()
                        val i = Intent(this, NuevaClave::class.java)
                        i.putExtra("email", email)
                        i.putExtra("codigo", codigo)
                        startActivity(i)
                        finish()
                    } else {
                        alerta(SweetAlertDialog.ERROR_TYPE, "Código inválido", "El código es incorrecto o ya venció. Solicita uno nuevo.")
                    }
                }
            }
        }
    }

    private fun configurarCajas(cajas: List<EditText>) {
        cajas.forEachIndexed { i, caja ->
            caja.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s != null && s.length == 1) {
                        if (i < cajas.size - 1) {
                            cajas[i + 1].requestFocus()
                        } else {
                            ocultarTeclado(caja)   // última casilla llena
                        }
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            caja.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN
                    && caja.text.isEmpty() && i > 0
                ) {
                    cajas[i - 1].text.clear()
                    cajas[i - 1].requestFocus()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun ocultarTeclado(vista: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(vista.windowToken, 0)
    }

    private fun iniciarTimer(tv: TextView, btn: Button, cajas: List<EditText>) {
        timer?.cancel()
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(ms: Long) { tv.text = "${ms / 1000} Segundos" }
            override fun onFinish() {
                tv.text = "Código vencido"
                codigoEnviado = false
                btn.text = "Reenviar código"
                cajas.forEach { it.text.clear() }
            }
        }.start()
    }

    private fun alerta(tipo: Int, titulo: String, msg: String) {
        SweetAlertDialog(this, tipo).setTitleText(titulo).setContentText(msg)
            .setConfirmText("Cerrar").setConfirmClickListener { it.dismissWithAnimation() }.show()
    }

    private fun post(params: Map<String, String>, cargando: SweetAlertDialog, onOk: (String) -> Unit) {
        val req = object : StringRequest(Method.POST, url,
            { r -> onOk(r.trim()) },
            { e ->
                cargando.dismissWithAnimation()
                val detalle = e.cause?.toString() ?: e.networkResponse?.statusCode?.toString() ?: e.javaClass.simpleName
                alerta(SweetAlertDialog.ERROR_TYPE, "Error de Red", "Falló la conexión con el servidor AWS. ($detalle)")
            }) {
            override fun getParams() = params
        }
        Volley.newRequestQueue(this).add(req)
    }

    override fun onDestroy() { super.onDestroy(); timer?.cancel() }
}