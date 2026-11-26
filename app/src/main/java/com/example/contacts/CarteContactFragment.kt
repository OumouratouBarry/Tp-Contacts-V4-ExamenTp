package com.example.contacts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class CarteContactFragment : Fragment() {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var contactName: String = "Contact"
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        latitude = arguments?.getDouble(ARG_LATITUDE, 0.0) ?: 0.0
        longitude = arguments?.getDouble(ARG_LONGITUDE, 0.0) ?: 0.0
        contactName = arguments?.getString(ARG_NAME) ?: "Contact"

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
        if (latitude in -90.0..90.0 && longitude in -180.0..180.0 && (latitude != 0.0 || longitude != 0.0)) {
            val point = GeoPoint(latitude, longitude)
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(point)

            val marker = Marker(mapView)
            marker.position = point
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = contactName
            marker.snippet = "Lat: $latitude, Lng: $longitude"
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
        private const val ARG_LATITUDE = "arg_latitude"
        private const val ARG_LONGITUDE = "arg_longitude"
        private const val ARG_NAME = "arg_name"

        fun newInstance(lat: Double, lng: Double, name: String): CarteContactFragment {
            val fragment = CarteContactFragment()
            val args = Bundle().apply {
                putDouble(ARG_LATITUDE, lat)
                putDouble(ARG_LONGITUDE, lng)
                putString(ARG_NAME, name)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
