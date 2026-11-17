package com.example.contacts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class CarteContactFragment : Fragment() {

    private val viewModel: ContactViewModel by activityViewModels()
    private var contactId: Int = -1
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId = arguments?.getInt(ARG_CONTACT_ID) ?: -1

        val ctx = requireContext().applicationContext
        val prefs = ctx.getSharedPreferences("${ctx.packageName}_preferences", Context.MODE_PRIVATE)
        Configuration.getInstance().userAgentValue = "ContactsApp/3.0 (Android; com.example.contacts)"
        Configuration.getInstance().load(ctx, prefs)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_carte_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.map)

        val streetMapTileSource = object : OnlineTileSourceBase(
            "OSMFR",
            0, 19, 256, ".png",
            arrayOf(
                "https://a.tile.openstreetmap.fr/osmfr/",
                "https://b.tile.openstreetmap.fr/osmfr/",
                "https://c.tile.openstreetmap.fr/osmfr/"
            )
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                        MapTileIndex.getX(pMapTileIndex) + "/" +
                        MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
            }
        }

        mapView.setTileSource(streetMapTileSource)
        mapView.setMultiTouchControls(true)

        val btnRetourCarte = view.findViewById<Button>(R.id.btnRetourCarte)
        btnRetourCarte.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        afficherPositionContact()
    }

    private fun afficherPositionContact() {
        val contact = viewModel.getContactById(contactId)
        val lat = contact?.latitude
        val lng = contact?.longitude

        if (contact != null && lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
            val point = GeoPoint(lat, lng)
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(point)

            val marker = Marker(mapView)
            marker.position = point
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "${contact.prenom} ${contact.nom}"
            marker.snippet = "Lat: $lat, Lng: $lng"
            mapView.overlays.add(marker)
            marker.showInfoWindow()
        } else {
            Toast.makeText(
                requireContext(),
                "Coordonnées GPS absentes ou invalides pour ce contact",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    companion object {
        private const val ARG_CONTACT_ID = "arg_contact_id"

        fun newInstance(idContact: Int): CarteContactFragment {
            val fragment = CarteContactFragment()
            val args = Bundle().apply {
                putInt(ARG_CONTACT_ID, idContact)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
