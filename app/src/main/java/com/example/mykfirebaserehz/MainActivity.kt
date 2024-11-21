package com.example.mykfirebaserehz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings.Secure
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mykfirebaserehz.databinding.ActivityMainBinding
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null
    private var miscontactos = ArrayList<Contacto>()
    private var adapter: MyAdapter? = null

    private val cameraActivityLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photoUriString = result.data?.getStringExtra("photoUri")
            imageUri = Uri.parse(photoUriString)
            binding.imgFoto.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            imageUri = result.data?.data
            binding.imgFoto.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        adapter = MyAdapter(this, miscontactos)
        binding.lvmiscontactosfirebase.adapter = adapter

        loadContactsFromFirebase()

        binding.btnCamara.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            cameraActivityLauncher.launch(intent)
        }

        binding.btnseleccionar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }

        binding.btnenviar.setOnClickListener {
            if (imageUri != null) {
                uploadImageToFirebase(imageUri!!)
            } else {
                Toast.makeText(this, "Primero selecciona o toma una foto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val fileReference = storageReference.child("images/${UUID.randomUUID()}.jpg")
        fileReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val nombre = binding.txtnombre.text.toString()
                    val alias = binding.txtalias.text.toString()
                    val codigo = Secure.getString(contentResolver, Secure.ANDROID_ID)
                    val key = databaseReference.child("Contacto").push().key
                    val contacto = Contacto(nombre, alias, 0, key ?: "", codigo, downloadUri.toString())

                    saveImageInfoToDatabase(contacto)
                    Toast.makeText(this, "Imagen subida con éxito", Toast.LENGTH_SHORT).show()
                    binding.imgFoto.setImageResource(android.R.color.transparent)
                    imageUri = null
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Error al subir imagen", e)
            }
    }

    private fun saveImageInfoToDatabase(contacto: Contacto) {
        contacto.key?.let {
            databaseReference.child("Contacto").child(it).setValue(contacto)
                .addOnSuccessListener {
                    Toast.makeText(this, "Datos guardados en la base de datos", Toast.LENGTH_SHORT).show()
                    binding.txtnombre.setText("")
                    binding.txtalias.setText("")
                    loadContactsFromFirebase()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Error al guardar datos", e)
                }
        }
    }

    private fun loadContactsFromFirebase() {
        databaseReference.child("Contacto").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                miscontactos.clear()
                for (postSnapshot in snapshot.children) {
                    val contacto = postSnapshot.getValue(Contacto::class.java)
                    contacto?.let { miscontactos.add(it) }
                }
                adapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error al cargar contactos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}