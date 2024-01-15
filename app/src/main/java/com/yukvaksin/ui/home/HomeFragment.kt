package com.yukvaksin.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yukvaksin.R
import com.yukvaksin.SplashScreenActivity
import im.delight.android.location.SimpleLocation
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private val REQ_PERMISSION = 100
    private var strCurrentLatitude = 0.0
    private var strCurrentLongitude = 0.0
    private var strCurrentLatLong: String? = null
    private lateinit var simpleLocation: SimpleLocation
    private lateinit var txtUsername: TextView
    private lateinit var btLogout: ImageView
    private lateinit var pbLoading: ProgressBar
    private lateinit var homeAdapter: HomeAdapter
    private lateinit var rvListHospital: RecyclerView
    private lateinit var tvCurrentLocation: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userReference: DatabaseReference
    private val modelHomeList: MutableList<ModelHome> = ArrayList()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        tvCurrentLocation = view.findViewById(R.id.tvCurrentLocation)
        pbLoading = view.findViewById(R.id.pbLoading)
        rvListHospital = view.findViewById(R.id.rvListHospital)
        txtUsername = view.findViewById(R.id.txtUsername)
        btLogout = view.findViewById(R.id.btLogout)


        btLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        getUsername()

        setPermission()
        setLocation()
        setInitLayout()

        getRumahSakit()
        getCurrentLocation()

        return view
    }

    private fun setLocation() {
        simpleLocation = SimpleLocation(requireContext())

        if (!simpleLocation.hasLocationEnabled()) {
            SimpleLocation.openSettings(requireContext())
        }

        strCurrentLatitude = simpleLocation.latitude
        strCurrentLongitude = simpleLocation.longitude
        strCurrentLatLong = "$strCurrentLatitude,$strCurrentLongitude"
    }

    private fun setInitLayout() {
        homeAdapter = HomeAdapter(requireContext(), modelHomeList)
        rvListHospital.setHasFixedSize(true)
        rvListHospital.layoutManager = LinearLayoutManager(requireContext())
        rvListHospital.adapter = homeAdapter

    }

    private fun getCurrentLocation() {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(strCurrentLatitude, strCurrentLongitude, 1)
            if (!addressList.isNullOrEmpty()) {
                val strCurrentLocation = addressList[0].locality
                tvCurrentLocation.text = strCurrentLocation
                tvCurrentLocation.isSelected = true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                val intent = requireActivity().intent
                requireActivity().finish()
                requireActivity().startActivity(intent)
            }
        }
    }

    private fun getRumahSakit() {
        pbLoading.visibility = View.VISIBLE
        AndroidNetworking.get("https://rs-bed-covid-api.vercel.app/api/get-hospitals?provinceid=34prop&cityid=3402&type=1")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        pbLoading.visibility = View.GONE
                        val jsonArrayHospitals = response.getJSONArray("hospitals")
                        val hospitalList = ArrayList<ModelHome>()

                        for (i in 0 until jsonArrayHospitals.length()) {
                            val jsonObjectHospital = jsonArrayHospitals.getJSONObject(i)

                            val hospital = ModelHome()
                            hospital.name = jsonObjectHospital.getString("name")
                            hospital.address = jsonObjectHospital.getString("address")
                            hospital.bedAvailability = jsonObjectHospital.getInt("bed_availability")
                            hospital.phone = if (!jsonObjectHospital.isNull("phone")) jsonObjectHospital.getString("phone") else null
                            hospital.info = jsonObjectHospital.getString("info")

                            hospitalList.add(hospital)
                        }

                        homeAdapter = HomeAdapter(requireContext(), hospitalList)
                        rvListHospital.adapter = homeAdapter

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    pbLoading.visibility = View.GONE
                    Toast.makeText(requireContext(), "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Konfirmasi Logout")
        builder.setMessage("Apakah Anda yakin ingin keluar?")

        builder.setPositiveButton("Ya") { _: DialogInterface, _: Int ->
            val sharedPreferences = requireContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), SplashScreenActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().finish()
        }

        builder.setNegativeButton("Tidak") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    private fun getUsername() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val userReference = database.getReference("users").child(userId.toString())

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val username = dataSnapshot.child("username").getValue(String::class.java)
                    txtUsername.text = username
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }



}
