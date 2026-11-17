package com.example.contacts.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE ${ContactContract.TABLE_NAME} (
                ${ContactContract.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ContactContract.COLUMN_NOM} TEXT NOT NULL,
                ${ContactContract.COLUMN_PRENOM} TEXT NOT NULL,
                ${ContactContract.COLUMN_TELEPHONE} TEXT NOT NULL,
                ${ContactContract.COLUMN_EMAIL} TEXT,
                ${ContactContract.COLUMN_PHOTO_URI} TEXT,
                ${ContactContract.COLUMN_LATITUDE} REAL,
                ${ContactContract.COLUMN_LONGITUDE} REAL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
        insertDemoContacts(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ContactContract.TABLE_NAME}")
        onCreate(db)
    }

    private fun insertDemoContacts(db: SQLiteDatabase) {
        val demoContacts = listOf(
            ContentValues().apply {
                put(ContactContract.COLUMN_NOM, "Barry")
                put(ContactContract.COLUMN_PRENOM, "Oumouratou")
                put(ContactContract.COLUMN_TELEPHONE, "22123456")
                put(ContactContract.COLUMN_EMAIL, "oumouratou@gmail.com")
                put(ContactContract.COLUMN_PHOTO_URI, "photo_barry")
                put(ContactContract.COLUMN_LATITUDE, 9.5370)
                put(ContactContract.COLUMN_LONGITUDE, -13.6785)
            },
            ContentValues().apply {
                put(ContactContract.COLUMN_NOM, "Diallo")
                put(ContactContract.COLUMN_PRENOM, "Fatoumata")
                put(ContactContract.COLUMN_TELEPHONE, "22345678")
                put(ContactContract.COLUMN_EMAIL, "fatoumata@gmail.com")
                put(ContactContract.COLUMN_PHOTO_URI, "photo_fatoumata")
                put(ContactContract.COLUMN_LATITUDE, 14.6937)
                put(ContactContract.COLUMN_LONGITUDE, -17.4441)
            },
            ContentValues().apply {
                put(ContactContract.COLUMN_NOM, "Camara")
                put(ContactContract.COLUMN_PRENOM, "Mamadou")
                put(ContactContract.COLUMN_TELEPHONE, "23456789")
                put(ContactContract.COLUMN_EMAIL, "mamadou@gmail.com")
                put(ContactContract.COLUMN_PHOTO_URI, "photo_mamadou")
                put(ContactContract.COLUMN_LATITUDE, 5.3600)
                put(ContactContract.COLUMN_LONGITUDE, -4.0083)
            },
            ContentValues().apply {
                put(ContactContract.COLUMN_NOM, "Sow")
                put(ContactContract.COLUMN_PRENOM, "Aminata")
                put(ContactContract.COLUMN_TELEPHONE, "24567890")
                put(ContactContract.COLUMN_EMAIL, "aminata@gmail.com")
                put(ContactContract.COLUMN_PHOTO_URI, "photo_aminata")
                putNull(ContactContract.COLUMN_LATITUDE)
                putNull(ContactContract.COLUMN_LONGITUDE)
            },
            ContentValues().apply {
                put(ContactContract.COLUMN_NOM, "Bah")
                put(ContactContract.COLUMN_PRENOM, "Ibrahima")
                put(ContactContract.COLUMN_TELEPHONE, "25678901")
                put(ContactContract.COLUMN_EMAIL, "ibrahima@gmail.com")
                put(ContactContract.COLUMN_PHOTO_URI, "photo_ibrahima")
                put(ContactContract.COLUMN_LATITUDE, 48.8566)
                put(ContactContract.COLUMN_LONGITUDE, 2.3522)
            }
        )

        for (values in demoContacts) {
            db.insert(ContactContract.TABLE_NAME, null, values)
        }
    }

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: ContactDbHelper? = null

        fun getInstance(context: Context): ContactDbHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactDbHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
