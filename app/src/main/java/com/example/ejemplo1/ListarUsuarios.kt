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
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class ListarUsuarios : AppCompatActivity() {

    // Listas para manejar los datos
    private val listaNombres = ArrayList<String>()
    private val listaUsuariosCompletos = ArrayList<UsuarioData>()
    private lateinit var adapter: ArrayAdapter<String>

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

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaNombres)
        listViewUsuarios.adapter = adapter

        // Al hacer clic, enviamos los datos a la pantalla ModificarEliminar1
        listViewUsuarios.setOnItemClickListener { _, _, position, _ ->
            val usuarioSeleccionado = listaUsuariosCompletos[position]
            val viaje = Intent(this, ModificarEliminar1::class.java).apply {
                putExtra("ID", usuarioSeleccionado.id)
                putExtra("NOMBRES", usuarioSeleccionado.nombres)
                putExtra("APELLIDOS", usuarioSeleccionado.apellidos)
                putExtra("EMAIL", usuarioSeleccionado.email)
            }
            startActivity(viaje)
        }

        // BUSCADOR (FILTRO) ACTIVADO
        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // NUEVO: Esta función se dispara cada vez que la pantalla vuelve a verse.
    // Así garantizamos que la lista siempre tenga los datos más recientes.
    override fun onResume() {
        super.onResume()
        if (NetworkHelper.hayConexion(this)) {
            cargarDesdeAWS()
        } else {
            cargarDesdeSQLite()
        }
    }

    private fun cargarDesdeAWS() {
        val url = "http://98.89.107.89/consulta.php"
        val queue = Volley.newRequestQueue(this)

        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando usuarios..."
        pDialog.setCancelable(false)
        pDialog.show()

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                pDialog.dismissWithAnimation()
                listaNombres.clear()
                listaUsuariosCompletos.clear()

                try {
                    for (i in 0 until response.length()) {
                        val jsonObject = response.getJSONObject(i)
                        val id = jsonObject.optString("id_usuario", "")
                        val nombres = jsonObject.getString("nombres")
                        val apellidos = jsonObject.getString("apellidos")
                        val email = jsonObject.getString("email")

                        // Así se verá en la lista visual del teléfono
                        val nombreVisual = "$nombres $apellidos\n$email"
                        listaNombres.add(nombreVisual)

                        // Guardamos el objeto completo de fondo para cuando hagan clic
                        listaUsuariosCompletos.add(UsuarioData(id, nombres, apellidos, email))
                    }
                    adapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error", "Error al procesar los datos.")
                }
            },
            { error ->
                pDialog.dismissWithAnimation()
                mostrarAlerta(SweetAlertDialog.WARNING_TYPE, "Sin red", "Fallo AWS. Cargando datos locales...")
                cargarDesdeSQLite()
            }
        )
        queue.add(request)
    }

    private fun cargarDesdeSQLite() {
        listaNombres.clear()
        listaUsuariosCompletos.clear()

        val helper = ConexionDbHelper(this)
        val db = helper.readableDatabase

        try {
            val cursor = db.rawQuery("SELECT * FROM USUARIOS", null)
            if (cursor.moveToFirst()) {
                do {
                    val nombres = cursor.getString(cursor.getColumnIndexOrThrow("NOMBRE"))
                    val apellidos = cursor.getString(cursor.getColumnIndexOrThrow("APELLIDO"))
                    val email = cursor.getString(cursor.getColumnIndexOrThrow("EMAIL"))

                    val nombreVisual = "$nombres $apellidos\n(Modo Offline)"
                    listaNombres.add(nombreVisual)
                    listaUsuariosCompletos.add(UsuarioData("", nombres, apellidos, email))
                } while (cursor.moveToNext())
            }
            cursor.close()
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            mostrarAlerta(SweetAlertDialog.ERROR_TYPE, "Error", "No se pudieron cargar los datos locales.")
        } finally {
            db.close()
        }
    }

    private fun mostrarAlerta(tipo: Int, titulo: String, mensaje: String) {
        SweetAlertDialog(this, tipo)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Cerrar")
            .setConfirmClickListener { it.dismissWithAnimation() }
            .show()
    }

    // Clase auxiliar para empaquetar los datos invisibles de cada usuario
    data class UsuarioData(val id: String, val nombres: String, val apellidos: String, val email: String)
}