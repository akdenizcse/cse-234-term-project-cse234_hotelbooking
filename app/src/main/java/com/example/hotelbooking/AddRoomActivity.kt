package com.example.hotelbooking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelbooking.databinding.ActivityAddRoomBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRoomBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private var selectedImageUri: Uri? = null
    private var hotelList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference.child("room_images")

        loadHotelNames()

        binding.selectImageButton.setOnClickListener {
            openImageChooser()
        }

        binding.addRoomButton.setOnClickListener {
            val roomName = binding.roomName.text.toString()
            val roomPrice = binding.roomPrice.text.toString()
            val bedCount = binding.bedCount.text.toString()
            val wifiAvailable = binding.wifiSwitch.isChecked
            val bathroomCount = binding.bathroomCount.text.toString()
            val selectedHotel = binding.hotelSpinner.selectedItem?.toString()

            if (roomName.isNotEmpty() && roomPrice.isNotEmpty() && bedCount.isNotEmpty() && bathroomCount.isNotEmpty() && selectedHotel != null && selectedImageUri != null) {
                uploadImageToStorage(selectedHotel, roomName, roomPrice, bedCount, wifiAvailable, bathroomCount)
            } else {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            }
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

    private fun loadHotelNames() {
        firestore.collection("hotels").get().addOnSuccessListener { documents ->
            for (document in documents) {
                hotelList.add(document.getString("name") ?: "")
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hotelList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.hotelSpinner.adapter = adapter
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load hotel names.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToStorage(hotelName: String, roomName: String, roomPrice: String, bedCount: String, wifiAvailable: Boolean, bathroomCount: String) {
        selectedImageUri?.let { uri ->
            val imageRef = storageRef.child("${hotelName}_${roomName}_${System.currentTimeMillis()}")

            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val room = Room(roomName, uri.toString(), roomPrice, bedCount, wifiAvailable, bathroomCount)

                        firestore.collection("hotels").whereEqualTo("name", hotelName).get().addOnSuccessListener { documents ->
                            if (documents.documents.isNotEmpty()) {
                                val hotelId = documents.documents[0].id
                                firestore.collection("hotels").document(hotelId).collection("rooms").add(room)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Room Added Successfully!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Failed to Add Room! ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to Upload Image! ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    data class Room(
        val name: String = "",
        val imageUrl: String = "",
        val price: String = "",
        val bedCount: String = "",
        val wifiAvailable: Boolean = false,
        val bathroomCount: String = ""
    )
}
