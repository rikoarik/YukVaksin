package com.yukvaksin.ui.maps

import com.yukvaksin.R
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yukvaksin.ui.home.HomeAdapter
import com.yukvaksin.ui.home.ModelHome
import org.json.JSONException
import org.json.JSONObject

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getRumahSakit()
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.isMyLocationEnabled = true

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    }
                }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    private fun getRumahSakit() {

        AndroidNetworking.get("https://rs-bed-covid-api.vercel.app/api/get-hospitals?provinceid=34prop&cityid=3402&type=1")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val jsonArrayHospitals = response.getJSONArray("hospitals")
                        val latitudeList = ArrayList<String>()
                        val longitudeList = ArrayList<String>()

                        for (i in 0 until jsonArrayHospitals.length()) {
                            val jsonObjectHospital = jsonArrayHospitals.getJSONObject(i)

                            val getLokasi = jsonObjectHospital.getString("id")
                            val latLong = getLokasi.split(",")
                            if (latLong.size == 2) {
                                val latitude = latLong[0].toDouble()
                                val longitude = latLong[1].toDouble()

                                val location = LatLng(latitude, longitude)

                                val hospitalLatLng = LatLng(latitude, longitude)
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(hospitalLatLng)
                                        .title(jsonObjectHospital.getString("name"))
                                        .snippet(jsonObjectHospital.getString("address"))
                                )
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                            }
                        }


                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    Toast.makeText(requireContext(), "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
