package com.yukvaksin.ui.statusvaksin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.yukvaksin.R
import com.yukvaksin.ui.ModelInput

class StatusVaksinasiFragment : Fragment() {

    private lateinit var dataAdapter: DataAdapter
    private lateinit var tvNama: TextView
    private lateinit var tvNik: TextView
    private lateinit var tvNotFound: TextView
    private lateinit var tvLokasiVaksin: TextView
    private lateinit var linearData: LinearLayout
    private lateinit var rvListData: RecyclerView
    private lateinit var fabDelete: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tvNama = view.findViewById(R.id.tvNama)
        tvNik = view.findViewById(R.id.tvNik)
        tvNotFound = view.findViewById(R.id.tvNotFound)
        tvLokasiVaksin = view.findViewById(R.id.tvLokasiVaksin)
        linearData = view.findViewById(R.id.linearData)
        rvListData = view.findViewById(R.id.rvListData)
        fabDelete = view.findViewById(R.id.fabDelete)

        setInitLayout()
        getDataFromFirebase()

        return view
    }

    private fun setInitLayout() {
        tvNotFound.visibility = View.GONE
        fabDelete.visibility = View.GONE

        dataAdapter = DataAdapter(requireContext())
        rvListData.setHasFixedSize(true)
        rvListData.layoutManager = LinearLayoutManager(requireContext())
        rvListData.adapter = dataAdapter
    }
    private fun getDataFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val databaseRef = FirebaseDatabase.getInstance().reference.child("datavaksin").child(userId)
            databaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val modelInputs: MutableList<ModelInput> = mutableListOf()

                    for (data in dataSnapshot.children) {
                        val modelInput = data.getValue(ModelInput::class.java)
                        modelInput?.let {
                            modelInputs.add(it)
                        }
                    }

                    if (modelInputs.isNotEmpty()) {
                        val firstData = modelInputs[0]

                        tvNama.text = firstData.nama
                        tvNik.text = firstData.nik
                        tvLokasiVaksin.text = firstData.namaRS

                        dataAdapter.setDataAdapter(modelInputs)

                        fabDelete.visibility = View.VISIBLE
                        fabDelete.setOnClickListener {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            userId?.let {
                                val databaseRef = FirebaseDatabase.getInstance().reference.child("datavaksin").child(userId)
                                val alertDialogBuilder = AlertDialog.Builder(requireContext())
                                alertDialogBuilder.setMessage("Apa kamu yakin ingin menghapus data ini?")
                                alertDialogBuilder.setPositiveButton("Ya") { dialogInterface, _ ->
                                    databaseRef.removeValue().addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Semua data terhapus", Toast.LENGTH_SHORT).show()
                                        fabDelete.visibility = View.GONE
                                    }.addOnFailureListener {
                                        Toast.makeText(requireContext(), "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                alertDialogBuilder.setNegativeButton("Batal") { dialogInterface, _ ->
                                    dialogInterface.cancel()
                                }

                                val alertDialog = alertDialogBuilder.create()
                                alertDialog.show()
                            }
                        }

                    } else {
                        tvNama.text = ""
                        tvNik.text = ""
                        tvLokasiVaksin.text = ""
                        tvNotFound.visibility = View.VISIBLE
                        linearData.visibility = View.GONE
                    }
                }


                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }
    }


}
