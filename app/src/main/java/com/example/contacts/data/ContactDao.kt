package com.example.contacts.data

import android.content.ContentValues
import android.content.Context
import com.example.contacts.Contact
import kotlin.system.measureTimeMillis

class ContactDao(context: Context) {
    private val dbHelper = ContactDbHelper.getInstance(context)

    fun insert(contact: Contact): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ContactContract.COLUMN_NOM, contact.nom)
            put(ContactContract.COLUMN_PRENOM, contact.prenom)
            put(ContactContract.COLUMN_TELEPHONE, contact.telephone)
            put(ContactContract.COLUMN_EMAIL, contact.email)
            put(ContactContract.COLUMN_PHOTO_URI, contact.photo)
            put(ContactContract.COLUMN_LATITUDE, contact.latitude)
            put(ContactContract.COLUMN_LONGITUDE, contact.longitude)
            put(ContactContract.COLUMN_FAVORI, contact.favori)
        }
        return db.insert(ContactContract.TABLE_NAME, null, values)
    }

    fun update(contact: Contact): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ContactContract.COLUMN_NOM, contact.nom)
            put(ContactContract.COLUMN_PRENOM, contact.prenom)
            put(ContactContract.COLUMN_TELEPHONE, contact.telephone)
            put(ContactContract.COLUMN_EMAIL, contact.email)
            put(ContactContract.COLUMN_PHOTO_URI, contact.photo)
            put(ContactContract.COLUMN_LATITUDE, contact.latitude)
            put(ContactContract.COLUMN_LONGITUDE, contact.longitude)
            put(ContactContract.COLUMN_FAVORI, contact.favori)
        }
        return db.update(
            ContactContract.TABLE_NAME,
            values,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(contact.id.toString())
        )
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            ContactContract.TABLE_NAME,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun getAll(sortOrderOption: String = "nom"): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val db = dbHelper.readableDatabase
        val sortOrder = when (sortOrderOption) {
            "favori" -> "${ContactContract.COLUMN_FAVORI} DESC, ${ContactContract.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactContract.COLUMN_PRENOM} COLLATE NOCASE ASC"
            "prenom" -> "${ContactContract.COLUMN_PRENOM} COLLATE NOCASE ASC, ${ContactContract.COLUMN_NOM} COLLATE NOCASE ASC"
            else -> "${ContactContract.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactContract.COLUMN_PRENOM} COLLATE NOCASE ASC"
        }

        db.query(
            ContactContract.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            sortOrder
        ).use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_ID)
            val nomIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_NOM)
            val prenomIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_PRENOM)
            val telIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_TELEPHONE)
            val emailIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_EMAIL)
            val photoIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_PHOTO_URI)
            val latIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_LATITUDE)
            val lngIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_LONGITUDE)
            val favIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_FAVORI)

            while (cursor.moveToNext()) {
                val id = cursor.getInt(idIdx)
                val nom = cursor.getString(nomIdx)
                val prenom = cursor.getString(prenomIdx)
                val telephone = cursor.getString(telIdx)
                val email = cursor.getString(emailIdx) ?: ""
                val photo = cursor.getString(photoIdx) ?: ""
                val latitude = if (cursor.isNull(latIdx)) null else cursor.getDouble(latIdx)
                val longitude = if (cursor.isNull(lngIdx)) null else cursor.getDouble(lngIdx)
                val favori = cursor.getInt(favIdx)

                contacts.add(Contact(id, nom, prenom, telephone, email, photo, latitude, longitude, favori))
            }
        }
        return contacts
    }

    fun getById(id: Int): Contact? {
        val db = dbHelper.readableDatabase
        db.query(
            ContactContract.TABLE_NAME,
            null,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val idIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_ID)
                val nomIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_NOM)
                val prenomIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_PRENOM)
                val telIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_TELEPHONE)
                val emailIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_EMAIL)
                val photoIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_PHOTO_URI)
                val latIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_LATITUDE)
                val lngIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_LONGITUDE)
                val favIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_FAVORI)

                return Contact(
                    id = cursor.getInt(idIdx),
                    nom = cursor.getString(nomIdx),
                    prenom = cursor.getString(prenomIdx),
                    telephone = cursor.getString(telIdx),
                    email = cursor.getString(emailIdx) ?: "",
                    photo = cursor.getString(photoIdx) ?: "",
                    latitude = if (cursor.isNull(latIdx)) null else cursor.getDouble(latIdx),
                    longitude = if (cursor.isNull(lngIdx)) null else cursor.getDouble(lngIdx),
                    favori = cursor.getInt(favIdx)
                )
            }
        }
        return null
    }

    fun search(texte: String, sortOrderOption: String = "nom"): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val db = dbHelper.readableDatabase
        val selection = "${ContactContract.COLUMN_NOM} LIKE ? OR ${ContactContract.COLUMN_PRENOM} LIKE ? OR ${ContactContract.COLUMN_TELEPHONE} LIKE ?"
        val searchArg = "%$texte%"
        val selectionArgs = arrayOf(searchArg, searchArg, searchArg)
        val sortOrder = when (sortOrderOption) {
            "favori" -> "${ContactContract.COLUMN_FAVORI} DESC, ${ContactContract.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactContract.COLUMN_PRENOM} COLLATE NOCASE ASC"
            "prenom" -> "${ContactContract.COLUMN_PRENOM} COLLATE NOCASE ASC, ${ContactContract.COLUMN_NOM} COLLATE NOCASE ASC"
            else -> "${ContactContract.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactContract.COLUMN_PRENOM} COLLATE NOCASE ASC"
        }

        db.query(
            ContactContract.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        ).use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_ID)
            val nomIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_NOM)
            val prenomIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_PRENOM)
            val telIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_TELEPHONE)
            val emailIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_EMAIL)
            val photoIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_PHOTO_URI)
            val latIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_LATITUDE)
            val lngIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_LONGITUDE)
            val favIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_FAVORI)

            while (cursor.moveToNext()) {
                val id = cursor.getInt(idIdx)
                val nom = cursor.getString(nomIdx)
                val prenom = cursor.getString(prenomIdx)
                val telephone = cursor.getString(telIdx)
                val email = cursor.getString(emailIdx) ?: ""
                val photo = cursor.getString(photoIdx) ?: ""
                val latitude = if (cursor.isNull(latIdx)) null else cursor.getDouble(latIdx)
                val longitude = if (cursor.isNull(lngIdx)) null else cursor.getDouble(lngIdx)
                val favori = cursor.getInt(favIdx)

                contacts.add(Contact(id, nom, prenom, telephone, email, photo, latitude, longitude, favori))
            }
        }
        return contacts
    }

    fun import100Contacts(withTransaction: Boolean): Long {
        val db = dbHelper.writableDatabase
        return measureTimeMillis {
            if (withTransaction) {
                db.beginTransaction()
                try {
                    for (i in 1..100) {
                        val values = ContentValues().apply {
                            put(ContactContract.COLUMN_NOM, "TestNom$i")
                            put(ContactContract.COLUMN_PRENOM, "TestPrenom$i")
                            put(ContactContract.COLUMN_TELEPHONE, "06000000$i")
                            put(ContactContract.COLUMN_EMAIL, "test$i@gmail.com")
                            put(ContactContract.COLUMN_FAVORI, 0)
                        }
                        db.insert(ContactContract.TABLE_NAME, null, values)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            } else {
                for (i in 1..100) {
                    val values = ContentValues().apply {
                        put(ContactContract.COLUMN_NOM, "TestNom$i")
                        put(ContactContract.COLUMN_PRENOM, "TestPrenom$i")
                        put(ContactContract.COLUMN_TELEPHONE, "06000000$i")
                        put(ContactContract.COLUMN_EMAIL, "test$i@gmail.com")
                        put(ContactContract.COLUMN_FAVORI, 0)
                    }
                    db.insert(ContactContract.TABLE_NAME, null, values)
                }
            }
        }
    }
}
