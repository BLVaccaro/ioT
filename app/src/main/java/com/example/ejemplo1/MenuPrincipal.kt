package com.example.ejemplo1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MenuPrincipal : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvFecha: TextView
    private val formato = SimpleDateFormat("dd-MM-yyyy, HH:mm:ss", Locale.getDefault())

    private val reloj = object : Runnable {
        override fun run() {
            tvFecha.text = "Fecha/Hora: " + formato.format(Date())
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvFecha = findViewById(R.id.textView2)

        findViewById<TextView>(R.id.btnCrud).setOnClickListener {
            startActivity(Intent(this, GestionUsuarios::class.java))
        }

        findViewById<TextView>(R.id.btnSensores).setOnClickListener {
            startActivity(Intent(this, DatosSensores::class.java))
        }

        findViewById<TextView>(R.id.btnDatos).setOnClickListener {
            startActivity(Intent(this, DatosDesarrollador::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(reloj)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(reloj)
    }
}