package com.example.ejemplo1

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
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


        mHandler.post(refrescar)

        imgAmpolleta = findViewById(R.id.img_ampolleta)
        imgLinterna = findViewById(R.id.img_linterna)

        cameraManager = getSystemService(CAMERA_SERVICE) as android.hardware.camera2.CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: Exception) {
            e.printStackTrace()
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
            estadoLinterna = !estadoLinterna
            try {
                cameraId?.let { id ->
                    cameraManager.setTorchMode(id, estadoLinterna)
                }

                if (estadoLinterna) {
                    imgLinterna.setImageResource(R.drawable.luz1)
                } else {
                    imgLinterna.setImageResource(R.drawable.luzoff_1_)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(this, "No se pudo acceder a la linterna", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fechahora(): String {
        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", Locale.getDefault())
        return sdf.format(c.time)
    }

    private fun obtenerDatos() {
        val url = "https://www.pnk.cl/muestra_datos.php"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    temp.text = "${response.getString("temperatura")} °C"
                    hum.text = "${response.getString("humedad")} %"

                    val valor = response.getString("temperatura").toFloat()
                    cambiarImagen(valor)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
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
}