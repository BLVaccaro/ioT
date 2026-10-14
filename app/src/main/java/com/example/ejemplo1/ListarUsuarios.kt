package com.example.ejemplo1

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley

class ListarUsuarios : AppCompatActivity() {

    private val url = "http://98.89.107.89/listar.php"
    private lateinit var repo: UsuariosLocal
    private lateinit var lista: ListView
    private lateinit var etBuscar: EditText
    private var mostrados: List<Usuario> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listar_usuarios)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repo = UsuariosLocal(this)
        lista = findViewById(R.id.listViewUsuarios)
        etBuscar = findViewById(R.id.etBuscar)

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { mostrar() }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        lista.setOnItemClickListener { _, _, posicion, _ ->
            val i = Intent(this, ModificarEliminar1::class.java)
            i.putExtra("uuid", mostrados[posicion].uuid)
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        mostrar()
        cargarDesdeAWS()
    }

    private fun mostrar() {
        mostrados = repo.listar(etBuscar.text.toString())
        val textos = mostrados.mapIndexed { i, u ->
            val marca = when {
                u.pendiente == "RECHAZADO" -> "  (rechazado)"
                u.pendiente != null -> "  (pendiente)"
                else -> ""
            }
            "${i + 1}. ${u.nombre} ${u.apellido}$marca"
        }
        lista.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, textos)
    }

    private fun cargarDesdeAWS() {
        if (!NetworkHelper.hayConexion(this)) return
        val req = JsonArrayRequest(url,
            { arr ->
                val remotos = (0 until arr.length()).map {
                    val o = arr.getJSONObject(it)
                    Usuario(o.getString("uuid"), o.optString("nombres"), o.optString("apellidos"),
                        o.optString("email"), null)
                }
                repo.actualizarDesdeServidor(remotos)
                mostrar()
            },
            { _ -> /* sin respuesta de AWS: se queda con la copia local */ })
        Volley.newRequestQueue(this).add(req)
    }
}