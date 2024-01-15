package com.yukvaksin.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.BuildConfig
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.yukvaksin.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class InputDataActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_PICK_PHOTO = 1
        const val REQUEST_IMAGE_CAPTURE = 2
    }
    private var photoFile: File? = null
    private var photoUri: Uri? = null
    private var umurPeserta = 0
    private lateinit var imageBytes: ByteArray
    private lateinit var fileDirectory: File
    private lateinit var imageFilename: File
    private lateinit var toolbar: Toolbar
    private lateinit var inputNIK: EditText
    private lateinit var inputNama: EditText
    private lateinit var inputTanggalLahir: EditText
    private lateinit var inputTanggalVaksin: EditText
    private lateinit var inputAlamat: EditText
    private lateinit var imageKTP: ImageView
    private lateinit var btnGallery: ImageView
    private lateinit var btnCamera: ImageView
    private lateinit var fabSave: ExtendedFloatingActionButton
    private var strNik = ""
    private var strNama = ""
    private var strTanggalLahir = ""
    private var strTanggalVaksin = ""
    private var strAlamat = ""
    private var strTimeStamp = ""
    private var strImageName = ""
    private var strFilePath: String? = null
    private var strEncodedImage = ""
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_data)

        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("datavaksin")

        toolbar = findViewById(R.id.toolbar)
        inputNIK = findViewById(R.id.inputNIK)
        inputNama = findViewById(R.id.inputNama)
        inputTanggalLahir = findViewById(R.id.inputTanggalLahir)
        inputTanggalVaksin = findViewById(R.id.inputTanggalVaksin)
        inputAlamat = findViewById(R.id.inputAlamat)
        imageKTP = findViewById(R.id.imageKTP)
        btnGallery = findViewById(R.id.imageGallery)
        btnCamera = findViewById(R.id.imageCamera)
        fabSave = findViewById(R.id.fabSave)


        setStatusBar()
        setInitLayout()
    }
    private fun setInitLayout() {

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        inputTanggalLahir.setOnClickListener {
            val tanggalLahir = Calendar.getInstance()
            val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                tanggalLahir.set(Calendar.YEAR, year)
                tanggalLahir.set(Calendar.MONTH, monthOfYear)
                tanggalLahir.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val strFormatDefault = "dd-MM-yyyy"
                val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                inputTanggalLahir.setText(simpleDateFormat.format(tanggalLahir.time))
            }

            DatePickerDialog(this@InputDataActivity, date, tanggalLahir
                .get(Calendar.YEAR), tanggalLahir.get(Calendar.MONTH),
                tanggalLahir.get(Calendar.DAY_OF_MONTH)).show()
        }

        inputTanggalVaksin.setOnClickListener {
            val tanggalVaksin = Calendar.getInstance()
            val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                tanggalVaksin.set(Calendar.YEAR, year)
                tanggalVaksin.set(Calendar.MONTH, monthOfYear)
                tanggalVaksin.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val strFormatDefault = "dd-MM-yyyy"
                val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                inputTanggalVaksin.setText(simpleDateFormat.format(tanggalVaksin.time))
            }

            DatePickerDialog(this@InputDataActivity, date, tanggalVaksin
                .get(Calendar.YEAR), tanggalVaksin.get(Calendar.MONTH),
                tanggalVaksin.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnGallery.setOnClickListener {
            openGallery()
        }

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    REQUEST_IMAGE_CAPTURE
                )
            } else {
                openCamera()
            }
        }


        fabSave.setOnClickListener {
            strNik = inputNIK.text.toString()
            strNama = inputNama.text.toString()
            strTanggalLahir = inputTanggalLahir.text.toString()
            strTanggalVaksin = inputTanggalVaksin.text.toString()
            strAlamat = inputAlamat.text.toString()
            umurPeserta = Constant.cekUmurPeserta(inputTanggalLahir.text.toString())
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid


            if (strNik.isNullOrBlank() || strNama.isNullOrBlank() || strTanggalLahir.isNullOrBlank() || strAlamat.isNullOrBlank() || strFilePath == null) {
                Toast.makeText(this@InputDataActivity, "Mohon lengkapi form pendaftaran!", Toast.LENGTH_SHORT).show()
            } else if (umurPeserta <= 17) {
                Toast.makeText(this@InputDataActivity, "Umur harus lebih dari 17 Tahun!", Toast.LENGTH_SHORT).show()
            } else {
                val imageName = "images/${UUID.randomUUID()}.jpg"
                val imageRef = storageReference.child(imageName)
                val uploadTask = imageRef.putBytes(imageBytes)

                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageURL = uri.toString()

                        if (userId != null) {
                            val modelInput = Constant.namaRS?.let { it1 ->
                                ModelInput(
                                    strNik,
                                    strNama,
                                    strTanggalLahir,
                                    strTanggalVaksin,
                                    strAlamat,
                                    it1,
                                    imageURL)
                            }

                            val dataKey = databaseReference.child(userId).push().key
                            if (dataKey != null) {
                                databaseReference.child(userId).child(dataKey).setValue(modelInput)
                            }

                            Toast.makeText(this@InputDataActivity, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@InputDataActivity, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this@InputDataActivity, "Gagal mengunggah gambar!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        try {
            startActivityForResult(galleryIntent, REQUEST_PICK_PHOTO)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Gallery is not available.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                photoFile = try {
                    createImageFile()
                } catch (ex: IOException) {

                    throw ex
                }
                photoFile.also {
                    photoUri = it?.let { it1 ->
                        FileProvider.getUriForFile(
                            this,
                            "com.yukvaksin.fileprovider",
                            it1
                        )
                    }
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }


    private fun createImageFile(): File {
        strTimeStamp = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date())
        strImageName = "IMG_"
        fileDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "")
        imageFilename = File.createTempFile(strImageName, ".jpg", fileDirectory)
        strFilePath = imageFilename?.absolutePath
        return imageFilename!!
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_PHOTO -> {
                    data?.data?.let { selectedImage ->
                        val mediaPath = getImagePath(selectedImage)
                        strFilePath = mediaPath
                        convertImage(mediaPath)
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    strFilePath?.let { convertImage(it) }
                }
                else -> {
                    // Handle other request codes if needed
                }
            }
        }
    }
    private fun getImagePath(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val imagePath = cursor?.getString(columnIndex ?: 0)
        cursor?.close()
        return imagePath ?: ""
    }

    private fun convertImage(imageFilePath: String?) {
        imageFilePath?.let {
            val imageFile = File(it)
            if (imageFile.exists()) {
                val options = BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeFile(strFilePath, options)

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                Glide.with(this)
                    .load(bitmap)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_image_upload)
                    .into(imageKTP)
                imageBytes = baos.toByteArray()
                strEncodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            }
        }
    }
    private fun setStatusBar() {
        if (Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val window: Window = activity.window
        var layoutParams: WindowManager.LayoutParams = window.attributes
        if (on) {
            layoutParams.flags = layoutParams.flags or bits
        } else {
            layoutParams.flags = layoutParams.flags and bits.inv()
        }
        window.attributes = layoutParams
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}