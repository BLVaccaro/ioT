package com.example.ejemplo1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ConexionDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Se agrega SINCRONIZADO para el Desafío (0 = pendiente de subir a AWS, 1 = subido)
    val sql = "CREATE TABLE USUARIOS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NOMBRE TEXT, APELLIDO TEXT, EMAIL TEXT, CLAVE TEXT, SINCRONIZADO INTEGER)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS USUARIOS")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "CRUD_IOT"
        private const val DATABASE_VERSION = 1
    }
}