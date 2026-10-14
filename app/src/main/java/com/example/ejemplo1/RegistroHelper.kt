package com.example.ejemplo1

import android.app.Activity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.UUID

object RegistroHelper {
    private const val URL = "http://98.89.107.89/registro.php"

    fun registrar(act: Activity, nombres: String, apellidos: String, email: String,
                  clave: String, alTerminar: () -> Unit) {
        val uuid = UUID.randomUUID().toString()
        if (NetworkHelper.hayConexion(act)) {
            enLinea(act, uuid, nombres, apellidos, email, clave, alTerminar)
        } else {
            guardarLocal(act, uuid, nombres, apellidos, email, clave, alTerminar)
        }
    }

    private fun enLinea(act: Activity, uuid: String, nombres: String, apellidos: String,
                        email: String, clave: String, alTerminar: () -> Unit) {
        val cargando = SweetAlertDialog(act, SweetAlertDialog.PROGRESS_TYPE)
        cargando.titleText = "Conectando con AWS..."
        cargando.setCancelable(false)
        cargando.show()

        val params = mapOf("uuid" to uuid, "nombres" to nombres, "apellidos" to apellidos,
            "email" to email, "clave" to clave)

        val req = object : StringRequest(Request.Method.POST, URL,
            { resp ->
                cargando.dismissWithAnimation()
                if (resp.trim() == "exito") {
                    // copia local ya sincronizada, para que exista también sin conexión
                    UsuariosLocal(act).insertar(nombres, apellidos, email, clave, uuid, true)
                    SweetAlertDialog(act, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("¡Éxito!")
                        .setContentText("Usuario registrado correctamente.")
                        .setConfirmText("Aceptar")
                        .setConfirmClickListener { it.dismissWithAnimation(); alTerminar() }
                        .show()
                } else {
                    alerta(act, SweetAlertDialog.WARNING_TYPE, "No se pudo registrar", resp.trim())
                }
            },
            { _ ->
                // AWS no respondió: se guarda local con el mismo uuid y se sube después
                cargando.dismissWithAnimation()
                guardarLocal(act, uuid, nombres, apellidos, email, clave, alTerminar)
            }) {
            override fun getParams() = params
        }
        Volley.newRequestQueue(act).add(req)
    }

    private fun guardarLocal(act: Activity, uuid: String, nombres: String, apellidos: String,
                             email: String, clave: String, alTerminar: () -> Unit) {
        val ok = UsuariosLocal(act).insertar(nombres, apellidos, email, clave, uuid)
        if (!ok) {
            alerta(act, SweetAlertDialog.WARNING_TYPE, "No se pudo registrar", "El correo ya está registrado.")
            return
        }
        SweetAlertDialog(act, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Modo Offline")
            .setContentText("Datos guardados en el celular. Se subirán a AWS cuando haya internet.")
            .setConfirmText("Aceptar")
            .setConfirmClickListener { it.dismissWithAnimation(); alTerminar() }
            .show()
        SyncManager.sincronizar(act.applicationContext)
    }

    private fun alerta(act: Activity, tipo: Int, titulo: String, msg: String) {
        SweetAlertDialog(act, tipo).setTitleText(titulo).setContentText(msg)
            .setConfirmText("Entendido").setConfirmClickListener { it.dismissWithAnimation() }.show()
    }
}