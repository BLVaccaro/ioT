package com.example.ejemplo1

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GestionUsuarios : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestion_usuarios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val registro = findViewById<TextView>(R.id.btnRegistrarUsuarios)
        registro.setOnClickListener {
            val viaje = Intent(this, RegistroUsuarios::class.java)
            startActivity(viaje)
        }

        val listado = findViewById<TextView>(R.id.btnListar)
        listado.setOnClickListener {
            val viaje = Intent(this, ListarUsuarios::class.java)
            startActivity(viaje)
        }
    }
}