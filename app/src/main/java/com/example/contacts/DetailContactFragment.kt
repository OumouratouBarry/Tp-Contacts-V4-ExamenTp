package com.example.contacts

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import java.io.File

class DetailContactFragment : Fragment() {

    private val viewModel: ContactViewModel by activityViewModels()

    private lateinit var imgDetail: ImageView
    private lateinit var edtNom: EditText
    private lateinit var edtPrenom: EditText
    private lateinit var edtTelephone: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtLatitude: EditText
    private lateinit var edtLongitude: EditText
    private lateinit var btnSupprimer: Button

    private var contactId: Int = -1
    private var photoUri: String = ""
    private var tempPhotoUri: Uri? = null

    private val galerieLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore
                }
                photoUri = uri.toString()
                ContactAdapter.chargerPhoto(requireContext(), imgDetail, photoUri)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempPhotoUri != null) {
                photoUri = tempPhotoUri.toString()
                ContactAdapter.chargerPhoto(requireContext(), imgDetail, photoUri)
                Toast.makeText(requireContext(), "Photo enregistrée", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Aucune photo prise", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId = arguments?.getInt(ARG_CONTACT_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgDetail = view.findViewById(R.id.imgDetail)
        edtNom = view.findViewById(R.id.edtNom)
        edtPrenom = view.findViewById(R.id.edtPrenom)
        edtTelephone = view.findViewById(R.id.edtTelephone)
        edtEmail = view.findViewById(R.id.edtEmail)
        edtLatitude = view.findViewById(R.id.edtLatitude)
        edtLongitude = view.findViewById(R.id.edtLongitude)
        btnSupprimer = view.findViewById(R.id.btnSupprimer)

        val btnGalerie = view.findViewById<Button>(R.id.btnGalerie)
        val btnCamera = view.findViewById<Button>(R.id.btnCamera)
        val btnVoirCarte = view.findViewById<Button>(R.id.btnVoirCarte)
        val btnAppeler = view.findViewById<Button>(R.id.btnAppeler)
        val btnEmail = view.findViewById<Button>(R.id.btnEmail)
        val btnEnregistrer = view.findViewById<Button>(R.id.btnEnregistrer)
        val btnAnnuler = view.findViewById<Button>(R.id.btnAnnuler)

        if (contactId == -1) {
            btnSupprimer.visibility = View.GONE
        } else {
            btnSupprimer.visibility = View.VISIBLE
            val contact = viewModel.getContactById(contactId)
            if (contact != null) {
                edtNom.setText(contact.nom)
                edtPrenom.setText(contact.prenom)
                edtTelephone.setText(contact.telephone)
                edtEmail.setText(contact.email)
                photoUri = contact.photo
                ContactAdapter.chargerPhoto(requireContext(), imgDetail, photoUri)

                if (contact.latitude != null) {
                    edtLatitude.setText(contact.latitude.toString())
                }
                if (contact.longitude != null) {
                    edtLongitude.setText(contact.longitude.toString())
                }
            }
        }

        btnGalerie.setOnClickListener {
            galerieLauncher.launch(arrayOf("image/*"))
        }

        btnCamera.setOnClickListener {
            try {
                val fichier = File(requireContext().filesDir, "contact_${contactId}_${System.currentTimeMillis()}.jpg")
                tempPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    fichier
                )
                cameraLauncher.launch(tempPhotoUri!!)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Caméra indisponible: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnAppeler.setOnClickListener {
            val telephone = edtTelephone.text.toString().trim()
            if (telephone.isEmpty()) {
                edtTelephone.error = "Entrez un numéro"
                return@setOnClickListener
            }
            val intentAppel = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telephone"))
            ouvrirApplication(intentAppel)
        }

        btnEmail.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            if (email.isEmpty()) {
                edtEmail.error = "Entrez une adresse e-mail"
                return@setOnClickListener
            }
            val intentEmail = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
            ouvrirApplication(intentEmail)
        }

        btnVoirCarte.setOnClickListener {
            val latStr = edtLatitude.text.toString().trim()
            val lngStr = edtLongitude.text.toString().trim()

            val lat = latStr.toDoubleOrNull()
            val lng = lngStr.toDoubleOrNull()

            if (lat == null || lng == null || lat !in -90.0..90.0 || lng !in -180.0..180.0) {
                Toast.makeText(
                    requireContext(),
                    "Coordonnées GPS absentes ou invalides pour ce contact",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (contactId != -1) {
                val c = viewModel.getContactById(contactId)
                if (c != null) {
                    c.latitude = lat
                    c.longitude = lng
                    viewModel.updateContact(c)
                }
            }

            val carteFragment = CarteContactFragment.newInstance(contactId)
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragment_container, carteFragment)
                addToBackStack(null)
            }
        }

        btnEnregistrer.setOnClickListener {
            enregistrerModifications()
        }

        btnSupprimer.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation de suppression")
                .setMessage("Voulez-vous vraiment supprimer ce contact ?")
                .setPositiveButton("Oui") { _, _ ->
                    viewModel.deleteContact(contactId) {
                        Toast.makeText(requireContext(), "Contact supprimé", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
                .setNegativeButton("Non", null)
                .show()
        }

        btnAnnuler.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun ouvrirApplication(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Aucune application compatible trouvée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enregistrerModifications() {
        val nom = edtNom.text.toString().trim()
        val prenom = edtPrenom.text.toString().trim()
        val telephone = edtTelephone.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val latStr = edtLatitude.text.toString().trim()
        val lngStr = edtLongitude.text.toString().trim()

        if (nom.isEmpty()) {
            edtNom.error = "Le nom est obligatoire"
            edtNom.requestFocus()
            return
        }

        if (prenom.isEmpty()) {
            edtPrenom.error = "Le prénom est obligatoire"
            edtPrenom.requestFocus()
            return
        }

        if (telephone.isEmpty()) {
            edtTelephone.error = "Le téléphone est obligatoire"
            edtTelephone.requestFocus()
            return
        }

        if (email.isEmpty()) {
            edtEmail.error = "L'e-mail est obligatoire"
            edtEmail.requestFocus()
            return
        }

        var lat: Double? = null
        if (latStr.isNotEmpty()) {
            lat = latStr.toDoubleOrNull()
            if (lat == null || lat !in -90.0..90.0) {
                edtLatitude.error = "Latitude invalide (-90 à 90)"
                edtLatitude.requestFocus()
                return
            }
        }

        var lng: Double? = null
        if (lngStr.isNotEmpty()) {
            lng = lngStr.toDoubleOrNull()
            if (lng == null || lng !in -180.0..180.0) {
                edtLongitude.error = "Longitude invalide (-180 à 180)"
                edtLongitude.requestFocus()
                return
            }
        }

        val contact = Contact(
            id = if (contactId == -1) 0 else contactId,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email,
            photo = photoUri,
            latitude = lat,
            longitude = lng
        )

        if (contactId == -1) {
            viewModel.insertContact(contact) {
                Toast.makeText(requireContext(), "Contact créé", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        } else {
            viewModel.updateContact(contact) {
                Toast.makeText(requireContext(), "Modifications enregistrées", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    companion object {
        private const val ARG_CONTACT_ID = "arg_contact_id"

        fun newInstance(idContact: Int): DetailContactFragment {
            val fragment = DetailContactFragment()
            val args = Bundle().apply {
                putInt(ARG_CONTACT_ID, idContact)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
