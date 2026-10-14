package com.example.ejemplo1

import android.text.InputFilter

object Validaciones {

    const val MIN_CLAVE = 8

    val soloLetras = "^[\\p{L} ]+$".toRegex()

    val filtroLetras = InputFilter { source, start, end, _, _, _ ->
        val limpio = StringBuilder()
        for (i in start until end) {
            val c = source[i]
            if (Character.isLetter(c) || c == ' ') limpio.append(c)
        }
        if (limpio.length == end - start) null else limpio.toString()
    }

    fun errorClave(clave: String): String? {
        if (clave.length < MIN_CLAVE) {
            return "La contraseña debe tener al menos $MIN_CLAVE caracteres."
        }
        val faltan = mutableListOf<String>()
        if (!clave.any { it.isUpperCase() }) faltan.add("una mayúscula")
        if (!clave.any { it.isLowerCase() }) faltan.add("una minúscula")
        if (!clave.any { it.isDigit() }) faltan.add("un número")
        if (clave.all { it.isLetterOrDigit() }) faltan.add("un carácter especial")

        if (faltan.isEmpty()) return null
        return "A la contraseña le falta: " + faltan.joinToString(", ") + "."
    }
}