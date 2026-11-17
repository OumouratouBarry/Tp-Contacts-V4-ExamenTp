package com.example.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contacts.data.ContactDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val contactDao = ContactDao(application)

    private val _displayedContacts = MutableLiveData<List<Contact>>()
    val displayedContacts: LiveData<List<Contact>> get() = _displayedContacts

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            val contacts = withContext(Dispatchers.IO) {
                contactDao.getAll()
            }
            _displayedContacts.value = contacts
        }
    }

    fun getContactById(id: Int): Contact? {
        return runBlocking(Dispatchers.IO) {
            contactDao.getById(id)
        }
    }

    fun insertContact(contact: Contact, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                contactDao.insert(contact)
            }
            loadContacts()
            onComplete()
        }
    }

    fun updateContact(contact: Contact, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                contactDao.update(contact)
            }
            loadContacts()
            onComplete()
        }
    }

    fun deleteContact(id: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                contactDao.delete(id)
            }
            loadContacts()
            onComplete()
        }
    }

    fun filterContacts(query: String) {
        viewModelScope.launch {
            val contacts = withContext(Dispatchers.IO) {
                if (query.trim().isEmpty()) {
                    contactDao.getAll()
                } else {
                    contactDao.search(query.trim())
                }
            }
            _displayedContacts.value = contacts
        }
    }
}
