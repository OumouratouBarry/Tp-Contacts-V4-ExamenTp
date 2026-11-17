package com.example.contacts

data class Contact(
    val id: Int,
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String,
    var photo: String = "",
    var latitude: Double? = null,
    var longitude: Double? = null
)
