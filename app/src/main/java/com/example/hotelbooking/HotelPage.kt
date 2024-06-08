package com.example.hotelbooking

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hotelbooking.databinding.ActivityHotelPageBinding
import com.google.firebase.database.*

class HotelPage : AppCompatActivity() {
    private lateinit var binding: ActivityHotelPageBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var hotelId: String
    private var rooms: List<DataSnapshot> = listOf()
    private var roomListDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotelPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        hotelId = intent.getStringExtra("hotelId") ?: ""

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.roomListButton.setOnClickListener {
            showRoomsDialog()
        }

        loadHotelData()
    }

    private fun loadHotelData() {
        val hotelRef = database.reference.child("hotels").child(hotelId)

        hotelRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hotelName = snapshot.child("name").getValue(String::class.java) ?: ""
                val hotelImageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""

                binding.titleTxt.text = hotelName
                Glide.with(this@HotelPage).load(hotelImageUrl).into(binding.picDetail)

                rooms = snapshot.child("rooms").children.toList()
                if (rooms.isNotEmpty()) {
                    showRoom(rooms[0])
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun showRoom(roomSnapshot: DataSnapshot) {
        val roomName = roomSnapshot.child("name").getValue(String::class.java) ?: ""
        val bathroomCount = roomSnapshot.child("bathroomCount").getValue(Int::class.java) ?: 0
        val bedCount = roomSnapshot.child("bedCount").getValue(Int::class.java) ?: 0
        val wifiAvailable = roomSnapshot.child("wifiAvailable").getValue(Boolean::class.java) ?: false
        val price = roomSnapshot.child("price").getValue(Double::class.java) ?: 0.0

        binding.roomNameTextView.text = roomName
        binding.bathroomCountTextView.text = "Bathroom Count: $bathroomCount"
        binding.bedCountTextView.text = "Bed Count: $bedCount"
        binding.wifiAvailabilityTextView.text = "WiFi Available: ${if (wifiAvailable) "Yes" else "No"}"
        binding.priceTextView.text = "Price: $$price"
    }

    private fun showRoomsDialog() {
        val sortedRooms = rooms.sortedBy { it.child("price").getValue(Double::class.java) ?: 0.0 }
        val roomNamesAndPrices = sortedRooms.map {
            val roomName = it.child("name").getValue(String::class.java) ?: "Unknown"
            val price = it.child("price").getValue(Double::class.java) ?: 0.0
            "$roomName - $$price"
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select a Room")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, roomNamesAndPrices)
        val listView = ListView(this)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            showRoom(sortedRooms[position])
            roomListDialog?.dismiss() // Dismiss the dialog
        }

        builder.setView(listView)
        roomListDialog = builder.create()
        roomListDialog?.show()
    }
}
