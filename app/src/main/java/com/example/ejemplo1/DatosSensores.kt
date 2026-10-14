package com.example.ejemplo1

import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DatosSensores : AppCompatActivity() {

    private lateinit var fecha: TextView
    private lateinit var temp: TextView
    private lateinit var hum: TextView
    private lateinit var imagenTemp: ImageView
    private lateinit var datos: RequestQueue

    private val mHandler = Handler(Looper.getMainLooper())
    private var errorMostrado = false
    private var consultando = false

    private lateinit var imgAmpolleta: ImageView
    private lateinit var imgLinterna: ImageView
    private var estadoAmpolleta = false
    private var estadoLinterna = false
    private lateinit var cameraManager: android.hardware.camera2.CameraManager
    private var cameraId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_datos_sensores)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fecha = findViewById(R.id.txt_fecha)
        temp = findViewById(R.id.txt_temp)
        hum = findViewById(R.id.txt_humedad)
        imagenTemp = findViewById(R.id.imagen_temp)

        datos = Volley.newRequestQueue(this)

        imgAmpolleta = findViewById(R.id.img_ampolleta)
        imgLinterna = findViewById(R.id.img_linterna)

        cameraManager = getSystemService(CAMERA_SERVICE) as android.hardware.camera2.CameraManager

        cameraId = try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val c = cameraManager.getCameraCharacteristics(id)
                c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                        c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        imgAmpolleta.setOnClickListener {
            estadoAmpolleta = !estadoAmpolleta
            if (estadoAmpolleta) {
                imgAmpolleta.setImageResource(R.drawable.luz1)
            } else {
                imgAmpolleta.setImageResource(R.drawable.luzoff_1_)
            }
        }

        imgLinterna.setOnClickListener {
            val id = cameraId
            if (id == null) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Sin linterna", "Este teléfono no tiene linterna disponible.")
                return@setOnClickListener
            }
            try {
                val nuevoEstado = !estadoLinterna
                cameraManager.setTorchMode(id, nuevoEstado)
                estadoLinterna = nuevoEstado
                if (estadoLinterna) {
                    imgLinterna.setImageResource(R.drawable.linternaon)
                } else {
                    imgLinterna.setImageResource(R.drawable.linternaoff)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error", "No se pudo acceder a la linterna del equipo.")
            }
        }
    }

    private fun fechahora(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy, HH:mm:ss", Locale.getDefault())
        return "Fecha/Hora: " + sdf.format(Calendar.getInstance().time)
    }

    private fun obtenerDatos() {
        if (!NetworkHelper.hayConexion(this)) {
            temp.text = "-- °C"
            hum.text = "-- %"
            if (!errorMostrado) {
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Modo Offline", "Sin conexión. Los sensores no se actualizarán.")
                errorMostrado = true
            }
            return
        }


        if (consultando) return
        consultando = true

        val url = "https://www.pnk.cl/muestra_datos.php"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                consultando = false
                try {
                    temp.text = "${response.getString("temperatura")} °C"
                    hum.text = "${response.getString("humedad")} %"

                    val valor = response.getString("temperatura").toFloat()
                    cambiarImagen(valor)
                    errorMostrado = false
                } catch (e: Exception) {

                    e.printStackTrace()
                    if (!errorMostrado) {
                        mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error de API", "La respuesta de pnk.cl no tiene el formato esperado.")
                        errorMostrado = true
                    }
                }
            },
            { error ->
                consultando = false
                error.printStackTrace()
                temp.text = "-- °C"
                hum.text = "-- %"
                if (!errorMostrado) {
                    if (!NetworkHelper.hayConexion(this)) {
                        mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Modo Offline", "Sin conexión. Los sensores no se actualizarán.")
                    } else {
                        mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error de API", "No se pudieron obtener los datos de pnk.cl")
                    }
                    errorMostrado = true
                }
            }
        )
        datos.add(request)
    }

    private fun cambiarImagen(valor: Float) {
        if (valor >= 30) {
            imagenTemp.setImageResource(R.drawable.temperaturaalta)
        } else {
            imagenTemp.setImageResource(R.drawable.temperaturabaja)
        }
    }


    private val refrescar = object : Runnable {
        override fun run() {
            fecha.text = fechahora()
            obtenerDatos()
            mHandler.postDelayed(this, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        errorMostrado = false
        consultando = false
        mHandler.post(refrescar)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(refrescar)
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