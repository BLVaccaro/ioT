package com.example.ejemplo1

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MenuPrincipal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val crud = findViewById<TextView>(R.id.btnCrud)
        crud.setOnClickListener {
            val viaje = Intent(this, GestionUsuarios::class.java)
            startActivity(viaje)
        }

        val sensores = findViewById<TextView>(R.id.btnSensores)
        sensores.setOnClickListener {
            val viaje = Intent(this, DatosSensores::class.java)
            startActivity(viaje)
        }

        val datos = findViewById<TextView>(R.id.btnDatos)
        datos.setOnClickListener {
            val viaje = Intent(this, DatosDesarrollador::class.java)
            startActivity(viaje)
        }
    }
}