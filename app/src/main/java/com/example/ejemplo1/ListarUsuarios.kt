package com.example.ejemplo1

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ListarUsuarios : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listar_usuarios)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listViewUsuarios = findViewById<ListView>(R.id.listViewUsuarios)


        val datosDePrueba = arrayListOf("1. Xiao Chiye", "2. Duamu Wuqiu", "3. Luo Binghe", "4. Shen Zechuan", "5. Chi Muyao", "6. Li Yu", "7. Mu Tianchi", "8. Wen Ren E")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datosDePrueba)
        listViewUsuarios.adapter = adapter


        listViewUsuarios.setOnItemClickListener { _, _, _, _ ->
            val viaje = Intent(this, ModificarEliminar1::class.java)
            startActivity(viaje)
        }
    }
}