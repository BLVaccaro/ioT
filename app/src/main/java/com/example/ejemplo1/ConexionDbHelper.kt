package com.example.ejemplo1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ConexionDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // SINCRONIZADO: 0 = pendiente de subir a AWS, 1 = ya está en AWS
    // PENDIENTE: operación por subir ("INSERT", "UPDATE" o "DELETE"), null si no hay
    // ELIMINADO: 1 = borrado localmente, a la espera de borrarse también en MySQL
    private val sql = "CREATE TABLE USUARIOS (" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "UUID TEXT, " +
            "NOMBRE TEXT, " +
            "APELLIDO TEXT, " +
            "EMAIL TEXT, " +
            "CLAVE TEXT, " +
            "SINCRONIZADO INTEGER DEFAULT 1, " +
            "PENDIENTE TEXT, " +
            "ELIMINADO INTEGER DEFAULT 0)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(sql)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS IDX_UUID ON USUARIOS(UUID)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE USUARIOS ADD COLUMN UUID TEXT")
            db.execSQL("ALTER TABLE USUARIOS ADD COLUMN PENDIENTE TEXT")
            db.execSQL("ALTER TABLE USUARIOS ADD COLUMN ELIMINADO INTEGER DEFAULT 0")
            db.execSQL("UPDATE USUARIOS SET UUID = lower(hex(randomblob(16))) WHERE UUID IS NULL")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS IDX_UUID ON USUARIOS(UUID)")
        }
    }

    companion object {
        private const val DATABASE_NAME = "CRUD_IOT"
        private const val DATABASE_VERSION = 2
    }
}