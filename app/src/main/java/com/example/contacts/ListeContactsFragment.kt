package com.example.contacts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListeContactsFragment : Fragment() {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter
    private lateinit var txtAucunResultat: TextView
    private lateinit var recyclerContacts: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_liste_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val edtRecherche = view.findViewById<EditText>(R.id.edtRecherche)
        txtAucunResultat = view.findViewById<TextView>(R.id.txtAucunResultat)
        recyclerContacts = view.findViewById<RecyclerView>(R.id.recyclerContacts)
        val fabAddContact = view.findViewById<FloatingActionButton>(R.id.fabAddContact)

        setupAdapter()
        recyclerContacts.layoutManager = LinearLayoutManager(requireContext())
        recyclerContacts.adapter = adapter

        viewModel.displayedContacts.observe(viewLifecycleOwner) { contacts ->
            adapter.updateData(contacts)
            txtAucunResultat.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
        }

        edtRecherche.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterContacts(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fabAddContact.setOnClickListener {
            ouvrirDetailContact(-1)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadContacts()
        setupAdapter()
        recyclerContacts.adapter = adapter
    }

    private fun setupAdapter() {
        val context = requireContext()
        val fontSize = PreferencesManager.getFontSize(context)
        val textColor = PreferencesManager.getTextColor(context)
        val showCall = PreferencesManager.isCallFromListEnabled(context)

        adapter = ContactAdapter(
            contacts = viewModel.displayedContacts.value ?: emptyList(),
            showCallButton = showCall,
            textColor = textColor,
            fontSize = fontSize,
            onContactClick = { contact ->
                ouvrirDetailContact(contact.id)
            },
            onCallClick = { contact ->
                val tel = contact.telephone.trim()
                if (tel.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Aucune application de téléphonie trouvée", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Numéro de téléphone invalide", Toast.LENGTH_SHORT).show()
                }
            },
            onFavoriteClick = { contact ->
                contact.favori = if (contact.favori == 1) 0 else 1
                viewModel.updateContact(contact) {
                    viewModel.loadContacts()
                }
            }
        )
    }

    private fun ouvrirDetailContact(idContact: Int) {
        val detailFragment = DetailContactFragment.newInstance(idContact)
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container, detailFragment)
            addToBackStack(null)
        }
    }
}
