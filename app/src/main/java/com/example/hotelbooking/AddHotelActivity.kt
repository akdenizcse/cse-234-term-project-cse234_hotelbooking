package com.example.hotelbooking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelbooking.databinding.ActivityAddHotelBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddHotelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddHotelBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHotelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference.child("hotel_images")

        binding.addButton.setOnClickListener {
            val hotelName = binding.hotelName.text.toString()
            val hotelLocation = binding.hotelLocation.text.toString()
            val hotelDescription = binding.hotelDescription.text.toString()

            if (hotelName.isNotEmpty() && hotelLocation.isNotEmpty() && hotelDescription.isNotEmpty() && selectedImageUri != null) {
                uploadImageToStorage(hotelName, hotelLocation, hotelDescription)
            } else {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.selectImageButton.setOnClickListener {
            openImageChooser()
        }

        binding.backToAdminPageButton.setOnClickListener {
            val intent = Intent(this, AdminPageActivity::class.java)
            startActivity(intent)
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.selectedImageView.setImageURI(it)
            binding.selectedImageView.visibility = View.VISIBLE
        }
    }

    private fun openImageChooser() {
        getContent.launch("image/*")
    }

    private fun uploadImageToStorage(hotelName: String, hotelLocation: String, hotelDescription: String) {
        selectedImageUri?.let { uri ->
            val imageRef = storageRef.child("${hotelName}_${System.currentTimeMillis()}")

            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val hotel = Hotel(hotelName, hotelLocation, hotelDescription, uri.toString())

                        firestore.collection("hotels")
                            .add(hotel)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(
                                    this,
                                    "Hotel Added Successfully with ID: ${documentReference.id}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Failed to Add Hotel! ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Failed to Upload Image! ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    data class Hotel(
        val name: String = "",
        val location: String = "",
        val description: String = "",
        val imageUrl: String = ""
    )
}
